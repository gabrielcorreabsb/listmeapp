package com.listme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @NotBlank(message = "O login não pode estar em branco")
    @Size(min = 3, max = 50, message = "O login deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "O login deve conter apenas letras, números e os caracteres . _ -")
    @Column(name = "login", length = 50, nullable = false, unique = true)
    private String login;

    @NotBlank(message = "O nome não pode estar em branco")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "O nome deve conter apenas letras e espaços")
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @NotBlank(message = "O email não pode estar em branco")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Email inválido")
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "A senha não pode estar em branco")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(.{6,})$",
            message = "A senha deve conter pelo menos uma letra, um número e um caractere especial")
    @Column(name = "senha", columnDefinition = "TEXT", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @NotNull(message = "O cargo não pode ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(name = "cargo", nullable = false)
    private Cargo cargo;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Builder.Default
    @Column(name = "tentativas_login")
    private Integer tentativasLogin = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

    @PrePersist
    @PreUpdate
    protected void onPersistAndUpdate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (ultimoAcesso == null) {
            ultimoAcesso = LocalDateTime.now();
        }
        if (roles == null) {
            roles = new HashSet<>();
        }
        if (cargo != null) {
            roles.add("ROLE_" + cargo.name());
        }
    }

    public void atualizarUltimoAcesso() {
        this.ultimoAcesso = LocalDateTime.now();
    }

    public void incrementarTentativasLogin() {
        this.tentativasLogin++;
    }

    public void resetarTentativasLogin() {
        this.tentativasLogin = 0;
    }

    @JsonIgnore
    public boolean isContaBloqueada() {
        return tentativasLogin >= 5;
    }

    @JsonIgnore
    public boolean isAtivo() {
        return ativo;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (this.cargo != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + this.cargo.name()));
        }
        if (this.roles != null) {
            this.roles.forEach(role -> {
                if (!role.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                } else {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            });
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.senha;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.login;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !isContaBloqueada();
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.ativo;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", login='" + login + '\'' +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", dataCriacao=" + dataCriacao +
                ", ultimoAcesso=" + ultimoAcesso +
                ", ativo=" + ativo +
                ", tentativasLogin=" + tentativasLogin +
                ", cargo=" + cargo +
                ", roles=" + roles +
                '}';
    }

    @Builder
    public Usuario(Integer idUsuario, String login, String nome, String email, String senha,
                   Cargo cargo, LocalDateTime dataCriacao, LocalDateTime ultimoAcesso,
                   Boolean ativo, Integer tentativasLogin, Set<String> roles,
                   String resetPasswordToken, LocalDateTime resetPasswordTokenExpiry) {
        this.idUsuario = idUsuario;
        this.login = login;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cargo = cargo;
        this.dataCriacao = dataCriacao;
        this.ultimoAcesso = ultimoAcesso;
        this.ativo = ativo != null ? ativo : true;
        this.tentativasLogin = tentativasLogin != null ? tentativasLogin : 0;
        this.roles = roles != null ? roles : new HashSet<>();
        this.resetPasswordToken = resetPasswordToken;
        this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;
        if (this.cargo != null) {
            this.roles.add("ROLE_" + this.cargo.name());
        }
    }
}