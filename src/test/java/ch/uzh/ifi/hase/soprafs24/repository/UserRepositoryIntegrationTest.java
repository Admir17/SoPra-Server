package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;

  private User createValidUser() {
    User user = new User();
    user.setName("Test User");
    user.setUsername("testuser");
    user.setPassword("securePassword123");
    user.setStatus(UserStatus.OFFLINE);
    user.setToken(UUID.randomUUID().toString());
    user.setCreationDate(LocalDate.now());
    return user;
  }

  @Test
  public void findByName_returnsCorrectUser() {
    // Arrange
    User user = createValidUser();
    user.setName("Unique Name");
    entityManager.persistAndFlush(user);

    // Act
    User found = userRepository.findByName("Unique Name");

    // Assert
    assertNotNull(found.getId());
    assertEquals("Unique Name", found.getName());
    assertEquals(user.getUsername(), found.getUsername());
    assertEquals(user.getToken(), found.getToken());
    assertEquals(UserStatus.OFFLINE, found.getStatus());
    assertTrue(found.getCreationDate().isEqual(LocalDate.now()));
  }

  @Test
  public void findByUsername_returnsCorrectUser() {
    // Arrange
    User user = createValidUser();
    user.setUsername("unique_username");
    entityManager.persistAndFlush(user);

    // Act
    User found = userRepository.findByUsername("unique_username");

    // Assert
    assertNotNull(found);
    assertEquals(user.getId(), found.getId());
    assertEquals("unique_username", found.getUsername());
    assertEquals("Test User", found.getName());
  }

  @Test
  public void findByUsername_nonExistingUser_returnsNull() {
    // Act
    User found = userRepository.findByUsername("nonexistent");

    // Assert
    assertNull(found);
  }

  @Test
  public void findByUsername_isCaseSensitive() {
    // Arrange
    User user = createValidUser();
    user.setUsername("CaseSensitive");
    entityManager.persistAndFlush(user);

    // Act
    User found = userRepository.findByUsername("casesensitive");

    // Assert
    assertNull(found);
  }

  @Test
  public void createUser_persistsAllRequiredFields() {
    // Arrange
    User newUser = createValidUser();

    // Act
    User saved = userRepository.saveAndFlush(newUser);

    // Assert
    assertNotNull(saved.getId());
    assertEquals("testuser", saved.getUsername());
    assertEquals("Test User", saved.getName());
    assertEquals(UserStatus.OFFLINE, saved.getStatus());
    assertNotNull(saved.getToken());
    assertEquals(LocalDate.now(), saved.getCreationDate());
  }
}
