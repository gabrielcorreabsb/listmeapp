package com.listme.securingweb;

import com.listme.model.Usuario;
import com.listme.repository.IUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final com.listme.securingweb.JwtTokenProvider tokenProvider;
    private final IUsuario usuarioRepository;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(com.listme.securingweb.JwtTokenProvider tokenProvider, IUsuario usuarioRepository) {
        this.tokenProvider = tokenProvider;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            logger.debug("JWT recebido: {}", jwt != null ? "presente" : "ausente");

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userLogin = tokenProvider.getUserLoginFromToken(jwt);
                logger.debug("Login do usuário extraído do token: {}", userLogin);

                Usuario usuario = usuarioRepository.findByLogin(userLogin)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

                // Criar lista de autoridades
                List<GrantedAuthority> authorities = new ArrayList<>();

                // Adicionar ROLE baseada no cargo
                String roleFromCargo = "ROLE_" + usuario.getCargo().toString();
                authorities.add(new SimpleGrantedAuthority(roleFromCargo));

                // Opcional: adicionar também uma role genérica USER
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

                logger.debug("Autoridades atribuídas: {}", authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                authorities
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Autenticação configurada para usuário: {}", userLogin);
            }
        } catch (Exception ex) {
            logger.error("Não foi possível autenticar o usuário", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}