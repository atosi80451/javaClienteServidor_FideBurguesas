public enum EstadoPedido {
    PENDIENTE, EN_PREPARACION, LISTO, FACTURADO, CANCELADO;

    public boolean puedePasarA(EstadoPedido n) {
        return switch (this) {
            case PENDIENTE -> n == EN_PREPARACION || n == CANCELADO;
            case EN_PREPARACION -> n == LISTO;
            case LISTO -> n == FACTURADO;
            default -> false;
        };
    }
}
