import java.io.Serializable;

public abstract class ItemMenu implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private String nombre;
    private boolean disponible = true;

    protected ItemMenu(int id, String nombre) throws FideException {
        this.id = id;
        renombrar(nombre);
    }

    public void renombrar(String nuevo) throws FideException {
        if (nuevo == null || nuevo.isBlank()) throw new FideException("El nombre es obligatorio.");
        this.nombre = nuevo.trim();
    }

    public abstract double getPrecio();
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean d) { disponible = d; }
    @Override public String toString() { return String.format("%s  (₡%,.0f)", nombre, getPrecio()); }
}
