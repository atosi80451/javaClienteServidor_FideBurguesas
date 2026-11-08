import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;
    private int numero;
    private final LocalDateTime fechaHora = LocalDateTime.now();
    private EstadoPedido estado = EstadoPedido.PENDIENTE;
    private String cliente = "Cliente general";
    private final String cajero;
    private final List<LineaPedido> lineas = new ArrayList<>();

    public Pedido(String cajero) { this.cajero = cajero; }

    public void agregarLinea(ItemMenu item, int cantidad) throws FideException {
        if (cantidad <= 0) throw new FideException("La cantidad debe ser positiva.");
        if (!item.isDisponible()) throw new FideException(item.getNombre() + " no está disponible.");
        for (LineaPedido l : lineas)
            if (l.getItem().getId() == item.getId()) { l.sumar(cantidad); return; }
        lineas.add(new LineaPedido(item, cantidad));
    }
    public void quitarLinea(int indice) { if (indice >= 0 && indice < lineas.size()) lineas.remove(indice); }
    public double getTotal() { return lineas.stream().mapToDouble(LineaPedido::getSubtotal).sum(); }

    public void cambiarEstado(EstadoPedido nuevo) throws FideException {
        if (!estado.puedePasarA(nuevo)) throw new FideException("No se puede pasar de " + estado + " a " + nuevo + ".");
        estado = nuevo;
    }
    public List<LineaPedido> getLineas() { return Collections.unmodifiableList(lineas); }
    public int getNumero() { return numero; }
    public void setNumero(int n) { numero = n; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public EstadoPedido getEstado() { return estado; }
    public String getCliente() { return cliente; }
    public void setCliente(String c) { if (c != null && !c.isBlank()) cliente = c.trim(); }
    public String getCajero() { return cajero; }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(String.format("#%04d | %tR | %s | ", numero, fechaHora, estado));
        for (LineaPedido l : lineas) sb.append(l.getCantidad()).append("x ").append(l.getItem().getNombre()).append(", ");
        return sb.toString().replaceAll(", $", "");
    }
}