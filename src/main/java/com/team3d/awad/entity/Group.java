package com.team3d.awad.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Group {

    @Id
    private String uuid;

    private String name;

    @Builder.Default
    private String description = "";

    private String section;

    private String subject;

    @Builder.Default
    private String room = "";

    private String ownerId;

    @Builder.Default
    private List<String> coOwnerIds = new ArrayList<>();

    @Builder.Default
    private List<String> memberIds = new ArrayList<>();

    public Group update(Group clazz) {
        name = clazz.getName();
        description = clazz.getDescription();
        section = clazz.getSection();
        subject = clazz.getSubject();
        room = clazz.getRoom();
        return this;
    }

    public Group normalize() {
        if (coOwnerIds == null) {
            coOwnerIds = new ArrayList<>();
        }

        if (memberIds == null) {
            memberIds = new ArrayList<>();
        }

        return this;
    }

    public Group addMember(String memberId) {
        normalize();
        if (!this.memberIds.contains(memberId)) {
            this.memberIds.add(memberId);
        }
        return this;
    }

    public Group addMembers(List<String> memberIds) {
        normalize();
        this.memberIds = Stream.concat(this.memberIds.stream(), memberIds.stream())
                .distinct().collect(Collectors.toList());
        return this;
    }

    public Group removeMember(String memberId) {
        normalize();
        this.memberIds.remove(memberId);
        return this;
    }

    public Group updateMembers(List<String> memberIds) {
        normalize();
        this.memberIds = memberIds;
        return this;
    }

    public Group addCoOwners(List<String> coOwnerIds) {
        normalize();
        this.coOwnerIds = Stream.concat(this.coOwnerIds.stream(), coOwnerIds.stream())
                .distinct().collect(Collectors.toList());
        return this;
    }

    public Group removeCoOwner(String coOwnerId) {
        normalize();
        this.coOwnerIds.remove(coOwnerId);
        return this;
    }
}
