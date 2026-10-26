package com.senai.nucitalanches.nucitalanches.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Testa a associação do pedido com a mesa e o controle dos itens. */
class PedidoTest {

    /** Garante que o pedido começa ligado à mesa e aberto. */
    @Test
    void criaPedidoAbertoVinculadoAMesa() {
        Mesa mesa = new Mesa(1);

        Pedido pedido = new Pedido(mesa);

        assertSame(mesa, pedido.getMesa());
        assertEquals(StatusPedido.ABERTO, pedido.getStatus());
        assertEquals(0, pedido.getItens().size());
    }

    /** Garante que o modelo não aceita pedido sem mesa. */
    @Test
    void rejeitaMesaNula() {
        assertThrows(IllegalArgumentException.class, () -> new Pedido(null));
    }

    /** Confirma que o pedido guarda mais de um item na ordem de inclusão. */
    @Test
    void permiteAdicionarVariosItens() {
        Pedido pedido = new Pedido(new Mesa(1));
        ItemPedido primeiro = new ItemPedido(new Produto("Suco", 5.5), 1);
        ItemPedido segundo = new ItemPedido(new Produto("Lanche", 12.0), 2);

        pedido.adicionarItem(primeiro);
        pedido.adicionarItem(segundo);

        assertEquals(List.of(primeiro, segundo), pedido.getItens());
    }

    /** Mantém o pedido vazio quando recebe um item nulo. */
    @Test
    void rejeitaItemNulo() {
        Pedido pedido = new Pedido(new Mesa(1));

        assertThrows(IllegalArgumentException.class, () -> pedido.adicionarItem(null));
        assertEquals(0, pedido.getItens().size());
    }

    /** Impede alterações externas na lista e devolve uma cópia atualizada. */
    @Test
    void listaDeItensNaoPodeSerAlteradaPeloChamador() {
        Pedido pedido = new Pedido(new Mesa(1));
        ItemPedido item = new ItemPedido(new Produto("Suco", 5.5), 1);
        List<ItemPedido> itens = pedido.getItens();

        assertThrows(UnsupportedOperationException.class, () -> itens.add(item));

        pedido.adicionarItem(item);
        assertEquals(0, itens.size());
        assertEquals(1, pedido.getItens().size());
    }
}
