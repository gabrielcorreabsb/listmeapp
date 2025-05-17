package com.listme.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser um valor positivo")
    @Column(nullable = false)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A unidade de medida é obrigatória")
    @Column(name = "unidade_medida", nullable = false)
    private UnidadeMedida unidadeMedida;

    @Column(name = "url_imagem")
    private String urlImagem;

    @Column(name = "ativo")
    private Boolean ativo;

    @PrePersist
    protected void onCreate() {
        if (ativo == null) {
            ativo = true;
        }
    }

    // Enum para unidades de medida
    public enum UnidadeMedida {
        UNIDADE("un"),
        CAIXA("cx"),
        PACOTE("pct"),
        QUILOGRAMA("kg"),
        GRAMA("g"),
        LITRO("l"),
        MILILITRO("ml"),
        METRO("m"),
        CENTIMETRO("cm"),
        DUZIA("dz");

        private final String abreviacao;

        UnidadeMedida(String abreviacao) {
            this.abreviacao = abreviacao;
        }

        public String getAbreviacao() {
            return abreviacao;
        }
    }
}