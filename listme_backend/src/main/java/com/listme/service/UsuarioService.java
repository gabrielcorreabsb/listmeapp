package com.listme.service;

import com.listme.dto.UsuarioUpdateDTO;
import com.listme.model.Usuario;
import com.listme.repository.IUsuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UsuarioService {

    private final IUsuario repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(IUsuario repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }


    public Optional<Usuario> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public void createPasswordResetTokenForUser(Usuario user, String token, LocalDateTime expiryDate) {
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(expiryDate);
        repository.save(user);
    }

    public String validatePasswordResetToken(String token) {
        final Usuario user = repository.findByResetPasswordToken(token).orElse(null);
        if (user == null) {
            return "Token de redefinição inválido.";
        }
        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            return "Token de redefinição expirado.";
        }
        return null; // Token válido
    }

    public Optional<Usuario> getUserByPasswordResetToken(String token) {
        return repository.findByResetPasswordToken(token);
    }

    public void changeUserPassword(Usuario user, String password) {
        // Adicione validações de pattern e size aqui se não estiverem no DTO/entidade para este fluxo
        // ou se você quiser garantir no serviço.
        // Ex: if (password.length() < 6) throw new IllegalArgumentException("Senha muito curta");
        user.setSenha(passwordEncoder.encode(password));
        user.setResetPasswordToken(null); // Invalida o token
        user.setResetPasswordTokenExpiry(null);
        repository.save(user);
    }

    public List<Usuario> listarUsuario() {
        log.info("Listando todos os usuários");
        return repository.findAll();
    }

    public List<Usuario> listarUsuariosOrdenadosPorLogin() {
        log.info("Listando todos os usuários ordenados por login");
        return repository.findAllByOrderByLoginAsc();
    }

    @Transactional
    public Usuario criarUsuario(Usuario usuario) {
        log.info("Criando novo usuário com login: {}", usuario.getLogin());

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }

        if (repository.existsByLogin(usuario.getLogin())) {
            throw new RuntimeException("Login já existe");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setAtivo(true);
        usuario.setTentativasLogin(0);

        return repository.save(usuario);
    }

    @Transactional
    public Usuario editarUsuario(Integer id, UsuarioUpdateDTO dto) {
        Usuario usuarioExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        // Atualizar os campos da entidade com os valores do DTO
        usuarioExistente.setNome(dto.getNome());
        usuarioExistente.setLogin(dto.getLogin()); // Adicionar verificação de unicidade se necessário
        usuarioExistente.setEmail(dto.getEmail()); // Adicionar verificação de unicidade se necessário
        usuarioExistente.setCargo(dto.getCargo());
        usuarioExistente.setAtivo(dto.getAtivo());

        // Lógica para atualizar a senha SOMENTE se uma nova foi fornecida
        if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
            // As validações @Size e @Pattern no DTO já terão sido aplicadas pelo @Valid no controller
            // se uma senha foi fornecida.
            usuarioExistente.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        // Se dto.getSenha() for null ou vazia, a senha existente no banco não é alterada.

        // A lógica @PreUpdate na sua entidade Usuario deve cuidar de atualizar as roles
        // se o cargo mudou.
        return repository.save(usuarioExistente);
    }

    @Transactional
    public Boolean excluirUsuario(Integer idUsuario) {
        log.info("Excluindo usuário com ID: {}", idUsuario);

        if (!repository.existsById(idUsuario)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        repository.deleteById(idUsuario);
        return true;
    }

    @Transactional
    public Usuario verificarCredenciais(String login, String senha) {
        log.info("Verificando credenciais para login: {}", login);

        return repository.findByLogin(login)
                .map(usuario -> {
                    if (!usuario.isEnabled()) {
                        throw new RuntimeException("Conta desativada");
                    }

                    if (usuario.isContaBloqueada()) {
                        throw new RuntimeException("Conta bloqueada");
                    }

                    if (passwordEncoder.matches(senha, usuario.getSenha())) {
                        usuario.resetarTentativasLogin();
                        usuario.atualizarUltimoAcesso();
                        return repository.save(usuario);
                    } else {
                        usuario.incrementarTentativasLogin();
                        repository.save(usuario);
                        throw new RuntimeException("Senha inválida");
                    }
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));
    }

    @Transactional
    public Usuario bloquearUsuario(Integer idUsuario) {
        log.info("Bloqueando usuário com ID: {}", idUsuario);
        return repository.findById(idUsuario)
                .map(usuario -> {
                    usuario.setAtivo(false);
                    return repository.save(usuario);
                })
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Transactional
    public Usuario desbloquearUsuario(Integer idUsuario) {
        log.info("Desbloqueando usuário com ID: {}", idUsuario);
        return repository.findById(idUsuario)
                .map(usuario -> {
                    usuario.setAtivo(true);
                    usuario.resetarTentativasLogin();
                    return repository.save(usuario);
                })
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Optional<Usuario> buscarPorLogin(String login) {
        log.info("Buscando usuário pelo login: {}", login);
        return repository.findByLogin(login);
    }

    public Optional<Usuario> buscarPorId(Integer id) {
        log.info("Buscando usuário pelo ID: {}", id);
        return repository.findById(id);
    }
}