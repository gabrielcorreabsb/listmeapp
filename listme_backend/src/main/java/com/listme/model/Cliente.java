package com.listme.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O telefone é obrigatório")
    @Column(nullable = false)
    private String telefone;

    @NotBlank(message = "O endereço é obrigatório")
    @Column(nullable = false)
    private String endereco;

    @NotBlank(message = "O CNPJ é obrigatório")
    @Pattern(regexp = "^([0-9]{2}(\\.?[0-9]{3}){2}\\/?[0-9]{4}\\-?[0-9]{2})|([0-9]{3}(\\.?[0-9]{3}){2}\\-?[0-9]{2})$",
            message = "CNPJ/CPF inválido.")
    @Column(nullable = false, unique = true)
    private String cnpj;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    @Column(nullable = false, unique = true)
    private String email;
}