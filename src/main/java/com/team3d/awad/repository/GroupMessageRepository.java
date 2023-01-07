package com.team3d.awad.repository;

import com.team3d.awad.entity.Message;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface GroupMessageRepository extends ReactiveMongoRepository<Message, String> {

    Flux<Message> findAllByPresentationIdOrderByCreatedDateDesc(String presentationId);
}
