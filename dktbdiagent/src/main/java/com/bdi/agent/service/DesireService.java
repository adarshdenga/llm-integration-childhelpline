package com.bdi.agent.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Desire;
import com.bdi.agent.repository.DesireRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@PropertySource("classpath:config.properties")
public class DesireService {

    @Value("${localMode}")
    private boolean localMode;

    private DesireRepository desireRepository;
    private final ActionService actionService;

    @Value("${desires.file}")
    private String desiresFile;

    // configuration for Azure Blob Storage
    String connectionString = "DefaultEndpointsProtocol=https;AccountName=dktblobstorage;AccountKey=JRaAWGN9SbJ+gvn5ec0brrpuvOPT3HS+VSTyLfJoE4/EQKf9eEVIPGqCeniJCiHUKA4JNYymNDtsl1/TDIjEKA==;EndpointSuffix=core.windows.net";


    public DesireService(DesireRepository desireRepository, ActionService actionService) {
        this.desireRepository = desireRepository;
        this.actionService = actionService;
    }

    public void addDesire(Desire desire) {
        desireRepository.save(desire);
    }

    public Desire getById(Long id) {
        return desireRepository.getById(id);
    }

    public Desire getByAgentIdAndName(Long agentId, String name) {
        return desireRepository.findByAgentIdAndName(agentId, name);
    }

    public List<Desire> getByAgent(Long agentId) {
        return desireRepository.findByAgentIdOrderByName(agentId);
    }


    public void addDesires(Set<Desire> desires) {
        for (Desire desire : desires) {
            desireRepository.save(desire);
        }
    }

    public Desire getActiveGoal(Long agentId) {
        List<Desire> desires = getByAgent(agentId);

        for (Desire desire : desires) {
            if (desire.isActive()) {
                return desire;
            }
        }

        return null;
    }

    public HashSet<Desire> readDesiresFromCsv(Agent agent) {
        HashSet<Desire> result = new HashSet<>();

        try {

            if (!localMode) {
                desiresFile = getDesiresFromBlobStorage();
            }
            CSVReader reader = new CSVReader(new FileReader(desiresFile));
            List<String[]> records = reader.readAll();

            for (String[] record : records) {
                Desire d = new Desire();
                d.setAgent(agent);
                d.setName(record[0]);
                d.setFullName(record[1]);
                d.setActive(Boolean.valueOf(record[2]));
                actionService.addActionsToDesire(d);
                result.add(d);
            }

            reader.close();

        } catch (IOException | CsvException e) {
            System.err.println("readDesiresFromCsv: could not initialize desires");
        }

        return result;
    }

    private String getDesiresFromBlobStorage() {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        String containerName = "bdi";
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        String fileName = "desires.csv";
        String downloadFileName = fileName.replace(".csv", "DOWNLOAD.csv");
        File downloadedFile = new File(downloadFileName);
        System.out.println("\nDownloading blob to\n\t " + downloadFileName);

        if (!downloadedFile.exists()) {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.downloadToFile(downloadFileName);
        }

        return downloadedFile.getAbsolutePath();
    }
}
