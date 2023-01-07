package com.team3d.awad.router.handler;

import com.team3d.awad.entity.Question;
import com.team3d.awad.payload.CreateAnswerRequest;
import com.team3d.awad.payload.CreateQuestionRequest;
import com.team3d.awad.repository.QuestionRepository;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.utils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class QuestionHandler {

    private static final Logger LOGGER = LogManager.getLogger(QuestionHandler.class);

    private final QuestionRepository questionRepository;

    private final TokenProvider tokenProvider;

    public QuestionHandler(QuestionRepository questionRepository, TokenProvider tokenProvider) {
        this.questionRepository = questionRepository;
        this.tokenProvider = tokenProvider;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        return ServerResponse.ok().body(questionRepository
                .findAllByPresentationId(request.pathVariable("id")), Question.class);
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return questionRepository.findById(request.pathVariable("id"))
                .flatMap(question -> ServerResponse.ok().body(Mono.just(question), Question.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateQuestionRequest.class)
                .flatMap(payload -> {
                    Question question = Question.builder()
                            .title(payload.getTitle())
                            .content(payload.getContent())
                            .presentationId(payload.getPresentationId())
                            .build();
                    return questionRepository.save(question);
                })
                .flatMap(question -> ServerResponse.ok().body(Mono.just(question), Question.class));
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        String actionQuery = request.queryParam("action").orElse(null);
        if (actionQuery == null) {
            return ServerResponse.badRequest().build();
        }

        QuestionAction action = QuestionAction.valueOf(actionQuery);
        switch (action) {
            case ANSWER:
                return answer(request);
            case UPVOTE:
                return upvote(request);
            case MARK_ANSWERED:
                return markAnswered(request);
            default:
                break;
        }
        return ServerResponse.badRequest().build();
    }

    private Mono<ServerResponse> upvote(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);

        return questionRepository.findById(request.pathVariable("id"))
                .flatMap(question -> question.upvote(userId))
                .flatMap(questionRepository::save)
                .flatMap(question -> ServerResponse.ok().body(question, Question.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> answer(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        return request.bodyToMono(CreateAnswerRequest.class)
                .flatMap(payload -> {
                    String questionId = request.pathVariable("id");
                    return questionRepository.findById(questionId)
                            .flatMap(question -> question.answer(questionId, userId, payload))
                            .flatMap(questionRepository::save)
                            .switchIfEmpty(Mono.empty());
                })
                .flatMap(question -> ServerResponse.ok().body(Mono.just(question), Question.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> markAnswered(ServerRequest request) {
        return questionRepository.findById(request.pathVariable("id"))
                .flatMap(Question::markAnswered)
                .flatMap(questionRepository::save)
                .flatMap(question -> ServerResponse.ok().body(Mono.just(question), Question.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> allAnswers(ServerRequest request) {
        return questionRepository.findById(request.pathVariable("id"))
                .flatMap(question -> ServerResponse.ok().body(
                        Flux.just(question.getAnswers()),
                        Question.Answer.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return questionRepository.deleteById(request.pathVariable("id"))
                .then(ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public enum QuestionAction {
        UPVOTE,
        ANSWER,
        MARK_ANSWERED
    }
}
