package br.edu.infnet.pagamento.dominio.modelo;

import br.edu.infnet.pagamento.dominio.evento.EventoDePagamento;
import br.edu.infnet.pagamento.dominio.evento.PagamentoAprovado;
import br.edu.infnet.pagamento.dominio.evento.PagamentoEstornado;
import br.edu.infnet.pagamento.dominio.evento.PagamentoRecusado;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * AGGREGATE ROOT do Bounded Context de Pagamento.
 *
 * Diferencas em relacao a entidade JPA "Pagamento" do monolito:
 *  - nao conhece as entidades Pedido e Usuario: referencia outros contextos so por ID (PedidoId, ClienteId);
 *  - nao tem setters: o estado so muda por metodos de negocio (aprovar, recusar, estornar),
 *    que protegem as invariantes e as transicoes de status;
 *  - usa Value Objects (Dinheiro, CartaoMascarado, ...) em vez de tipos primitivos;
 *  - e livre de framework (sem anotacoes JPA/Spring) - a persistencia fica num adapter;
 *  - registra Eventos de Dominio para avisar outros contextos (ex.: Pedido) sem acoplamento direto.
 *
 * Invariantes:
 *  1. Existe no maximo um Pagamento por Pedido (garantido pelo repositorio + chave unica).
 *  2. So um pagamento PENDENTE pode ser aprovado ou recusado.
 *  3. Pagamento APROVADO sempre tem codigo de autorizacao; RECUSADO sempre tem motivo.
 *  4. So um pagamento APROVADO pode ser estornado.
 */
public class Pagamento {

    private final PagamentoId id;
    private final PedidoId pedidoId;
    private final ClienteId clienteId;
    private final Dinheiro valor;
    private final FormaPagamento forma;          // null quando a forma informada nao e suportada
    private final CartaoMascarado cartao;
    private final Instant criadoEm;

    private StatusPagamento status;
    private MotivoRecusa motivoRecusa;
    private String codigoAutorizacao;
    private Instant processadoEm;

    private final List<EventoDePagamento> eventos = new ArrayList<>();

    private Pagamento(PagamentoId id, PedidoId pedidoId, ClienteId clienteId, Dinheiro valor,
                      FormaPagamento forma, CartaoMascarado cartao, StatusPagamento status,
                      MotivoRecusa motivoRecusa, String codigoAutorizacao,
                      Instant criadoEm, Instant processadoEm) {
        this.id = Objects.requireNonNull(id);
        this.pedidoId = Objects.requireNonNull(pedidoId);
        this.clienteId = Objects.requireNonNull(clienteId);
        this.valor = Objects.requireNonNull(valor);
        this.forma = forma;
        this.cartao = Objects.requireNonNull(cartao);
        this.status = Objects.requireNonNull(status);
        this.motivoRecusa = motivoRecusa;
        this.codigoAutorizacao = codigoAutorizacao;
        this.criadoEm = Objects.requireNonNull(criadoEm);
        this.processadoEm = processadoEm;
    }

    // ------------------------------------------------------------------ fabricas

    /**
     * Cria um novo pagamento para um pedido. Aplica a politica de negocio na criacao:
     * se alguma regra for violada, o pagamento ja nasce RECUSADO (sem chamar o gateway),
     * exatamente como o monolito fazia (ele gravava o pagamento recusado com o motivo).
     */
    public static Pagamento solicitar(PedidoId pedidoId, ClienteId clienteId, Dinheiro valor,
                                      Optional<FormaPagamento> forma, DadosCartao dadosCartao,
                                      PoliticaDePagamento politica, Clock relogio) {
        Pagamento pagamento = new Pagamento(
                PagamentoId.novo(), pedidoId, clienteId, valor,
                forma.orElse(null), CartaoMascarado.de(dadosCartao),
                StatusPagamento.PENDENTE, null, null, Instant.now(relogio), null);

        politica.avaliar(valor, forma, dadosCartao)
                .ifPresent(motivo -> pagamento.recusar(motivo, relogio));
        return pagamento;
    }

    /** Reconstroi um pagamento ja existente (usado pelo adapter de persistencia). Nao gera eventos. */
    public static Pagamento reconstituir(PagamentoId id, PedidoId pedidoId, ClienteId clienteId,
                                         Dinheiro valor, FormaPagamento forma, CartaoMascarado cartao,
                                         StatusPagamento status, MotivoRecusa motivoRecusa,
                                         String codigoAutorizacao, Instant criadoEm, Instant processadoEm) {
        return new Pagamento(id, pedidoId, clienteId, valor, forma, cartao, status,
                motivoRecusa, codigoAutorizacao, criadoEm, processadoEm);
    }

    // ------------------------------------------------------------------ comportamento

    /** Pode seguir para autorizacao no gateway externo? */
    public boolean aguardandoAutorizacao() {
        return status == StatusPagamento.PENDENTE;
    }

    public void aprovar(String codigoAutorizacao, Clock relogio) {
        exigirStatus(StatusPagamento.PENDENTE, "aprovar");
        if (codigoAutorizacao == null || codigoAutorizacao.isBlank()) {
            throw new IllegalArgumentException("Pagamento aprovado exige codigo de autorizacao");
        }
        this.status = StatusPagamento.APROVADO;
        this.codigoAutorizacao = codigoAutorizacao;
        this.processadoEm = Instant.now(relogio);
        eventos.add(new PagamentoAprovado(id, pedidoId, valor, codigoAutorizacao, processadoEm));
    }

    public void recusar(MotivoRecusa motivo, Clock relogio) {
        exigirStatus(StatusPagamento.PENDENTE, "recusar");
        this.status = StatusPagamento.RECUSADO;
        this.motivoRecusa = Objects.requireNonNull(motivo, "Recusa exige motivo");
        this.processadoEm = Instant.now(relogio);
        eventos.add(new PagamentoRecusado(id, pedidoId, motivo, processadoEm));
    }

    /** Compensacao: desfaz um pagamento aprovado (ex.: o pedido falhou depois da aprovacao). */
    public void estornar(Clock relogio) {
        exigirStatus(StatusPagamento.APROVADO, "estornar");
        this.status = StatusPagamento.ESTORNADO;
        Instant agora = Instant.now(relogio);
        eventos.add(new PagamentoEstornado(id, pedidoId, valor, agora));
    }

    private void exigirStatus(StatusPagamento esperado, String operacao) {
        if (this.status != esperado) {
            throw new TransicaoDeStatusInvalidaException(
                    "Nao e possivel " + operacao + " um pagamento " + status + " (esperado: " + esperado + ")");
        }
    }

    // ------------------------------------------------------------------ eventos de dominio

    /** Eventos gerados desde a ultima publicacao. */
    public List<EventoDePagamento> eventosPendentes() {
        return Collections.unmodifiableList(eventos);
    }

    public void limparEventos() {
        eventos.clear();
    }

    // ------------------------------------------------------------------ leitura (sem setters)

    public PagamentoId id() { return id; }
    public PedidoId pedidoId() { return pedidoId; }
    public ClienteId clienteId() { return clienteId; }
    public Dinheiro valor() { return valor; }
    public Optional<FormaPagamento> forma() { return Optional.ofNullable(forma); }
    public CartaoMascarado cartao() { return cartao; }
    public StatusPagamento status() { return status; }
    public Optional<MotivoRecusa> motivoRecusa() { return Optional.ofNullable(motivoRecusa); }
    public Optional<String> codigoAutorizacao() { return Optional.ofNullable(codigoAutorizacao); }
    public Instant criadoEm() { return criadoEm; }
    public Optional<Instant> processadoEm() { return Optional.ofNullable(processadoEm); }

    /** Igualdade de Entidade/Aggregate: pela identidade, nao pelos atributos. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Pagamento outro && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
