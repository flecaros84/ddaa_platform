package com.ddaa.authservice.controller;

import com.ddaa.authservice.dto.CreateUserRequest;
import com.ddaa.authservice.model.User;
import com.ddaa.authservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ddaa.authservice.service.JwtService;

/**
 * Pruebas unitarias de AuthController.
 *
 * Se valida el contrato básico de endpoints sin levantar Spring Security.
 * El objetivo es cubrir respuestas públicas, sesión actual y delegación al service.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController controller;

    @Test
    void testShouldReturnServiceStatus() {
        // Act: endpoint público de health simple del auth-service.
        Map<String, String> response = controller.test();

        // Assert: respuesta esperada para verificar disponibilidad.
        assertThat(response).containsEntry("service", "auth-service");
        assertThat(response).containsEntry("status", "ok");
    }

    @Test
    void loginShouldReturnGoogleLoginInstruction() {
        // Act: endpoint informativo de login.
        Map<String, String> response = controller.login();

        // Assert: informa la ruta OAuth2 usada por el frontend.
        assertThat(response.get("message")).contains("/oauth2/authorization/google");
    }

    @Test
    void errorShouldReturnAuthenticationFailureMessage() {
        // Act: endpoint usado cuando falla autenticación.
        Map<String, String> response = controller.error();

        // Assert: expone una respuesta simple y controlada.
        assertThat(response).containsEntry("authenticated", "false");
        assertThat(response.get("message")).contains("Authentication failed");
    }

    @Test
    void meShouldReturnAnonymousSessionWhenPrincipalIsNull() {
        // Act: no existe usuario autenticado.
        Map<String, Object> response = controller.me(null);

        // Assert: sesión anónima.
        assertThat(response).containsEntry("authenticated", false);
        assertThat(response).doesNotContainKeys("email", "role", "active");
    }

    @Test
    void meShouldReturnAuthenticatedSessionWithoutPersistedUserDataWhenEmailIsUnknown() {
        // Arrange: principal OAuth existe, pero no hay usuario persistido.
        OAuth2User principal = principal(
                "google-1",
                "Usuario Google",
                "usuario@camanchaca.cl",
                "camanchaca.cl"
        );

        when(userService.findByEmail("usuario@camanchaca.cl")).thenReturn(Optional.empty());

        // Act: se consulta la sesión.
        Map<String, Object> response = controller.me(principal);

        // Assert: se informa autenticación y datos básicos de Google.
        assertThat(response).containsEntry("authenticated", true);
        assertThat(response).containsEntry("name", "Usuario Google");
        assertThat(response).containsEntry("email", "usuario@camanchaca.cl");
        assertThat(response).containsEntry("googleId", "google-1");
        assertThat(response).containsEntry("domain", "camanchaca.cl");

        // No hay datos internos si el usuario no está persistido.
        assertThat(response).doesNotContainKeys("role", "active", "lastLogin");
    }

    @Test
    void meShouldReturnAuthenticatedSessionWithPersistedUserData() {
        // Arrange: principal OAuth y usuario interno existente.
        OAuth2User principal = principal(
                "google-1",
                "Usuario Google",
                "usuario@camanchaca.cl",
                "camanchaca.cl"
        );

        User user = user("google-1", "Usuario Google", "usuario@camanchaca.cl", "ADMIN", true);
        user.setLastLogin(LocalDateTime.of(2026, 6, 20, 10, 0));

        when(userService.findByEmail("usuario@camanchaca.cl")).thenReturn(Optional.of(user));

        // El controller genera un JWT cuando encuentra al usuario interno.
        // Se mockea para no depender de la clave real ni de la implementación criptográfica.
        when(jwtService.generateToken(user)).thenReturn("jwt-test-token");

        // Act: se consulta la sesión.
        Map<String, Object> response = controller.me(principal);

        // Assert: la respuesta mezcla datos Google, datos internos y token JWT.
        assertThat(response).containsEntry("authenticated", true);
        assertThat(response).containsEntry("email", "usuario@camanchaca.cl");
        assertThat(response).containsEntry("role", "ADMIN");
        assertThat(response).containsEntry("active", true);
        assertThat(response).containsEntry("lastLogin", LocalDateTime.of(2026, 6, 20, 10, 0));
        assertThat(response).containsEntry("accessToken", "jwt-test-token");
        assertThat(response).containsEntry("tokenType", "Bearer");
    }

    @Test
    void createTestUserShouldDelegateToUserService() {
        // Arrange: solicitud y usuario creado.
        CreateUserRequest request = new CreateUserRequest();
        request.setGoogleId("google-new");
        request.setName("Nuevo Usuario");
        request.setEmail("nuevo@test.cl");

        User created = user("google-new", "Nuevo Usuario", "nuevo@test.cl", "USER", true);
        when(userService.createUser(request)).thenReturn(created);

        // Act: el controller crea usuario de prueba.
        User response = controller.createTestUser(request);

        // Assert: retorna exactamente el usuario creado por el service.
        assertThat(response).isSameAs(created);
    }

    @Test
    void getUsersShouldReturnUsersFromService() {
        // Arrange: listado simulado.
        User first = user("google-1", "Uno", "uno@test.cl", "USER", true);
        User second = user("google-2", "Dos", "dos@test.cl", "ADMIN", true);

        when(userService.getAllUsers()).thenReturn(List.of(first, second));

        // Act: se listan usuarios.
        List<User> response = controller.getUsers();

        // Assert: el controller delega correctamente.
        assertThat(response).containsExactly(first, second);
    }

    /**
     * Mock simple de OAuth2User para probar /auth/me sin levantar seguridad.
     */
    private OAuth2User principal(String sub, String name, String email, String hd) {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("sub")).thenReturn(sub);
        when(principal.getAttribute("name")).thenReturn(name);
        when(principal.getAttribute("email")).thenReturn(email);
        when(principal.getAttribute("hd")).thenReturn(hd);
        return principal;
    }

    /**
     * Usuario interno reutilizable en pruebas del controller.
     */
    private User user(String googleId, String name, String email, String role, Boolean active) {
        User user = new User();
        user.setGoogleId(googleId);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(active);
        return user;
    }
}