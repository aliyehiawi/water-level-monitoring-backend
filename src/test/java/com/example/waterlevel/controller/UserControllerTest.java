package com.example.waterlevel.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.waterlevel.entity.Role;
import com.example.waterlevel.entity.User;
import com.example.waterlevel.repository.UserRepository;
import com.example.waterlevel.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private UserRepository userRepository;
  @MockBean private AuditService auditService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void getAllUsers_Success() throws Exception {
    User user1 = new User();
    user1.setId(1L);
    user1.setUsername("user1");
    user1.setEmail("user1@example.com");
    user1.setRole(Role.USER);
    user1.setCreatedAt(LocalDateTime.now());

    User user2 = new User();
    user2.setId(2L);
    user2.setUsername("user2");
    user2.setEmail("user2@example.com");
    user2.setRole(Role.ADMIN);
    user2.setCreatedAt(LocalDateTime.now());

    List<User> users = Arrays.asList(user1, user2);
    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 20), 2);
    when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

    mockMvc
        .perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].username").value("user1"))
        .andExpect(jsonPath("$.content[1].username").value("user2"));
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void promoteUser_Success() throws Exception {
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    admin.setEmail("admin@example.com");
    admin.setRole(Role.ADMIN);

    User user = new User();
    user.setId(2L);
    user.setUsername("user");
    user.setEmail("user@example.com");
    user.setRole(Role.USER);
    user.setCreatedAt(LocalDateTime.now());

    User promotedUser = new User();
    promotedUser.setId(2L);
    promotedUser.setUsername("user");
    promotedUser.setEmail("user@example.com");
    promotedUser.setRole(Role.ADMIN);
    promotedUser.setCreatedAt(LocalDateTime.now());

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(promotedUser);

    mockMvc
        .perform(put("/users/2/promote"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("ADMIN"));

    verify(userRepository).save(any(User.class));
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void deleteUser_Success() throws Exception {
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    admin.setEmail("admin@example.com");
    admin.setRole(Role.ADMIN);

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(userRepository.existsById(2L)).thenReturn(true);
    doNothing().when(userRepository).deleteById(2L);

    mockMvc.perform(delete("/users/2")).andExpect(status().isNoContent());

    verify(userRepository).deleteById(2L);
  }

  @Test
  @WithMockUser(roles = "ADMIN", username = "admin")
  void deleteUser_NotFound_ReturnsNotFound() throws Exception {
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin");
    admin.setEmail("admin@example.com");
    admin.setRole(Role.ADMIN);

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(userRepository.existsById(2L)).thenReturn(false);

    mockMvc.perform(delete("/users/2")).andExpect(status().isBadRequest());

    verify(userRepository, never()).deleteById(any());
  }
}
