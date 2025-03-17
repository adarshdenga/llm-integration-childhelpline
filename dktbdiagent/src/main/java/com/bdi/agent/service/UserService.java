package com.bdi.agent.service;

import com.bdi.agent.authorization.JwtUserDetailsService;
import com.bdi.agent.exceptions.InvalidJoinRequest;
import com.bdi.agent.model.AssignTrainerResponse;
import com.bdi.agent.model.Conversation;
import com.bdi.agent.model.Role;
import com.bdi.agent.model.User;
import com.bdi.agent.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final ConversationService conversationService;

    private final ReportService reportService;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtUserDetailsService userDetailsService;

    /**
     * Constructor for the UserService.
     *
     * @param userRepository      the UserRepository
     * @param conversationService the ConversationService
     * @param reportService       the ReportService
     */
    @Autowired
    public UserService(UserRepository userRepository,
                       ConversationService conversationService,
                       ReportService reportService, BCryptPasswordEncoder passwordEncoder,
                       JwtUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.conversationService = conversationService;
        this.reportService = reportService;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    public boolean containsUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User getByUserId(Long userId) {
        return userRepository.getById(userId);
    }

    // TODO the user is currently fetched taking the capitalization of the username into account
    public User getByUsername(String username) {
        return userRepository.getByUsername(username);
    }

    /**
     * Gets the id of the user with the username provided.
     *
     * @param username of the user
     * @return id of the user with the given username, or null if the user does not exist
     */
    public Long getUserId(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();
        return user.getId();
    }

    /**
     * Gets the username of the user id provided.
     *
     * @param userId of the user
     * @return string username of the user with the id provided, or null if the user does not exist
     */
    public String getUsername(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();
        return user.getUsername();
    }

    /**
     * Gets the email of the user provided.
     *
     * @param username of the user to get the email
     * @return string email of the user, or null if the user does not exist
     */
    public String getUserEmail(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();
        return user.getEmail();
    }

    /**
     * Gets the role of the user provided as a string.
     *
     * @param username of the user to get the role
     * @return string of the role of the user, or null if the user does not exist
     */
    public String getUserRole(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();
        return user.getRole().name();
    }

    /**
     * Updates the role of the user provided. New role must not be equal to the old role.
     * Also removes the user from the list of assigned trainers of other users if the
     * user's role is no longer of the trainer type.
     *
     * @param username of the user to update the role
     * @param newRole to assign the user
     * @return boolean indicating whether the action was successful or not
     */
    public boolean updateUserRole(String username, Role newRole) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        if (user.getRole() == newRole) {
            return false;
        }

        if (!"TRAINER".equals(newRole.getRole())) {
            removeUserFromAllAssignedTrainers(user.getId());
        }

        user.setRole(newRole);
        userRepository.save(user);
        return true;
    }

    /**
     * Changes the password of the user provided. New password must not be equal to the old password.
     *
     * @param username of user to change the password
     * @param newPassword string of the new password for the user
     * @return boolean indicating whether the action was successful or not
     */
    public boolean changePassword(String username, String newPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        if (user.getPassword().equals(newPassword)) {
            return false;
        }

        String passwordEncoded = this.passwordEncoder.encode(newPassword);
        userDetailsService.updatePassword(username, passwordEncoded);
        return true;
    }

    /**
     * Gets the list of trainer ids assigned to the user provided.
     *
     * @param username of the user to get assigned trainers
     * @return list of user ids of the trainers assigned to that user, or null if the user does not exist
     */
    public List<Long> getAssignedTrainers(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();
        return user.getAssignedTrainerIds();
    }

    /**
     * Updates the assigned trainers of the user provided, all ids provided must be of existing users
     * and the roles of the users to assign must all be of type TRAINER.
     *
     * @param username of user to update assign trainers
     * @param newTrainerIds list of user ids to assign as trainers
     * @return a pair of an optional of a list and a string.
     *         The string represents the result of the action: "Success" is returned if the action was successful
     *         and a descriptive error message is returned otherwise.
     *         The optional contains a list of the users that caused the failed request with boolean values as reasons,
     *         and it is empty if the action was successful or if the user provided does not exist
     *         or if the list of trainer ids is null.
     */
    public Pair<Optional<List<AssignTrainerResponse>>, String> updateAssignedTrainers(String username,
                                                                                      List<Long> newTrainerIds) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return Pair.of(Optional.empty(), "User with username provided does not exist");
        }
        if (newTrainerIds == null) {
            return Pair.of(Optional.empty(), "List of trainers to assign is null");
        }

        List<AssignTrainerResponse> failedUsers = new ArrayList<>();
        boolean trainerDoesNotExist = false;
        boolean userIsNotTrainer = false;

        // Check if the trainers actually exist with the ids provided
        // and that the account type is a trainer
        for (Long trainerId : newTrainerIds) {
            if (!userRepository.existsById(trainerId)) {
                trainerDoesNotExist = true;
                failedUsers.add(AssignTrainerResponse.builder()
                        .userId(trainerId).username("").userExists(false).userIsTrainer(false).build());
            }

            Optional<User> optionalTrainer = userRepository.findById(trainerId);
            if (optionalTrainer.isEmpty()) {
                trainerDoesNotExist = true;
                failedUsers.add(AssignTrainerResponse.builder()
                        .userId(trainerId).username("").userExists(false).userIsTrainer(false).build());
            }
            User trainerUser = optionalTrainer.get();
            if (trainerUser.getRole() != Role.TRAINER) {
                userIsNotTrainer = true;
                failedUsers.add(AssignTrainerResponse.builder()
                        .userId(trainerId).username(trainerUser.getUsername()).userExists(true).userIsTrainer(false)
                        .build());
            }
        }

        if (trainerDoesNotExist && userIsNotTrainer) {
            return Pair.of(Optional.of(failedUsers),
                    "Some user(s) provided do not exist and some user(s) are not trainer accounts");
        } else if (trainerDoesNotExist) {
            return Pair.of(Optional.of(failedUsers),
                    "Some trainer(s) provided do not exist");
        } else if (userIsNotTrainer) {
            return Pair.of(Optional.of(failedUsers),
                    "Some user(s) provided are not trainer accounts");
        }

        User user = optionalUser.get();
        user.setAssignedTrainerIds(newTrainerIds);

        userRepository.save(user);
        return Pair.of(Optional.empty(), "Success");
    }

    /**
     * Removes the provided user id from the list of assigned trainers of other users.
     * For example, if "user1" and "user2" has assigned "user3" as their trainer and
     * this method is called with user3's id, then user3's id is removed from
     * the assigned trainers lists of both user1 and user2.
     *
     * @param userId of the user to remove from other user's lists
     */
    public void removeUserFromAllAssignedTrainers(Long userId) {
        List<Long> usersAssigning = userRepository.getTrainerIdsByUserId(userId);

        for (Long assigningUserId : usersAssigning) {
            String username = userRepository.getById(assigningUserId).getUsername();
            List<Long> trainerList = getAssignedTrainers(username);

            if (trainerList.remove(userId)) {
                updateAssignedTrainers(username, trainerList);
            }
        }
    }

    /**
     * Deletes all stored report files associated with the provided user.
     * Calls the conversation service to retrieve all user conversations
     * and then calls the report service to delete each report per conversation.
     *
     * @param username of user to delete reports
     */
    public void deleteAllUserReports(String username) {
        Long userId = userRepository.getByUsername(username).getId();

        List<Conversation> userConversations = conversationService.getAllConversationsByUserId(userId);
        List<String> reportPaths = userConversations.stream()
                .map(Conversation::getReportFilePath)
                .filter(Objects::nonNull).toList();

        for (String path : reportPaths) {
            reportService.deleteReport(path);
        }
    }

    /**
     * Deletes a user and their details from the repository.
     * Also removes them from the assigned trainer lists of other users
     * and deletes all report files associated with the user
     *
     * @param username of user to delete
     * @return boolean value indicating whether the action was successful or not
     */
    public boolean deleteUser(String username) {
        Long userId = userRepository.getByUsername(username).getId();
        removeUserFromAllAssignedTrainers(userId);

        deleteAllUserReports(username);

        return userRepository.deleteUserByUsername(username) == 1L;
    }
    
    /**
     * Adds a conversation to the user's list of past chat conversations with Lilobot.
     *
     * @param user the user
     * @param conversation the conversation to add
     */
    public void addConversation(User user, Conversation conversation) {
        conversationService.saveConversation(conversation);
        user.getConversations().add(conversation);
        userRepository.save(user);
    }

    /**
     * Checks whether a user is another user's assigned trainer.
     *
     * @param username name of the learner
     * @param trainerName name of the trainer
     */
    public boolean isAssignedTrainer(String username, String trainerName) throws InvalidJoinRequest {
        Optional<User> learner = userRepository.findByUsername(username);
        if (learner.isEmpty()) {
            throw new InvalidJoinRequest("There is no learner with the name " + username);
        }
        Long trainerId = userRepository.findByUsername(trainerName).get().getId();
        return learner.get().getAssignedTrainerIds().contains(trainerId);
    }

    /**
     * Retrieves user from the given conversation id.
     *
     * @param conversationId id of the conversation
     * @return user that owns the conversation
     */
    public List<User> getUsersFromConversationId(Long conversationId) {
        List<User> users = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            for (Conversation conversation : user.getConversations()) {
                if (conversation.getConversationId().equals(conversationId)) {
                    users.add(user);
                }
            }
        }

        return users;
    }

}