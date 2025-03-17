package com.bdi.agent.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import com.bdi.agent.model.enums.BeliefUpdateType;
import com.bdi.agent.model.util.BeliefUpdateLogEntry;
import com.bdi.agent.model.util.DesireUpdateLogEntry;
import com.bdi.agent.model.util.LogEntry;
import com.bdi.agent.model.util.MessageLogEntry;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;

@Service
public class ReportService {

    @Value("${localMode}")
    private boolean localMode;

    private final BeliefService beliefService;
    private final DesireService desireService;
    private final LogEntryService logEntryService;

    int percentage = 100;
    String fontStyle = "Lato";

    @Value("${reports.path}")
    String localPath;

    @Value("${ktPrefix}")
    String ktPrefix;
    @Value("${liloPrefix}")
    String liloPrefix;

    @Value("${beliefIncrease}")
    String beliefIncrease;
    @Value("${beliefDecrease}")
    String beliefDecrease;
    @Value("${beliefSetTo}")
    String beliefSetTo;

    @Value("${desireIncrease}")
    String desireIncrease;
    @Value("${desireDecrease}")
    String desireDecrease;

    @Value("${intentionPrefix}")
    String intentionPrefix;

    // configuration for Azure Blob Storage
    private final String connectionString = "DefaultEndpointsProtocol=https;AccountName=dktblobstorage;AccountKey=JRaAWGN9SbJ+gvn5ec0brrpuvOPT3HS+VSTyLfJoE4/EQKf9eEVIPGqCeniJCiHUKA4JNYymNDtsl1/TDIjEKA==;EndpointSuffix=core.windows.net";
    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    String containerName = "reports";
    BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

    @Autowired
    public ReportService(BeliefService beliefService, LogEntryService logEntryService, DesireService desireService) {
        this.beliefService = beliefService;
        this.logEntryService = logEntryService;
        this.desireService = desireService;
    }

    /**
     * Generates an info text for the generated report. For each advanced option that is chosen, an explanation of the
     * option is included in the info text in suitable locations.
     *
     * @param agent the agent to generate the report of
     * @param showAbbreviations boolean for showing abbreviations of beliefs and desires (e.g. 'B3', 'D1')
     * @param showBeliefSetTo boolean for showing all belief updates rather than only increases and decreases
     * @param showBeliefUpdateCause boolean for showing the cause behind each belief update
     * @param showDesireUpdate boolean for showing desire updates
     * @param showNewValue boolean for showing the numeric values of beliefs that get updated
     * @return the info text for the customized advanced report report
     */
    private String getReportInfoText(Agent agent,
                                     Boolean showAbbreviations,
                                     Boolean showDesireUpdate,
                                     Boolean showBeliefSetTo,
                                     Boolean showBeliefUpdateCause,
                                     Boolean showNewValue) {
        StringBuilder sb = new StringBuilder(
        "Hier is een transcriptie van je gesprek met Lilobot met zijn gedachten tijdens het gesprek. Lilobot " +
                "heeft een reeks overtuigingen en intenties die tijdens het gesprek constant worden bijgewerkt op " +
                "basis van wat je tegen hem zegt. In de onderstaande tabel kun je zien wat Lilobot's overtuigingen " +
                "waren aan het begin van het gesprek en aan het einde. Het transcript van het gesprek laat zien " +
                "welke overtuigingen ");
        if (showDesireUpdate) {
            sb.append("en verlangens ");
        }
        sb.append("veranderen op basis van jouw berichten. ");
        if (showAbbreviations) {
            sb.append("De afkortingen van deze overtuigingen ");
            if (showDesireUpdate) {
                sb.append("en verlangens ");
            }
            sb.append("worden links van de volledige namen weergeven. ");
        }
        if (showBeliefUpdateCause) {
            sb.append("De reden voor iedere verandering in de overtuigingen wordt ook weergeven rechts naast de " +
                    "volledige naam van de overtuiging. ");
        }
        sb.append("Het symbool ↑ betekent dat de overtuiging ");

        if (showDesireUpdate) {
            sb.append("of het verlangen ");
        }
        sb.append("toeneemt, terwijl ↓ betekent dat de overtuiging ");

        if (showDesireUpdate) {
            sb.append("of het verlangen ");
        }
        sb.append("afneemt. ");

        if (showBeliefSetTo) {
            sb.append("Het symbool → betekent dat de overtuiging op dat moment naar een specifieke waarde is gezet, " +
                    "vaak omdat de waarde afhankelijk was van andere overtuigingen, of omdat deze waarde handmatig " +
                    "was gekozen door een trainer. ");
        }
        if (showNewValue) {
            sb.append("Voor iedere verandering in overtuigingen kun je ook zien welke numerieke waarde deze " +
                    "overtuiging hierdoor heeft gekregen. ");
            if (showDesireUpdate) {
                sb.append("Voor verlangens zijn er maar twee mogelijke waarden, dus ↓ betekent altijd dat de waarde " +
                        "nu FALSE is, en ↑ dat het nu TRUE is. ");
            }
        }
        sb.append("Het transcript laat ook zien welke intenties Lilobot had op het moment in het gesprek. " +
                "Al deze notaties zijn cursief weergegeven tussen jullie gesprek. \n");

        sb.append("Je code voor deze sessie is ").append(agent.getUserId());

        return sb.toString();
    }

    /**
     * Creates a basic report given an agent. It calls the createReport method with no extra options enabled.
     *
     * @param agent the agent to generate the report of
     * @return the absolute filepath of the generated report
     */
    public String createBasicReport(Agent agent) {
        return createReport(agent, false, false, false, false, false);
    }

    /**
     * Creates a report given an agent. It includes an overview of belief values at the start and end the conversation,
     * as well as a transcript of the conversation. The information included in this transcript is customizable through
     * the boolean arguments.
     *
     * @param agent the agent to generate the report of
     * @param showAbbreviations boolean for showing abbreviations of beliefs and desires (e.g. 'B3', 'D1')
     * @param showBeliefSetTo boolean for showing all belief updates rather than only increases and decreases
     * @param showBeliefUpdateCause boolean for showing the cause behind each belief update
     * @param showDesireUpdate boolean for showing desire updates
     * @param showNewValue boolean for showing the numeric values of beliefs that get updated
     * @return the absolute filepath of the generated report
     */
    public String createReport(Agent agent, Boolean showAbbreviations, Boolean showDesireUpdate,
                               Boolean showBeliefSetTo, Boolean showBeliefUpdateCause, Boolean showNewValue) {
        try {

            String fileName = agent.getUserId().concat(".docx");

            String rootPath = System.getProperty("user.dir");
            File rootDir = new File(rootPath);

            File reportsDir = new File(rootDir, localPath);

            File file;
            if (reportsDir.exists()) {
                file = new File(reportsDir, fileName);
            } else {
                try {
                    File dir = new File(rootDir, localPath);
                    if (!dir.exists()) {
                        if (dir.mkdirs()) {
                            System.out.println("Directory created: " + dir.getAbsolutePath());
                        } else {
                            System.err.println("Failed to create directory: " + dir.getAbsolutePath());
                        }
                    }
                    file = new File(dir, fileName);
                } catch (Exception e) {
                    System.err.println("Failed to create temporary file: " + e.getMessage());
                    return null;
                }
            }

            FileOutputStream out = new FileOutputStream(file);

            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph t1 = doc.createParagraph();
            t1.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun t1run = t1.createRun();
            t1run.setFontFamily(fontStyle);
            t1run.setBold(true);
            t1run.setText("FEEDBACK GESPREK");
            t1.setSpacingAfter(200);

            XWPFParagraph p1 = doc.createParagraph();
            p1.setSpacingAfter(300);
            XWPFRun r1 = p1.createRun();
            r1.setFontFamily("Lato");
            r1.setText(getReportInfoText(agent, showAbbreviations, showDesireUpdate, showBeliefSetTo,
                    showBeliefUpdateCause, showNewValue));

            XWPFTable beliefTable = doc.createTable(18, 5);
            beliefTable.setWidth("100%");
            XWPFTableRow headerRow = beliefTable.getRow(0);
            headerRow.getCell(0).setText("Overtuiging");
            headerRow.getCell(1).setText("Vijffasemodel ");
            headerRow.getCell(2).setText("Begin");
            headerRow.getCell(3).setText("Eind");
            headerRow.getCell(4).setText("Verschil");

            HashSet<Belief> initialBeliefs = beliefService.readBeliefsFromCsv();
            Belief[] beliefArray = new Belief[agent.getBeliefs().size()];
            beliefService.getByAgent(agent.getId()).toArray(beliefArray);

            for (int i = 0; i < beliefArray.length; i++) {
                Belief b = beliefArray[i];
                float initialValue = beliefService.getBeliefValue(initialBeliefs, b.getName());

                XWPFTableRow currentRow = beliefTable.getRow(i+1);
                if (showAbbreviations) {
                    currentRow.getCell(0).setText(String.format("%s", b.getName() + ": " + b.getFullName()));
                } else {
                    currentRow.getCell(0).setText(String.format("%s", b.getFullName()));
                }
                currentRow.getCell(1).setText(String.format("%s", b.getPhase()));
                currentRow.getCell(2).setText(String.format("%s", floatToPercentage(initialValue)));
                currentRow.getCell(3).setText(String.format("%s", floatToPercentage(b.getValue())));
                currentRow.getCell(4).setText(String.format("%s", calculateDifference(initialValue, b.getValue())));
            }

            XWPFParagraph t2 = doc.createParagraph();
            t2.setAlignment(ParagraphAlignment.CENTER);
            t2.setSpacingBefore(200);
            t2.setSpacingAfter(200);
            XWPFRun t2run = t2.createRun();
            t2run.setFontFamily(fontStyle);
            t2run.setBold(true);
            t2run.setText("TRANSCRIPT");

            for (LogEntry logEntry : logEntryService.getChronologicalLogsByAgent(agent.getId())) {
                formatLogEntryForReport(doc, logEntry, agent, showAbbreviations, showDesireUpdate, showBeliefSetTo,
                        showBeliefUpdateCause, showNewValue);
            }

            doc.write(out);
            out.close();
            doc.close();

            if (!localMode) {
                BlobClient blobClient = containerClient.getBlobClient(fileName);
                System.out.println("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());
                if (!blobClient.exists()) {
                    blobClient.uploadFromFile(localPath + fileName);
                }
                return generateSasToken(blobClient);
            }

            return file.getAbsolutePath();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("createReport: could not create report");
        }

        return null;

    }

    /**
     * Formats and sets a log entry in a document. All logs except the messages are added in cursive.
     *
     * @param doc the document to add the log entry to
     * @param logEntry the log entry to add to the document
     * @param agent the agent we are logging for
     */
    public void formatLogEntryForReport(XWPFDocument doc, LogEntry logEntry, Agent agent,
                                        Boolean showAbbreviations,
                                        Boolean showDesireUpdate,
                                        Boolean showBeliefSetTo,
                                        Boolean showBeliefUpdateCause,
                                        Boolean showNewValue) {
        switch (logEntry.getLogEntryType()) {
            case MESSAGE:
                XWPFParagraph p = doc.createParagraph();
                p.setSpacingBefore(200);

                MessageLogEntry msgLog = (MessageLogEntry) logEntry;

                if (msgLog.getFromUser()) {
                    XWPFRun messageRun = p.createRun();
                    String msgLogString = ktPrefix + " " + msgLog.getMessage();

                    messageRun.setText(String.format("%s%n", msgLogString));
                } else {
                    if (msgLog.getIntention() != null) {
                        XWPFRun intentionRun = p.createRun();
                        intentionRun.setItalic(true);

                        String fullDesireName = desireService
                                .getByAgentIdAndName(agent.getId(), msgLog.getIntention().toString()).getFullName();

                        if (showAbbreviations) {
                            fullDesireName = msgLog.getIntention().toString() + ": " + fullDesireName;
                        }

                        String intentionLogString = intentionPrefix + "\t" + fullDesireName;

                        intentionRun.setText(String.format("%s%n", intentionLogString));
                        intentionRun.addBreak();
                    }
                    XWPFRun messageRun = p.createRun();

                    String msgLogString = liloPrefix + " " + msgLog.getMessage();

                    messageRun.setText(String.format("%s%n", msgLogString));
                }
                break;
            case BELIEF_UPDATE:
                BeliefUpdateLogEntry beliefLog = (BeliefUpdateLogEntry) logEntry;

                switch (beliefLog.getBeliefUpdateType()) {
                    case INCREASE:
                    case DECREASE:
                        createBeliefUpdateReportLine(doc, beliefLog, agent,
                                showNewValue, showBeliefUpdateCause, showAbbreviations);
                        break;
                    case SET_TO:
                        if (showBeliefSetTo) {
                            createBeliefUpdateReportLine(doc, beliefLog, agent,
                                    showNewValue, showBeliefUpdateCause, showAbbreviations);
                        }
                        break;
                    default:
                        break;
                }
                break;
            case DESIRE_UPDATE:
                if (showDesireUpdate) {
                    DesireUpdateLogEntry desireLog = (DesireUpdateLogEntry) logEntry;

                    p = doc.createParagraph();
                    p.setSpacingBefore(200);

                    XWPFRun desireUpdateRun = p.createRun();
                    desireUpdateRun.setItalic(true);

                    String fullDesireName = desireService
                            .getByAgentIdAndName(agent.getId(), desireLog.getDesireName().toString()).getFullName();

                    if (showAbbreviations) {
                        fullDesireName = desireLog.getDesireName().toString() + ": " + fullDesireName;
                    }

                    String desireLogString = getDesireUpdatePrefix(desireLog.getNewValue()) + "\t" + fullDesireName;

                    desireUpdateRun.setText(String.format("%s%n", desireLogString));
                }
                break;
            default:
                break;
        }
    }

    /**
     * Creates a line in the report for a belief update log entry.
     *
     * @param doc the doc to write to
     * @param beliefLog the log to write a report entry of
     * @param agent the agent to which the report belongs
     * @param showNewBeliefValue boolean for showing the numeric belief value after the update
     * @param showBeliefUpdateCause boolean for showing the cause of the belief update
     * @param showAbbreviations boolean for showing abbreviations, e.g. 'B3'
     */
    private void createBeliefUpdateReportLine(XWPFDocument doc, BeliefUpdateLogEntry beliefLog, Agent agent,
                                              Boolean showNewBeliefValue,
                                              Boolean showBeliefUpdateCause,
                                              Boolean showAbbreviations) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);

        XWPFRun beliefUpdateRun = p.createRun();
        beliefUpdateRun.setItalic(true);

        String fullBeliefName = beliefService
                .getByAgentIdAndName(agent.getId(), beliefLog.getBeliefName().toString()).getFullName();

        if (showAbbreviations) {
            fullBeliefName = beliefLog.getBeliefName().toString() + ": " + fullBeliefName;
        }

        String options = (showNewBeliefValue ? "\s(" + String.format("%.2f", beliefLog.getValue()) + ")" : "");

        String beliefLogString = getBeliefUpdatePrefix(beliefLog.getBeliefUpdateType())
                + options + "\t" + fullBeliefName;

        beliefUpdateRun.setText(String.format("%s%n", beliefLogString));

        if (showBeliefUpdateCause) {
            XWPFRun updateCauseRun = p.createRun();
            updateCauseRun.setItalic(true);
            updateCauseRun.setColor("888888");

            String updateCauseString = formatUpdateCause(beliefLog);

            updateCauseRun.setText(String.format("%s%n", "(" + updateCauseString + ")"));
        }
    }

    /**
     * Formats the belief update cause. When an update is set to be a manual update, the cause should not be in
     * quotation marks. Manual update causes are already initiated since the text can differ depending on whether it
     * was a direct manual update or an update of a belief that was dependent on the direct update. When the update
     * is not manual, but the cause is null, then the update must have been caused by a trigger, so "Trigger" is shown
     * as the cause with no quotation marks. All other causes must be KT's messages, so these are shown in quotation
     * marks.
     *
     * @param beliefUpdateLogEntry the belief update log to format the cause of
     * @return the update cause as a String
     */
    private String formatUpdateCause(BeliefUpdateLogEntry beliefUpdateLogEntry) {
        if (beliefUpdateLogEntry.getIsManualUpdate()) {
            return beliefUpdateLogEntry.getCause();
        } else if (beliefUpdateLogEntry.getCause() == null) {
            return "Trigger";
        } else {
            return "\"" + beliefUpdateLogEntry.getCause() + "\"";
        }
    }

    /**
     * Gets the prefix for a belief update type.
     *
     * @param beliefUpdateType the belief update type to get the prefix for
     * @return the update prefix
     */
    private String getBeliefUpdatePrefix(BeliefUpdateType beliefUpdateType) {
        return switch(beliefUpdateType) {
            case INCREASE -> beliefIncrease;
            case DECREASE -> beliefDecrease;
            case SET_TO -> beliefSetTo;
        };
    }

    /**
     * Gets the prefix for a desire update. The new value is a boolean, so "true" means it increased, and "false"
     * means it decreased.
     *
     * @param desireValue the new value of the desire
     * @return the update prefix
     */
    private String getDesireUpdatePrefix(Boolean desireValue) {
        if (desireValue) {
            return desireIncrease;
        } else {
            return desireDecrease;
        }
    }

    /**
     * Deletes the file found at the given path.
     *
     * @param filePath the filePath to delete the file of
     */
    public boolean deleteReport(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("File " + filePath + " deleted successfully.");
                } else {
                    System.err.println("Failed to delete file " + filePath + ".");
                    return false;
                }
            } else {
                System.out.println("File does not exist.");
            }
        }
        return true;
    }

    private String floatToPercentage(float value) {
        int result = Math.round(value * percentage);
        return Integer.toString(result).concat("%");
    }

    private String calculateDifference(float begin, float end) {
        int result = Math.round((end-begin) * percentage);
        return Integer.toString(result).concat("%");
    }


    private String generateSasToken(BlobClient blobClient) {

        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission blobSasPermission =  new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues serviceSasValues = new BlobServiceSasSignatureValues(expiryTime, blobSasPermission);
        String sasToken = blobClient.generateSas(serviceSasValues);

        String urlWithToken = blobClient.getBlobUrl() + "?" + sasToken;

        System.out.println("file url: "  + blobClient.getBlobUrl() + "?" + sasToken);

        return urlWithToken;
    }

}
