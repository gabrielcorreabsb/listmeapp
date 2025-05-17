package com.listme.dto;

import com.listme.model.Cargo;
import com.listme.model.Usuario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Integer idUsuario;
    private String login;
    private String nome;
    private String email;
    private LocalDateTime dataCriacao;
    private Boolean ativo;
    private Cargo cargo;

    public UserDTO(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.login = usuario.getLogin();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.dataCriacao = usuario.getDataCriacao();
        this.ativo = usuario.getAtivo();
        this.cargo = usuario.getCargo();
    }
}