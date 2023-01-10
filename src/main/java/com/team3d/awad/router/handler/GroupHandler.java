package com.team3d.awad.router.handler;

import com.team3d.awad.entity.Group;
import com.team3d.awad.entity.User;
import com.team3d.awad.repository.GroupRepository;
import com.team3d.awad.repository.UserRepository;
import com.team3d.awad.security.TokenProvider;
import com.team3d.awad.utils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GroupHandler {

    private static final Logger LOGGER = LogManager.getLogger(GroupHandler.class);

    private final GroupRepository groupRepository;

    private final TokenProvider tokenProvider;

    private final UserRepository userRepository;


    public GroupHandler(GroupRepository groupRepository, TokenProvider tokenProvider, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        LOGGER.info("[*] Hit API #Get All Groups, of user ID: {}", userId);
        List<String> coOwnerIds = Collections.singletonList(userId);
        List<String> memberIds = Collections.singletonList(userId);
        return ServerResponse.ok().body(groupRepository
                .findAllByOwnerIdOrCoOwnerIdsInOrMemberIdsIn(userId, coOwnerIds, memberIds), Group.class);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        String userId = tokenProvider.getUserIdFromToken(JWT);
        LOGGER.info("[*] Hit API #Create Group, with Owner ID: {}", userId);

        return request.bodyToMono(Group.class)
                .flatMap(group -> {
                    group.setOwnerId(userId);
                    return Mono.just(group);
                })
                .flatMap(group -> Mono.just(group.normalize()))
                .flatMap(groupRepository::save)
                .flatMap(group -> ServerResponse.ok().body(Mono.just(group.getUuid()), String.class));
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return groupRepository.findById(request.pathVariable("id"))
                .flatMap(group -> ServerResponse.ok().body(Mono.just(group), Group.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        LOGGER.info("[*] Hit API #Update Group, of Group ID: {}",
                request.pathVariable("id"));
        return Mono
                .zip(
                        (data) -> {
                            Group group = (Group) data[0];
                            Group input = (Group) data[1];
                            group.update(input);
                            return group;
                        },
                        groupRepository.findById(request.pathVariable("id")),
                        request.bodyToMono(Group.class)
                )
                .cast(Group.class)
                .flatMap(groupRepository::save)
                .flatMap(group -> ServerResponse.ok().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return ServerResponse.noContent()
                .build(groupRepository.deleteById(request.pathVariable("id")));
    }

    public Mono<ServerResponse> allMembers(ServerRequest request) {
        return groupRepository.findById(request.pathVariable("id"))
                .flatMap(group -> {
                    Flux<User> members = userRepository.findAllByUuidIn(group.getMemberIds());
                    return ServerResponse.ok().body(members, User.class);
                })
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> addMembers(ServerRequest request) {
        LOGGER.info("[*] Hit API #Add Member, for Group ID: {}",
                request.pathVariable("id"));

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
                .flatMap(group -> ServerResponse.ok().body(Mono.just(group.getUuid()), String.class));
    }

    public Mono<ServerResponse> join(ServerRequest request) {
        final String JWT = RequestUtils.getJwtFromRequest(request);
        if (JWT == null) {
            return Mono.error(new Exception("Not found JWT"));
        }
        String userId = tokenProvider.getUserIdFromToken(JWT);
        LOGGER.info("[*] Hit API #Join Group, with user ID: {}", userId);

        return groupRepository.findById(request.pathVariable("id"))
                .switchIfEmpty(Mono.error(new Exception("No group found.")))
                .flatMap(group -> Mono.just(group.addMember(userId)))
                .flatMap(groupRepository::save)
                .flatMap(group -> ServerResponse
                        .created(URI.create("http://localhost:3000/classes/" + group.getUuid()))
                        .build());

    }

    public Mono<ServerResponse> removeMember(ServerRequest request) {
        return groupRepository.findById(request.pathVariable("id"))
                .flatMap(group -> {
                    String memberId = request.pathVariable("memberId");
                    LOGGER.info("[*] Hit API #Remove Member, with User ID: {}",
                            memberId);
                    return groupRepository.save(group.removeMember(memberId));
                })
                .flatMap(group -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> allCoOwners(ServerRequest request) {
        return groupRepository.findById(request.pathVariable("id"))
                .flatMap(group -> {
                    Flux<User> members = userRepository.findAllByUuidIn(group.getCoOwnerIds());
                    return ServerResponse.ok().body(members, User.class);
                })
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> addCoOwner(ServerRequest request) {
        LOGGER.info("[*] Hit API #Add CoOwner, for Group ID: {}",
                request.pathVariable("id"));

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
        return groupRepository.findById(request.pathVariable("id"))
                .flatMap(group -> {
                    String coOwnerId = request.pathVariable("coOwnerId");
                    LOGGER.info("[*] Hit API #Remove Co-Owner, of Group ID: {}, with User ID: {}",
                            request.pathVariable("id"), coOwnerId);
                    return groupRepository.save(group.removeCoOwner(coOwnerId));
                })
                .flatMap(group -> ServerResponse.noContent().build());
    }
}
