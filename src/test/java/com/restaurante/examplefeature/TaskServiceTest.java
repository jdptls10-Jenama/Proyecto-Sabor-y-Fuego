
package com.restaurante.examplefeature;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    void tasks_are_stored_in_the_database_with_the_current_timestamp() {

        Instant ahora = Instant.now();
        String descripcion = "Do this";
        LocalDate fechaEntrega = LocalDate.of(2025, 2, 7);

        taskService.createTask(descripcion, fechaEntrega);

        var tarea = taskService.list(PageRequest.ofSize(1)).get(0);

        assertThat(tarea.getDescription()).isEqualTo(descripcion);
        assertThat(tarea.getDueDate()).isEqualTo(fechaEntrega);
        assertThat(tarea.getCreationDate()).isAfterOrEqualTo(ahora);
    }

    @Test
    void tasks_are_validated_before_they_are_stored() {

        assertThatThrownBy(() ->
                taskService.createTask(
                        "X".repeat(Task.DESCRIPTION_MAX_LENGTH + 1),
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
