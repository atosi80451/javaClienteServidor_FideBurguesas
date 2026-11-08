import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Repositorio {
    private static final String ARCHIVO = "fideburguesas.dat";
    private static final Repositorio INSTANCIA = new Repositorio();
    public static Repositorio get() { return INSTANCIA; }

    private static class Datos implements Serializable {
        private static final long serialVersionUID = 1L;
        final List<Usuario> usuarios = new ArrayList<>();
        final List<ItemMenu> items = new ArrayList<>();
        final List<Pedido> pedidos = new ArrayList<>();
        final List<Factura> facturas = new ArrayList<>();
        int sigPedido = 1, sigFactura = 1, sigItem = 1;
    }

    private Datos d;
    private final BlockingQueue<Pedido> cola = new LinkedBlockingQueue<>();

    private Repositorio() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ARCHIVO))) {
            d = (Datos) in.readObject();
        } catch (Exception e) {
            d = new Datos();
            sembrar();
        }
    }

    private void sembrar() {
        try {
            d.usuarios.add(new Administrador("admin", "admin123", "Administrador"));
            d.usuarios.add(new Cajero("cajero1", "1234", "1-1111-1111", "Ana Lisa", "Diurno", "Heredia"));
            Producto h = new Producto(d.sigItem++, "Hamburguesa Clásica", "Plato Principal", 3500);
            Producto p = new Producto(d.sigItem++, "Papas Fritas", "Acompañamiento", 1800);
            Producto r = new Producto(d.sigItem++, "Refresco", "Bebida", 1200);
            d.items.addAll(List.of(h, p, r));
            d.items.add(new Combo(d.sigItem++, "Combo Clásico", List.of(h, p, r), 10));
            guardar();
        } catch (FideException e) { throw new IllegalStateException(e); }
    }

    public synchronized void guardar() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ARCHIVO))) {
            out.writeObject(d);
        } catch (IOException e) { System.err.println("No se pudo guardar: " + e.getMessage()); }
    }

    public synchronized Usuario login(String u, String clave) throws FideException {
        for (Usuario x : d.usuarios) if (x.autenticar(u, clave)) return x;
        throw new FideException("Usuario o contraseña incorrectos.");
    }
    public synchronized void agregarUsuario(Usuario nuevo) throws FideException {
        for (Usuario x : d.usuarios)
            if (x.getUsuario().equalsIgnoreCase(nuevo.getUsuario())) throw new FideException("Ese usuario ya existe.");
        d.usuarios.add(nuevo); guardar();
    }
    public synchronized List<Cajero> cajeros() {
        List<Cajero> r = new ArrayList<>();
        for (Usuario u : d.usuarios) if (u instanceof Cajero c) r.add(c);
        return r;
    }
    public synchronized void actualizarCajero(Cajero c, String nombre, String turno, String sucursal, String nuevaClave) throws FideException {
        c.actualizar(nombre, turno, sucursal);
        if (nuevaClave != null && !nuevaClave.isEmpty()) c.cambiarContrasena(nuevaClave);
        guardar();
    }

    public synchronized void cambiarEstado(Usuario u, boolean activo) {
        u.setActivo(activo);
        guardar();
    }

    public synchronized Producto agregarProducto(String nombre, String cat, double precio) throws FideException {
        Producto p = new Producto(d.sigItem, nombre, cat, precio);
        d.sigItem++; d.items.add(p); guardar(); return p;
    }
    public synchronized Producto agregarProducto(String nombre, String cat, String desc, double precio, boolean disponible) throws FideException {
        Producto p = new Producto(d.sigItem, nombre, cat, desc, precio);
        p.setDisponible(disponible);
        d.sigItem++; d.items.add(p); guardar(); return p;
    }

    public synchronized void actualizarProducto(Producto p, String nombre, String cat, String desc, double precio) throws FideException {
        p.actualizar(nombre, cat, desc, precio);
        guardar();
    }
    public synchronized Combo agregarCombo(String nombre, String desc, java.util.Map<Producto, Integer> composicion, double descuento, boolean disponible) throws FideException {
        Combo c = new Combo(d.sigItem, nombre, desc, composicion, descuento);
        c.setDisponible(disponible);
        d.sigItem++; d.items.add(c); guardar(); return c;
    }
    public synchronized Combo agregarCombo(String nombre, List<Producto> ps, double desc) throws FideException {
        Combo c = new Combo(d.sigItem, nombre, ps, desc);
        d.sigItem++; d.items.add(c); guardar(); return c;
    }
    public synchronized void actualizarProducto(Producto p, String nombre, String cat, double precio) throws FideException {
        p.actualizar(nombre, cat, precio);
        guardar();
    }
    public synchronized void cambiarDisponibilidad(ItemMenu i, boolean disponible) {
        i.setDisponible(disponible);
        guardar();
    }
    public synchronized List<ItemMenu> items() { return new ArrayList<>(d.items); }
    public synchronized List<Producto> productos() {
        List<Producto> r = new ArrayList<>();
        for (ItemMenu i : d.items) if (i instanceof Producto p) r.add(p);
        return r;
    }
    public synchronized List<Combo> combos() {
        List<Combo> r = new ArrayList<>();
        for (ItemMenu i : d.items) if (i instanceof Combo c) r.add(c);
        return r;
    }

    public synchronized void enviarACocina(Pedido p) throws FideException {
        if (p.getLineas().isEmpty()) throw new FideException("El pedido no tiene productos.");
        p.setNumero(d.sigPedido++);
        d.pedidos.add(p);
        cola.add(p);
        guardar();
    }
    public BlockingQueue<Pedido> getCola() { return cola; }
    public synchronized List<Pedido> pedidos() { return new ArrayList<>(d.pedidos); }

    public synchronized Factura facturar(Pedido p, String metodo, double recibido) throws FideException {
        if (p.getEstado() != EstadoPedido.LISTO) throw new FideException("Solo se factura un pedido en estado LISTO.");
        Factura f = new Factura(d.sigFactura, p, metodo, recibido);
        p.cambiarEstado(EstadoPedido.FACTURADO);
        d.sigFactura++; d.facturas.add(f); guardar();
        return f;
    }
}
