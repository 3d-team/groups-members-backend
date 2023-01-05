package com.team3d.awad.repository;

import com.team3d.awad.entity.GroupMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface GroupMessageRepository extends ReactiveMongoRepository<GroupMessage, String> {

    Flux<GroupMessage> findAllByGroupIdOrderByCreatedTimeDesc(String groupId);
}
