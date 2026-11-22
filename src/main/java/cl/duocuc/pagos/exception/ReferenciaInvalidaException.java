package cl.duocuc.pagos.exception;

/** El pago apunta a un pedido que no existe en el microservicio Pedidos. */
public class ReferenciaInvalidaException extends RuntimeException {
    public ReferenciaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
