package com.listme.controller;

import com.listme.dto.LoginRequest;
import com.listme.dto.LoginResponse;
import com.listme.dto.MessageResponse;
import com.listme.dto.UserDTO;
import com.listme.model.Usuario;
import com.listme.repository.IUsuario;
import com.listme.securingweb.JwtTokenProvider;
import com.listme.service.EmailService;
import com.listme.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final IUsuario usuarioRepository;
    private final EmailService emailService;     // NOVO SERVIÇO
    private final UsuarioService usuarioService;


    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          IUsuario usuarioRepository,
                          UsuarioService usuarioService,
                          EmailService emailService) {

        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.emailService = emailService;

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String userEmail) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userEmail)
                    .orElse(null);

            if (usuario == null) {
                // Não revele se o email existe ou não por segurança.
                // Sempre retorne uma mensagem genérica de sucesso.
                return ResponseEntity.ok(new MessageResponse("Se um email correspondente for encontrado, um link de redefinição de senha será enviado."));
            }

            String token = UUID.randomUUID().toString();
            // Defina um tempo de expiração (ex: 1 hora)
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

            usuarioService.createPasswordResetTokenForUser(usuario, token, expiryDate);

            // Construa o link de reset
            // ATENÇÃO: O frontendUrl deve ser o URL da sua página/app que lida com o reset
            String frontendUrl = "https://listmeapp.tech/reset-password-page"; // Ou o deep link do seu app
            String resetLink = frontendUrl + "?token=" + token;

            emailService.sendPasswordResetEmail(usuario.getEmail(), usuario.getNome(), resetLink);

            return ResponseEntity.ok(new MessageResponse("Um email com instruções para redefinir sua senha foi enviado para " + userEmail + ". Verifique sua caixa de entrada e spam."));
        } catch (Exception e) {
            // Logar a exceção e.getMessage()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Erro ao processar a solicitação de redefinição de senha."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
                                           @RequestParam("password") String newPassword,
                                           @RequestParam("confirmPassword") String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(new MessageResponse("As senhas não coincidem."));
        }

        // Adicione validação de complexidade para newPassword aqui se desejar,
        // similar à da entidade Usuario, ou confie na validação do serviço.

        try {
            String result = usuarioService.validatePasswordResetToken(token);
            if (result != null) { // "invalidToken", "expiredToken"
                return ResponseEntity.badRequest().body(new MessageResponse(result));
            }

            Usuario usuario = usuarioService.getUserByPasswordResetToken(token)
                    .orElseThrow(() -> new RuntimeException("Token inválido ou usuário não encontrado."));

            // Validar a nova senha contra as regras da entidade (ex: @Pattern, @Size)
            // Idealmente, o UsuarioService teria um método para isso que também codifica.
            // Por simplicidade, vamos assumir que o serviço lida com a validação e codificação.
            usuarioService.changeUserPassword(usuario, newPassword);

            return ResponseEntity.ok(new MessageResponse("Sua senha foi redefinida com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getLogin(),
                            loginRequest.getSenha()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            Usuario usuario = usuarioRepository.findByLogin(loginRequest.getLogin())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            UserDTO userDTO = new UserDTO(usuario);
            LoginResponse response = new LoginResponse(jwt, "Bearer", userDTO);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Credenciais inválidas"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().body(new MessageResponse("Logout realizado com sucesso"));
    }
}