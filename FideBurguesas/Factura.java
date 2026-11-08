import java.io.Serializable;
import java.time.LocalDateTime;

public class Factura implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double IVA = 0.13; // precios del menú incluyen IVA
    private final int numero;
    private final Pedido pedido;
    private final LocalDateTime fecha = LocalDateTime.now();
    private final double subtotal, impuesto, total, recibido, vuelto;
    private final String metodoPago;

    public Factura(int numero, Pedido pedido, String metodoPago, double recibido) throws FideException {
        this.numero = numero; this.pedido = pedido; this.metodoPago = metodoPago;
        this.total = pedido.getTotal();
        this.subtotal = total / (1 + IVA);
        this.impuesto = total - subtotal;
        if (recibido < total) throw new FideException("El monto recibido es menor al total.");
        this.recibido = recibido; this.vuelto = recibido - total;
    }
    public int getNumero() { return numero; }
    public Pedido getPedido() { return pedido; }
    public LocalDateTime getFecha() { return fecha; }
    public double getSubtotal() { return subtotal; }
    public double getImpuesto() { return impuesto; }
    public double getTotal() { return total; }
    public double getRecibido() { return recibido; }
    public double getVuelto() { return vuelto; }
    public String getMetodoPago() { return metodoPago; }
}