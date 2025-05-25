package com.listme.dto;

import com.listme.model.Cargo;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email; // Melhor usar @Email
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioUpdateDTO {

    @NotBlank(message = "O nome não pode estar em branco")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "O nome deve conter apenas letras e espaços")
    private String nome;

    @NotBlank(message = "O login não pode estar em branco")
    @Size(min = 3, max = 50, message = "O login deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "O login deve conter apenas letras, números e os caracteres . _ -")
    private String login;

    @NotBlank(message = "O email não pode estar em branco")
    @Email(message = "Email inválido") // Usar @Email para validação de formato de email
    private String email;

    // Para a senha: se for fornecida, deve ser válida. Se não, é ignorada.
    @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres (se fornecida)")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(.{6,})$",
            message = "A nova senha deve conter pelo menos uma letra, um número e um caractere especial (se fornecida)")
    private String senha; // SEM @NotBlank aqui, tornando-a opcional

    @NotNull(message = "O cargo não pode ser nulo")
    @Enumerated(EnumType.STRING) // Se você quiser que o Jackson desserialize direto para o Enum
    private Cargo cargo;

    @NotNull(message = "O status 'ativo' não pode ser nulo")
    private Boolean ativo;
}