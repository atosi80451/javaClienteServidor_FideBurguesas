public class Administrador extends Usuario {
    private static final long serialVersionUID = 1L;
    private final String nombre;

    public Administrador(String usuario, String clave, String nombre) throws FideException {
        super(usuario, clave);
        this.nombre = nombre;
    }
    
    @Override 
    public String getRol() {
        return "ADMIN"; 
    }
    
    @Override 
    public String getNombre() {
        return nombre; 
    }
}
