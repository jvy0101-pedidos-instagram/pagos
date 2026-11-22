package cl.duocuc.pagos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duocuc.pagos.client.PedidoClient;
import cl.duocuc.pagos.dto.PedidoResponse;
import cl.duocuc.pagos.exception.ReferenciaInvalidaException;
import cl.duocuc.pagos.exception.ReglaNegocioException;
import cl.duocuc.pagos.model.EstadoPago;
import cl.duocuc.pagos.model.MetodoPago;
import cl.duocuc.pagos.model.Pago;
import cl.duocuc.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de las reglas de negocio de Pagos.
 * El microservicio Pedidos y la base de datos se reemplazan por mocks (objetos simulados).
 */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository repository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private PagoService service;

    private Pago pago(String monto, EstadoPago estado) {
        Pago p = new Pago();
        p.setPedidoId(1L);
        p.setMonto(new BigDecimal(monto));
        p.setMetodo(MetodoPago.TRANSFERENCIA);
        p.setEstado(estado);
        return p;
    }

    @Test
    void crearConfirmadoGuardaYMarcaElPedidoComoPagado() {
        when(pedidoClient.buscarPedido(1L))
                .thenReturn(new PedidoResponse(1L, new BigDecimal("25980.00"), "PENDIENTE"));
        when(repository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago creado = service.crear(pago("25980", EstadoPago.CONFIRMADO));

        assertThat(creado.getEstado()).isEqualTo(EstadoPago.CONFIRMADO);
        verify(pedidoClient).marcarComoPagado(1L);
    }

    @Test
    void crearRechazaUnMontoDistintoAlTotal() {
        when(pedidoClient.buscarPedido(1L))
                .thenReturn(new PedidoResponse(1L, new BigDecimal("25980.00"), "PENDIENTE"));

        assertThatThrownBy(() -> service.crear(pago("1000", EstadoPago.PENDIENTE)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no coincide");

        verify(repository, never()).save(any());
    }

    @Test
    void crearRechazaUnPedidoQueNoExiste() {
        when(pedidoClient.buscarPedido(1L))
                .thenThrow(new ReferenciaInvalidaException("No existe pedido con id 1"));

        assertThatThrownBy(() -> service.crear(pago("25980", EstadoPago.PENDIENTE)))
                .isInstanceOf(ReferenciaInvalidaException.class);

        verify(repository, never()).save(any());
    }
}
