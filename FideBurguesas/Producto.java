public class Producto extends ItemMenu {
    private static final long serialVersionUID = 1L;
    private String categoria;
    private String descripcion;
    private double precioBase;

    public Producto(int id, String nombre, String categoria, String descripcion, double precio) throws FideException {
        super(id, nombre);
        validarPrecio(precio);
        this.categoria = categoria;
        this.descripcion = limpiar(descripcion);
        this.precioBase = precio;
    }

    public Producto(int id, String nombre, String categoria, double precio) throws FideException {
        this(id, nombre, categoria, "", precio);
    }

    private static void validarPrecio(double precio) throws FideException {
        if (precio <= 0) throw new FideException("El precio debe ser mayor a 0.");
    }

    private static String limpiar(String s) { return s == null ? "" : s.trim(); }

    public void actualizar(String nombre, String categoria, String descripcion, double precio) throws FideException {
        validarPrecio(precio);
        renombrar(nombre);
        this.categoria = categoria;
        this.descripcion = limpiar(descripcion);
        this.precioBase = precio;
    }

    public void actualizar(String nombre, String categoria, double precio) throws FideException {
        actualizar(nombre, categoria, getDescripcion(), precio);
    }

    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion == null ? "" : descripcion; }
    @Override public double getPrecio() { return precioBase; }
}