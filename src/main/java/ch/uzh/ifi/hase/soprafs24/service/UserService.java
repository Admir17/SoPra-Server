package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = new BCryptPasswordEncoder();

  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {

    // validating not empty
    if (newUser.getUsername() == null || newUser.getUsername().trim().isEmpty() ||
        newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username und Passwort dürfen nicht leer sein");
    }

    newUser.setName(newUser.getUsername());
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setCreationDate(LocalDate.now());

    checkIfUserExists(newUser);

    newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          String.format(baseErrorMessage, "username and the name", "are"));
    } 
  }

  public User loginUser(String username, String password) {
    User user = userRepository.findByUsername(username);
    if (user != null && checkPassword(password, user.getPassword())) {
      user.setStatus(UserStatus.ONLINE);
      return userRepository.save(user);
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

  public void logoutUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    user.setStatus(UserStatus.OFFLINE);
    userRepository.save(user);
  }

  public User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  public void updateUserBirthDate(Long userId, LocalDate birthDate) {
    User user = getUserById(userId);
    if (birthDate != null) {
      user.setBirthDate(birthDate);
      userRepository.save(user);
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Birth date cannot be null");
    }
  }

  @Transactional
  public void updateUser(Long userId, UserPutDTO userPutDTO) {
    User user = getUserById(userId);

    if (userPutDTO.getUsername() != null && !userPutDTO.getUsername().isEmpty()) {
      user.setUsername(userPutDTO.getUsername());
    }

    if (userPutDTO.getBirthDate() != null) {
      user.setBirthDate(userPutDTO.getBirthDate());
    }

    userRepository.save(user);
  }

  // helper method to check hashed passwords
  private boolean checkPassword(String inputPassword, String storedPassword) {
    return passwordEncoder.matches(inputPassword, storedPassword);
  }

}
