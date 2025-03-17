package com.bdi.agent.api;

import com.bdi.agent.authorization.JwtTokenUtils;
import com.bdi.agent.exceptions.InvalidJoinRequest;
import com.bdi.agent.model.AssignTrainerResponse;
import com.bdi.agent.model.Role;
import com.bdi.agent.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {

    private final transient UserService userService;
    private final JwtTokenUtils jwtTokenUtils;

    @Autowired
    public UserController(UserService userService, JwtTokenUtils jwtTokenUtils) {
        this.userService = userService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    /**
     * Endpoint which gets the email of the user provided.
     *
     * @param authHeader header containing the token
     * @return a response indicating either failure or success, and the email of the user if successful
     */
    @GetMapping(path = "/email")
    public ResponseEntity<String> getEmail(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        System.out.println("get email of user: " + username);

        if (!userService.existsByUsername(username)) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        String email = userService.getUserEmail(username);

        return new ResponseEntity<>(email, HttpStatus.OK);
    }

    /**
     * Endpoint which gets the role of the user provided.
     *
     * @param authHeader header containing token
     * @return a response indicating either failure or success, and the role of the user if successful
     */
    @GetMapping(path = "/role")
    public ResponseEntity<String> getRole(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        System.out.println("get role of user: " + username);

        if (!userService.existsByUsername(username)) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        String role = userService.getUserRole(username);

        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    /**
     * Endpoint which gets the list of trainers assigned to the user provided.
     *
     * @param authHeader header containing token
     * @return a response indicating either failure or success,
     *         and if successful contains a list of the assigned trainers containing their user id and username
     */
    @GetMapping(path = "/trainers")
    public ResponseEntity<List<Pair<Long, String>>> getTrainers(@RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                            String authHeader) {
        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        System.out.println("get list of assigned trainers for user: " + username);

        if (!userService.existsByUsername(username)) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        List<Long> trainers = userService.getAssignedTrainers(username);
        List<Pair<Long, String>> trainerPairs = trainers.stream().map(
                trainerId -> Pair.of(trainerId, userService.getUsername(trainerId))).toList();

        return new ResponseEntity<>(trainerPairs, HttpStatus.OK);
    }

    /**
     * Endpoint which assigns a trainer to the user provided.
     *
     * @param authHeader header containing token
     * @param trainerUsername username of the user to assign as trainer
     * @return a response indicating either failure or success,
     *         and if it failed due to some trainer ids not passing certain criteria the response body
     *         will contain a list of the user ids that caused the failure and booleans indicating
     *         the failed criteria.
     */
    @PatchMapping(path = "/assign_trainer")
    public ResponseEntity<?> assignTrainer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                           @RequestBody String trainerUsername) {
        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        System.out.println("assign trainer " + trainerUsername + " to user: " + username);

        if (username.equals(trainerUsername)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.WARNING, "Cannot assign user as their own trainer").build();
        }

        if (!userService.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.WARNING, "User with provided username does not exist")
                    .body("User " + username + " cannot be found.");
        }
        if (!userService.existsByUsername(trainerUsername)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.WARNING, "Trainer with provided username does not exist")
                    .body("Trainer " + trainerUsername + " does not exist.");
        }

        Long newTrainerId = userService.getUserId(trainerUsername);
        List<Long> trainers = userService.getAssignedTrainers(username);

        // If trainer provided is already assigned to the user
        if (trainers.contains(newTrainerId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.WARNING, "Trainer is already assigned to user")
                    .body(trainerUsername + " is already assigned as your trainer.");
        }

        trainers.add(newTrainerId);
        Pair<Optional<List<AssignTrainerResponse>>, String> response;
        response = userService.updateAssignedTrainers(username, trainers);

        if (response.getSecond().equals("Success")) {
            return ResponseEntity.status(HttpStatus.OK).body(trainerUsername);
        }
        if (response.getFirst().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(
                    HttpHeaders.WARNING, response.getSecond())
                    .body(response.getSecond());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(
                HttpHeaders.WARNING, response.getSecond())
                .body(response.getFirst().get());
    }

    /**
     * Endpoint which removes a trainer assigned to the user provided.
     *
     * @param authHeader header containing token
     * @param trainerUsername username of user to remove as assigned trainer
     * @return a response indicating either failure or success,
     *         and if it failed due to some trainer ids not passing certain criteria the response body
     *         will contain a list of the user ids that caused the failure and booleans indicating
     *         the failed criteria.
     */
    @PatchMapping(path = "/remove_trainer")
    public ResponseEntity<?> removeTrainer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                           @RequestBody String trainerUsername) {
        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        System.out.println("remove trainer " + trainerUsername + " from user: " + username);

        if (!userService.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.WARNING, "User with provided username does not exist")
                    .body("User " + username + " cannot be found.");
        }
        if (!userService.existsByUsername(trainerUsername)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.WARNING, "Trainer with provided username does not exist")
                    .body("Trainer " + trainerUsername + " does not exist.");
        }

        Long trainerId = userService.getUserId(trainerUsername);
        List<Long> trainers = userService.getAssignedTrainers(username);

        // If the trainer provided is not an assigned trainer of the user provided
        // (therefore there is no trainer to remove)
        if (!trainers.contains(trainerId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.WARNING, "Trainer is not assigned to user")
                    .body(trainerUsername + " is no longer a trainer.");
        }

        trainers.remove(trainerId);
        Pair<Optional<List<AssignTrainerResponse>>, String> response;
        response = userService.updateAssignedTrainers(username, trainers);
        if (response.getSecond().equals("Success")) {
            return ResponseEntity.status(HttpStatus.OK).body(trainerUsername);
        }
        if (response.getFirst().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(
                    HttpHeaders.WARNING, response.getSecond())
                    .body(response.getSecond());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(
                HttpHeaders.WARNING, response.getSecond()).body(response.getFirst().get());
    }

    /**
     * Endpoint which updates the role of the user provided.
     *
     * @param authHeader header containing token
     * @param newRole to assign the user
     * @return a response indicating either failure or success
     */
    @PatchMapping(path = "/update_role")
    public ResponseEntity<?> updateRole(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                        @RequestBody Role newRole) {
        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        System.out.println("update role to " + newRole.toString() + " of user: " + username);
        if (userService.updateUserRole(username, newRole)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * Endpoint which updates the password of the user provided.
     *
     * @param authHeader header containing token
     * @param newPassword string of the new password for the user
     * @return a response indicating either failure or success
     */
    @PatchMapping(path = "/update_password")
    public ResponseEntity<?> updatePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                            @RequestBody String newPassword) {
        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        System.out.println("update password of user: " + username);
        if (userService.changePassword(username, newPassword)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * Endpoint which deletes the repository entry of the user provided.
     *
     * @param authHeader authorization header
     * @return 200 OK message if account was successfully deleted,
     */
    @DeleteMapping(path = "/delete")
    public ResponseEntity<?> deleteAccount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        String username = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        System.out.println("delete user: " + username);

        if (!userService.existsByUsername(username)) {
            return ResponseEntity.notFound().build();
        }

        if (userService.deleteUser(username)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.internalServerError().build();
    }

    /**
     * Checks if a trainer is assigned to the provided user.
     *
     * @param authHeader header containing the trainer's token
     * @param learnerName name of the learner
     * @return 200 OK message if trainer is assigned to learner
     */
    @GetMapping(path = "/join_request/{learner}")
    public ResponseEntity<?> checkAssignedTrainer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                  @PathVariable("learner") String learnerName) {
        String trainerName = this.jwtTokenUtils.retrieveUsernameFromToken(authHeader);
        try {
            if (userService.isAssignedTrainer(learnerName, trainerName)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(trainerName + " is assigned as a trainer for " + learnerName);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("You are not assigned as " + learnerName + "'s trainer.");
            }
        } catch (InvalidJoinRequest e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
