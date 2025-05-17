package com.listme.service;

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
    public Usuario editarUsuario(Usuario usuario) {
        log.info("Editando usuário com ID: {}", usuario.getIdUsuario());

        if (!repository.existsById(usuario.getIdUsuario())) {
            throw new RuntimeException("Usuário não encontrado");
        }

        if (repository.existsByLoginAndIdUsuarioNot(usuario.getLogin(), usuario.getIdUsuario())) {
            throw new RuntimeException("Login já existe para outro usuário");
        }

        Optional<Usuario> usuarioExistente = repository.findById(usuario.getIdUsuario());
        if (usuarioExistente.isPresent()) {
            Usuario usuarioAtual = usuarioExistente.get();

            if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
                usuario.setSenha(usuarioAtual.getSenha());
            } else {
                usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            }

            if (usuario.getDataCriacao() == null) {
                usuario.setDataCriacao(usuarioAtual.getDataCriacao());
            }
            if (usuario.getUltimoAcesso() == null) {
                usuario.setUltimoAcesso(usuarioAtual.getUltimoAcesso());
            }
            usuario.setTentativasLogin(usuarioAtual.getTentativasLogin());
        }

        return repository.save(usuario);
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