package com.bdi.agent.repository;

import com.bdi.agent.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User getByUsername(String username);

    // Gets all the users that have assigned the provided user as their trainer.
    // For example, if user "user1" has assigned "user2" as their trainer
    // then by calling this method with user2's id a list containing user1's id will be returned
    @Query("SELECT u.id FROM User u WHERE :userId MEMBER OF u.assignedTrainerIds")
    List<Long> getTrainerIdsByUserId(@Param("userId") Long userId);

    @Transactional
    // return type is Long instead of the desired type boolean
    // due to it throwing errors otherwise
    Long deleteUserByUsername(String username);
}

