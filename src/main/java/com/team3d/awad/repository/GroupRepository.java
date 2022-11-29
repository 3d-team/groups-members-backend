package com.team3d.awad.repository;

import com.team3d.awad.entity.Group;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface GroupRepository extends ReactiveMongoRepository<Group, String> {

    Flux<Group> findAllByOwnerId(String ownerId);
}
