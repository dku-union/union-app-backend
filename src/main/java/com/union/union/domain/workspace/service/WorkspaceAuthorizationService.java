package com.union.union.domain.workspace.service;

import com.union.union.domain.workspace.repository.WorkspaceMemberRepository;
import com.union.union.global.common.exception.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceAuthorizationService {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public void validateMembership(UUID workspaceId, UUID publisherId) {
        if (!workspaceMemberRepository.existsByWorkspace_WorkspaceIdAndPublisher_PublisherId(workspaceId, publisherId)) {
            throw new UnauthorizedAccessException("해당 워크스페이스의 멤버가 아닙니다");
        }
    }
}
