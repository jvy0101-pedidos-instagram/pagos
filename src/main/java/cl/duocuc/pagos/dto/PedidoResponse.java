package cl.duocuc.pagos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * Los datos de un pedido que Pagos necesita, tal como los responde el microservicio Pedidos.
 * ignoreUnknown descarta los campos que Pagos no usa (detalles, fecha...).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoResponse(
        Long id,
        BigDecimal total,
        String estado) {
}
