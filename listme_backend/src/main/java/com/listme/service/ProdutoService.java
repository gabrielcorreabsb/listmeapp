package com.listme.service;

import com.listme.dto.ProdutoDTO;
import com.listme.model.Produto;
import com.listme.repository.IProduto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final IProduto produtoRepository;

    /**
     * Lista todos os produtos cadastrados
     * @return Lista de DTOs de produtos
     */
    public List<ProdutoDTO> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(ProdutoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Busca um produto pelo ID
     * @param id ID do produto
     * @return DTO do produto encontrado
     * @throws EntityNotFoundException se o produto não for encontrado
     */
    public ProdutoDTO buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .map(ProdutoDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    /**
     * Cria um novo produto
     * @param produtoDTO DTO com os dados do produto
     * @return DTO do produto criado
     */
    @Transactional
    public ProdutoDTO criar(ProdutoDTO produtoDTO) {
        Produto produto = produtoDTO.toEntity();
        produto = produtoRepository.save(produto);
        return ProdutoDTO.fromEntity(produto);
    }

    /**
     * Atualiza um produto existente
     * @param id ID do produto a ser atualizado
     * @param produtoDTO DTO com os novos dados do produto
     * @return DTO do produto atualizado
     * @throws EntityNotFoundException se o produto não for encontrado
     */
    @Transactional
    public ProdutoDTO atualizar(Long id, ProdutoDTO produtoDTO) {
        if (!produtoRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado");
        }

        Produto produto = produtoDTO.toEntity();
        produto.setId(id);
        produto = produtoRepository.save(produto);
        return ProdutoDTO.fromEntity(produto);
    }

    /**
     * Remove um produto
     * @param id ID do produto a ser removido
     * @throws EntityNotFoundException se o produto não for encontrado
     */
    @Transactional
    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado");
        }
        produtoRepository.deleteById(id);
    }

    /**
     * Busca produtos pelo nome
     * @param nome Nome ou parte do nome para busca
     * @return Lista de DTOs dos produtos encontrados
     */
    public List<ProdutoDTO> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(ProdutoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lista apenas produtos ativos
     * @return Lista de DTOs dos produtos ativos
     */
    public List<ProdutoDTO> listarAtivos() {
        return produtoRepository.findByAtivo(true)
                .stream()
                .map(ProdutoDTO::fromEntity)
                .collect(Collectors.toList());
    }
}