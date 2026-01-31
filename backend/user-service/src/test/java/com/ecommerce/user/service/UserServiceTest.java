package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * User Service Unit Tests
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .address("123 Main St")
                .city("New York")
                .postalCode("10001")
                .country("USA")
                .build();

        testUserDto = UserDto.builder()
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .address("123 Main St")
                .city("New York")
                .postalCode("10001")
                .country("USA")
                .build();
    }

    @Nested
    @DisplayName("Get All Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users")
        void getAllUsers_ReturnsAllUsers() {
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

            List<UserDto> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
        }
    }

    @Nested
    @DisplayName("Get User By ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found")
        void getUserById_WhenExists_ReturnsUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserDto result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getUserById_WhenNotFound_ThrowsException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_WithValidData_ReturnsCreatedUser() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserDto result = userService.createUser(testUserDto);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createUser_WithDuplicateEmail_ThrowsException() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(testUserDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already registered");
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void updateUser_WhenExists_ReturnsUpdatedUser() {
            UserDto updateDto = UserDto.builder()
                    .email("john@example.com")
                    .firstName("John Updated")
                    .lastName("Doe")
                    .build();

            User updatedUser = User.builder()
                    .id(1L)
                    .email("john@example.com")
                    .firstName("John Updated")
                    .lastName("Doe")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            UserDto result = userService.updateUser(1L, updateDto);

            assertThat(result.getFirstName()).isEqualTo("John Updated");
        }

        @Test
        @DisplayName("Should throw exception when changing to existing email")
        void updateUser_WithDuplicateEmail_ThrowsException() {
            UserDto updateDto = UserDto.builder()
                    .email("existing@example.com")
                    .firstName("John")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already in use");
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_WhenExists_DeletesSuccessfully() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            userService.deleteUser(1L);

            verify(userRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void deleteUser_WhenNotFound_ThrowsException() {
            when(userRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
