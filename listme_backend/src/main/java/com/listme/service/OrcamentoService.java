package com.listme.service;

import com.listme.dto.ItemOrcamentoDTO;
import com.listme.dto.OrcamentoRequestDTO;
import com.listme.dto.OrcamentoResponseDTO;
import com.listme.dto.UserDTO; // Para o OrcamentoResponseDTO
import com.listme.model.*;
import com.listme.repository.ICliente;
import com.listme.repository.IOrcamento;
import com.listme.repository.IProduto;
import com.listme.repository.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Import para LocalDateTime
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final IOrcamento orcamentoRepository;
    private final ICliente clienteRepository;
    private final IUsuario usuarioRepository;
    private final IProduto produtoRepository;

    @Transactional
    public OrcamentoResponseDTO criarOrcamento(OrcamentoRequestDTO requestDTO) {
        Cliente cliente = clienteRepository.findById(requestDTO.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + requestDTO.getClienteId()));

        String loginFuncionario = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario funcionario = usuarioRepository.findByLogin(loginFuncionario)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + loginFuncionario));

        Orcamento orcamento = new Orcamento();
        orcamento.setCliente(cliente);
        orcamento.setFuncionario(funcionario);
        orcamento.setFormaPagamento(requestDTO.getFormaPagamento());
        orcamento.setObservacoes(requestDTO.getObservacoes());
        orcamento.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : Orcamento.StatusOrcamento.PENDENTE);

        List<ItemOrcamento> itensOrcamentoEntities = new ArrayList<>();
        BigDecimal valorTotalCalculado = BigDecimal.ZERO;

        if (requestDTO.getItens() == null || requestDTO.getItens().isEmpty()) {
            throw new IllegalArgumentException("O orçamento deve ter pelo menos um item.");
            // Ou, se um orçamento com 0 itens e total 0 for permitido, ajuste a validação @Positive
            // em Orcamento.valorTotal para @PositiveOrZero e sete valorTotalCalculado para ZERO.
        }

        for (ItemOrcamentoDTO itemDTO : requestDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + itemDTO.getProdutoId()));

            if (produto.getAtivo() == null || !produto.getAtivo()) {
                throw new RuntimeException("Produto '" + produto.getNome() + "' não está ativo e não pode ser adicionado ao orçamento.");
            }
            if (itemDTO.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade para o produto '" + produto.getNome() + "' deve ser positiva.");
            }
            if (produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Preço para o produto '" + produto.getNome() + "' deve ser positivo.");
            }


            ItemOrcamento itemEntity = new ItemOrcamento();
            itemEntity.setProduto(produto);
            itemEntity.setQuantidade(itemDTO.getQuantidade());
            itemEntity.setPrecoUnitarioMomento(produto.getPreco());

            // Calcular e definir o valor total do item AQUI
            BigDecimal totalItem = produto.getPreco().multiply(new BigDecimal(itemDTO.getQuantidade()));
            itemEntity.setValorTotalItem(totalItem);

            // itemEntity.setOrcamento(orcamento); // O JoinColumn e Cascade cuidam disso ao salvar o Orcamento
            itensOrcamentoEntities.add(itemEntity);
            valorTotalCalculado = valorTotalCalculado.add(totalItem);
        }

        orcamento.setItens(itensOrcamentoEntities);
        orcamento.setValorTotal(valorTotalCalculado); // Define o valor total calculado

        // Validação explícita antes de salvar, se ainda tiver @Positive e não @PositiveOrZero
        if (orcamento.getValorTotal().compareTo(BigDecimal.ZERO) <= 0 && !orcamento.getItens().isEmpty()) {
            // Isso não deveria acontecer se os produtos e quantidades são válidos,
            // mas é uma checagem de segurança.
            throw new IllegalStateException("O valor total do orçamento calculado é zero ou negativo, mas há itens. Verifique os preços e quantidades dos produtos.");
        }


        Orcamento novoOrcamento = orcamentoRepository.save(orcamento);
        return OrcamentoResponseDTO.fromEntity(novoOrcamento);
    }

    // ... (listarTodos, buscarPorId) ...

    public List<OrcamentoResponseDTO> listarTodos() {
        return orcamentoRepository.findAll().stream()
                .map(OrcamentoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public OrcamentoResponseDTO buscarPorId(Long id) {
        return orcamentoRepository.findById(id)
                .map(OrcamentoResponseDTO::fromEntity) // Converte a entidade Orcamento para OrcamentoResponseDTO
                .orElseThrow(() -> new EntityNotFoundException("Orçamento não encontrado com ID: " + id));
    }

    @Transactional
    public OrcamentoResponseDTO atualizarOrcamento(Long id, OrcamentoRequestDTO requestDTO) {
        Orcamento orcamentoExistente = orcamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orçamento não encontrado com ID: " + id));

        Cliente cliente = clienteRepository.findById(requestDTO.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + requestDTO.getClienteId()));

        orcamentoExistente.setCliente(cliente);
        orcamentoExistente.setFormaPagamento(requestDTO.getFormaPagamento());
        orcamentoExistente.setObservacoes(requestDTO.getObservacoes());
        if (requestDTO.getStatus() != null) {
            orcamentoExistente.setStatus(requestDTO.getStatus());
        }
        // Data de atualização pode ser um novo campo ou deixar o banco lidar se tiver auditoria

        // Limpar itens antigos para simplificar (cuidado com performance em listas grandes)
        orcamentoExistente.getItens().clear();
        // Para forçar o delete dos itens órfãos imediatamente ANTES de adicionar novos e recalcular
        // Isso pode ser necessário dependendo da configuração do cascade e para evitar problemas
        // com o cálculo do valorTotal antes que os itens antigos sejam realmente removidos.
        orcamentoRepository.flush(); // Força o delete dos itens órfãos

        List<ItemOrcamento> novosItensEntities = new ArrayList<>();
        BigDecimal novoValorTotalCalculado = BigDecimal.ZERO;

        if (requestDTO.getItens() == null || requestDTO.getItens().isEmpty()) {
            throw new IllegalArgumentException("A atualização do orçamento deve conter pelo menos um item.");
        }

        for (ItemOrcamentoDTO itemDTO : requestDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + itemDTO.getProdutoId()));
            if (produto.getAtivo() == null || !produto.getAtivo()) {
                throw new RuntimeException("Produto '" + produto.getNome() + "' não está ativo.");
            }
            if (itemDTO.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade para o produto '" + produto.getNome() + "' deve ser positiva.");
            }
            if (produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Preço para o produto '" + produto.getNome() + "' deve ser positivo.");
            }

            ItemOrcamento itemEntity = new ItemOrcamento();
            itemEntity.setProduto(produto);
            itemEntity.setQuantidade(itemDTO.getQuantidade());
            itemEntity.setPrecoUnitarioMomento(produto.getPreco());

            BigDecimal totalItem = produto.getPreco().multiply(new BigDecimal(itemDTO.getQuantidade()));
            itemEntity.setValorTotalItem(totalItem);

            // itemEntity.setOrcamento(orcamentoExistente); // O JoinColumn e Cascade cuidam disso
            novosItensEntities.add(itemEntity);
            novoValorTotalCalculado = novoValorTotalCalculado.add(totalItem);
        }

        orcamentoExistente.setItens(novosItensEntities);
        orcamentoExistente.setValorTotal(novoValorTotalCalculado);

        if (orcamentoExistente.getValorTotal().compareTo(BigDecimal.ZERO) <= 0 && !orcamentoExistente.getItens().isEmpty()) {
            throw new IllegalStateException("O valor total do orçamento atualizado é zero ou negativo, mas há itens.");
        }

        Orcamento orcamentoAtualizado = orcamentoRepository.save(orcamentoExistente);
        return OrcamentoResponseDTO.fromEntity(orcamentoAtualizado);
    }


    @Transactional
    public void deletarOrcamento(Long id) {
        if (!orcamentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Orçamento não encontrado com ID: " + id);
        }
        orcamentoRepository.deleteById(id);
    }
}