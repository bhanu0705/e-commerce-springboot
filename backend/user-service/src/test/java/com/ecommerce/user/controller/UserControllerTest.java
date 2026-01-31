package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * User Controller Integration Tests
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .id(1L)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .city("New York")
                .country("USA")
                .build();
    }

    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Returns 200 and list of users")
        void getAllUsers_ReturnsUserList() throws Exception {
            when(userService.getAllUsers()).thenReturn(Arrays.asList(testUserDto));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].email", is("john@example.com")));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserByIdTests {

        @Test
        @DisplayName("Returns 200 and user when found")
        void getUserById_WhenExists_ReturnsUser() throws Exception {
            when(userService.getUserById(1L)).thenReturn(testUserDto);

            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", is("john@example.com")))
                    .andExpect(jsonPath("$.firstName", is("John")));
        }

        @Test
        @DisplayName("Returns 404 when user not found")
        void getUserById_WhenNotFound_Returns404() throws Exception {
            when(userService.getUserById(999L))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            mockMvc.perform(get("/api/users/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUserTests {

        @Test
        @DisplayName("Returns 201 when user created successfully")
        void createUser_WithValidData_Returns201() throws Exception {
            UserDto inputDto = UserDto.builder()
                    .email("new@example.com")
                    .firstName("New")
                    .lastName("User")
                    .build();

            when(userService.createUser(any(UserDto.class))).thenReturn(testUserDto);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Returns 400 when email is invalid")
        void createUser_WithInvalidEmail_Returns400() throws Exception {
            UserDto invalidDto = UserDto.builder()
                    .email("invalid-email")
                    .firstName("John")
                    .build();

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 400 when firstName is missing")
        void createUser_WithoutFirstName_Returns400() throws Exception {
            UserDto invalidDto = UserDto.builder()
                    .email("test@example.com")
                    .build();

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUserTests {

        @Test
        @DisplayName("Returns 200 when user updated successfully")
        void updateUser_WithValidData_Returns200() throws Exception {
            when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(testUserDto);

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserDto)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUserTests {

        @Test
        @DisplayName("Returns 204 when user deleted successfully")
        void deleteUser_WhenExists_Returns204() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isNoContent());
        }
    }
}
