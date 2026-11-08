import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;

public final class GeneradorFactura {
    private GeneradorFactura() {}

    public static Path generar(Factura f) throws FideException {
        StringBuilder sb = new StringBuilder();
        sb.append("FIDEBURGUESAS\n");
        sb.append(String.format("Factura N° %06d   %s%n", f.getNumero(),
                f.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        sb.append("Cajero: ").append(f.getPedido().getCajero()).append('\n');
        sb.append("Cliente: ").append(f.getPedido().getCliente()).append('\n');
        sb.append("------------------------------------\n");
        for (LineaPedido l : f.getPedido().getLineas())
            sb.append(String.format("%dx %-22s ₡%,10.0f%n", l.getCantidad(), l.getItem().getNombre(), l.getSubtotal()));
        sb.append("------------------------------------\n");
        sb.append(String.format("%-26s ₡%,10.0f%n", "Subtotal", f.getSubtotal()));
        sb.append(String.format("%-26s ₡%,10.0f%n", "IVA 13%", f.getImpuesto()));
        sb.append(String.format("%-26s ₡%,10.0f%n", "TOTAL", f.getTotal()));
        sb.append(String.format("Pago: %s   Recibido: ₡%,.0f   Vuelto: ₡%,.0f%n", f.getMetodoPago(), f.getRecibido(), f.getVuelto()));
        try {
            Path dir = Paths.get("facturas");
            Files.createDirectories(dir);
            return Files.writeString(dir.resolve(String.format("factura_%06d.txt", f.getNumero())), sb.toString());
        } catch (IOException e) {
            throw new FideException("No se pudo escribir la factura: " + e.getMessage(), e);
        }
    }
}
