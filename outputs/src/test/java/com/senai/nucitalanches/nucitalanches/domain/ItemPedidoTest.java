package com.senai.nucitalanches.nucitalanches.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPedidoTest {

    @Test
    void criaItemComProdutoEQuantidadeValidos() {
        Produto produto = new Produto("Suco", 5.5);

        ItemPedido item = new ItemPedido(produto, 2);

        assertSame(produto, item.getProduto());
        assertEquals(2, item.getQuantidade());
    }

    @Test
    void rejeitaProdutoNuloNoConstrutorESetter() {
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(null, 1));

        Produto produto = new Produto("Suco", 5.5);
        ItemPedido item = new ItemPedido(produto, 1);

        assertThrows(IllegalArgumentException.class, () -> item.setProduto(null));
        assertSame(produto, item.getProduto());
    }

    @Test
    void rejeitaQuantidadeNulaOuNaoPositivaNoConstrutor() {
        Produto produto = new Produto("Suco", 5.5);

        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(produto, null));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(produto, 0));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(produto, -1));
    }

    @Test
    void setterRejeitaQuantidadeNulaOuNaoPositivaESemAlterarValorAtual() {
        ItemPedido item = new ItemPedido(new Produto("Suco", 5.5), 2);

        assertThrows(IllegalArgumentException.class, () -> item.setQuantidade(null));
        assertThrows(IllegalArgumentException.class, () -> item.setQuantidade(0));
        assertThrows(IllegalArgumentException.class, () -> item.setQuantidade(-1));

        assertNotNull(item.getProduto());
        assertEquals(2, item.getQuantidade());
    }
}
