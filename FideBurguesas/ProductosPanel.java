import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;


public class ProductosPanel extends Estilo implements Refrescable {
    private static final String[] CATEGORIAS = {"Hamburguesa", "Bebida", "Acompañamiento", "Postre"};
    private static final Color FONDO_DISPONIBLE = new Color(0xFFFBF5), FONDO_NO_DISPONIBLE = new Color(0xEEEBE6);

    private final CardLayout cartas = new CardLayout();

    private final DefaultTableModel m = Ui.modelo("Nombre", "Categoría", "Descripción", "Precio", "Estado");
    private final JTable tabla = new JTable(m);
    private final JButton btnEstado = new Boton("Marcar no disponible", 1);
    private List<Producto> datos = new ArrayList<>();

    private Producto editando;
    private boolean cargando, tocado;
    private final JTextField nombre = entrada(new JTextField());
    private final JTextField descripcion = entrada(new JTextField());
    private final JTextField precio = entrada(new JTextField());
    private final JComboBox<String> categoria = new JComboBox<>(CATEGORIAS);
    private final JComboBox<String> disponibilidad = new JComboBox<>(new String[]{"Disponible", "No disponible"});
    private final JButton btnCancelar = new Boton("Cancelar", 1);
    private final JButton btnGuardar = new Boton("✓  Guardar producto", 0);

    private final JLabel migas = texto("", 12, GRIS, false);
    private final JLabel titulo = texto("", 30, TEXTO, true);
    private final JLabel subtitulo = texto("", 13, GRIS, false);
    private final JLabel errorPrecio = texto(" ", 11, ROJO, false);
    private final JLabel pieTexto = texto("", 11, GRIS, false);

    private final Tarjeta cajaEstado = new Tarjeta(CAJA, null, 12);
    private final JLabel estadoTitulo = texto("", 13, TEXTO, true);
    private final JLabel estadoTexto = texto("", 12, GRIS, false);

    private final Tarjeta banner = new Tarjeta(ROJO_SUAVE, null, 10);
    private final JLabel bannerTitulo = texto("", 13, ROJO, true);
    private final JLabel bannerTexto = texto("", 12, ROJO, false);

    private final Tarjeta previewCard = new Tarjeta(FONDO_NO_DISPONIBLE, BORDE, 14);
    private final JLabel pvNombre = texto("", 22, TEXTO, true);
    private final JLabel pvDesc = texto("", 13, GRIS, false);
    private final JLabel pvEstado = texto("", 12, GRIS, true);
    private final JLabel pvPrecio = texto("", 14, TEXTO, true);

    public ProductosPanel() {
        estilizar(categoria);
        estilizar(disponibilidad);
        escuchar(nombre);
        escuchar(descripcion);
        escuchar(precio);
        categoria.addActionListener(e -> { if (!cargando) actualizar(); });
        disponibilidad.addActionListener(e -> { if (!cargando) actualizar(); });
        setLayout(cartas);
        setBackground(CREMA);
        add(vistaLista(), "lista");
        add(vistaFormulario(), "form");
        refrescar();
    }

    private void escuchar(JTextField f) {
        f.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cambio(); }
            @Override public void removeUpdate(DocumentEvent e) { cambio(); }
            @Override public void changedUpdate(DocumentEvent e) { cambio(); }
        });
    }

    private void cambio() {
        if (cargando) return;
        tocado = true;
        actualizar();
    }

    private JPanel vistaLista() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(CREMA);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel enc = new JPanel();
        enc.setOpaque(false);
        enc.setLayout(new BoxLayout(enc, BoxLayout.Y_AXIS));
        enc.add(izq(texto("ADMINISTRACIÓN", 10, ROJO, true)));
        enc.add(Box.createVerticalStrut(4));
        enc.add(izq(texto("Productos", 30, TEXTO, true)));
        enc.add(Box.createVerticalStrut(4));
        enc.add(izq(texto("Registra los artículos del menú, su precio y si están disponibles en caja.", 13, GRIS, false)));
        p.add(enc, BorderLayout.NORTH);

        tabla.setRowHeight(34);
        tabla.setFont(new Font(FUENTE, Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font(FUENTE, Font.BOLD, 12));
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(BORDE);
        tabla.setSelectionBackground(ROJO_SUAVE);
        tabla.setSelectionForeground(TEXTO);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editarSeleccionado();
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int r = tabla.getSelectedRow();
            if (r >= 0 && r < datos.size())
                btnEstado.setText(datos.get(r).isDisponible() ? "Marcar no disponible" : "Marcar disponible");
        });
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(new BordeRedondo(BORDE, 12));
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp);

        JButton nuevo = new Boton("+  Nuevo producto", 0), editar = new Boton("Editar", 1);
        nuevo.addActionListener(e -> abrirFormulario(null));
        editar.addActionListener(e -> editarSeleccionado());
        btnEstado.addActionListener(e -> {
            Producto sel = seleccionado();
            if (sel != null) {
                Repositorio.get().cambiarDisponibilidad(sel, !sel.isDisponible());
                refrescar();
            }
        });
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        b.setOpaque(false);
        b.add(nuevo);
        b.add(editar);
        b.add(btnEstado);
        p.add(b, BorderLayout.SOUTH);
        return p;
    }

    private Producto seleccionado() {
        int r = tabla.getSelectedRow();
        if (r < 0 || r >= datos.size()) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.");
            return null;
        }
        return datos.get(r);
    }

    private void editarSeleccionado() {
        Producto p = seleccionado();
        if (p != null) abrirFormulario(p);
    }

    private JPanel vistaFormulario() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(CREMA);
        p.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        migas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        migas.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { mostrarLista(); }
        });
        JPanel cab = new JPanel();
        cab.setOpaque(false);
        cab.setLayout(new BoxLayout(cab, BoxLayout.Y_AXIS));
        cab.add(izq(migas));
        cab.add(Box.createVerticalStrut(8));
        cab.add(izq(titulo));
        cab.add(Box.createVerticalStrut(4));
        cab.add(izq(subtitulo));
        p.add(cab, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        g.weighty = 1;
        g.gridx = 0; g.weightx = 0.64; g.insets = new Insets(0, 0, 0, 16);
        cuerpo.add(tarjetaDatos(), g);
        g.gridx = 1; g.weightx = 0.36; g.insets = new Insets(0, 0, 0, 0);
        cuerpo.add(columnaDerecha(), g);

        JScrollPane sc = new JScrollPane(cuerpo);
        sc.setBorder(null);
        sc.getViewport().setBackground(CREMA);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        p.add(sc);

        btnCancelar.addActionListener(e -> mostrarLista());
        btnGuardar.addActionListener(e -> guardar());
        return p;
    }

    private JPanel campoCon(String etiqueta, JComponent campo) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(izq(texto(etiqueta, 12, TEXTO, true)));
        p.add(Box.createVerticalStrut(5));
        p.add(izq(campo));
        p.add(Box.createVerticalStrut(6));
        return p;
    }

    private JPanel dosColumnas(JComponent a, JComponent b) {
        JPanel f = new JPanel(new GridLayout(1, 2, 12, 0));
        f.setOpaque(false);
        f.add(a);
        f.add(b);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        return f;
    }

    private Tarjeta tarjetaDatos() {
        Tarjeta c = new Tarjeta(Color.WHITE, BORDE, 16);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createEmptyBorder(24, 26, 22, 26));

        c.add(izq(texto("Datos del producto", 18, TEXTO, true)));
        c.add(Box.createVerticalStrut(14));

        Tarjeta pastilla = new Tarjeta(ROJO_SUAVE, null, 20);
        pastilla.setLayout(new BorderLayout());
        pastilla.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        pastilla.add(texto("●  Producto", 11, ROJO, true));
        JPanel pastillaWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pastillaWrap.setOpaque(false);
        pastillaWrap.add(pastilla);
        pastillaWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        c.add(izq(pastillaWrap));
        c.add(Box.createVerticalStrut(14));

        c.add(izq(dosColumnas(campoCon("Nombre", nombre), campoCon("Categoría", categoria))));
        c.add(izq(campoCon("Descripción · opcional", descripcion)));
        c.add(izq(texto("Información breve que verá el equipo de caja.", 11, GRIS, false)));
        c.add(Box.createVerticalStrut(14));

        c.add(izq(dosColumnas(campoCon("Precio · ₡", precio), campoCon("Disponibilidad", disponibilidad))));
        c.add(izq(errorPrecio));
        c.add(Box.createVerticalStrut(10));

        cajaEstado.setLayout(new BoxLayout(cajaEstado, BoxLayout.Y_AXIS));
        cajaEstado.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        cajaEstado.add(izq(estadoTitulo));
        cajaEstado.add(Box.createVerticalStrut(3));
        cajaEstado.add(izq(estadoTexto));
        cajaEstado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        c.add(izq(cajaEstado));
        c.add(Box.createVerticalStrut(14));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        c.add(izq(sep));
        c.add(Box.createVerticalStrut(14));

        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        banner.add(izq(bannerTitulo));
        banner.add(Box.createVerticalStrut(4));
        banner.add(izq(bannerTexto));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        banner.setVisible(false);
        c.add(izq(banner));
        c.add(Box.createVerticalStrut(14));

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(pieTexto, BorderLayout.WEST);
        JPanel bs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bs.setOpaque(false);
        bs.add(btnCancelar);
        bs.add(btnGuardar);
        pie.add(bs, BorderLayout.EAST);
        pie.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        c.add(izq(pie));
        return c;
    }

    private JPanel columnaDerecha() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        Tarjeta vista = new Tarjeta(Color.WHITE, BORDE, 16);
        vista.setLayout(new BoxLayout(vista, BoxLayout.Y_AXIS));
        vista.setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));
        vista.add(izq(texto("Vista previa en caja", 18, TEXTO, true)));
        vista.add(Box.createVerticalStrut(14));

        previewCard.setLayout(new BoxLayout(previewCard, BoxLayout.Y_AXIS));
        previewCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        previewCard.add(izq(texto("🍔", 30, TEXTO, false)));
        previewCard.add(Box.createVerticalStrut(14));
        previewCard.add(izq(pvNombre));
        previewCard.add(Box.createVerticalStrut(8));
        previewCard.add(izq(pvDesc));
        previewCard.add(Box.createVerticalStrut(10));
        previewCard.add(izq(pvEstado));
        previewCard.add(Box.createVerticalStrut(12));
        previewCard.add(izq(pvPrecio));
        previewCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        vista.add(izq(previewCard));
        vista.add(Box.createVerticalStrut(12));
        vista.add(izq(texto("Vista previa · No se publicará hasta guardar.", 11, GRIS, false)));

        Tarjeta tip = new Tarjeta(BEIGE, null, 16);
        tip.setLayout(new BoxLayout(tip, BoxLayout.Y_AXIS));
        tip.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        tip.add(izq(texto("Un menú bien definido", 18, TEXTO, true)));
        tip.add(Box.createVerticalStrut(12));
        tip.add(izq(texto("<html><body style='width:220px'>Nombre y categoría identifican el artículo. "
                + "El precio debe ser positivo; la disponibilidad controla su venta.</body></html>", 13, TEXTO, false)));

        col.add(izq(vista));
        col.add(Box.createVerticalStrut(18));
        col.add(izq(tip));
        return col;
    }
        
    public void nuevoProducto() { 
        abrirFormulario(null); 
    }

    private void mostrarLista() {
        refrescar();
        cartas.show(this, "lista");
    }

    private static String fmt(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void abrirFormulario(Producto p) {
        editando = p;
        boolean ed = p != null;
        cargando = true;
        nombre.setText(ed ? p.getNombre() : "");
        descripcion.setText(ed ? p.getDescripcion() : "");
        precio.setText(ed ? fmt(p.getPrecio()) : "");
        categoria.setSelectedItem(ed ? p.getCategoria() : CATEGORIAS[0]);
        disponibilidad.setSelectedIndex(ed && !p.isDisponible() ? 1 : 0);
        cargando = false;
        tocado = false;

        migas.setText("←  Productos  /  " + (ed ? p.getNombre() : "Nuevo producto"));
        titulo.setText(ed ? "Editar producto" : "Registrar producto");
        subtitulo.setText(ed ? "Actualiza los datos, el precio y la disponibilidad del artículo."
                : "Agrega un artículo al menú con nombre, categoría y precio de venta.");
        pieTexto.setText(ed ? "Editando producto" : "Nuevo producto · Aún sin guardar");
        btnGuardar.setText(ed ? "✓  Guardar cambios" : "✓  Guardar producto");
        actualizar();
        cartas.show(this, "form");
    }

    private Double leerPrecio() {
        try { return Double.valueOf(precio.getText().trim().replace(',', '.')); }
        catch (NumberFormatException e) { return null; }
    }

    private static void marcarError(JTextField f, boolean error) {
        f.setBorder(BorderFactory.createCompoundBorder(new BordeRedondo(error ? ROJO : BORDE_CAMPO, 10),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
    }

    private void actualizar() {
        String errNombre = nombre.getText().isBlank() ? "El nombre es obligatorio." : null;
        Double v = leerPrecio();
        String errPrecio = null;
        if (precio.getText().isBlank() || v == null) errPrecio = "El precio debe ser un número.";
        else if (v <= 0) errPrecio = "El precio debe ser mayor a 0.";
        boolean valido = errNombre == null && errPrecio == null;

        marcarError(nombre, tocado && errNombre != null);
        marcarError(precio, tocado && errPrecio != null);
        errorPrecio.setText(tocado && errPrecio != null ? errPrecio : " ");
        btnGuardar.setEnabled(valido);

        if (tocado && errPrecio != null) {
            bannerTitulo.setText("Revisa el precio antes de guardar");
            bannerTexto.setText("Ingresa un importe mayor a 0. El producto todavía no está registrado.");
            banner.setVisible(true);
        } else if (tocado && errNombre != null) {
            bannerTitulo.setText("Falta el nombre del producto");
            bannerTexto.setText("Escribe un nombre para identificar el artículo en caja.");
            banner.setVisible(true);
        } else {
            banner.setVisible(false);
        }

        boolean disp = disponibilidad.getSelectedIndex() == 0;
        estadoTitulo.setText(disp ? "Disponible en caja" : "No disponible en caja");
        estadoTexto.setText(disp ? "El artículo se podrá añadir a nuevos pedidos."
                : "El artículo no se podrá añadir a nuevos pedidos mientras mantenga este estado.");

        previewCard.setFondo(disp ? FONDO_DISPONIBLE : FONDO_NO_DISPONIBLE);
        pvNombre.setText(nombre.getText().isBlank() ? "Nombre del producto" : "<html>" + esc(nombre.getText().trim()) + "</html>");
        String d = descripcion.getText().trim();
        pvDesc.setText("<html><body style='width:200px'>" + (d.isEmpty() ? "Sin descripción." : esc(d)) + "</body></html>");
        pvEstado.setText(disp ? "●  Disponible" : "●  No disponible");
        pvEstado.setForeground(disp ? VERDE : GRIS);
        if (errPrecio == null) {
            pvPrecio.setText(Ui.money(v));
            pvPrecio.setForeground(TEXTO);
        } else {
            pvPrecio.setText("Precio pendiente de corregir");
            pvPrecio.setForeground(ROJO);
        }
    }

    private void guardar() {
        tocado = true;
        actualizar();
        if (!btnGuardar.isEnabled()) return;
        try {
            double v = leerPrecio();
            String cat = (String) categoria.getSelectedItem();
            boolean disp = disponibilidad.getSelectedIndex() == 0;
            if (editando == null) {
                Repositorio.get().agregarProducto(nombre.getText(), cat, descripcion.getText(), v, disp);
            } else {
                Repositorio.get().actualizarProducto(editando, nombre.getText(), cat, descripcion.getText(), v);
                Repositorio.get().cambiarDisponibilidad(editando, disp);
            }
            mostrarLista();
        } catch (FideException e) { Ui.error(this, e); }
    }

    @Override public void refrescar() {
        datos = Repositorio.get().productos();
        m.setRowCount(0);
        for (Producto p : datos)
            m.addRow(new Object[]{p.getNombre(), p.getCategoria(), p.getDescripcion(), Ui.money(p.getPrecio()),
                    p.isDisponible() ? "Disponible" : "No disponible"});
    }
}