package cl.duocuc.pagos.controller;

import cl.duocuc.pagos.model.Pago;
import cl.duocuc.pagos.service.PagoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Capa de presentacion: expone los endpoints REST del microservicio. */
@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    // GET /api/v1/pagos
    @GetMapping
    public List<Pago> listar() {
        return service.listar();
    }

    // GET /api/v1/pagos/{id}
    @GetMapping("/{id}")
    public Pago buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // GET /api/v1/pagos/pedido/{pedidoId}
    @GetMapping("/pedido/{pedidoId}")
    public List<Pago> listarPorPedidoId(@PathVariable Long pedidoId) {
        return service.listarPorPedidoId(pedidoId);
    }

    // POST /api/v1/pagos  -> 201 Created
    @PostMapping
    public ResponseEntity<Pago> crear(@Valid @RequestBody Pago pago) {
        Pago creado = service.crear(pago);
        return ResponseEntity.created(URI.create("/api/v1/pagos/" + creado.getId())).body(creado);
    }

    // PUT /api/v1/pagos/{id}
    @PutMapping("/{id}")
    public Pago actualizar(@PathVariable Long id, @Valid @RequestBody Pago pago) {
        return service.actualizar(id, pago);
    }

    // DELETE /api/v1/pagos/{id}  -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
