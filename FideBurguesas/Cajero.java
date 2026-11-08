public class Cajero extends Usuario {
    private static final long serialVersionUID = 1L;
    private final String cedula;
    private String nombre, turno, sucursal;

    public Cajero(String usuario, String clave, String cedula, String nombre, String turno, String sucursal) throws FideException {
        super(usuario, clave);
        if (cedula == null || cedula.isBlank()) throw new FideException("La cédula es obligatoria.");
        this.cedula = cedula.trim();
        asignar(nombre, turno, sucursal);
    }

    private void asignar(String nombre, String turno, String sucursal) throws FideException {
        if (nombre == null || nombre.isBlank()) throw new FideException("El nombre es obligatorio.");
        if (sucursal == null || sucursal.isBlank()) throw new FideException("La sucursal es obligatoria.");
        this.nombre = nombre.trim(); this.turno = turno; this.sucursal = sucursal.trim();
    }
    
    public void actualizar(String nombre, String turno, String sucursal) throws FideException {
        asignar(nombre, turno, sucursal);
    }

    public String getCedula() {
        return cedula; 
    }
    
    public String getTurno() { 
        return turno; 
    }

    public String getSucursal() {
        return sucursal; 
    }
    
    @Override 
    public String getRol() { 
        return "CAJERO"; 
    }

    @Override 
    public String getNombre() { 
        return nombre; 
    }
}
