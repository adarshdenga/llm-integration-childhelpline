package com.bdi.agent.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import com.bdi.agent.model.ExceptionalBelief;
import com.bdi.agent.model.api.BeliefChangeClientModel;
import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BeliefUpdateType;
import com.bdi.agent.model.util.BeliefUpdateLogEntry;
import com.bdi.agent.model.util.MessageLogEntry;
import com.bdi.agent.repository.BeliefRepository;
import com.bdi.agent.utils.FloatComparer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:config.properties")
public class BeliefService {

    @Value("${localMode}")
    private boolean localMode;

    @Value("${minValue}")
    private float minValue;
    @Value("${maxValue}")
    private float maxValue;
    
    private final BeliefRepository beliefRepository;

    private final LogEntryService logEntryService;

    private final FloatComparer floatComparer;

    @Value("${beliefIncrease}")
    String beliefIncrease;
    @Value("${beliefDecrease}")
    String beliefDecrease;

    @Value("${beliefs.file}")
    private String beliefsFile;

    // configuration for Azure Blob Storage
    String connectionString = "DefaultEndpointsProtocol=https;AccountName=dktblobstorage;"
            + "AccountKey=JRaAWGN9SbJ+gvn5ec0brrpuvOPT3HS+VSTyLfJoE4/EQKf9eEVIPGqCeniJCiHUKA4JNYymNDtsl1/TDIjEKA==;"
            + "EndpointSuffix=core.windows.net";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a BeliefService.
     *
     * @param beliefRepository  The repository storing the beliefs.
     * @param floatComparer     The floatComparer, used to compare float values safely.
     */
    @Autowired
    public BeliefService(BeliefRepository beliefRepository, LogEntryService logEntryService,
                         FloatComparer floatComparer) {
        this.beliefRepository = beliefRepository;
        this.logEntryService = logEntryService;
        this.floatComparer = floatComparer;
    }

    /**
     * Get the belief values sorted by their name by the agent, in form of an array of
     * floats.
     *
     * @param agentId   The id of the agent.
     * @return belief values sorted by their name by the agent.
     */
    public float[] getValuesByAgentSortByName(Long agentId) {
        List<Belief> beliefs = sortBeliefsByName(beliefRepository.findByAgentIdOrderByPhaseAsc(agentId));
        float[] values = new float[beliefs.size()];

        int idx = 0;
        for (Belief belief : beliefs) {
            values[idx] = belief.getValue();
            idx++;
        }

        return values;
    }

    /**
     * Sets the belief file.
     *
     * @param beliefsFile The name of the belief file.
     */
    public void setBeliefsFile(String beliefsFile) {
        this.beliefsFile = beliefsFile;
    }

    /**
     * Get the beliefs by the agent id.
     *
     * @param agentId The id of the agent.
     * @return The beliefs of the agent associated with the agent id.
     */
    public Set<Belief> getByAgent(Long agentId) {
        return beliefRepository.findByAgentIdOrderByPhaseAsc(agentId);
    }

    /**
     * Gets a belief by the agent id and the name of the belief.
     *
     * @param agentId   The id of the agent.
     * @param name      The name of the belief.
     * @return the belief by the agent id and the name of the belief.
     */
    public Belief getByAgentIdAndName(Long agentId, String name) {
        return beliefRepository.findByAgentIdAndName(agentId, name);
    }

    /**
     * Saves a set of beliefs.
     *
     * @param beliefs the set of beliefs to save
     */
    public void addBeliefs(Set<Belief> beliefs) {
        for (Belief belief : beliefs) {
            beliefRepository.save(belief);
        }
    }

    /**
     * Increases the value of the given belief for the given agent.
     * Also sends the new belief to the client.
     *
     * @param agent the agent to change belief for
     * @param name the name of the belief
     * @param value the value to increase the belief by
     */
    public Optional<BeliefUpdateLogEntry> increaseBeliefValue(Agent agent, String name, Float value) {
        Belief belief = beliefRepository.findByAgentIdAndName(agent.getId(), name);

        if (floatComparer.greaterOrEqual(belief.getValue(), maxValue)) {
            return Optional.empty();
        }

        Float newValue = belief.getValue() + value;

        belief.setValue(newValue);
        beliefRepository.save(belief);
        System.out.printf("%-20s %s%n", beliefIncrease, belief.getFullName());
        return Optional.of(new BeliefUpdateLogEntry(BeliefUpdateType.INCREASE, newValue,
                BeliefName.valueOf(name), "", agent));
    }

    /**
     * Decreases the value of the given belief for the given agent.
     * Also sends the new belief to the client.
     *
     * @param agent the agent to change belief for
     * @param name the name of the belief
     * @param value the value to decrease the belief by
     */
    public Optional<BeliefUpdateLogEntry> decreaseBeliefValue(Agent agent, String name, Float value) {
        Belief belief = beliefRepository.findByAgentIdAndName(agent.getId(), name);

        if (floatComparer.lessOrEqual(belief.getValue(), minValue)) {
            return Optional.empty();
        }

        Float newValue = belief.getValue() - value;

        belief.setValue(newValue);
        beliefRepository.save(belief);
        System.out.printf("%-20s %s%n", beliefDecrease, belief.getFullName());
        return Optional.of(new BeliefUpdateLogEntry(BeliefUpdateType.DECREASE, newValue,
                BeliefName.valueOf(name), "", agent));
    }

    /**
     * Sets the value of the given belief for the given agent.
     * Also sends the new belief to the client.
     *
     * @param agent the agent to change belief for
     * @param name the name of the belief
     * @param value the new value of the belief
     */
    public Optional<BeliefUpdateLogEntry> setBeliefValue(Agent agent, String name, Float value)
            throws EntityNotFoundException {
        Belief belief = beliefRepository.findByAgentIdAndName(agent.getId(), name);
        if (belief == null) {
            throw new EntityNotFoundException("Belief was not found");
        }

        if (belief.getValue().equals(value)) {
            return Optional.empty();
        }

        belief.setValue(value);
        beliefRepository.save(belief);
        return Optional.of(new BeliefUpdateLogEntry(BeliefUpdateType.SET_TO, value,
                BeliefName.valueOf(name), "", agent));
    }

    /**
     * Gets the average value of the given beliefs for an agent.
     *
     * @param agentId the id of the agent
     * @param beliefNames the names of the beliefs
     * @return the average value of the provided beliefs for the agent
     */
    public float averageBeliefValue(Long agentId, String[] beliefNames) {
        float sum = 0;
        for (String beliefName : beliefNames) {
            sum += getByAgentIdAndName(agentId, beliefName).getValue();
        }

        return sum / beliefNames.length;
    }

    /**
     * Reads the initial beliefs from a csv file or blob storage and returns them as a HashSet.
     * Also sets the agent for each belief.
     *
     * @param agent the agent for which the beliefs are read
     * @return HashSet of Beliefs
     */
    public HashSet<Belief> readBeliefsFromCsv(Agent agent) {

        HashSet<Belief> result = new HashSet<>();

        try {

            if (!localMode) {
                beliefsFile = getBeliefsFromBlobStorage();
            }

            CSVReader reader = new CSVReader(new FileReader(beliefsFile));
            List<String[]> records = reader.readAll();

            for (String[] record : records) {
                Belief b = new Belief();
                b.setAgent(agent);
                b.setName(record[0]);
                b.setFullName(record[1]);
                b.setPhase(record[2]);
                b.setValue(Float.valueOf(record[3]));
                result.add(b);
            }

            reader.close();

        } catch (IOException | CsvException e) {
            System.err.println("readBeliefsFromCsv: could not initialize beliefs");
        }

        return result;
    }

    /**
     * Reads the initial beliefs from a csv file or blob storage and returns them as a HashSet.
     *
     * @return HashSet of Beliefs
     */
    public HashSet<Belief> readBeliefsFromCsv() {
        HashSet<Belief> result = new HashSet<>();

        try {

            if (!localMode) {
                beliefsFile = getBeliefsFromBlobStorage();
            }

            CSVReader reader = new CSVReader(new FileReader(beliefsFile));
            List<String[]> records = reader.readAll();

            for (String[] record : records) {
                Belief b = new Belief();
                b.setName(record[0]);
                b.setFullName(record[1]);
                b.setPhase(record[2]);
                b.setValue(Float.valueOf(record[3]));
                if (record.length > 4 && record[4].matches("^(?i)(f|false)$")) {
                    String reason = (record.length > 5) ? record[5] : "reason unknown";
                    b = new ExceptionalBelief(
                            b.getAgent(), b.getName(), b.getFullName(), b.getPhase(), b.getValue(), reason
                    );
                }
                result.add(b);
            }

            reader.close();

        } catch (IOException | CsvException e) {
            System.err.println("readBeliefsFromCsv: could not initialize beliefs");
        }

        return result;
    }

    /**
     * Gets the value of a certain belief from a set of beliefs.
     *
     * @param beliefs the set of beliefs to get value from
     * @param name the name of the belief
     * @return the value of the belief
     */
    public float getBeliefValue(Set<Belief> beliefs, String name) {
        for (Belief b : beliefs) {
            if (b.getName().equals(name)) {
                return b.getValue();
            }
        }

        return 0;
    }

    /**
     * Downloads the initial set of beliefs from the Azure Blob Storage.
     *
     * @return the path of the set of beliefs
     */
    private String getBeliefsFromBlobStorage() {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString).buildClient();
        String containerName = "bdi";
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        String fileName = "beliefs.csv";
        String downloadFileName = fileName.replace(".csv", "DOWNLOAD.csv");
        File downloadedFile = new File(downloadFileName);
        System.out.println("\nDownloading blob to\n\t " + downloadFileName);

        if (!downloadedFile.exists()) {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.downloadToFile(downloadFileName);
        }

        return downloadedFile.getAbsolutePath();
    }

    /**
     * This method computes the BDI outcome of the agent. Used as part of the experiment of the thesis.
     *
     * @param agent the agent
     * @return the BDI outcome
     */
    public float calculateScore(Agent agent) {
        String[] beliefNames = {"B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B14", "B17"};

        float result = 0;
        for (String beliefName : beliefNames) {
            Belief belief = beliefRepository.findByAgentIdAndName(agent.getId(), beliefName);

            if (beliefName.equals("B8") || beliefName.equals("B17")) {
                result += (1 - belief.getValue());
            } else {
                result += belief.getValue();
            }
        }

        System.out.println("BDI result: " + result);
        return result;
    }

    /**
     * This method sends the belief updates in json format to the client via websockets, and saves the log entries.
     * Client is subscribed to /topic/beliefs/{sessionId}
     *
     * @param agent the agent whose beliefs were updated to retrieve the session id
     * @param beliefUpdateLogs the beliefs that were updated and logging information
     */
    public void sendBeliefsToClientAndLog(Agent agent, List<BeliefUpdateLogEntry> beliefUpdateLogs) {
        try {
            for (BeliefUpdateLogEntry log : beliefUpdateLogs) {
                String body = new ObjectMapper().writeValueAsString(getBeliefChangeClientModelFromLog(log, agent));
                messagingTemplate.convertAndSend("/topic/beliefs/" + agent.getUserId(), body);
            }
        } catch (JsonProcessingException e) {
            System.err.println("sendBeliefsToClient: Belief could not be parsed");
        } catch (NullPointerException e) {
            System.err.println("sendBeliefsToClient: Agent is null");
        } catch (IllegalStateException e) {
            System.err.println("sendBeliefsToClient: Agent log does not contain a KT message");
        }
    }

    /**
     * Creates a BeliefChangeClientModel from a BeliefUpdateLogEntry. It takes all necessary info from the log entry,
     * and when possible searches for the agent's most recent message that matches the cause of the update. If a
     * cause is found, the BeliefChangeClientModel gets the cause log's index as logIndex.
     *
     * @param log the belief update log to make a BeliefChangeClientModel of
     * @param agent the agent the log belongs to
     * @return the new BeliefChangeClientModel for the log
     */
    public BeliefChangeClientModel getBeliefChangeClientModelFromLog(BeliefUpdateLogEntry log, Agent agent) {
        // get all message logs
        List<MessageLogEntry> logs = logEntryService.getUserMessageLogsByAgentChronologicalUntilTimestamp(
                agent.getId(), log.getTimestamp());

        // index of the cause initiated to the next possible index
        int msgLogIndex = -1;

        // search for the message that caused it if this message should exist
        if (log.getCause() != null && !log.getIsManualUpdate()) {
            // search for the most recent message that could have caused the belief update
            for (int i = logs.size() - 1; i >= 0; i--) {
                if (logs.get(i).getMessage().equals(log.getCause())) {
                    msgLogIndex = i;
                    break;
                }
            }
        }
        return new BeliefChangeClientModel(log.getBeliefName().toString(), log.getValue(), log.getCause(),
                msgLogIndex, log.getIsManualUpdate(), log.getBeliefUpdateType());
    }

    /**
     * Sort the given beliefs by their identifier (numerically, by only sorting the integer after 'B').
     *
     * @param beliefs The set of beliefs to sort.
     * @return The given beliefs sorted by their identifier.
     */
    public List<Belief> sortBeliefsByName(Set<Belief> beliefs) {
        return beliefs.stream()
                .sorted(Comparator.comparingInt(BeliefService::extractNumber))
                .collect(Collectors.toList());
    }

    /**
     * This method takes the initial beliefs from the CSV file and returns it as an ArrayList where
     * the beliefs are sorted by their identifier (numerically, by only sorting the integer after 'B').
     */
    public List<Belief> getAllInitialBeliefsSorted() {
        HashSet<Belief> beliefs = readBeliefsFromCsv();
        return sortBeliefsByName(beliefs);
    }

    /**
     * This method takes a belief identifier and extracts the integer from it, so they can be properly sorted.
     *
     * @param b the belief to extract the integer from
     */
    private static int extractNumber(Belief b) throws IndexOutOfBoundsException {
        String field = b.getName();
        // Assuming the name is always "B" followed by a number
        String numberString = field.substring(1);
        if (numberString.matches("\\d+")) {
            return Integer.parseInt(numberString);
        } else {
            throw new IllegalArgumentException(
                    "Invalid identifier '" + field + "': no numeric substring after " + field.charAt(0) + " found."
            );
        }
    }
}
