package com.bdi.agent.api;

import com.bdi.agent.model.Assessment;
import com.bdi.agent.repository.AssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AssessmentController{

    @Autowired
    private AssessmentRepository repository;

    @PostMapping("/assessment")
    public Assessment saveButtonClick(@RequestBody Assessment assessment) {
        //System.out.println(buttonClick.isFirstSession());
        assessment.setSubmissionTime(Timestamp.from(Instant.now()));
        return repository.save(assessment);
    }
}