package com.listme.dto;

import com.listme.model.Cliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private Long id;
    private String nome;
    private String telefone;
    private String endereco;
    private String cnpj;
    private String email;

    /**
     * Converte uma entidade Cliente para ClienteDTO
     * @param cliente Entidade a ser convertida
     * @return DTO correspondente à entidade
     */
    public static ClienteDTO fromEntity(Cliente cliente) {
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .telefone(cliente.getTelefone())
                .endereco(cliente.getEndereco())
                .cnpj(cliente.getCnpj())
                .email(cliente.getEmail())
                .build();
    }

    /**
     * Converte este DTO para uma entidade Cliente
     * @return Entidade correspondente a este DTO
     */
    public Cliente toEntity() {
        return Cliente.builder()
                .id(this.id)
                .nome(this.nome)
                .telefone(this.telefone)
                .endereco(this.endereco)
                .cnpj(this.cnpj)
                .email(this.email)
                .build();
    }
}