package com.senai.nucitalanches.nucitalanches.domain;

/** Representa um produto disponível para consumo. */
public class Produto {
    private Long id;
    private String nome;
    private double preco;

    /**
     * Cria um produto com nome e preço válidos.
     *
     * @param nome nome do produto
     * @param preco preço do produto
     * @throws IllegalArgumentException se o nome for nulo/vazio ou o preço for negativo/não finito
     */
    public Produto(String nome, double preco) {
        setNome(nome);
        setPreco(preco);
    }

    /** Retorna o identificador atribuído ao produto. */
    public Long getId() {
        return id;
    }

    /** Define o identificador do produto. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Retorna o nome do produto. */
    public String getNome() {
        return nome;
    }

    /** Define o nome do produto e remove espaços no início e no fim. */
    public void setNome(String nome) {
        validarNome(nome);
        this.nome = nome.trim();
    }

    /** Retorna o preço do produto. */
    public double getPreco() {
        return preco;
    }

    /** Define um preço finito e não negativo. */
    public void setPreco(double preco) {
        validarPreco(preco);
        this.preco = preco;
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio ou nulo.");
        }
    }

    private static void validarPreco(double preco) {
        if (!Double.isFinite(preco) || preco < 0) {
            throw new IllegalArgumentException("Preço deve ser um valor finito e não negativo.");
        }
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", preco=" + preco +
                '}';
    }
}
