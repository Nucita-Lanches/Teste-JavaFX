package com.senai.nucitalanches.nucitalanches.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Representa um pedido de uma mesa e os itens incluídos nele. */
public class Pedido {
    private Integer numeroPedido;
    private final Mesa mesa;
    private final List<ItemPedido> itens = new ArrayList<>();
    private StatusPedido status = StatusPedido.ABERTO;

    /**
     * Cria um pedido para uma mesa.
     *
     * @param mesa mesa associada ao pedido
     * @throws IllegalArgumentException se a mesa for nula
     */
    public Pedido(Mesa mesa) {
        if (mesa == null) {
            throw new IllegalArgumentException("Mesa do pedido não pode ser nula.");
        }
        this.mesa = mesa;
    }

    /** Retorna o número atribuído ao pedido pelo armazenamento. */
    public Integer getNumeroPedido() {
        return numeroPedido;
    }

    /** Define o número do pedido atribuído pelo armazenamento. */
    public void setNumeroPedido(Integer numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    /** Retorna a mesa associada ao pedido. */
    public Mesa getMesa() {
        return mesa;
    }

    /** Retorna uma cópia somente para leitura dos itens do pedido. */
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(new ArrayList<>(itens));
    }

    /**
     * Adiciona um item ao pedido.
     *
     * @param item item a ser adicionado
     * @throws IllegalArgumentException se o item for nulo
     */
    public void adicionarItem(ItemPedido item) {
        if (item == null) {
            throw new IllegalArgumentException("Item do pedido não pode ser nulo.");
        }
        itens.add(item);
    }

    /** Retorna o estado atual do pedido. */
    public StatusPedido getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "numeroPedido=" + numeroPedido +
                ", mesa=" + mesa +
                ", itens=" + itens +
                ", status=" + status +
                '}';
    }
}
