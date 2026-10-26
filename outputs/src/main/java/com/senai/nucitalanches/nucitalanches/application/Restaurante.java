package com.senai.nucitalanches.nucitalanches.application;

import com.senai.nucitalanches.nucitalanches.domain.ItemPedido;
import com.senai.nucitalanches.nucitalanches.domain.Mesa;
import com.senai.nucitalanches.nucitalanches.domain.Pedido;
import com.senai.nucitalanches.nucitalanches.domain.Produto;
import com.senai.nucitalanches.nucitalanches.storage.BancoDeDados;

import java.util.ArrayList;
import java.util.Collection;

/** Coordena as operações disponíveis para o restaurante. */
public class Restaurante {

    /** Cadastra um produto e atribui seu identificador. */
    public void cadastroProduto(Produto produto) {
        BancoDeDados.getInstancia().salvarProduto(produto);
    }

    /** Retorna os produtos cadastrados. */
    public Collection<Produto> listarProdutos() {
        return BancoDeDados.getInstancia().listarProdutos();
    }

    /** Cadastra uma mesa e informa se ela foi aceita. */
    public boolean cadastrarMesa(Mesa mesa) {
        return BancoDeDados.getInstancia().adicionarMesa(mesa);
    }

    /** Retorna a mesa cadastrada ou {@code null} quando não for encontrada. */
    public Mesa consultarMesaPorNumero(Integer numero) {
        return BancoDeDados.getInstancia().buscarMesaPorNumero(numero);
    }

    /** Retorna uma cópia da lista de mesas cadastradas. */
    public ArrayList<Mesa> listarMesas() {
        return BancoDeDados.getInstancia().listarMesas();
    }

    /**
     * Cria um pedido para uma mesa já cadastrada.
     *
     * @param numeroMesa número da mesa cadastrada
     * @return pedido criado com número atribuído pelo armazenamento
     * @throws IllegalArgumentException se a mesa não estiver cadastrada
     */
    public Pedido criarPedido(Integer numeroMesa) {
        BancoDeDados bancoDeDados = BancoDeDados.getInstancia();
        Mesa mesa = bancoDeDados.buscarMesaPorNumero(numeroMesa);
        if (mesa == null) {
            throw new IllegalArgumentException("Mesa não cadastrada.");
        }

        return bancoDeDados.salvarPedido(new Pedido(mesa));
    }

    /**
     * Adiciona ao pedido um produto com quantidade válida.
     *
     * @param numeroPedido número do pedido existente
     * @param produto produto do item
     * @param quantidade quantidade do produto
     * @return item adicionado ao pedido
     * @throws IllegalArgumentException se o pedido não existir ou os dados do item forem inválidos
     */
    public ItemPedido adicionarItemAoPedido(Integer numeroPedido, Produto produto, Integer quantidade) {
        Pedido pedido = BancoDeDados.getInstancia().buscarPedidoPorNumero(numeroPedido);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não encontrado.");
        }

        ItemPedido item = new ItemPedido(produto, quantidade);
        pedido.adicionarItem(item);
        return item;
    }
}
