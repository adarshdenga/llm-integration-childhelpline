package com.bdi.agent.api;
import com.bdi.agent.model.MetaExperiment;
import com.bdi.agent.repository.MetaExperimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.bdi.agent.service.MetaExperimentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")

public class MetaExperimentController {

    @Autowired
    private MetaExperimentRepository repository;

    @Autowired
    private MetaExperimentService service;


    @PostMapping("/metaExperiment")
    public MetaExperiment saveButtonClick(@RequestBody MetaExperiment metaExperiment) {
        return repository.save(metaExperiment);
    }

    @GetMapping("/meta-experiment-user")
    public MetaExperiment getMetaExperiment(@RequestParam String username) {
        return service.getByUsername(username);
    }

    @PutMapping("/updateSessNum")
    public ResponseEntity<?> updateSessNum(@RequestBody UpdateSessNumRequest request) {
        try {
            service.updateSessNum(request.getUsername(), request.getSessNum());
            return ResponseEntity.ok("Session number updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating session number.");
        }
    }


    @PutMapping("/updateKnowledgeOrder")
    public ResponseEntity<?> updateKnowledgeOrder(@RequestBody UpdateKnowledgeOrderUpdtRequest request) {
        try {
            service.updateKnowledgeOrderUpdt(request.getUsername(), request.getknowledgeOrderUpdt());
            return ResponseEntity.ok("Session number updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating session number.");
        }
    }



}

class UpdateSessNumRequest {
    private String username;
    private int sessNum;

    // getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getSessNum() {
        return sessNum;
    }

    public void setSessNum(int sessNum) {
        this.sessNum = sessNum;
    }
}


class UpdateKnowledgeOrderUpdtRequest {
    private String username;
    private String knowledgeOrderUpdt;

    // getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getknowledgeOrderUpdt() {
        return knowledgeOrderUpdt;
    }

    public void setknowledgeOrderUpdt(String knowledgeOrderUpdt) {
        this.knowledgeOrderUpdt = knowledgeOrderUpdt;
    }
}