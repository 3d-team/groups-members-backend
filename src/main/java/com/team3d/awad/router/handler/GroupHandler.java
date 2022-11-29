package com.team3d.awad.router.handler;

import com.team3d.awad.entity.Group;
import com.team3d.awad.repository.GroupRepository;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.utils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GroupHandler {

    private static final Logger LOGGER = LogManager.getLogger(GroupHandler.class);

    private final GroupRepository groupRepository;

    private final TokenProvider tokenProvider;


    public GroupHandler(GroupRepository groupRepository, TokenProvider tokenProvider) {
        this.groupRepository = groupRepository;
        this.tokenProvider = tokenProvider;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        LOGGER.info("API Get all groups");
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        return ServerResponse.ok().body(groupRepository.findAllByOwnerId(userId), Group.class);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        LOGGER.info("Hit API createGroup endpoint.");

        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        return request.bodyToMono(Group.class)
                .flatMap(group -> {
                    group.setOwnerId(userId);
                    return Mono.just(group);
                })
                .flatMap(groupRepository::save)
                .flatMap(group -> ServerResponse.created(URI.create("/groups/" + group.getUuid())).build());
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return groupRepository.findById(request.pathVariable("id"))
                .flatMap(group -> ServerResponse.ok().body(Mono.just(group), Group.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        return Mono
                .zip(
                        (data) -> {
                            Group Class = (Group) data[0];
                            Group input = (Group) data[1];
                            Class.update(input);
                            return Class;
                        },
                        groupRepository.findById(request.pathVariable("id")),
                        request.bodyToMono(Group.class)
                )
                .cast(Group.class)
                .flatMap(groupRepository::save)
                .flatMap(Group -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return ServerResponse.noContent()
                .build(groupRepository.deleteById(request.pathVariable("id")));
    }

    public Mono<ServerResponse> addMember(ServerRequest request) {
        LOGGER.info("Hit API addMember");

        return request.bodyToMono(String[].class)
                .flatMap(memberIds -> {
                            List<String> members = Stream.of(memberIds).distinct()
                                    .collect(Collectors.toList());
                            return groupRepository.findById(request.pathVariable("id"))
                                    .switchIfEmpty(Mono.error(new Exception("No group found.")))
                                    .flatMap(group -> Mono.just(group.addMembers(members)))
                                    .flatMap(groupRepository::save);
                        }
                )
                .flatMap(x -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> removeMember(ServerRequest request) {
        LOGGER.info("Hit API removeMember");

        return groupRepository.findById(request.pathVariable("id"))
                .flatMap(group -> {
                    String memberId = request.pathVariable("memberId");
                    return groupRepository.save(group.removeMember(memberId));
                })
                .flatMap(group -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> addCoOwner(ServerRequest request) {
        LOGGER.info("Hit API addCoOwner");

        return request.bodyToMono(String[].class)
                .flatMap(userIds -> {
                            List<String> coOwners = Stream.of(userIds).distinct()
                                    .collect(Collectors.toList());
                            return groupRepository.findById(request.pathVariable("id"))
                                    .switchIfEmpty(Mono.error(new Exception("No group found.")))
                                    .flatMap(group -> Mono.just(group.addCoOwners(coOwners)))
                                    .flatMap(groupRepository::save);
                        }
                )
                .flatMap(x -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> removeCoOwner(ServerRequest request) {
        LOGGER.info("Hit API removeCoOwner");

        return groupRepository.findById(request.pathVariable("id"))
                .flatMap(group -> {
                    String coOwnerId = request.pathVariable("coOwnerId");
                    return groupRepository.save(group.removeCoOwner(coOwnerId));
                })
                .flatMap(group -> ServerResponse.noContent().build());
    }
}
