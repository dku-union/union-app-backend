package com.union.union.domain.workspace.repository;

import com.union.union.domain.workspace.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    boolean existsByWorkspace_WorkspaceIdAndPublisher_PublisherId(UUID workspaceId, UUID publisherId);

    List<WorkspaceMember> findByPublisher_PublisherId(UUID publisherId);
}
