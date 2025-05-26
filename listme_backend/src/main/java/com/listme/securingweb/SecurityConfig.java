package com.listme.securingweb;

import com.listme.repository.IUsuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUsuario usuarioRepository;

    public SecurityConfig(
            CustomAuthenticationProvider customAuthenticationProvider,
            JwtTokenProvider jwtTokenProvider,
            IUsuario usuarioRepository) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, usuarioRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir OPTIONS para todos os endpoints (necessário para CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Autenticação
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // Clientes
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("ADMIN")

                        // Produtos
                        .requestMatchers(HttpMethod.GET, "/api/produtos/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.POST, "/api/produtos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/produtos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasRole("ADMIN")

                        // Pedidos
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasRole("ADMIN")

                        // Usuários
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")

                        // Orçamentos

                        .requestMatchers(HttpMethod.POST, "/api/orcamentos/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET, "/api/orcamentos/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/orcamentos/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/orcamentos/**").hasRole("ADMIN")

                        // Recursos estáticos
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/home.html",
                                "/cadastro.html",
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Endpoints públicos para a aplicação móvel
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Qualquer outra requisição precisa de autenticação
                        .anyRequest().authenticated()
                )
                .authenticationProvider(customAuthenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "https://listmeapp.tech",
                "http://localhost:8080",
                "http://localhost:3000",
                "capacitor://localhost",
                "http://localhost",
                "http://localhost:8100"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}