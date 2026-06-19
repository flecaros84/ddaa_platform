package com.ddaa.authservice.controller;

import com.ddaa.authservice.dto.CreateUserRequest;
import com.ddaa.authservice.model.User;
import com.ddaa.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ddaa.authservice.service.JwtService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints REST del servicio de autenticacion. El flujo OAuth completo se inicia fuera de Swagger.")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/test")
    @Operation(summary = "Verificar estado del servicio", description = "Endpoint publico para confirmar que auth-service responde.")
    @ApiResponse(responseCode = "200", description = "Servicio disponible")
    public Map<String, String> test() {
        return Map.of(
                "service", "auth-service",
                "status", "ok"
        );
    }

    @GetMapping("/login")
    @Operation(summary = "Informar endpoint de login", description = "Indica la ruta que debe abrirse en navegador para iniciar login con Google.")
    @ApiResponse(responseCode = "200", description = "Mensaje informativo")
    public Map<String, String> login() {
        return Map.of(
                "message", "Use /oauth2/authorization/google to sign in with Google"
        );
    }

    @GetMapping("/error")
    @Operation(summary = "Error de autenticacion", description = "Respuesta usada cuando falla la autenticacion o el dominio no esta autorizado.")
    @ApiResponse(responseCode = "200", description = "Mensaje de error de autenticacion")
    public Map<String, String> error() {
        return Map.of(
                "authenticated", "false",
                "message", "Authentication failed or unauthorized domain"
        );
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "sessionCookie")
    @Operation(summary = "Obtener usuario autenticado", description = "Devuelve informacion de la sesion actual si existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de autenticacion de la sesion"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = java.util.Map.class)))
    })
    public Map<String, Object> me(@Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (principal == null) {
            response.put("authenticated", false);
            return response;
        }

        String email = principal.getAttribute("email");
        Optional<User> userOpt = userService.findByEmail(email);

        response.put("authenticated", true);
        response.put("name", principal.getAttribute("name"));
        response.put("email", email);
        response.put("googleId", principal.getAttribute("sub"));
        response.put("domain", principal.getAttribute("hd"));

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            response.put("id", user.getId());
            response.put("role", user.getRole());
            response.put("active", user.getActive());
            response.put("lastLogin", user.getLastLogin());
            response.put("accessToken", jwtService.generateToken(user));
            response.put("tokenType", "Bearer");
        }

        return response;
    }

    @PostMapping("/users/test")
    @SecurityRequirement(name = "sessionCookie")
    @Operation(summary = "Crear usuario de prueba", description = "Endpoint auxiliar para crear usuarios internos durante desarrollo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario creado"),
            @ApiResponse(responseCode = "401", description = "Sesion requerida")
    })
    public User createTestUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/users")
    @SecurityRequirement(name = "sessionCookie")
    @Operation(summary = "Listar usuarios internos", description = "Entrega los usuarios persistidos por auth-service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de usuarios"),
            @ApiResponse(responseCode = "401", description = "Sesion requerida")
    })
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
}
