package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.TaskDto;
import org.example.dto.UserDto;
import org.example.entity.*;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    private final Logger logger = LoggerFactory.getLogger(TaskControllerTest.class);

    private MockMvc mockMvc;

    private TaskRepository taskRepository;

    private UserRepository userRepository;

    private ObjectMapper objectMapper;

    @Autowired
    public TaskControllerTest(MockMvc mockMvc, TaskRepository taskRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private User author;
    private User assignee;
    private Task task;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        author = User.builder()
                .email("author@example.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        userRepository.save(author);
        logger.info("Author с ID: {} успешно создан", author.getId());

        assignee = User.builder()
                .email("assignee@example.com")
                .password("password")
                .role(Role.USER)
                .build();
        userRepository.save(assignee);
        logger.info("Assignee с ID: {} успешно создан", assignee.getId());


        task = Task.builder()
                .title("Task")
                .description("Test Description")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .author(author)
                .assignee(assignee)
                .build();
        taskRepository.save(task);
    }

    @Test
    @WithMockUser(username = "author@example.com", roles = {"USER"})
    void TestGetTasksByAuthor() throws Exception {
        mockMvc.perform(get("/api/tasks/author/{authorId}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is(task.getTitle())))
                .andExpect(jsonPath("$.content[0].author.email", is(author.getEmail())));
    }

    @Test
    @WithMockUser(username = "assignee@example.com", roles = {"USER"})
    void TestGetTasksByAssignee() throws Exception {
        mockMvc.perform(get("/api/tasks/assignee/{assigneeId}", assignee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].description").value(task.getDescription()))
                .andExpect(jsonPath("$.content[0].assignee.email").value(assignee.getEmail()));
    }

    @Test
    @WithMockUser(username = "author@example.com", roles = {"ADMIN"})
    void TestUpdateTask_AsAdmin() throws Exception {
        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setPriority(TaskPriority.HIGH);
        updateDto.setStatus(TaskStatus.IN_PROGRESS);
        updateDto.setAssignee(new UserDto().toUserDto(assignee));

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updateDto.getTitle())))
                .andExpect(jsonPath("$.description", is(updateDto.getDescription())))
                .andExpect(jsonPath("$.priority", is(updateDto.getPriority().toString())))
                .andExpect(jsonPath("$.status", is(updateDto.getStatus().toString())));
    }

    @Test
    @WithMockUser(username = "assignee@example.com", roles = {"USER"})
    void TestUpdateTaskStatus_AsAssignee() throws Exception {
        TaskDto updateDto = new TaskDto();
        updateDto.setStatus(TaskStatus.COMPLETED);

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(updateDto.getStatus().toString())));
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = {"USER"})
    void TestFailUpdateTask_AsUser() throws Exception {

        User otherUser = User.builder()
                .email("other@example.com")
                .password("password")
                .role(Role.USER)
                .build();
        userRepository.save(otherUser);
        logger.info("Assignee с ID: {} успешно создан", otherUser.getId());

        TaskDto updateDto = new TaskDto();
        updateDto.setStatus(TaskStatus.COMPLETED);

        mockMvc.perform(put("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}
