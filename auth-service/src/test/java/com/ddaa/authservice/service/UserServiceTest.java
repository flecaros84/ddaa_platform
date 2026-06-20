package com.ddaa.authservice.service;

import com.ddaa.authservice.dto.CreateUserRequest;
import com.ddaa.authservice.model.User;
import com.ddaa.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

/**
 * Pruebas unitarias de UserService.
 *
 * Se cubre la lógica principal de usuarios sin levantar Spring ni SQL Server.
 * El repositorio se reemplaza por Mockito para validar reglas de negocio.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserShouldUseDefaultRoleAndActiveWhenRequestDoesNotProvideThem() {
        // Arrange: solicitud mínima sin rol ni estado activo.
        CreateUserRequest request = new CreateUserRequest();
        request.setGoogleId("google-1");
        request.setName("Usuario Test");
        request.setEmail("usuario@test.cl");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: se crea el usuario.
        User result = userService.createUser(request);

        // Assert: el servicio aplica valores por defecto seguros.
        assertThat(result.getGoogleId()).isEqualTo("google-1");
        assertThat(result.getName()).isEqualTo("Usuario Test");
        assertThat(result.getEmail()).isEqualTo("usuario@test.cl");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getLastLogin()).isNull();
    }

    @Test
    void createUserShouldRespectExplicitRoleAndActiveValue() {
        // Arrange: solicitud con rol y estado definidos explícitamente.
        CreateUserRequest request = new CreateUserRequest();
        request.setGoogleId("google-admin");
        request.setName("Admin Test");
        request.setEmail("admin@test.cl");
        request.setRole("ADMIN");
        request.setActive(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: se crea el usuario.
        User result = userService.createUser(request);

        // Assert: los valores explícitos no son reemplazados por defaults.
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(result.getActive()).isFalse();
    }

    @Test
    void getAllUsersShouldDelegateToRepository() {
        // Arrange: repositorio con dos usuarios simulados.
        User first = user("google-1", "Uno", "uno@test.cl", "USER", true);
        User second = user("google-2", "Dos", "dos@test.cl", "ADMIN", true);

        when(userRepository.findAll()).thenReturn(List.of(first, second));

        // Act: se consulta el listado.
        List<User> result = userService.getAllUsers();

        // Assert: el servicio retorna exactamente lo entregado por el repositorio.
        assertThat(result).containsExactly(first, second);
        verify(userRepository).findAll();
    }

    @Test
    void findByEmailShouldDelegateToRepository() {
        // Arrange: usuario existente por correo.
        User existing = user("google-1", "Usuario Test", "usuario@test.cl", "USER", true);
        when(userRepository.findByEmail("usuario@test.cl")).thenReturn(Optional.of(existing));

        // Act: se busca por email.
        Optional<User> result = userService.findByEmail("usuario@test.cl");

        // Assert: se retorna el usuario encontrado.
        assertThat(result).contains(existing);
        verify(userRepository).findByEmail("usuario@test.cl");
    }

    @Test
    void createOrUpdateGoogleUserShouldCreateNewUserWhenEmailDoesNotExist() {
        // Arrange: no existe usuario para el correo recibido desde Google.
        when(userRepository.findByEmail("nuevo@camanchaca.cl")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: se crea usuario desde datos Google.
        User result = userService.createOrUpdateGoogleUser(
                "google-new",
                "Nuevo Usuario",
                "nuevo@camanchaca.cl"
        );

        // Assert: el nuevo usuario queda activo, con rol USER y fechas inicializadas.
        assertThat(result.getGoogleId()).isEqualTo("google-new");
        assertThat(result.getName()).isEqualTo("Nuevo Usuario");
        assertThat(result.getEmail()).isEqualTo("nuevo@camanchaca.cl");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getLastLogin()).isNotNull();
    }

    @Test
    void createOrUpdateGoogleUserShouldUpdateExistingActiveUser() {
        // Arrange: existe usuario activo con el mismo correo.
        User existing = user("old-google", "Nombre Antiguo", "usuario@camanchaca.cl", "ADMIN", true);

        when(userRepository.findByEmail("usuario@camanchaca.cl")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: se actualizan datos provenientes de Google.
        User result = userService.createOrUpdateGoogleUser(
                "new-google",
                "Nombre Nuevo",
                "usuario@camanchaca.cl"
        );

        // Assert: conserva email/rol/activo, pero actualiza googleId, nombre y último login.
        assertThat(result).isSameAs(existing);
        assertThat(result.getGoogleId()).isEqualTo("new-google");
        assertThat(result.getName()).isEqualTo("Nombre Nuevo");
        assertThat(result.getEmail()).isEqualTo("usuario@camanchaca.cl");
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(result.getActive()).isTrue();
        assertThat(result.getLastLogin()).isNotNull();
    }

    @Test
    void createOrUpdateGoogleUserShouldRejectInactiveExistingUser() {
        // Arrange: el usuario existe, pero está inactivo.
        User inactive = user("google-1", "Usuario Inactivo", "inactivo@camanchaca.cl", "USER", false);
        when(userRepository.findByEmail("inactivo@camanchaca.cl")).thenReturn(Optional.of(inactive));

        // Act & Assert: un usuario inactivo no puede autenticarse.
        assertThatThrownBy(() -> userService.createOrUpdateGoogleUser(
                "google-2",
                "Usuario Inactivo",
                "inactivo@camanchaca.cl"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User is inactive");

        // No se guarda nada si el usuario está bloqueado.
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldSendExpectedEntityToRepository() {
        // Arrange: solicitud válida para crear usuario interno.
        CreateUserRequest request = new CreateUserRequest();
        request.setGoogleId("google-captor");
        request.setName("Captor Test");
        request.setEmail("captor@test.cl");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: se crea el usuario.
        userService.createUser(request);

        // Assert: se captura la entidad enviada al repositorio.
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getGoogleId()).isEqualTo("google-captor");
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.getActive()).isTrue();
    }

    /**
     * Construye usuarios de prueba sin depender de base de datos.
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