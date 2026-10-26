package com.senai.nucitalanches.nucitalanches.application;

import com.senai.nucitalanches.nucitalanches.domain.ItemPedido;
import com.senai.nucitalanches.nucitalanches.domain.Mesa;
import com.senai.nucitalanches.nucitalanches.domain.Pedido;
import com.senai.nucitalanches.nucitalanches.domain.Produto;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testa a criação de pedidos e a inclusão de itens pela aplicação. */
class RestaurantePedidoTest {
    private static final AtomicInteger PROXIMO_NUMERO_MESA = new AtomicInteger(1_000_000_000);

    private final Restaurante restaurante = new Restaurante();

    /** Cria uma mesa de teste com número ainda não usado. */
    private int cadastrarMesa() {
        int numero = PROXIMO_NUMERO_MESA.getAndIncrement();
        assertTrue(restaurante.cadastrarMesa(new Mesa(numero)));
        return numero;
    }

    /** Cria pedido associado à mesa cadastrada e recebe um número do armazenamento. */
    @Test
    void criaPedidoParaMesaCadastrada() {
        int numeroMesa = cadastrarMesa();

        Pedido pedido = restaurante.criarPedido(numeroMesa);

        assertSame(restaurante.consultarMesaPorNumero(numeroMesa), pedido.getMesa());
        assertTrue(pedido.getNumeroPedido() > 0);
    }

    /** Recusa pedido quando o número informado não corresponde a uma mesa cadastrada. */
    @Test
    void rejeitaCriacaoParaMesaInexistente() {
        assertThrows(IllegalArgumentException.class, () -> restaurante.criarPedido(Integer.MAX_VALUE));
    }

    /** Inclui no pedido existente o produto e a quantidade informados. */
    @Test
    void adicionaItemAoPedidoExistente() {
        Pedido pedido = restaurante.criarPedido(cadastrarMesa());
        Produto produto = new Produto("Suco", 5.5);

        ItemPedido item = restaurante.adicionarItemAoPedido(pedido.getNumeroPedido(), produto, 2);

        assertSame(produto, pedido.getItens().get(0).getProduto());
        assertSame(item, pedido.getItens().get(0));
        assertEquals(2, pedido.getItens().get(0).getQuantidade());
    }

    /** Recusa inclusão quando o número informado não corresponde a um pedido cadastrado. */
    @Test
    void rejeitaInclusaoEmPedidoInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                restaurante.adicionarItemAoPedido(Integer.MAX_VALUE, new Produto("Suco", 5.5), 1));
    }
}
