package com.listme.securingweb;

import com.listme.model.Usuario;
import com.listme.repository.IUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final IUsuario usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    public CustomAuthenticationProvider(IUsuario usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String login = authentication.getName();
        String senha = authentication.getCredentials().toString();

        logger.debug("Tentativa de autenticação para usuário: {}", login);

        try {
            Usuario usuario = usuarioRepository.findByLogin(login)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));

            logger.debug("Usuário encontrado: {}", usuario.getLogin());
            logger.debug("Cargo do usuário: {}", usuario.getCargo());

            if (!usuario.isAtivo()) {
                logger.debug("Usuário está inativo: {}", login);
                throw new DisabledException("Usuário está desativado");
            }

            if (!passwordEncoder.matches(senha, usuario.getSenha())) {
                logger.debug("Senha inválida para usuário: {}", login);
                throw new BadCredentialsException("Senha inválida");
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getCargo().toString()));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            logger.debug("Autoridades atribuídas: {}", authorities);
            logger.debug("Autenticação bem-sucedida para usuário: {}", login);

            return new UsernamePasswordAuthenticationToken(
                    usuario,
                    senha,
                    authorities
            );
        } catch (Exception e) {
            logger.error("Erro durante autenticação: ", e);
            throw e;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}