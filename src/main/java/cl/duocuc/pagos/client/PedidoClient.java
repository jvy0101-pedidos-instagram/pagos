package cl.duocuc.pagos.client;

import cl.duocuc.pagos.dto.PedidoResponse;
import cl.duocuc.pagos.exception.ReferenciaInvalidaException;
import cl.duocuc.pagos.exception.ServicioNoDisponibleException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/** Consulta y actualiza al microservicio Pedidos por HTTP. */
@Component
public class PedidoClient {

    private final RestClient restClient;

    // La URL viene de application.properties (o de la variable PEDIDOS_URL en Docker).
    public PedidoClient(@Value("${servicios.pedidos.url:http://localhost:8083}") String baseUrl) {
        // Se usa el cliente HTTP de Java porque soporta PATCH (el cliente simple no).
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory tiempos = new JdkClientHttpRequestFactory(httpClient);
        tiempos.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(tiempos)
                .build();
    }

    /** Devuelve el pedido con su total, o lanza una excepcion si no existe. */
    public PedidoResponse buscarPedido(Long pedidoId) {
        try {
            return restClient.get()
                    .uri("/api/v1/pedidos/{id}", pedidoId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ReferenciaInvalidaException("No existe pedido con id " + pedidoId);
                    })
                    .body(PedidoResponse.class);
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new ServicioNoDisponibleException("El servicio Pedidos no responde");
        }
    }

    /** Le avisa a Pedidos que el pedido ya fue pagado (PATCH /api/v1/pedidos/{id}/estado). */
    public void marcarComoPagado(Long pedidoId) {
        try {
            restClient.patch()
                    .uri("/api/v1/pedidos/{id}/estado", pedidoId)
                    .body(Map.of("estado", "PAGADO"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new ServicioNoDisponibleException("El servicio Pedidos no responde");
        }
    }
}
