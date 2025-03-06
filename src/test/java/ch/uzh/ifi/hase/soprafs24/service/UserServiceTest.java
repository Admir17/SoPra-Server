package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    testUser = new User();
    testUser.setId(1L);
    testUser.setName("Test User");
    testUser.setUsername("testuser");
    testUser.setPassword("testPassword");

    when(userRepository.save(any())).thenReturn(testUser);
  }

  @Test
  void createUser_validInput_createsUserWithHashedPassword() {
    // Act
    User createdUser = userService.createUser(testUser);

    // Assert
    verify(userRepository, times(1)).save(any());
    verify(userRepository, times(1)).flush();

    assertAll(
        () -> assertEquals(testUser.getId(), createdUser.getId()),
        () -> assertEquals("testuser", createdUser.getUsername()),
        () -> assertEquals(UserStatus.ONLINE, createdUser.getStatus()),
        () -> assertNotNull(createdUser.getToken()),
        () -> assertEquals(LocalDate.now(), createdUser.getCreationDate()),
        () -> assertTrue(new BCryptPasswordEncoder().matches("testPassword", createdUser.getPassword())));
  }

  @Test
  void createUser_duplicateUsername_throwsConflictException() {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(testUser);

    // Act & Assert
    assertThrows(ResponseStatusException.class,
        () -> userService.createUser(testUser),
        "Expected conflict exception for duplicate username");

    verify(userRepository, never()).save(testUser);
  }

  @Test
  void createUser_emptyPassword_throwsBadRequest() {
    // Arrange
    testUser.setPassword("");

    // Act & Assert
    assertThrows(ResponseStatusException.class,
        () -> userService.createUser(testUser),
        "Should reject empty password");
  }

  @Test
  void createUser_autoGeneratesToken() {
    // Act
    User createdUser = userService.createUser(testUser);

    // Assert
    assertNotNull(createdUser.getToken());
    assertEquals(36, createdUser.getToken().length()); // UUID length check
  }

  @Test
  void createUser_setsCorrectInitialStatus() {
    // Act
    User createdUser = userService.createUser(testUser);

    // Assert
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }
}
