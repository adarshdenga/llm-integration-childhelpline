package com.bdi.agent.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bdi.agent.model.MetaExperiment;
import com.bdi.agent.repository.MetaExperimentRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MetaExperimentService {

    @Autowired
    private MetaExperimentRepository repository;

    public MetaExperiment getByUsername(String username) {
        return repository.findByUsername(username);
    }


    public void updateSessNum(String username, int sessNum) {
        MetaExperiment metaExperiment = repository.findByUsername(username);
        if (metaExperiment != null) {
            metaExperiment.setSessNum(sessNum);
            repository.save(metaExperiment);
        } else {
            throw new RuntimeException("User not found.");
        }
    }


    // Function to update the KnowledgeOrderUpdt for a given username
    public void updateKnowledgeOrderUpdt(String username, String updatedOrder) {
        MetaExperiment metaExperiment = repository.findByUsername(username);
        if (metaExperiment != null) {
            metaExperiment.setKnowledgeOrderUpdt(updatedOrder);
            repository.save(metaExperiment);
        } else {
            throw new RuntimeException("User not found.");
        }
    }

    // Function to get the KnowledgeOrderUpdt for a given username
    public String getKnowledgeOrderUpdt(String username) {
        MetaExperiment metaExperiment = repository.findByUsername(username);
        if (metaExperiment != null) {
            return metaExperiment.getKnowledgeOrderUpdt();
        } else {
            throw new RuntimeException("User not found.");
        }
    }

    // Function to get the last number and update the KnowledgeOrderUpdt
    public String getLastKnowledgeOrderAndUpdate(String username) {

        String knowledgeOrder = getKnowledgeOrderUpdt(username);

        if (knowledgeOrder != null && !knowledgeOrder.isEmpty()) {
            List<String> numbers = new ArrayList<>(Arrays.asList(knowledgeOrder.split(",")));
            String lastNumber = numbers.remove(numbers.size() - 1);
            updateKnowledgeOrderUpdt(username, String.join(",", numbers));

            return lastNumber;
        } else {
            throw new RuntimeException("Knowledge order is empty or null.");
        }
    }


}
