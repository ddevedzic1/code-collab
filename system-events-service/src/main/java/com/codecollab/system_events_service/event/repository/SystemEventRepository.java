package com.codecollab.system_events_service.event.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codecollab.system_events_service.event.model.ActionType;
import com.codecollab.system_events_service.event.model.SystemEvent;

public interface SystemEventRepository extends JpaRepository<SystemEvent, UUID> {

	boolean existsByResourceAndActionType(String resource, ActionType actionType);
}
