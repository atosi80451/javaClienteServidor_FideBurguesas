import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Combo extends ItemMenu {
    private static final long serialVersionUID = 1L;
    private final Map<Producto, Integer> composicion = new LinkedHashMap<>();
    private final double descuento; // monto fijo en colones
    private final String descripcion;

    public Combo(int id, String nombre, String descripcion, Map<Producto, Integer> composicion, double descuento) throws FideException {
        super(id, nombre);
        validar(composicion, descuento);
        this.composicion.putAll(composicion);
        this.descuento = descuento;
        this.descripcion = descripcion == null ? "" : descripcion.trim();
    }

    public Combo(int id, String nombre, List<Producto> productos, double porcentaje) throws FideException {
        this(id, nombre, "", contar(productos), montoDesde(productos, porcentaje));
    }

    private static Map<Producto, Integer> contar(List<Producto> ps) {
        Map<Producto, Integer> m = new LinkedHashMap<>();
        if (ps != null) for (Producto p : ps) m.merge(p, 1, Integer::sum);
        return m;
    }

    private static double montoDesde(List<Producto> ps, double porcentaje) throws FideException {
        if (porcentaje < 0 || porcentaje >= 100) throw new FideException("El descuento debe estar entre 0 y 99%.");
        double base = ps == null ? 0 : ps.stream().mapToDouble(Producto::getPrecio).sum();
        return base * porcentaje / 100;
    }

    public static double calcularBase(Map<Producto, Integer> c) {
        double suma = 0;
        for (Map.Entry<Producto, Integer> e : c.entrySet()) suma += e.getKey().getPrecio() * e.getValue();
        return suma;
    }

    private static void validar(Map<Producto, Integer> c, double descuento) throws FideException {
        if (c == null || c.size() < 2) throw new FideException("Un combo requiere al menos 2 productos existentes.");
        for (int cantidad : c.values())
            if (cantidad < 1) throw new FideException("Las cantidades deben ser de al menos 1.");
        if (descuento < 0) throw new FideException("El descuento no puede ser negativo.");
        if (calcularBase(c) - descuento <= 0)
            throw new FideException("El descuento no puede dejar el precio final en 0 o negativo.");
    }

    public Map<Producto, Integer> getComposicion() { return Collections.unmodifiableMap(composicion); }
    public List<Producto> getProductos() { return List.copyOf(composicion.keySet()); }
    public double getDescuento() { return descuento; }
    public String getDescripcion() { return descripcion == null ? "" : descripcion; }
    public double getPrecioBase() { return calcularBase(composicion); }

    public String getResumen() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Producto, Integer> e : composicion.entrySet())
            sb.append(e.getValue()).append("× ").append(e.getKey().getNombre()).append(", ");
        return sb.toString().replaceAll(", $", "");
    }

    @Override public double getPrecio() { return getPrecioBase() - descuento; }

    public boolean isHabilitado() { return super.isDisponible(); }

    @Override public boolean isDisponible() {
        return super.isDisponible() && composicion.keySet().stream().allMatch(Producto::isDisponible);
    }
}