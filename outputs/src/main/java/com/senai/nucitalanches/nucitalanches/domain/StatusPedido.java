package com.senai.nucitalanches.nucitalanches.domain;

/** Estados disponíveis para um pedido. A mudança de estado será tratada na etapa de finalização. */
public enum StatusPedido {
    /** Pedido que ainda pode receber itens. */
    ABERTO,

    /** Pedido que representa uma conta finalizada. */
    FINALIZADO
}
