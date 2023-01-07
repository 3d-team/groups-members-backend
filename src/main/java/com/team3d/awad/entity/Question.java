package com.team3d.awad.entity;

import com.team3d.awad.payload.CreateAnswerRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("question")
public class Question {

    @Id
    private String uuid;

    private String title;

    private String content;

    @Builder.Default
    private QuestionStatus status = QuestionStatus.NEW;

    @Builder.Default
    private List<String> voterIds = new ArrayList<>();

    @Builder.Default
    private List<Answer> answers = new ArrayList<>();

    private String presentationId;

    public Mono<Question> upvote(String voterId) {
        voterIds.add(voterId);
        return Mono.just(this);
    }

    public Mono<Question> answer(String questionId, String userId, CreateAnswerRequest payload) {
        Answer answer = Answer.builder()
                .answererId(userId)
                .answerer(payload.getAnswerer())
                .content(payload.getContent())
                .questionId(questionId)
                .build();
        answers.add(answer);
        return Mono.just(this);
    }

    public Mono<Question> markAnswered() {
        status = QuestionStatus.ANSWERED;
        return Mono.just(this);
    }

    public enum QuestionStatus {
        NEW,
        ANSWERED
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Answer {

        @Builder.Default
        private String uuid = UUID.randomUUID().toString();

        private String answererId;

        private String answerer;

        private String content;

        private String questionId;
    }
}
