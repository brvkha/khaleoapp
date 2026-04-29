package com.khaleo.flashcard.controller.admin.listening;
import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import com.khaleo.flashcard.service.listening.AdminListeningCrudService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin/listening/topics")
@RequiredArgsConstructor
public class AdminTopicController {
    private final AdminListeningCrudService crudService;
    @GetMapping
    public List<ListeningDtos.TopicDto> listTopics() {
        return crudService.listTopics();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningDtos.TopicDto createTopic(@Valid @RequestBody ListeningDtos.TopicDto request) {
        return crudService.saveTopic(null, request);
    }
    @GetMapping("/{topicId}")
    public ListeningDtos.TopicDto getTopic(@PathVariable UUID topicId) {
        return crudService.getTopic(topicId);
    }
    @PutMapping("/{topicId}")
    public ListeningDtos.TopicDto updateTopic(@PathVariable UUID topicId, @Valid @RequestBody ListeningDtos.TopicDto request) {
        return crudService.saveTopic(topicId, request);
    }
    @DeleteMapping("/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(@PathVariable UUID topicId) {
        crudService.deleteTopic(topicId);
    }
}
