package com.hotelbooking.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    List<GroupMembership> findByGroupGroupId(Long groupId);

    List<GroupMembership> findByMemberUserUserId(Long memberUserId);

    @Query("SELECT gm FROM GroupMembership gm WHERE gm.group.groupId = :groupId AND gm.memberUser.userId = :userId")
    Optional<GroupMembership> findByGroupIdAndMemberUserId(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId);

    boolean existsByGroupGroupIdAndMemberUserUserId(Long groupId, Long userId);
}
