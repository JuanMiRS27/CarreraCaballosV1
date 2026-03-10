package com.example.carreracaballos.repository;

import com.example.carreracaballos.model.GroupRoom;
import com.example.carreracaballos.model.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRoomRepository extends JpaRepository<GroupRoom, Long> {
    List<GroupRoom> findByStatusOrderByCreatedAtAsc(GroupStatus status);
}
