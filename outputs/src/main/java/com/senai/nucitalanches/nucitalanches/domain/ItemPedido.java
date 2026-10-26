package com.senai.nucitalanches.nucitalanches.domain;

/** Representa um produto e sua quantidade dentro de um pedido. */
public class ItemPedido {
    private Produto produto;
    private Integer quantidade;

    /**
     * Cria um item com produto e quantidade válidos.
     *
     * @param produto produto associado ao item
     * @param quantidade quantidade do produto
     * @throws IllegalArgumentException se o produto for nulo ou a quantidade não for positiva
     */
    public ItemPedido(Produto produto, Integer quantidade) {
        setProduto(produto);
        setQuantidade(quantidade);
    }

    /** Retorna o produto deste item. */
    public Produto getProduto() {
        return produto;
    }

    /**
     * Define o produto deste item.
     *
     * @param produto produto associado ao item
     * @throws IllegalArgumentException se o produto for nulo
     */
    public void setProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto do item não pode ser nulo.");
        }
        this.produto = produto;
    }

    /** Retorna a quantidade deste item. */
    public Integer getQuantidade() {
        return quantidade;
    }

    /**
     * Define uma quantidade maior que zero.
     *
     * @param quantidade nova quantidade
     * @throws IllegalArgumentException se a quantidade for nula ou não positiva
     */
    public void setQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "produto=" + produto +
                ", quantidade=" + quantidade +
                '}';
    }
}
