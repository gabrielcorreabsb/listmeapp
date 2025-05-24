package com.listme.controller;
import com.listme.dto.LoginRequest;
import com.listme.dto.MessageResponse;
import com.listme.dto.UserDTO;
import com.listme.dto.UsuarioUpdateDTO;
import com.listme.model.Usuario;
import com.listme.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.listarUsuario();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> criarUsuario(@Valid @RequestBody Usuario usuario) {
        try {
            Usuario novoUsuario = usuarioService.criarUsuario(usuario);
            return ResponseEntity.status(201).body(novoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao criar usuário");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable Integer id,
                                           @Valid @RequestBody UsuarioUpdateDTO usuarioUpdateDTO) { // Mude para UsuarioUpdateDTO
        try {
            Usuario usuarioAtualizado = usuarioService.editarUsuario(id, usuarioUpdateDTO); // Passe o DTO para o serviço
            // Retorne um UserDTO para consistência com outras respostas, se desejar
            return ResponseEntity.ok(new UserDTO(usuarioAtualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage())); // Envie MessageResponse
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Erro ao atualizar usuário")); // Envie MessageResponse
        }
    }

    @DeleteMapping("/{idUsuario}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Integer idUsuario) {
        try {
            usuarioService.excluirUsuario(idUsuario);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao excluir usuário");
        }
    }

    @PostMapping("/verificar")
    public ResponseEntity<?> verificarUsuario(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Usuario usuario = usuarioService.verificarCredenciais(
                loginRequest.getLogin(), 
                loginRequest.getSenha()
            );
            
            if (usuario != null) {
                return ResponseEntity.ok(usuario);
            }
            return ResponseEntity.status(401).body("Login ou senha inválidos");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao verificar credenciais");
        }
    }
}