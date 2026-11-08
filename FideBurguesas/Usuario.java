import java.io.Serializable;
import java.security.MessageDigest;
import java.util.HexFormat;

public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String usuario;
    private String hash;
    private boolean activo = true;

    protected Usuario(String usuario, String clave) throws FideException {
        if (usuario == null || usuario.isBlank()) throw new FideException("El usuario es obligatorio.");
        if (clave == null || clave.length() < 4) throw new FideException("La contraseña debe tener al menos 4 caracteres.");
        this.usuario = usuario.trim();
        this.hash = hash(clave);
    }

    private static String hash(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes()));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    public boolean autenticar(String u, String clave) {
        return activo && usuario.equals(u) && hash.equals(hash(clave));
    }

    public void cambiarContrasena(String nueva) throws FideException {
        if (nueva == null || nueva.length() < 4) throw new FideException("Contraseña muy corta.");
        hash = hash(nueva);
    }

    public String getUsuario() {
        return usuario; 
    }
    public boolean isActivo() {
        return activo; 
    }
    public void setActivo(boolean a) {
        activo = a; 
    }
    public abstract String getRol();
    public abstract String getNombre();
}
