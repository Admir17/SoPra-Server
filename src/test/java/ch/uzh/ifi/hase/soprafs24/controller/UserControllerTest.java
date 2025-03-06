package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
        .andExpect(jsonPath("$.token", is(user.getToken())));
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize object", e);
    }
  }

  @Test
  public void createUser_duplicateUsername_throwsException() throws Exception {
    // Given
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("existingUser");
    userPostDTO.setPassword("password123");

    given(userService.createUser(any()))
        .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username exists"));

    // When/Then
    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO)))
        .andExpect(status().isConflict());
  }

  @Test
  public void createUser_emptyPassword_throwsBadRequest() throws Exception {
    // Given
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("validUser");
    userPostDTO.setPassword("");

    given(userService.createUser(any()))
        .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password must not be empty"));

    // When/Then
    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createUser_setsCreationDate() throws Exception {
    // Given
    User user = new User();
    user.setId(1L);
    user.setUsername("testUser");
    user.setCreationDate(LocalDate.now());

    given(userService.createUser(any())).willReturn(user);

    // When/Then
    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(new UserPostDTO())))
        .andExpect(jsonPath("$.creationDate").exists());
  }

  @Test
  void getUser_validId_returnsUser() throws Exception {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    when(userService.getUserById(1L)).thenReturn(user);

    // Act & Assert
    mockMvc.perform(get("/users/1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value("testuser"));
  }

  @Test
  void getUser_invalidId_returns404() throws Exception {
    // Arrange
    when(userService.getUserById(999L))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // Act & Assert
    mockMvc.perform(get("/users/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateUser_nonExistingUser_returns404() throws Exception {
    // Arrange
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(userService).updateUser(eq(999L), any());

    // Act & Assert
    mockMvc.perform(put("/users/999")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isNotFound());
  }

}