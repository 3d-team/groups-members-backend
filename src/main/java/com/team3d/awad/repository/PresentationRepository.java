package com.team3d.awad.repository;

import com.team3d.awad.entity.Presentation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PresentationRepository extends ReactiveMongoRepository<Presentation, String> {

    Flux<Presentation> findAllByHostId(String hostId);
}
