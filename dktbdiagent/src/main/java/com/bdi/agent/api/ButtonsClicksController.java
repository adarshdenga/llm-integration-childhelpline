package com.bdi.agent.api;

import com.bdi.agent.repository.ButtonsClicksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bdi.agent.model.ButtonsClicks;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ButtonsClicksController {

    @Autowired
    private ButtonsClicksRepository repository;

    @PostMapping("/button-click")
    public ButtonsClicks saveButtonClick(@RequestBody ButtonsClicks buttonClick) {
        //System.out.println(buttonClick.isFirstSession());
        buttonClick.setCurrentTime(Timestamp.from(Instant.now()));
        return repository.save(buttonClick);
    }
}