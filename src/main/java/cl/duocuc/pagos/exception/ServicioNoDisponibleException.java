package cl.duocuc.pagos.exception;

/** Pedidos no respondio, por lo que no se puede validar el pago. */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
