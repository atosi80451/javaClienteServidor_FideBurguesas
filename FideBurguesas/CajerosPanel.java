import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CajerosPanel extends Estilo implements Refrescable {
    private static final String[] SUCURSALES = {"Centro", "Heredia", "Alajuela", "Cartago"};
    private static final String CLAVE_OCULTA = "••••••••••";

    private final CardLayout cartas = new CardLayout();

    private final DefaultTableModel m = Ui.modelo("Nombre", "Usuario", "Sucursal", "Turno", "Estado");
    private final JTable tabla = new JTable(m);
    private List<Cajero> datos = new ArrayList<>();

    private Cajero editando;
    private String clavePendiente;
    private final JTextField nombre = entrada(new JTextField());
    private final JTextField cedula = entrada(new JTextField());
    private final JTextField usuario = entrada(new JTextField());
    private final JPasswordField clave = entrada(new JPasswordField());
    private final JComboBox<String> turno = new JComboBox<>(new String[]{"Diurno", "Nocturno"});
    private final JComboBox<String> sucursal = new JComboBox<>(SUCURSALES);
    private final JButton btnCambiar = new Boton("Cambiar", 1);
    private final JButton btnDesactivar = new Boton("Desactivar acceso", 2);
    private final JButton btnGuardar = new Boton("✓  Guardar cambios", 0);

    private final JLabel migas = texto("", 12, GRIS, false);
    private final JLabel titulo = texto("", 30, TEXTO, true);
    private final JLabel subtitulo = texto("", 13, GRIS, false);
    private final JLabel hintUsuario = texto("", 11, GRIS, false);
    private final JLabel hintClave = texto("", 11, GRIS, false);

    private final Tarjeta perfilCard = new Tarjeta(Color.WHITE, BORDE, 16);
    private final JLabel avatar = texto("", 18, TEXTO, true);
    private final JLabel perfilNombre = texto("", 20, TEXTO, true);
    private final JLabel perfilDatos = texto("", 12, GRIS, false);
    private final Tarjeta badge = new Tarjeta(VERDE_SUAVE, null, 20);
    private final JLabel badgeTexto = texto("", 11, VERDE, true);
    private final JLabel perfilCedula = texto("", 13, TEXTO, true);
    private final JLabel perfilTurno = texto("", 13, TEXTO, true);

    private final Tarjeta cajaEstado = new Tarjeta(CAJA, null, 12);
    private final JLabel estadoTitulo = texto("", 13, TEXTO, true);
    private final JLabel estadoTexto = texto("", 12, GRIS, false);

    private final Tarjeta confirmCard = new Tarjeta(Color.WHITE, BORDE, 16);
    private final JLabel confirmTitulo = texto("", 17, TEXTO, true);
    private final JLabel confirmTexto = texto("", 12, TEXTO, false);

    public CajerosPanel() {
        estilizar(turno);
        estilizar(sucursal);
        sucursal.setEditable(true);
        setLayout(cartas);
        setBackground(CREMA);
        add(vistaLista(), "lista");
        add(vistaFormulario(), "form");
        refrescar();
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
        enc.add(izq(texto("Cajeros", 30, TEXTO, true)));
        enc.add(Box.createVerticalStrut(4));
        enc.add(izq(texto("Crea perfiles, asigna sucursal y controla quién puede iniciar sesión.", 13, GRIS, false)));
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
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(new BordeRedondo(BORDE, 12));
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp);

        JButton nuevo = new Boton("+  Nuevo cajero", 0), editar = new Boton("Editar", 1);
        nuevo.addActionListener(e -> abrirFormulario(null));
        editar.addActionListener(e -> editarSeleccionado());
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        b.setOpaque(false);
        b.add(nuevo);
        b.add(editar);
        p.add(b, BorderLayout.SOUTH);
        return p;
    }

    private void editarSeleccionado() {
        int r = tabla.getSelectedRow();
        if (r < 0 || r >= datos.size()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cajero de la tabla.");
            return;
        }
        abrirFormulario(datos.get(r));
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
        g.gridx = 0; g.weightx = 0.62; g.insets = new Insets(0, 0, 0, 16);
        cuerpo.add(tarjetaDatos(), g);
        g.gridx = 1; g.weightx = 0.38; g.insets = new Insets(0, 0, 0, 0);
        cuerpo.add(columnaDerecha(), g);

        JScrollPane sc = new JScrollPane(cuerpo);
        sc.setBorder(null);
        sc.getViewport().setBackground(CREMA);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        p.add(sc);

        btnCambiar.addActionListener(e -> cambiarClave());
        btnDesactivar.addActionListener(e -> alternarAcceso());
        btnGuardar.addActionListener(e -> guardar());
        return p;
    }

    private JPanel columna(String etiqueta, JComponent campo) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(izq(texto(etiqueta, 12, TEXTO, true)));
        p.add(Box.createVerticalStrut(5));
        p.add(izq(campo));
        p.add(Box.createVerticalStrut(12));
        return p;
    }

    private Tarjeta tarjetaDatos() {
        Tarjeta c = new Tarjeta(Color.WHITE, BORDE, 16);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createEmptyBorder(24, 26, 22, 26));

        c.add(izq(texto("Datos del cajero", 18, TEXTO, true)));
        c.add(Box.createVerticalStrut(8));
        c.add(izq(texto("<html><body style='width:360px'>El usuario debe ser único. La contraseña se almacena cifrada.</body></html>", 11, GRIS, false)));
        c.add(Box.createVerticalStrut(14));

        c.add(izq(columna("Nombre completo", nombre)));
        JPanel fila = new JPanel(new GridLayout(1, 2, 12, 0));
        fila.setOpaque(false);
        fila.add(columna("Cédula", cedula));
        fila.add(columna("Turno", turno));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        c.add(izq(fila));

        c.add(izq(columna("Usuario", usuario)));
        c.add(izq(hintUsuario));
        c.add(Box.createVerticalStrut(12));

        c.add(izq(texto("Contraseña", 12, TEXTO, true)));
        c.add(Box.createVerticalStrut(5));
        JPanel fc = new JPanel(new BorderLayout(8, 0));
        fc.setOpaque(false);
        fc.add(clave);
        fc.add(btnCambiar, BorderLayout.EAST);
        fc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        c.add(izq(fc));
        c.add(Box.createVerticalStrut(4));
        c.add(izq(hintClave));
        c.add(Box.createVerticalStrut(14));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        c.add(izq(sep));
        c.add(Box.createVerticalStrut(14));

        c.add(izq(columna("Sucursal asignada", sucursal)));

        cajaEstado.setLayout(new BoxLayout(cajaEstado, BoxLayout.Y_AXIS));
        cajaEstado.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        cajaEstado.add(izq(estadoTitulo));
        cajaEstado.add(Box.createVerticalStrut(3));
        cajaEstado.add(izq(estadoTexto));
        cajaEstado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        c.add(izq(cajaEstado));
        c.add(Box.createVerticalStrut(16));

        JPanel botones = new JPanel(new BorderLayout());
        botones.setOpaque(false);
        botones.add(btnDesactivar, BorderLayout.WEST);
        botones.add(btnGuardar, BorderLayout.EAST);
        botones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        c.add(izq(botones));
        return c;
    }

    private JPanel columnaDerecha() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        perfilCard.setLayout(new BoxLayout(perfilCard, BoxLayout.Y_AXIS));
        perfilCard.setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));
        perfilCard.add(izq(texto("Perfil actual", 18, TEXTO, true)));
        perfilCard.add(Box.createVerticalStrut(14));

        Tarjeta av = new Tarjeta(BEIGE, null, 100);
        av.setPreferredSize(new Dimension(60, 60));
        av.setLayout(new GridBagLayout());
        av.add(avatar);
        JPanel avWrap = new JPanel(new GridBagLayout());
        avWrap.setOpaque(false);
        avWrap.add(av);

        JPanel nombres = new JPanel();
        nombres.setOpaque(false);
        nombres.setLayout(new BoxLayout(nombres, BoxLayout.Y_AXIS));
        nombres.add(izq(perfilNombre));
        nombres.add(izq(perfilDatos));
        JPanel nombresWrap = new JPanel(new GridBagLayout());
        nombresWrap.setOpaque(false);
        nombresWrap.add(nombres);

        badge.setLayout(new BorderLayout());
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        badge.add(badgeTexto);
        JPanel badgeWrap = new JPanel(new GridBagLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        JPanel fila = new JPanel(new BorderLayout(12, 0));
        fila.setOpaque(false);
        fila.add(avWrap, BorderLayout.WEST);
        fila.add(nombresWrap);
        fila.add(badgeWrap, BorderLayout.EAST);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        perfilCard.add(izq(fila));
        perfilCard.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        perfilCard.add(izq(sep));
        perfilCard.add(Box.createVerticalStrut(10));
        perfilCard.add(izq(datoFila("Cédula", perfilCedula)));
        perfilCard.add(Box.createVerticalStrut(6));
        perfilCard.add(izq(datoFila("Turno", perfilTurno)));

        confirmCard.setLayout(new BoxLayout(confirmCard, BoxLayout.Y_AXIS));
        confirmCard.setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));
        Tarjeta icono = new Tarjeta(ROJO_SUAVE, null, 100);
        icono.setPreferredSize(new Dimension(40, 40));
        icono.setLayout(new GridBagLayout());
        icono.add(texto("⚠", 16, ROJO, true));
        JPanel iconoWrap = new JPanel(new GridBagLayout());
        iconoWrap.setOpaque(false);
        iconoWrap.add(icono);
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);
        cabecera.add(iconoWrap, BorderLayout.WEST);
        cabecera.add(confirmTitulo);
        cabecera.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        confirmCard.add(izq(cabecera));
        confirmCard.add(Box.createVerticalStrut(12));
        confirmCard.add(izq(confirmTexto));
        confirmCard.add(Box.createVerticalStrut(12));

        Tarjeta aviso = new Tarjeta(ROJO_SUAVE, null, 10);
        aviso.setLayout(new BoxLayout(aviso, BoxLayout.Y_AXIS));
        aviso.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        aviso.add(izq(texto("El perfil quedará inactivo", 12, ROJO, true)));
        aviso.add(Box.createVerticalStrut(4));
        aviso.add(izq(texto("<html><body style='width:230px'>Sus pedidos y facturas anteriores se conservarán. "
                + "Volver mantiene su acceso actual.</body></html>", 11, ROJO, false)));
        aviso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        confirmCard.add(izq(aviso));
        confirmCard.add(Box.createVerticalStrut(14));

        JButton volver = new Boton("Volver", 1), desactivar = new Boton("Desactivar", 0);
        volver.addActionListener(e -> confirmCard.setVisible(false));
        desactivar.addActionListener(e -> {
            Repositorio.get().cambiarEstado(editando, false);
            confirmCard.setVisible(false);
            refrescarPerfil();
            refrescar();
        });
        JPanel bs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bs.setOpaque(false);
        bs.add(volver);
        bs.add(desactivar);
        bs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        confirmCard.add(izq(bs));
        confirmCard.setVisible(false);

        col.add(izq(perfilCard));
        col.add(Box.createVerticalStrut(18));
        col.add(izq(confirmCard));
        return col;
    }

    private JPanel datoFila(String etiqueta, JLabel valor) {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.add(texto(etiqueta, 12, GRIS, false), BorderLayout.WEST);
        f.add(valor, BorderLayout.EAST);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return f;
    }

    private static void bloquear(JTextField f, boolean bloqueado) {
        f.setEditable(!bloqueado);
        f.setBackground(bloqueado ? CAJA : Color.WHITE);
        f.setForeground(bloqueado ? GRIS : TEXTO);
    }

    private void mostrarLista() {
        refrescar();
        cartas.show(this, "lista");
    }

    private void abrirFormulario(Cajero c) {
        editando = c;
        clavePendiente = null;
        confirmCard.setVisible(false);
        boolean ed = c != null;

        nombre.setText(ed ? c.getNombre() : "");
        cedula.setText(ed ? c.getCedula() : "");
        usuario.setText(ed ? c.getUsuario() : "");
        turno.setSelectedItem(ed ? c.getTurno() : "Diurno");
        sucursal.setSelectedItem(ed ? c.getSucursal() : "Centro");
        clave.setText(ed ? CLAVE_OCULTA : "");
        bloquear(cedula, ed);
        bloquear(usuario, ed);
        bloquear(clave, ed);

        btnCambiar.setVisible(ed);
        btnDesactivar.setVisible(ed);
        perfilCard.setVisible(ed);
        cajaEstado.setVisible(ed);

        migas.setText("←  Cajeros  /  " + (ed ? c.getNombre() : "Nuevo"));
        titulo.setText(ed ? "Editar perfil de cajero" : "Nuevo cajero");
        subtitulo.setText(ed ? "Actualiza los datos y la sucursal del cajero o desactiva su acceso."
                : "Completa los datos para crear el perfil del cajero.");
        hintUsuario.setText(ed ? "Usuario del perfil actual · No se puede cambiar." : "Debe ser único entre cajeros.");
        hintClave.setText(ed ? "Se almacena cifrada." : "Mínimo 4 caracteres. Se almacena cifrada.");
        btnGuardar.setText(ed ? "✓  Guardar cambios" : "✓  Crear cajero");
        if (ed) refrescarPerfil();
        cartas.show(this, "form");
    }

    private static String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        String s = partes[0].substring(0, 1);
        if (partes.length > 1) s += partes[1].substring(0, 1);
        return s.toUpperCase();
    }

    private void refrescarPerfil() {
        Cajero c = editando;
        boolean activo = c.isActivo();
        migas.setText("←  Cajeros  /  " + c.getNombre());
        avatar.setText(iniciales(c.getNombre()));
        perfilNombre.setText(c.getNombre());
        perfilDatos.setText(c.getUsuario() + " · " + c.getSucursal());
        perfilCedula.setText(c.getCedula());
        perfilTurno.setText(c.getTurno());
        badgeTexto.setText(activo ? "● Activo" : "● Inactivo");
        badgeTexto.setForeground(activo ? VERDE : ROJO);
        badge.setFondo(activo ? VERDE_SUAVE : ROJO_SUAVE);
        estadoTitulo.setText(activo ? "Perfil activo" : "Perfil inactivo");
        estadoTexto.setText(activo ? "Acceso habilitado como cajero de la sucursal " + c.getSucursal() + "."
                : "Sin acceso: no puede iniciar sesión hasta que se reactive.");
        btnDesactivar.setText(activo ? "Desactivar acceso" : "Reactivar acceso");
    }

    private void alternarAcceso() {
        if (editando.isActivo()) {
            confirmTitulo.setText("¿Desactivar a " + editando.getNombre() + "?");
            confirmTexto.setText("<html><body style='width:250px'>" + editando.getNombre()
                    + " perderá el acceso a Punto de venta y Pedidos y facturas en la sucursal "
                    + editando.getSucursal() + ". No podrá iniciar sesión como cajero.</body></html>");
            confirmCard.setVisible(true);
        } else {
            Repositorio.get().cambiarEstado(editando, true);
            refrescarPerfil();
            refrescar();
        }
    }

    private void cambiarClave() {
        JPasswordField nueva = new JPasswordField(15);
        int r = JOptionPane.showConfirmDialog(this, new Object[]{"Nueva contraseña (mínimo 4 caracteres):", nueva},
                "Cambiar contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        String s = new String(nueva.getPassword());
        if (s.length() < 4) {
            Ui.error(this, new FideException("La contraseña debe tener al menos 4 caracteres."));
            return;
        }
        clavePendiente = s;
        hintClave.setText("Nueva contraseña lista: se aplicará al guardar los cambios.");
    }

    private void guardar() {
        try {
            String suc = String.valueOf(sucursal.getSelectedItem()).trim();
            String tur = (String) turno.getSelectedItem();
            if (editando == null) {
                Repositorio.get().agregarUsuario(new Cajero(usuario.getText(), new String(clave.getPassword()),
                        cedula.getText(), nombre.getText(), tur, suc));
                mostrarLista();
            } else {
                Repositorio.get().actualizarCajero(editando, nombre.getText(), tur, suc, clavePendiente);
                clavePendiente = null;
                hintClave.setText("Se almacena cifrada.");
                refrescarPerfil();
                refrescar();
                JOptionPane.showMessageDialog(this, "Cambios guardados.");
            }
        } catch (FideException e) { Ui.error(this, e); }
    }

    @Override public void refrescar() {
        datos = Repositorio.get().cajeros();
        m.setRowCount(0);
        for (Cajero c : datos)
            m.addRow(new Object[]{c.getNombre(), c.getUsuario(), c.getSucursal(), c.getTurno(),
                    c.isActivo() ? "Activo" : "Inactivo"});
    }
}