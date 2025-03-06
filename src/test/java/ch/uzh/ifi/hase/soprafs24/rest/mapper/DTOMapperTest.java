package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DTOMapperTest {

  private User createFullUser() {
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testuser");
    user.setStatus(UserStatus.ONLINE);
    user.setToken("security-token-123");
    user.setCreationDate(LocalDate.of(2023, 1, 1));
    user.setBirthDate(LocalDate.of(2000, 5, 15));
    return user;
  }

  @Test
  void convertUserPostDTOtoEntity_validInput_MapsAllFields() {
    // Arrange
    UserPostDTO dto = new UserPostDTO();
    dto.setUsername("newuser");
    dto.setPassword("securePass123");

    // Act
    User result = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(dto);

    // Assert
    assertEquals("newuser", result.getUsername());
    assertEquals("securePass123", result.getPassword());
  }

  @Test
  void convertEntityToUserGetDTO_fullUser_MapsAllFieldsWithCorrectFormat() {
    // Arrange
    User user = createFullUser();

    // Act
    UserGetDTO result = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    // Assert
    assertAll(
        () -> assertEquals(1L, result.getId()),
        () -> assertEquals("Test User", result.getName()),
        () -> assertEquals("testuser", result.getUsername()),
        () -> assertEquals(UserStatus.ONLINE, result.getStatus()),
        () -> assertEquals("security-token-123", result.getToken()),
        () -> assertEquals("2023-01-01", result.getCreationDate()),
        () -> assertEquals("2000-05-15", result.getBirthDate()));
  }

  @Test
  void convertEntityToUserGetDTO_nullBirthDate_MapsToNull() {
    // Arrange
    User user = createFullUser();
    user.setBirthDate(null);

    // Act
    UserGetDTO result = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    // Assert
    assertNull(result.getBirthDate());
  }

  @Test
  void convertUserPostDTOtoEntity_nullValues_MapsToNull() {
    // Arrange
    UserPostDTO dto = new UserPostDTO();

    // Act
    User result = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(dto);

    // Assert
    assertNull(result.getUsername());
    assertNull(result.getPassword());
  }
}
