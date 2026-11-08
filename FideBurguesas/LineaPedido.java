import java.io.Serializable;

public class LineaPedido implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ItemMenu item;
    private int cantidad;
    private final double precioUnitario;

    public LineaPedido(ItemMenu item, int cantidad) {
        this.item = item; this.cantidad = cantidad; this.precioUnitario = item.getPrecio();
    }
    void sumar(int c) { cantidad += c; }
    public ItemMenu getItem() { return item; }
    public int getCantidad() { return cantidad; }
    public double getSubtotal() { return precioUnitario * cantidad; }
}
