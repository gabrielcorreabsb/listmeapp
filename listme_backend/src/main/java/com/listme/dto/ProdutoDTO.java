package com.listme.dto;

import com.listme.model.Produto;
import com.listme.model.Produto.UnidadeMedida;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser um valor positivo")
    private BigDecimal preco;

    @NotNull(message = "A unidade de medida é obrigatória")
    private UnidadeMedida unidadeMedida;

    private String urlImagem;

    private Boolean ativo;

    /**
     * Converte uma entidade Produto para ProdutoDTO
     * @param produto Entidade a ser convertida
     * @return DTO correspondente à entidade
     */
    public static ProdutoDTO fromEntity(Produto produto) {
        return ProdutoDTO.builder()
                .id(produto.getId())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .preco(produto.getPreco())
                .unidadeMedida(produto.getUnidadeMedida())
                .urlImagem(produto.getUrlImagem())
                .ativo(produto.getAtivo())
                .build();
    }

    /**
     * Converte este DTO para uma entidade Produto
     * @return Entidade correspondente a este DTO
     */
    public Produto toEntity() {
        return Produto.builder()
                .id(this.id)
                .nome(this.nome)
                .descricao(this.descricao)
                .preco(this.preco)
                .unidadeMedida(this.unidadeMedida)
                .urlImagem(this.urlImagem)
                .ativo(this.ativo)
                .build();
    }
}