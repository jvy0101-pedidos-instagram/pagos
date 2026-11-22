package cl.duocuc.pagos.service;

import cl.duocuc.pagos.client.PedidoClient;
import cl.duocuc.pagos.dto.PedidoResponse;
import cl.duocuc.pagos.exception.RecursoNoEncontradoException;
import cl.duocuc.pagos.exception.ReglaNegocioException;
import cl.duocuc.pagos.model.*;
import cl.duocuc.pagos.repository.PagoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capa de negocio: el controller llama al service y el service al repository. */
@Service
public class PagoService {

    private final PagoRepository repository;
    private final PedidoClient pedidoClient;

    // Inyeccion de dependencias por constructor.
    public PagoService(PagoRepository repository, PedidoClient pedidoClient) {
        this.repository = repository;
        this.pedidoClient = pedidoClient;
    }

    public List<Pago> listar() {
        return repository.findAll();
    }

    public Pago buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe pago con id " + id));
    }

    public List<Pago> listarPorPedidoId(Long pedidoId) {
        return repository.findByPedidoId(pedidoId);
    }

    @Transactional
    public Pago crear(Pago nuevo) {
        // 1. El pedido tiene que existir y estar esperando pago.
        PedidoResponse pedido = pedidoClient.buscarPedido(nuevo.getPedidoId());
        if (!"PENDIENTE".equals(pedido.estado())) {
            throw new ReglaNegocioException("El pedido " + pedido.id() + " no se puede pagar porque esta " + pedido.estado());
        }
        // 2. El monto pagado tiene que ser igual al total del pedido.
        validarMonto(nuevo, pedido);

        if (nuevo.getEstado() == null) nuevo.setEstado(EstadoPago.PENDIENTE);
        nuevo.setId(null); // el id lo genera la base de datos
        Pago guardado = repository.save(nuevo);

        // 3. Si el pago ya viene confirmado, se le avisa a Pedidos.
        avisarSiConfirmado(guardado);
        return guardado;
    }

    @Transactional
    public Pago actualizar(Long id, Pago datos) {
        Pago existente = buscarPorId(id);
        PedidoResponse pedido = pedidoClient.buscarPedido(datos.getPedidoId());
        validarMonto(datos, pedido);

        existente.setPedidoId(datos.getPedidoId());
        existente.setMonto(datos.getMonto());
        existente.setMetodo(datos.getMetodo());
        if (datos.getEstado() != null) existente.setEstado(datos.getEstado());
        Pago guardado = repository.save(existente);

        // Por ejemplo, un pago que pasa de PENDIENTE a CONFIRMADO marca el pedido como PAGADO.
        avisarSiConfirmado(guardado);
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        Pago existente = buscarPorId(id);
        repository.delete(existente);
    }

    /** compareTo compara el valor sin importar los decimales: 25980 es igual a 25980.00. */
    private void validarMonto(Pago pago, PedidoResponse pedido) {
        if (pago.getMonto().compareTo(pedido.total()) != 0) {
            throw new ReglaNegocioException("El monto " + pago.getMonto()
                    + " no coincide con el total del pedido " + pedido.id() + ": " + pedido.total());
        }
    }

    private void avisarSiConfirmado(Pago pago) {
        if (pago.getEstado() == EstadoPago.CONFIRMADO) {
            pedidoClient.marcarComoPagado(pago.getPedidoId());
        }
    }
}
