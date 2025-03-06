package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class UserServiceIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  void setup() {
    userRepository.deleteAll();
  }

  private User createValidUser() {
    User user = new User();
    user.setName("Test User");
    user.setUsername("testuser");
    user.setPassword("securePassword123");
    return user;
  }

  @Test
  void createUser_duplicateUsername_throwsConflictException() {
    // Arrange
    User firstUser = createValidUser();
    userService.createUser(firstUser);

    User secondUser = createValidUser();
    secondUser.setUsername("testuser");
    secondUser.setPassword("differentPassword");

    // Act & Assert
    assertThrows(ResponseStatusException.class,
        () -> userService.createUser(secondUser),
        "Should throw conflict for duplicate username");
  }

  @Test
  void createUser_emptyPassword_throwsBadRequest() {
    // Arrange
    User invalidUser = createValidUser();
    invalidUser.setPassword("");

    // Act & Assert
    assertThrows(ResponseStatusException.class,
        () -> userService.createUser(invalidUser),
        "Should reject empty password");
  }

  @Test
  void createUser_passwordIsHashed() {
    // Arrange
    User newUser = createValidUser();
    String rawPassword = newUser.getPassword();

    // Act
    User createdUser = userService.createUser(newUser);

    // Assert
    assertNotEquals(rawPassword, createdUser.getPassword());
    assertTrue(createdUser.getPassword().startsWith("$2a$10$"));
  }

  @Test
  void createUser_creationDateIsToday() {
    // Arrange
    User newUser = createValidUser();

    // Act
    User createdUser = userService.createUser(newUser);

    // Assert
    assertEquals(LocalDate.now(), createdUser.getCreationDate());
  }
}
