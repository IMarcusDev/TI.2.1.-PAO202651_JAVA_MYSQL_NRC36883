package ec.edu.monster.vista;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import ec.edu.monster.AplicacionCountryApi;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ApiControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.controlador.UniversidadControlador;
import ec.edu.monster.servicios.ClienteRest;
import ec.edu.monster.servicios.SesionServicio;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Ventana única de la app (equivale a VentanaPrincipal de escritorio). Implementa
 * {@link MenuVista}, arma los controladores con sus vistas y muestra una pantalla a la vez:
 * inicio de sesión, menú de tarjetas o el módulo elegido.
 *
 * <p>Cada acción del controlador corre en un hilo aparte con la barra de progreso visible y la
 * pantalla bloqueada; un error inesperado se muestra en la pantalla y no cierra la app.</p>
 *
 * @author Escobar Marcos
 */
public class PrincipalActivity extends AppCompatActivity implements MenuVista {

    static final String LOGIN = "login";
    static final String MENU = "menu";
    static final String IMPORTAR = "importar";
    static final String CONSULTAR = "consultar";
    static final String PAISES = "paises";
    static final String BUSCAR = "buscar";
    static final String REPORTES = "reportes";
    static final String UNIVERSIDADES = "universidades";

    private static final String TAG = "CountryAPI";
    private static final String ERROR_INESPERADO = ClienteRest.RESPUESTA_NO_VALIDA;
    private static final String ESTADO_PANTALLA = "pantalla";

    /** Un solo hilo: las acciones nunca se ejecutan a la vez (la pantalla está bloqueada). */
    private final ExecutorService hilo = Executors.newSingleThreadExecutor();
    private final Map<String, FragmentoBase> pantallas = new LinkedHashMap<>();
    private final Map<String, Runnable> accionesAlEntrar = new LinkedHashMap<>();
    private final Map<String, Integer> titulos = new LinkedHashMap<>();

    private View encabezado;
    private MaterialToolbar barra;
    private View progreso;
    private View bloqueo;
    private boolean ocupado;
    private String pantallaActual = LOGIN;

    private SesionServicio sesion;
    private LoginFragment login;
    private MenuFragment menu;
    private LoginControlador loginControlador;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        setContentView(R.layout.activity_principal);
        AplicacionCountryApi app = AplicacionCountryApi.de(this);
        sesion = app.getSesion();

        encabezado = findViewById(R.id.encabezado);
        barra = findViewById(R.id.barraSuperior);
        progreso = findViewById(R.id.progreso);
        bloqueo = findViewById(R.id.bloqueo);
        barra.inflateMenu(R.menu.menu_principal);
        barra.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_cerrar_sesion) {
                cerrarSesion();
                return true;
            }
            return false;
        });
        barra.setNavigationOnClickListener(v -> mostrar(MENU));
        barra.setNavigationContentDescription(R.string.menu_principal);
        IndicadorServidor indicador = findViewById(R.id.indicadorBarra);
        indicador.setComprobador(app.getCliente()::servidorDisponible);

        construir(app, estado != null);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (ocupado) {
                    return;
                }
                if (LOGIN.equals(pantallaActual) || MENU.equals(pantallaActual)) {
                    // En el inicio de sesión o el menú, «atrás» deja la app en segundo plano.
                    moveTaskToBack(true);
                } else {
                    mostrar(MENU);
                }
            }
        });

        String guardada = estado == null ? null : estado.getString(ESTADO_PANTALLA);
        if (sesion.estaActiva() && guardada != null && !LOGIN.equals(guardada)) {
            mostrarMenu(sesion.getNombre());
            if (!MENU.equals(guardada)) {
                cambiarA(guardada);
            }
        } else {
            irAInicioSesion(null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putString(ESTADO_PANTALLA, pantallaActual);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hilo.shutdown();
    }

    /**
     * Crea (o recupera, si Android recreó la actividad) cada pantalla y la conecta con su
     * controlador, igual que App.construir en el cliente de escritorio.
     */
    private void construir(AplicacionCountryApi app, boolean recreada) {
        FragmentManager gestor = getSupportFragmentManager();
        FragmentTransaction transaccion = gestor.beginTransaction().setReorderingAllowed(true);

        login = pantalla(gestor, transaccion, LOGIN, LoginFragment::new, R.string.app_name);
        menu = pantalla(gestor, transaccion, MENU, MenuFragment::new, R.string.app_name);
        ImportacionFragment importacion =
                pantalla(gestor, transaccion, IMPORTAR, ImportacionFragment::new, R.string.op_importar);
        ConsultaApiFragment consulta =
                pantalla(gestor, transaccion, CONSULTAR, ConsultaApiFragment::new, R.string.op_consultar);
        PaisFragment paises = pantalla(gestor, transaccion, PAISES, PaisFragment::new, R.string.op_paises);
        BusquedaFragment busqueda = pantalla(gestor, transaccion, BUSCAR, BusquedaFragment::new, R.string.op_buscar);
        ReporteFragment reportes = pantalla(gestor, transaccion, REPORTES, ReporteFragment::new, R.string.op_reportes);
        UniversidadFragment universidades =
                pantalla(gestor, transaccion, UNIVERSIDADES, UniversidadFragment::new, R.string.op_universidades);
        for (Fragment fragmento : pantallas.values()) {
            transaccion.hide(fragmento);
        }
        transaccion.commitNow();

        // Inicio y cierre de sesión
        loginControlador = new LoginControlador(login, this, app.getLoginServicio(), sesion);
        login.setControlador(loginControlador, app);

        // Países: CRUD, búsqueda, importación y consulta en la API
        ApiControlador apiControlador =
                new ApiControlador(consulta, importacion, app.getPaisServicio(), sesion);
        importacion.setControlador(apiControlador);
        consulta.setControlador(apiControlador);
        PaisControlador paisControlador = new PaisControlador(paises, busqueda, app.getPaisServicio(), sesion);
        paises.setControlador(paisControlador);
        busqueda.setControlador(paisControlador);

        // Reportes y universidades
        ReporteControlador reporteControlador = new ReporteControlador(reportes, app.getReporteServicio(), sesion);
        reportes.setControlador(reporteControlador);
        UniversidadControlador universidadControlador =
                new UniversidadControlador(universidades, app.getUniversidadServicio(), sesion);
        universidades.setControlador(universidadControlador);

        // Lo que se consulta al entrar a cada pantalla (como en escritorio)
        accionesAlEntrar.put(PAISES, paisControlador::listar);
        accionesAlEntrar.put(BUSCAR, paisControlador::buscarYFiltrar);
        accionesAlEntrar.put(REPORTES, reporteControlador::generar);
        accionesAlEntrar.put(UNIVERSIDADES, universidadControlador::listar);

        if (recreada) {
            Log.i(TAG, "Actividad recreada: pantallas recuperadas y conectadas de nuevo");
        }
    }

    @SuppressWarnings("unchecked")
    private <F extends FragmentoBase> F pantalla(FragmentManager gestor, FragmentTransaction transaccion,
            String etiqueta, Supplier<F> crear, int titulo) {
        F fragmento = (F) gestor.findFragmentByTag(etiqueta);
        if (fragmento == null) {
            fragmento = crear.get();
            transaccion.add(R.id.contenedor, fragmento, etiqueta);
        }
        pantallas.put(etiqueta, fragmento);
        titulos.put(etiqueta, titulo);
        return fragmento;
    }

    // ------------------------------------------------------------------ navegación

    /**
     * Navegación pedida por el usuario (tarjeta del menú, flecha o «atrás»): se ignora mientras
     * se procesa una petición.
     */
    void mostrar(String etiqueta) {
        if (!ocupado) {
            cambiarA(etiqueta);
        }
    }

    /** Muestra una pantalla, borra su aviso anterior y ejecuta lo que corresponde al entrar. */
    private void cambiarA(String etiqueta) {
        FragmentoBase destino = pantallas.get(etiqueta);
        if (destino == null) {
            return;
        }
        ocultarTeclado();
        FragmentTransaction transaccion = getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        for (Map.Entry<String, FragmentoBase> entrada : pantallas.entrySet()) {
            if (entrada.getValue() != destino && !entrada.getValue().isHidden()) {
                transaccion.hide(entrada.getValue());
            }
        }
        transaccion.show(destino).commitNow();
        pantallaActual = etiqueta;

        boolean enModulo = !LOGIN.equals(etiqueta) && !MENU.equals(etiqueta);
        barra.setTitle(titulos.get(etiqueta));
        if (enModulo) {
            barra.setNavigationIcon(R.drawable.ic_volver);
            barra.setNavigationContentDescription(R.string.menu_principal);
        } else {
            barra.setNavigationIcon(null);
        }
        if (MENU.equals(etiqueta) || LOGIN.equals(etiqueta)) {
            return;
        }
        destino.limpiarAviso();
        Runnable accion = accionesAlEntrar.get(etiqueta);
        if (accion != null) {
            ejecutar(accion, destino);
        }
    }

    /** Vuelve al inicio de sesión mostrando, si lo hay, el motivo (por ejemplo, sesión expirada). */
    void irAInicioSesion(String mensaje) {
        encabezado.setVisibility(View.GONE);
        barra.setSubtitle(null);
        cambiarA(LOGIN);
        login.mostrarFormulario();
        if (mensaje != null) {
            login.mostrarError(mensaje);
        }
    }

    /** Opción «Cerrar sesión» de la barra y del menú. */
    void cerrarSesion() {
        ejecutar(loginControlador::cerrarSesion, menu);
    }

    // ------------------------------------------------------------------ MenuVista

    @Override
    public void mostrarMenu(String nombre) {
        Hilos.enPrincipal(() -> {
            encabezado.setVisibility(View.VISIBLE);
            barra.setSubtitle(nombre);
            menu.limpiarAviso();
            menu.saludar(nombre);
            cambiarA(MENU);
        });
    }

    @Override
    public int leerOpcion() {
        // En Android cada opción del menú es una tarjeta; no hay opción que leer.
        return -1;
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        menu.mostrarMensaje(mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        menu.mostrarError(mensaje);
    }

    @Override
    public void irAInicioSesion() {
        Hilos.enPrincipal(() -> irAInicioSesion(null));
    }

    // ------------------------------------------------------------------ ejecución

    /** Ejecuta una acción del controlador en segundo plano; si ya hay una en curso, la ignora. */
    void ejecutar(Runnable accion, VistaBase vistaError) {
        if (ocupado) {
            return;
        }
        ocultarTeclado();
        setOcupado(true);
        hilo.execute(() -> {
            try {
                accion.run();
            } catch (RuntimeException e) {
                Log.e(TAG, "Error inesperado en una acción", e);
                vistaError.mostrarError(ERROR_INESPERADO);
            } finally {
                Hilos.enPrincipal(() -> setOcupado(false));
            }
        });
    }

    /** Mientras un diálogo espera al usuario se ocultan la barra de progreso y el bloqueo. */
    void setEsperandoUsuario(boolean esperando) {
        if (ocupado) {
            progreso.setVisibility(esperando ? View.INVISIBLE : View.VISIBLE);
            bloqueo.setVisibility(esperando ? View.INVISIBLE : View.VISIBLE);
        }
    }

    private void setOcupado(boolean valor) {
        ocupado = valor;
        progreso.setVisibility(valor ? View.VISIBLE : View.INVISIBLE);
        // INVISIBLE y no GONE: la capa sigue medida y su zona se redibuja al ocultarla.
        bloqueo.setVisibility(valor ? View.VISIBLE : View.INVISIBLE);
    }

    private void ocultarTeclado() {
        View foco = getCurrentFocus();
        if (foco != null) {
            InputMethodManager teclado = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            teclado.hideSoftInputFromWindow(foco.getWindowToken(), 0);
        }
    }
}
