package com.team3d.awad.repository;

import com.team3d.awad.entity.Question;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface QuestionRepository extends ReactiveMongoRepository<Question, String> {

    Flux<Question> findAllByPresentationId(String presentationId);
}
