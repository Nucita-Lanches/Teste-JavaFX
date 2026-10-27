package com.senai.nucitalanches.nucitalanches.domain;

/** Representa uma mesa pelo seu número. */
public class Mesa {
    private final Integer numero;

    /** Cria uma mesa; a validade do número é verificada ao cadastrá-la. */
    public Mesa(Integer numero) {
        this.numero = numero;
    }

    /** Retorna o número da mesa. */
    public Integer getNumero() {
        return numero;
    }

    @Override
    public String toString() {
        return "Mesa{" +
                "numero=" + numero +
                '}';
    }
}
