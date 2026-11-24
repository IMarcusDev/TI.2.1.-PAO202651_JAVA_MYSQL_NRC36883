package ec.edu.monster.web;

import ec.edu.monster.servicios.ClienteRest;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Al desplegar la aplicación crea un solo {@link ClienteRest} (sin estado, seguro para varios
 * hilos) apuntado a la dirección de servidor.properties, y lo comparte entre todos los
 * usuarios.
 *
 * @author Escobar Marcos
 */
@WebListener
public class AplicacionWeb implements ServletContextListener {

    static final String CLIENTE = "clienteRest";

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ServletContext contexto = evento.getServletContext();
        ClienteRest cliente = new ClienteRest();
        contexto.setAttribute(CLIENTE, cliente);
        contexto.log("CountryAPI web: servidor RESTful en " + cliente.getUrlBase());
    }

    static ClienteRest cliente(ServletContext contexto) {
        return (ClienteRest) contexto.getAttribute(CLIENTE);
    }
}
