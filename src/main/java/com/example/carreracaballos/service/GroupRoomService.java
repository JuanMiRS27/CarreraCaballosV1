package com.example.carreracaballos.service;

import com.example.carreracaballos.api.GroupSummaryResponse;
import com.example.carreracaballos.model.GroupMembership;
import com.example.carreracaballos.model.GroupRoom;
import com.example.carreracaballos.model.GroupStatus;
import com.example.carreracaballos.model.UserAccount;
import com.example.carreracaballos.repository.GroupMembershipRepository;
import com.example.carreracaballos.repository.GroupRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class GroupRoomService {
    public static final int MAX_GROUPS = 4;
    public static final int MAX_USERS_PER_GROUP = 4;

    private final GroupRoomRepository groupRoomRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public GroupRoomService(GroupRoomRepository groupRoomRepository, GroupMembershipRepository groupMembershipRepository) {
        this.groupRoomRepository = groupRoomRepository;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Transactional
    public GroupSummaryResponse assignUser(UserAccount user) {
        Optional<GroupMembership> existingMembership = groupMembershipRepository.findByUserId(user.getId());
        if (existingMembership.isPresent()) {
            return toSummary(existingMembership.get().getGroup());
        }

        List<GroupRoom> activeGroups = groupRoomRepository.findByStatusOrderByCreatedAtAsc(GroupStatus.ACTIVE);
        for (GroupRoom activeGroup : activeGroups) {
            if (groupMembershipRepository.countByGroupId(activeGroup.getId()) < MAX_USERS_PER_GROUP) {
                groupMembershipRepository.save(new GroupMembership(user, activeGroup, Instant.now()));
                return toSummary(activeGroup);
            }
        }

        if (activeGroups.size() >= MAX_GROUPS) {
            throw new IllegalArgumentException("La plataforma ya tiene 4 grupos activos de 4 usuarios. No hay cupos disponibles.");
        }

        GroupRoom newGroup = groupRoomRepository.save(new GroupRoom("GRUPO-" + (activeGroups.size() + 1), GroupStatus.ACTIVE, Instant.now()));
        groupMembershipRepository.save(new GroupMembership(user, newGroup, Instant.now()));
        return toSummary(newGroup);
    }

    @Transactional(readOnly = true)
    public GroupRoom getRequiredGroupForUser(Long userId) {
        GroupMembership membership = groupMembershipRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no pertenece a ningun grupo."));
        return membership.getGroup();
    }

    @Transactional(readOnly = true)
    public GroupSummaryResponse getSummaryForUser(Long userId) {
        return toSummary(getRequiredGroupForUser(userId));
    }

    private GroupSummaryResponse toSummary(GroupRoom group) {
        long memberCount = groupMembershipRepository.countByGroupId(group.getId());
        return new GroupSummaryResponse(group.getId(), group.getCode(), memberCount, MAX_USERS_PER_GROUP, MAX_GROUPS);
    }
}
