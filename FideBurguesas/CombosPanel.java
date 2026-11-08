import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;


public class CombosPanel extends Estilo implements Refrescable {
    private final CardLayout cartas = new CardLayout();
    private Runnable irAProducto;

    private final DefaultTableModel m = Ui.modelo("Combo", "Productos", "Descuento", "Precio", "Estado");
    private final JTable tabla = new JTable(m);
    private final JButton btnEstado = new Boton("Deshabilitar", 1);
    private List<Combo> datos = new ArrayList<>();

    private final Map<Producto, Integer> comp = new LinkedHashMap<>();
    private boolean cargando, tocado;
    private final JTextField nombre = entrada(new JTextField());
    private final JTextField descripcion = entrada(new JTextField());
    private final JTextField descuento = entrada(new JTextField());
    private final JComboBox<String> categoria = new JComboBox<>(new String[]{"Combos"});
    private final JComboBox<String> disponibilidad = new JComboBox<>(new String[]{"Disponible", "No disponible"});
    private final JPanel filas = new JPanel();
    private final JButton btnAnadir = new Boton("+  Añadir producto", 1);
    private final JButton btnCancelar = new Boton("Cancelar", 1);
    private final JButton btnGuardar = new Boton("✓  Guardar combo", 0);

    private final JLabel migas = texto("", 12, GRIS, false);
    private final JLabel titulo = texto("Registrar combo", 30, TEXTO, true);
    private final JLabel subtitulo = texto("Combina productos existentes y aplica un descuento fijo al precio automático.", 13, GRIS, false);
    private final JLabel resumen = texto("", 11, GRIS, false);
    private final JLabel errorDescuento = texto(" ", 11, ROJO, false);
    private final JLabel pieTexto = texto("", 11, GRIS, false);
    private final JLabel lblPrecioCalc = texto("", 14, TEXTO, true);

    private final Tarjeta banner = new Tarjeta(ROJO_SUAVE, null, 10);
    private final JLabel bannerTitulo = texto("", 13, ROJO, true);
    private final JLabel bannerTexto = texto("", 12, ROJO, false);

    private final JLabel lblBase = texto("", 13, TEXTO, true);
    private final JLabel lblDesc = texto("", 13, TEXTO, true);
    private final JLabel lblFinal = texto("", 28, TEXTO, true);

    private final JLabel pvNombre = texto("", 22, TEXTO, true);
    private final JLabel pvComp = texto("", 12, GRIS, false);
    private final JLabel pvPrecio = texto("", 30, TEXTO, true);
    private final Tarjeta pvPastilla = new Tarjeta(ROJO_SUAVE, null, 20);
    private final JLabel pvPastillaTexto = texto("", 11, ROJO, true);

    public CombosPanel() {
        estilizar(categoria);
        estilizar(disponibilidad);
        categoria.setEnabled(false);
        escuchar(nombre);
        escuchar(descripcion);
        escuchar(descuento);
        disponibilidad.addActionListener(e -> { if (!cargando) actualizar(); });
        setLayout(cartas);
        setBackground(CREMA);
        add(vistaLista(), "lista");
        add(vistaFormulario(), "form");
        refrescar();
    }

    public void setAlElegirProducto(Runnable r) { irAProducto = r; }

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
        enc.add(izq(texto("Combos", 30, TEXTO, true)));
        enc.add(Box.createVerticalStrut(4));
        enc.add(izq(texto("Agrupa productos con un descuento fijo para ofrecer promociones en caja.", 13, GRIS, false)));
        p.add(enc, BorderLayout.NORTH);

        tabla.setRowHeight(34);
        tabla.setFont(new Font(FUENTE, Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font(FUENTE, Font.BOLD, 12));
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(BORDE);
        tabla.setSelectionBackground(ROJO_SUAVE);
        tabla.setSelectionForeground(TEXTO);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int r = tabla.getSelectedRow();
            if (r >= 0 && r < datos.size()) btnEstado.setText(datos.get(r).isHabilitado() ? "Deshabilitar" : "Habilitar");
        });
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(new BordeRedondo(BORDE, 12));
        sp.getViewport().setBackground(Color.WHITE);        
        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { ajustarColumnas(); }
        });
        p.add(sp);

        JButton nuevo = new Boton("+  Nuevo combo", 0);
        nuevo.addActionListener(e -> abrirFormulario());
        btnEstado.addActionListener(e -> {
            int r = tabla.getSelectedRow();
            if (r < 0 || r >= datos.size()) {
                JOptionPane.showMessageDialog(this, "Seleccione un combo de la tabla.");
                return;
            }
            Combo c = datos.get(r);
            Repositorio.get().cambiarDisponibilidad(c, !c.isHabilitado());
            refrescar();
        });
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        b.setOpaque(false);
        b.add(nuevo);
        b.add(btnEstado);
        p.add(b, BorderLayout.SOUTH);
        return p;
    }

    private JPanel vistaFormulario() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(CREMA);
        p.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        migas.setText("←  Combos  /  Nuevo combo");
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
        g.gridx = 0; g.weightx = 0.66; g.insets = new Insets(0, 0, 0, 16);
        cuerpo.add(tarjetaDatos(), g);
        g.gridx = 1; g.weightx = 0.34; g.insets = new Insets(0, 0, 0, 0);
        cuerpo.add(columnaDerecha(), g);

        JScrollPane sc = new JScrollPane(cuerpo);
        sc.setBorder(null);
        sc.getViewport().setBackground(CREMA);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        p.add(sc);

        btnAnadir.addActionListener(e -> anadirProducto());
        btnCancelar.addActionListener(e -> mostrarLista());
        btnGuardar.addActionListener(e -> guardar());
        return p;
    }

    private Tarjeta opcionTipo(String icono, String titulo, String sub, boolean elegido) {
        Tarjeta t = new Tarjeta(elegido ? ROJO_SUAVE : Color.WHITE, elegido ? ROJO : BORDE_CAMPO, 12);
        t.setLayout(new BorderLayout(12, 0));
        t.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(izq(texto(titulo, 13, elegido ? ROJO : TEXTO, true)));
        textos.add(izq(texto(sub, 10, GRIS, false)));
        t.add(texto(icono, 20, TEXTO, false), BorderLayout.WEST);
        t.add(textos);
        t.add(texto(elegido ? "✔" : "○", 16, elegido ? ROJO : GRIS, true), BorderLayout.EAST);
        return t;
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

    private JSeparator separador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return sep;
    }

    private Tarjeta tarjetaDatos() {
        Tarjeta c = new Tarjeta(Color.WHITE, BORDE, 16);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        Tarjeta opProducto = opcionTipo("📦", "Producto", "Un artículo individual", false);
        opProducto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        opProducto.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (irAProducto != null) irAProducto.run(); }
        });
        Tarjeta opCombo = opcionTipo("🍽", "Combo", "Varios productos en uno", true);
        JPanel tipos = new JPanel(new GridLayout(1, 2, 12, 0));
        tipos.setOpaque(false);
        tipos.add(opProducto);
        tipos.add(opCombo);
        tipos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        c.add(izq(tipos));
        c.add(Box.createVerticalStrut(14));

        c.add(izq(dosColumnas(campoCon("Nombre", nombre), campoCon("Categoría", categoria))));
        c.add(izq(campoCon("Descripción · opcional", descripcion)));
        c.add(izq(campoCon("Disponibilidad al guardar", disponibilidad)));
        c.add(Box.createVerticalStrut(8));
        c.add(izq(separador()));
        c.add(Box.createVerticalStrut(14));

        JPanel cabComp = new JPanel(new BorderLayout());
        cabComp.setOpaque(false);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(izq(texto("Composición del combo", 16, TEXTO, true)));
        t.add(Box.createVerticalStrut(2));
        t.add(izq(texto("Cantidades incluidas en cada combo vendido.", 11, GRIS, false)));
        cabComp.add(t);
        cabComp.add(btnAnadir, BorderLayout.EAST);
        cabComp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        c.add(izq(cabComp));
        c.add(Box.createVerticalStrut(10));

        filas.setLayout(new BoxLayout(filas, BoxLayout.Y_AXIS));
        filas.setOpaque(false);
        Tarjeta tablaComp = new Tarjeta(Color.WHITE, BORDE, 8);
        tablaComp.setLayout(new BorderLayout());
        tablaComp.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tablaComp.add(filas);
        c.add(izq(tablaComp));
        c.add(Box.createVerticalStrut(12));

        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        banner.add(izq(bannerTitulo));
        banner.add(Box.createVerticalStrut(4));
        banner.add(izq(bannerTexto));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        c.add(izq(banner));
        c.add(Box.createVerticalStrut(8));
        c.add(izq(resumen));
        c.add(Box.createVerticalStrut(12));

        Tarjeta precioCalc = new Tarjeta(CAJA, BORDE, 10);
        precioCalc.setLayout(new BorderLayout());
        precioCalc.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        precioCalc.add(lblPrecioCalc);
        precioCalc.add(texto("Solo lectura", 10, GRIS, false), BorderLayout.EAST);
        precioCalc.setPreferredSize(new Dimension(200, 42));
        precioCalc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        c.add(izq(dosColumnas(campoCon("Descuento · ₡", descuento), campoCon("Precio calculado · ₡", precioCalc))));
        c.add(izq(errorDescuento));
        c.add(Box.createVerticalStrut(10));

        Tarjeta totales = new Tarjeta(CAJA, BORDE, 12);
        totales.setLayout(new BoxLayout(totales, BoxLayout.Y_AXIS));
        totales.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        totales.add(izq(datoFila("Suma base", lblBase, GRIS)));
        totales.add(Box.createVerticalStrut(6));
        totales.add(izq(datoFila("Descuento fijo", lblDesc, GRIS)));
        totales.add(Box.createVerticalStrut(10));
        totales.add(izq(separador()));
        totales.add(Box.createVerticalStrut(10));
        JPanel fin = new JPanel(new BorderLayout());
        fin.setOpaque(false);
        fin.add(texto("Precio final calculado", 14, TEXTO, true), BorderLayout.WEST);
        fin.add(lblFinal, BorderLayout.EAST);
        totales.add(izq(fin));
        totales.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        c.add(izq(totales));
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

    private JPanel datoFila(String etiqueta, JLabel valor, Color colorEtiqueta) {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.add(texto(etiqueta, 13, colorEtiqueta, false), BorderLayout.WEST);
        f.add(valor, BorderLayout.EAST);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return f;
    }

    private JPanel columnaDerecha() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        Tarjeta vista = new Tarjeta(Color.WHITE, BORDE, 16);
        vista.setLayout(new BoxLayout(vista, BoxLayout.Y_AXIS));
        vista.setBorder(BorderFactory.createEmptyBorder(22, 22, 20, 22));
        vista.add(izq(texto("Vista previa del combo", 17, TEXTO, true)));
        vista.add(Box.createVerticalStrut(14));

        Tarjeta tarjeta = new Tarjeta(Color.WHITE, BORDE, 14);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        tarjeta.add(izq(texto("🍴", 22, ROJO, false)));
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(izq(pvNombre));
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(izq(pvComp));
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(izq(pvPrecio));
        tarjeta.add(Box.createVerticalStrut(12));
        pvPastilla.setLayout(new BorderLayout());
        pvPastilla.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        pvPastilla.add(pvPastillaTexto);
        JPanel pastillaWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pastillaWrap.setOpaque(false);
        pastillaWrap.add(pvPastilla);
        pastillaWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        tarjeta.add(izq(pastillaWrap));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        vista.add(izq(tarjeta));
        vista.add(Box.createVerticalStrut(12));
        vista.add(izq(texto("No aparecerá en caja hasta completar y guardar.", 11, GRIS, false)));

        Tarjeta tip = new Tarjeta(BEIGE, null, 16);
        tip.setLayout(new BoxLayout(tip, BoxLayout.Y_AXIS));
        tip.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        tip.add(izq(texto("Un combo, varios productos", 17, TEXTO, true)));
        tip.add(Box.createVerticalStrut(12));
        tip.add(izq(texto("<html><body style='width:200px'>Mínimo 2 productos existentes. El precio se calcula "
                + "con las cantidades y el descuento; no es editable.</body></html>", 12, TEXTO, false)));
        tip.add(Box.createVerticalStrut(10));
        tip.add(izq(texto("<html><body style='width:200px'>El descuento no puede dejar el precio final "
                + "en ₡0 o negativo.</body></html>", 12, TEXTO, false)));

        col.add(izq(vista));
        col.add(Box.createVerticalStrut(18));
        col.add(izq(tip));
        return col;
    }

    private static final int ANCHO_CANT = 120, ANCHO_PRECIO = 90, ANCHO_ACCION = 70;

    private JPanel filaTabla(JComponent nombreCol, JComponent cantCol, JComponent precioCol, JComponent accionCol, boolean cabecera) {
        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(cabecera);
        if (cabecera) f.setBackground(CAJA);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(cabecera ? 0 : 1, 0, 0, 0, BORDE),
                BorderFactory.createEmptyBorder(cabecera ? 8 : 10, 14, cabecera ? 8 : 10, 14)));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        f.add(nombreCol, g);
        g.weightx = 0; g.fill = GridBagConstraints.NONE;
        cantCol.setPreferredSize(new Dimension(ANCHO_CANT, cantCol.getPreferredSize().height));
        precioCol.setPreferredSize(new Dimension(ANCHO_PRECIO, precioCol.getPreferredSize().height));
        accionCol.setPreferredSize(new Dimension(ANCHO_ACCION, accionCol.getPreferredSize().height));
        g.gridx = 1; f.add(cantCol, g);
        g.gridx = 2; f.add(precioCol, g);
        g.gridx = 3; f.add(accionCol, g);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, cabecera ? 34 : 62));
        return f;
    }

    private JLabel derecha(JLabel l) {
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private JLabel centro(JLabel l) {
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private void reconstruirFilas() {
        filas.removeAll();
        filas.add(izq(filaTabla(texto("Producto", 11, GRIS, false), centro(texto("Cantidad", 11, GRIS, false)),
                derecha(texto("Precio unit.", 11, GRIS, false)), derecha(texto("Acción", 11, GRIS, false)), true)));
        if (comp.isEmpty()) {
            JLabel vacio = texto("Aún no hay productos. Usa «Añadir producto».", 12, GRIS, false);
            vacio.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
            filas.add(izq(vacio));
        }
        for (Map.Entry<Producto, Integer> en : new ArrayList<>(comp.entrySet())) {
            Producto p = en.getKey();
            int cantidad = en.getValue();

            JPanel nom = new JPanel();
            nom.setOpaque(false);
            nom.setLayout(new BoxLayout(nom, BoxLayout.Y_AXIS));
            nom.add(izq(texto(p.getNombre(), 13, TEXTO, true)));
            nom.add(izq(texto(p.getCategoria() + " · Producto existente", 10, GRIS, false)));

            JButton menos = new Boton("−", 1), mas = new Boton("+", 1);
            menos.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
            mas.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
            menos.setEnabled(cantidad > 1);
            menos.addActionListener(e -> { comp.put(p, comp.get(p) - 1); tocado = true; reconstruirFilas(); actualizar(); });
            mas.addActionListener(e -> { comp.put(p, Math.min(99, comp.get(p) + 1)); tocado = true; reconstruirFilas(); actualizar(); });
            JPanel stepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            stepper.setOpaque(false);
            stepper.add(menos);
            stepper.add(texto(String.valueOf(cantidad), 13, TEXTO, true));
            stepper.add(mas);

            JLabel eliminar = derecha(texto("Eliminar", 11, ROJO, true));
            eliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eliminar.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { comp.remove(p); tocado = true; reconstruirFilas(); actualizar(); }
            });

            filas.add(izq(filaTabla(nom, stepper, derecha(texto(Ui.money(p.getPrecio()), 13, TEXTO, false)), eliminar, false)));
        }
        filas.revalidate();
        filas.repaint();
    }

    private void anadirProducto() {
        List<Producto> opciones = new ArrayList<>();
        for (Producto p : Repositorio.get().productos())
            if (p.isDisponible() && !comp.containsKey(p)) opciones.add(p);
        if (opciones.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay más productos disponibles para añadir.");
            return;
        }
        JComboBox<Producto> cb = new JComboBox<>(opciones.toArray(new Producto[0]));
        JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        int r = JOptionPane.showConfirmDialog(this, new Object[]{"Producto:", cb, "Cantidad por combo:", cantidad},
                "Añadir producto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        comp.put((Producto) cb.getSelectedItem(), (Integer) cantidad.getValue());
        tocado = true;
        reconstruirFilas();
        actualizar();
    }

    private void mostrarLista() {
        refrescar();
        cartas.show(this, "lista");
    }

    private void abrirFormulario() {
        cargando = true;
        comp.clear();
        nombre.setText("");
        descripcion.setText("");
        descuento.setText("0");
        disponibilidad.setSelectedIndex(0);
        cargando = false;
        tocado = false;
        reconstruirFilas();
        actualizar();
        cartas.show(this, "form");
    }

    private Double leerDescuento() {
        String t = descuento.getText().trim();
        if (t.isEmpty()) return 0.0;
        try { return Double.valueOf(t.replace(',', '.')); }
        catch (NumberFormatException e) { return null; }
    }

    private static void marcarError(JTextField f, boolean error) {
        f.setBorder(BorderFactory.createCompoundBorder(new BordeRedondo(error ? ROJO : BORDE_CAMPO, 10),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
    }

    private static String plural(int n, String singular, String plural) { return n + " " + (n == 1 ? singular : plural); }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void actualizar() {
        double base = Combo.calcularBase(comp);
        Double d = leerDescuento();
        String errDesc = null;
        if (d == null) errDesc = "El descuento debe ser un número.";
        else if (d < 0) errDesc = "El descuento no puede ser negativo.";
        else if (!comp.isEmpty() && base - d <= 0) errDesc = "El descuento no puede dejar el precio final en ₡0 o negativo.";
        double usado = (d != null && d >= 0) ? d : 0;
        double fin = Math.max(0, base - usado);

        int nProd = comp.size();
        int unidades = comp.values().stream().mapToInt(Integer::intValue).sum();
        String errNombre = nombre.getText().isBlank() ? "El nombre es obligatorio." : null;
        boolean completa = nProd >= 2;
        boolean valido = completa && errDesc == null && errNombre == null;

        marcarError(nombre, tocado && errNombre != null);
        marcarError(descuento, tocado && errDesc != null);
        errorDescuento.setText(tocado && errDesc != null ? errDesc : " ");
        btnGuardar.setEnabled(valido);

        String bt = null, bx = null;
        if (!completa) {
            bt = "Agrega al menos 2 productos existentes";
            bx = nProd == 0 ? "Todavía no hay productos en el combo. Añade productos del catálogo para poder guardarlo."
                    : "Solo hay 1 producto en el combo. Añade otro producto del catálogo para poder guardarlo.";
        } else if (tocado && errDesc != null) {
            bt = "Revisa el descuento"; bx = errDesc;
        } else if (tocado && errNombre != null) {
            bt = "Falta el nombre del combo"; bx = "Escribe un nombre para identificar el combo en caja.";
        }
        banner.setVisible(bt != null);
        if (bt != null) { bannerTitulo.setText(bt); bannerTexto.setText(bx); }

        resumen.setText(plural(nProd, "producto", "productos") + " · " + plural(unidades, "unidad", "unidades")
                + " por combo · " + (completa ? "Composición completa" : "Aún incompleto"));
        pieTexto.setText(valido ? "Listo para guardar."
                : !completa ? "No se puede guardar: composición incompleta." : "No se puede guardar: revisa los datos marcados.");

        lblBase.setText(Ui.money(base));
        lblDesc.setText("−" + Ui.money(usado));
        lblFinal.setText(Ui.money(fin));
        lblPrecioCalc.setText(Ui.money(fin));

        pvNombre.setText(nombre.getText().isBlank() ? "Nombre del combo" : "<html>" + esc(nombre.getText().trim()) + "</html>");
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Producto, Integer> en : comp.entrySet())
            sb.append(en.getValue() > 1 ? en.getValue() + "× " : "").append(esc(en.getKey().getNombre())).append(" · ");
        String lista = sb.length() == 0 ? "Sin productos" : sb.substring(0, sb.length() - 3);
        pvComp.setText("<html><body style='width:190px'>" + lista + (completa ? "" : " · Composición incompleta") + "</body></html>");
        pvPrecio.setText(Ui.money(fin));
        pvPastillaTexto.setText(valido ? "●  Listo para publicar" : "●  Sin publicar");
        pvPastillaTexto.setForeground(valido ? VERDE : ROJO);
        pvPastilla.setFondo(valido ? VERDE_SUAVE : ROJO_SUAVE);
    }

    private void guardar() {
        tocado = true;
        actualizar();
        if (!btnGuardar.isEnabled()) return;
        try {
            Repositorio.get().agregarCombo(nombre.getText(), descripcion.getText(), new LinkedHashMap<>(comp),
                    leerDescuento(), disponibilidad.getSelectedIndex() == 0);
            mostrarLista();
        } catch (FideException e) { Ui.error(this, e); }
    }

    private void ajustarColumnas() {
        int total = 0;
        for (int col = 0; col < tabla.getColumnCount(); col++) {
            javax.swing.table.TableColumn tc = tabla.getColumnModel().getColumn(col);
            Component encabezado = tabla.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(tabla, tc.getHeaderValue(), false, false, -1, col);
            int ancho = encabezado.getPreferredSize().width;
            for (int fila = 0; fila < tabla.getRowCount(); fila++) {
                Component celda = tabla.prepareRenderer(tabla.getCellRenderer(fila, col), fila, col);
                ancho = Math.max(ancho, celda.getPreferredSize().width);
            }
            ancho += 24;
            tc.setPreferredWidth(ancho);
            total += ancho;
        }
        int visible = tabla.getParent() instanceof JViewport v ? v.getWidth() : 0;
        tabla.setAutoResizeMode(total < visible || visible == 0 ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
        tabla.revalidate();
    }

    @Override public void refrescar() {
        datos = Repositorio.get().combos();
        m.setRowCount(0);
        for (Combo c : datos) {
            String estado = !c.isHabilitado() ? "Deshabilitado"
                    : (!c.isDisponible() ? "No disponible (producto inactivo)" : "Disponible");
            m.addRow(new Object[]{c.getNombre(), c.getResumen(), "−" + Ui.money(c.getDescuento()), Ui.money(c.getPrecio()), estado});
        }
        ajustarColumnas();
    }
}