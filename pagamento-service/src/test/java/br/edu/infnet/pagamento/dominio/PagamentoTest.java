package br.edu.infnet.pagamento.dominio;

import br.edu.infnet.pagamento.dominio.evento.PagamentoAprovado;
import br.edu.infnet.pagamento.dominio.evento.PagamentoEstornado;
import br.edu.infnet.pagamento.dominio.evento.PagamentoRecusado;
import br.edu.infnet.pagamento.dominio.modelo.ClienteId;
import br.edu.infnet.pagamento.dominio.modelo.DadosCartao;
import br.edu.infnet.pagamento.dominio.modelo.Dinheiro;
import br.edu.infnet.pagamento.dominio.modelo.FormaPagamento;
import br.edu.infnet.pagamento.dominio.modelo.MotivoRecusa;
import br.edu.infnet.pagamento.dominio.modelo.Pagamento;
import br.edu.infnet.pagamento.dominio.modelo.PedidoId;
import br.edu.infnet.pagamento.dominio.modelo.PoliticaDePagamento;
import br.edu.infnet.pagamento.dominio.modelo.StatusPagamento;
import br.edu.infnet.pagamento.dominio.modelo.TransicaoDeStatusInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Testes do Aggregate: dominio puro, sem Spring, sem banco. */
class PagamentoTest {

    private final Clock relogio = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
    private final PoliticaDePagamento politica = new PoliticaDePagamento(Dinheiro.de("10000.00"));

    private Pagamento solicitar(String valor, String forma, String cartao) {
        return Pagamento.solicitar(new PedidoId(1L), new ClienteId(1L), Dinheiro.de(valor),
                FormaPagamento.de(forma), new DadosCartao(cartao), politica, relogio);
    }

    @Test
    void novoPagamentoValidoFicaPendenteEAguardaAutorizacao() {
        Pagamento p = solicitar("9350.00", "CARTAO", "4111111111111111");
        assertEquals(StatusPagamento.PENDENTE, p.status());
        assertTrue(p.aguardandoAutorizacao());
        assertEquals("**** **** **** 1111", p.cartao().formatado());
        assertTrue(p.eventosPendentes().isEmpty());
    }

    @Test
    void aprovarGeraEventoECodigo() {
        Pagamento p = solicitar("100.00", "cartao", "4111111111111111");
        p.aprovar("ABC12345", relogio);
        assertEquals(StatusPagamento.APROVADO, p.status());
        assertEquals(Optional.of("ABC12345"), p.codigoAutorizacao());
        assertInstanceOf(PagamentoAprovado.class, p.eventosPendentes().get(0));
    }

    @Test
    void politicaRecusaNaCriacaoComMesmosMotivosDoLegado() {
        assertEquals(Optional.of(MotivoRecusa.VALOR_INVALIDO), solicitar("0", "CARTAO", "4111111111111111").motivoRecusa());
        assertEquals(Optional.of(MotivoRecusa.FORMA_PAGAMENTO_NAO_SUPORTADA), solicitar("10", "PIX", "4111111111111111").motivoRecusa());
        assertEquals(Optional.of(MotivoRecusa.CARTAO_INVALIDO), solicitar("10", "CARTAO", "41").motivoRecusa());
        Pagamento acimaDoLimite = solicitar("15000.00", "CARTAO", "5555555555554444");
        assertEquals(StatusPagamento.RECUSADO, acimaDoLimite.status());
        assertEquals(Optional.of(MotivoRecusa.LIMITE_EXCEDIDO), acimaDoLimite.motivoRecusa());
        assertInstanceOf(PagamentoRecusado.class, acimaDoLimite.eventosPendentes().get(0));
        assertFalse(acimaDoLimite.aguardandoAutorizacao());
    }

    @Test
    void naoPermiteAprovarPagamentoJaRecusado() {
        Pagamento p = solicitar("15000.00", "CARTAO", "4111111111111111");
        assertThrows(TransicaoDeStatusInvalidaException.class, () -> p.aprovar("X", relogio));
    }

    @Test
    void soEstornaPagamentoAprovado() {
        Pagamento pendente = solicitar("10.00", "CARTAO", "4111111111111111");
        assertThrows(TransicaoDeStatusInvalidaException.class, () -> pendente.estornar(relogio));

        pendente.aprovar("COD", relogio);
        pendente.estornar(relogio);
        assertEquals(StatusPagamento.ESTORNADO, pendente.status());
        assertInstanceOf(PagamentoEstornado.class, pendente.eventosPendentes().get(1));
    }

    @Test
    void numeroCompletoDoCartaoNaoApareceNoToString() {
        assertFalse(new DadosCartao("4111111111111111").toString().contains("4111111111111111"));
    }
}
