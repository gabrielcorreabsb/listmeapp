package com.listme.repository;

import com.listme.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuario extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByLogin(String login);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetPasswordToken(String token);
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdUsuarioNot(String login, Integer idUsuario);
    List<Usuario> findByLoginContaining(String texto);
    List<Usuario> findByAtivo(boolean ativo);
    List<Usuario> findAllByOrderByLoginAsc();
}