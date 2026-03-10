package com.example.carreracaballos.repository;

import com.example.carreracaballos.model.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    Optional<GroupMembership> findByUserId(Long userId);

    long countByGroupId(Long groupId);
}
