import java.awt.*;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CajaPanel extends JPanel implements Refrescable {
    private final Usuario usuario;
    private Pedido actual;
    private final DefaultListModel<ItemMenu> menu = new DefaultListModel<>();
    private final JList<ItemMenu> lista = new JList<>(menu);
    private final JSpinner cant = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final DefaultTableModel mLineas = Ui.modelo("Cant", "Ítem", "Subtotal");
    private final JTable tLineas = new JTable(mLineas);
    private final JLabel total = new JLabel("Total: ₡0");
    private final JTextField cliente = new JTextField("Cliente general");
    private final DefaultTableModel mPedidos = Ui.modelo("N°", "Hora", "Cliente", "Total", "Estado");
    private final JTable tPedidos = new JTable(mPedidos);
    private List<Pedido> visibles = new ArrayList<>();
    private final javax.swing.Timer timer;

    public CajaPanel(Usuario u) {
        usuario = u; nuevo();
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel izq = new JPanel(new BorderLayout(4, 4));
        izq.setPreferredSize(new Dimension(300, 0));
        izq.setBorder(BorderFactory.createTitledBorder("Menú"));
        izq.add(new JScrollPane(lista));
        JButton btnAdd = new JButton("Agregar");
        btnAdd.addActionListener(e -> agregar());
        JPanel pie = new JPanel(); pie.add(new JLabel("Cantidad")); pie.add(cant); pie.add(btnAdd);
        izq.add(pie, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(4, 4));
        centro.setBorder(BorderFactory.createTitledBorder("Pedido actual"));
        centro.add(Ui.fila("Cliente", cliente), BorderLayout.NORTH);
        centro.add(new JScrollPane(tLineas));
        JButton quitar = new JButton("Quitar línea"), enviar = new JButton("Enviar a cocina"), cancelar = new JButton("Limpiar");
        quitar.addActionListener(e -> { actual.quitarLinea(tLineas.getSelectedRow()); redibujar(); });
        enviar.addActionListener(e -> enviar());
        cancelar.addActionListener(e -> { nuevo(); redibujar(); });
        JPanel acc = new JPanel(); acc.add(total); acc.add(quitar); acc.add(cancelar); acc.add(enviar);
        centro.add(acc, BorderLayout.SOUTH);

        JPanel sur = new JPanel(new BorderLayout(4, 4));
        sur.setBorder(BorderFactory.createTitledBorder("Pedidos"));
        JScrollPane sp = new JScrollPane(tPedidos); sp.setPreferredSize(new Dimension(0, 150));
        JButton fact = new JButton("Facturar pedido LISTO");
        fact.addActionListener(e -> facturar());
        sur.add(sp); sur.add(fact, BorderLayout.SOUTH);

        add(izq, BorderLayout.WEST); add(centro); add(sur, BorderLayout.SOUTH);
        timer = new javax.swing.Timer(2000, e -> cargarPedidos());
        timer.start();
        refrescar();
    }

    public void detener() {
        timer.stop(); 
    }

    private void nuevo() { 
        actual = new Pedido(usuario.getNombre()); 
    }

    @Override public void refrescar() {
        menu.clear();
        for (ItemMenu i : Repositorio.get().items()) if (i.isDisponible()) menu.addElement(i);
        cargarPedidos();
    }

    private void agregar() {
        ItemMenu it = lista.getSelectedValue();
        if (it == null) { JOptionPane.showMessageDialog(this, "Seleccione un producto o combo."); return; }
        try { actual.agregarLinea(it, (int) cant.getValue()); redibujar(); }
        catch (FideException e) { Ui.error(this, e); }
    }

    private void redibujar() {
        mLineas.setRowCount(0);
        for (LineaPedido l : actual.getLineas())
            mLineas.addRow(new Object[]{l.getCantidad(), l.getItem().getNombre(), Ui.money(l.getSubtotal())});
        total.setText("Total: " + Ui.money(actual.getTotal()));
    }

    private void enviar() {
        actual.setCliente(cliente.getText());
        try {
            Repositorio.get().enviarACocina(actual);
            nuevo(); redibujar(); cargarPedidos();
        } catch (FideException e) { Ui.error(this, e); }
    }

    private void cargarPedidos() {
        int sel = tPedidos.getSelectedRow();
        visibles = Repositorio.get().pedidos();
        mPedidos.setRowCount(0);
        for (Pedido p : visibles)
            mPedidos.addRow(new Object[]{p.getNumero(), p.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm")),
                    p.getCliente(), Ui.money(p.getTotal()), p.getEstado()});
        if (sel >= 0 && sel < mPedidos.getRowCount()) tPedidos.setRowSelectionInterval(sel, sel);
    }

    private void facturar() {
        int r = tPedidos.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione un pedido."); return; }
        Pedido p = visibles.get(r);
        try {
            if (p.getEstado() != EstadoPedido.LISTO) throw new FideException("El pedido aún no está LISTO.");
            Object m = JOptionPane.showInputDialog(this, "Método de pago:", "Facturar #" + p.getNumero(),
                    JOptionPane.QUESTION_MESSAGE, null, new String[]{"Efectivo", "Tarjeta"}, "Efectivo");
            if (m == null) return;
            double recibido = p.getTotal();
            if (m.equals("Efectivo")) {
                String s = JOptionPane.showInputDialog(this, "Total " + Ui.money(p.getTotal()) + ". Monto recibido:");
                if (s == null) return;
                try { recibido = Double.parseDouble(s.trim()); }
                catch (NumberFormatException ex) { throw new FideException("Monto inválido."); }
            }
            Factura f = Repositorio.get().facturar(p, m.toString(), recibido);
            Path ruta = GeneradorFactura.generar(f);
            JOptionPane.showMessageDialog(this, "Factura generada: " + ruta.toAbsolutePath()
                    + "\nVuelto: " + Ui.money(f.getVuelto()));
            cargarPedidos();
        } catch (FideException e) { Ui.error(this, e); }
    }
}
