package ec.edu.monster.controladores;

import ec.edu.monster.modelos.data.Pais;
import ec.edu.monster.modelos.data.Region;
import ec.edu.monster.modelos.dto.ConteoRegion;
import ec.edu.monster.modelos.dto.PromedioRegion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.interfaces.IPaisDAO;
import ec.edu.monster.modelos.interfaces.IRegionDAO;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO en memoria para probar los controladores sin MySQL. Imitan las restricciones de la base:
 * nombre de país único y catálogo de seis regiones.
 */
final class DaosFalsos {

    private DaosFalsos() {
    }

    static final class RegionDAOFalso implements IRegionDAO {

        private final List<Region> regiones = List.of(
                new Region(1, "América"), new Region(2, "Europa"), new Region(3, "Asia"),
                new Region(4, "África"), new Region(5, "Oceanía"), new Region(6, "Antártida"));

        @Override
        public Region buscarPorNombre(String nombre) {
            return regiones.stream().filter(r -> r.getNombre().equalsIgnoreCase(nombre)).findFirst().orElse(null);
        }

        @Override
        public List<Region> listar() {
            return regiones;
        }
    }

    static final class PaisDAOFalso implements IPaisDAO {

        final Map<Integer, Pais> paises = new LinkedHashMap<>();
        private int siguienteId = 1;
        List<Pais> ultimoLoteImportado;

        Pais agregar(String nombre, String region, long poblacion) {
            Pais p = new Pais(nombre, "Capital", new Region(0, region), "Sub", poblacion, 10, "Moneda");
            p.setId(siguienteId++);
            paises.put(p.getId(), p);
            return p;
        }

        @Override
        public List<Pais> listar() {
            return paises.values().stream().sorted(Comparator.comparing(Pais::getNombre)).toList();
        }

        @Override
        public List<Pais> buscarPorNombre(String texto) {
            return listar().stream().filter(p -> p.getNombre().toLowerCase().contains(texto.toLowerCase())).toList();
        }

        @Override
        public List<Pais> buscar(String nombre, String capital, Integer idRegion) {
            return listar().stream()
                    .filter(p -> nombre == null || p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .filter(p -> idRegion == null || p.getRegion().getId() == idRegion)
                    .toList();
        }

        @Override
        public Pais buscarPorId(int id) {
            return paises.get(id);
        }

        @Override
        public boolean existePorNombre(String nombre) {
            return paises.values().stream().anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
        }

        @Override
        public int insertar(Pais pais) throws SQLIntegrityConstraintViolationException {
            if (existePorNombre(pais.getNombre())) {
                throw new SQLIntegrityConstraintViolationException("Duplicate entry");
            }
            pais.setId(siguienteId++);
            paises.put(pais.getId(), pais);
            return pais.getId();
        }

        @Override
        public boolean actualizar(Pais pais) throws SQLIntegrityConstraintViolationException {
            if (!paises.containsKey(pais.getId())) {
                return false;
            }
            boolean repetido = paises.values().stream()
                    .anyMatch(p -> p.getId() != pais.getId() && p.getNombre().equalsIgnoreCase(pais.getNombre()));
            if (repetido) {
                throw new SQLIntegrityConstraintViolationException("Duplicate entry");
            }
            paises.put(pais.getId(), pais);
            return true;
        }

        @Override
        public boolean eliminar(int id) {
            return paises.remove(id) != null;
        }

        @Override
        public ResumenImportacion guardarTodos(List<Pais> lote) {
            ultimoLoteImportado = new ArrayList<>(lote);
            int registrados = 0;
            int actualizados = 0;
            for (Pais p : lote) {
                if (existePorNombre(p.getNombre())) {
                    actualizados++;
                } else {
                    p.setId(siguienteId++);
                    paises.put(p.getId(), p);
                    registrados++;
                }
            }
            return new ResumenImportacion(lote.size(), registrados, actualizados);
        }

        @Override
        public Pais guardarOActualizar(Pais pais) {
            pais.setId(siguienteId++);
            paises.put(pais.getId(), pais);
            return pais;
        }

        @Override
        public List<ConteoRegion> contarPorRegion() {
            Map<String, Integer> conteo = new LinkedHashMap<>();
            for (Region r : new RegionDAOFalso().listar()) {
                conteo.put(r.getNombre(), 0);
            }
            paises.values().forEach(p -> conteo.merge(p.getNombreRegion(), 1, Integer::sum));
            List<ConteoRegion> filas = new ArrayList<>();
            conteo.forEach((region, cantidad) -> filas.add(new ConteoRegion(region, cantidad)));
            return filas;
        }

        @Override
        public Pais paisMasPoblado() {
            return paises.values().stream().max(Comparator.comparingLong(Pais::getPoblacion)).orElse(null);
        }

        @Override
        public List<PromedioRegion> promedioPoblacionPorRegion() {
            return List.of();
        }
    }
}
