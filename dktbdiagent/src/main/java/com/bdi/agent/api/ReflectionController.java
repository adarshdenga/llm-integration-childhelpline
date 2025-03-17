package com.bdi.agent.api;

import com.bdi.agent.repository.ReflectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bdi.agent.model.Reflection;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ReflectionController {

    @Autowired
    private ReflectionRepository Refrepository;

    @PostMapping("/reflection")
    public Reflection submitReflection(@RequestBody Reflection reflection) {
        reflection.setSubmissionTime(Timestamp.from(Instant.now()));
        return Refrepository.save(reflection);
    }
}