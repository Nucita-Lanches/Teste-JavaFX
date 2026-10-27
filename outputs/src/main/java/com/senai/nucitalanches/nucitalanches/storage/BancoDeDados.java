package com.senai.nucitalanches.nucitalanches.storage;

import com.senai.nucitalanches.nucitalanches.domain.Mesa;
import com.senai.nucitalanches.nucitalanches.domain.Pedido;
import com.senai.nucitalanches.nucitalanches.domain.Produto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Guarda os dados da aplicação em coleções durante a execução. */
public class BancoDeDados {

    private static final BancoDeDados INSTANCIA = new BancoDeDados();

    // Produtos
    private final Map<Long, Produto> produtos = new LinkedHashMap<>();
    private Long ultimoIdProduto = 0L;

    // Mesas
    private final ArrayList<Mesa> mesas = new ArrayList<>();

    // Pedidos
    private final Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
    private Integer ultimoNumeroPedido = 0;

    private BancoDeDados() {
    }

    /** Retorna a instância única do armazenamento em memória. */
    public static BancoDeDados getInstancia() {
        return INSTANCIA;
    }

    // PRODUTOS
    /** Salva o produto, atribui um identificador e retorna o produto salvo. */
    public Produto salvarProduto(Produto produto) {
        Objects.requireNonNull(produto, "Produto não pode ser nulo.");
        ultimoIdProduto += 1;
        produto.setId(ultimoIdProduto);
        produtos.put(ultimoIdProduto, produto);
        return produto;
    }

    /** Retorna uma cópia somente para leitura dos produtos cadastrados. */
    public Collection<Produto> listarProdutos() {
        return Collections.unmodifiableList(new ArrayList<>(produtos.values()));
    }

    /** Retorna uma cópia somente para leitura do mapa de produtos. */
    public Map<Long, Produto> getProdutos() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(produtos));
    }

    /** Retorna o produto pelo identificador ou {@code null} se não existir. */
    public Produto buscarProdutoPorId(Long id) {
        return produtos.get(id);
    }

    // MESAS
    /** Cadastra uma mesa se o número for positivo e ainda não estiver em uso. */
    public boolean adicionarMesa(Mesa mesa) {
        if (mesa == null || mesa.getNumero() == null || mesa.getNumero() <= 0) {
            return false;
        }

        if (buscarMesaPorNumero(mesa.getNumero()) != null) {
            return false;
        }

        mesas.add(mesa);
        return true;
    }

    /** Retorna a mesa pelo número ou {@code null} se não existir. */
    public Mesa buscarMesaPorNumero(Integer numero) {
        if (numero == null) {
            return null;
        }

        for (Mesa mesa : mesas) {
            if (mesa.getNumero() != null && mesa.getNumero().equals(numero)) {
                return mesa;
            }
        }

        return null;
    }

    /** Retorna uma cópia da lista de mesas cadastradas. */
    public ArrayList<Mesa> listarMesas() {
        return new ArrayList<>(mesas);
    }

    /** Retorna uma cópia da lista de mesas cadastradas. */
    public ArrayList<Mesa> getMesas() {
        return new ArrayList<>(mesas);
    }

    // PEDIDOS
    /** Salva o pedido, atribui seu número e retorna o pedido salvo. */
    public Pedido salvarPedido(Pedido pedido) {
        Objects.requireNonNull(pedido, "Pedido não pode ser nulo.");
        ultimoNumeroPedido += 1;
        pedido.setNumeroPedido(ultimoNumeroPedido);
        pedidos.put(ultimoNumeroPedido, pedido);
        return pedido;
    }

    /** Retorna uma cópia somente para leitura dos pedidos cadastrados. */
    public Collection<Pedido> listarPedidos() {
        return Collections.unmodifiableList(new ArrayList<>(pedidos.values()));
    }

    /** Retorna uma cópia somente para leitura do mapa de pedidos. */
    public Map<Integer, Pedido> getPedidos() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(pedidos));
    }

    /** Retorna o pedido pelo número ou {@code null} se não existir. */
    public Pedido buscarPedidoPorNumero(Integer numeroPedido) {
        return pedidos.get(numeroPedido);
    }
}
