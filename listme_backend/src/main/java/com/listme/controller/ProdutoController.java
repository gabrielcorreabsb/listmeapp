package com.listme.controller;

import com.listme.dto.ProdutoDTO;
import com.listme.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    /**
     * Lista todos os produtos cadastrados
     * @return Lista de produtos
     */
    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarTodos() {
        return ResponseEntity.ok(produtoService.listarTodos());
    }

    /**
     * Lista apenas produtos ativos
     * @return Lista de produtos ativos
     */
    @GetMapping("/ativos")
    public ResponseEntity<List<ProdutoDTO>> listarAtivos() {
        return ResponseEntity.ok(produtoService.listarAtivos());
    }

    /**
     * Busca um produto específico pelo ID
     * @param id ID do produto
     * @return Produto encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    /**
     * Busca produtos pelo nome
     * @param nome Nome ou parte do nome para busca
     * @return Lista de produtos que correspondem à busca
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProdutoDTO>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(produtoService.buscarPorNome(nome));
    }

    /**
     * Cria um novo produto
     * @param produtoDTO Dados do produto a ser criado
     * @return Produto criado
     */
    @PostMapping
    public ResponseEntity<ProdutoDTO> criar(@Valid @RequestBody ProdutoDTO produtoDTO) {
        ProdutoDTO novoProduto = produtoService.criar(produtoDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(novoProduto.getId())
                .toUri();
        return ResponseEntity.created(location).body(novoProduto);
    }

    /**
     * Atualiza um produto existente
     * @param id ID do produto a ser atualizado
     * @param produtoDTO Novos dados do produto
     * @return Produto atualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoDTO> atualizar(@PathVariable Long id,
                                                @Valid @RequestBody ProdutoDTO produtoDTO) {
        return ResponseEntity.ok(produtoService.atualizar(id, produtoDTO));
    }

    /**
     * Remove um produto
     * @param id ID do produto a ser removido
     * @return Resposta sem conteúdo (204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}