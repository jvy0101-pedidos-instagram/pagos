package cl.duocuc.pagos.service;

import cl.duocuc.pagos.exception.RecursoNoEncontradoException;
import cl.duocuc.pagos.model.*;
import cl.duocuc.pagos.repository.PagoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capa de negocio: el controller llama al service y el service al repository. */
@Service
public class PagoService {

    private final PagoRepository repository;

    // Inyeccion de dependencias por constructor.
    public PagoService(PagoRepository repository) {
        this.repository = repository;
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
        if (nuevo.getEstado() == null) nuevo.setEstado(EstadoPago.PENDIENTE);
        nuevo.setId(null); // el id lo genera la base de datos
        return repository.save(nuevo);
    }

    @Transactional
    public Pago actualizar(Long id, Pago datos) {
        Pago existente = buscarPorId(id);
        existente.setPedidoId(datos.getPedidoId());
        existente.setMonto(datos.getMonto());
        existente.setMetodo(datos.getMetodo());
        if (datos.getEstado() != null) existente.setEstado(datos.getEstado());
        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Pago existente = buscarPorId(id);
        repository.delete(existente);
    }
}
