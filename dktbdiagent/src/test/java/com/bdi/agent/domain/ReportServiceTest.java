package com.bdi.agent.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import com.bdi.agent.model.Desire;
import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BeliefUpdateType;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.util.BeliefUpdateLogEntry;
import com.bdi.agent.model.util.DesireUpdateLogEntry;
import com.bdi.agent.model.util.LogEntry;
import com.bdi.agent.model.util.MessageLogEntry;
import com.bdi.agent.service.BeliefService;
import com.bdi.agent.service.DesireService;
import com.bdi.agent.service.ReportService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"mockBeliefService", "mockDesireService"})
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReportServiceTest {

    @Autowired
    private transient ReportService reportService;

    @Autowired
    private transient BeliefService beliefService;

    @Autowired
    private transient DesireService desireService;

    @Test
    public void testFormatLogEntryForBasicReport() throws IOException {
        // Create a sample XWPFDocument, Agent, Belief and Desire
        XWPFDocument doc = new XWPFDocument();
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, null, false, null);
        Belief belief = new Belief(0L, agent, "B1", "Belief number one", "", 0.3f);
        Desire desire = new Desire(0L, agent, "D1", "Desire number one", false, null);

        when(beliefService.getByAgentIdAndName(1L, "B1")).thenReturn(belief);
        when(desireService.getByAgentIdAndName(1L, "D1")).thenReturn(desire);

        // Create log entries that should be written to te doc
        LogEntry KtMessageLogEntry = new MessageLogEntry("test message from KT", true, agent);
        LogEntry LiloMessageLogEntry = new MessageLogEntry("test reply", false, agent, DesireName.D1);
        LogEntry increaseBeliefLogEntry = new BeliefUpdateLogEntry(BeliefUpdateType.INCREASE, 0.5F, BeliefName.B1,
                "test message from KT", agent);
        LogEntry decreaseBeliefLogEntry = new BeliefUpdateLogEntry(BeliefUpdateType.DECREASE, 0.2F, BeliefName.B1,
                "test message from KT", agent);

        // Create log entries that should not be written to the doc
        LogEntry desireUpdateLogEntry = new DesireUpdateLogEntry(true, DesireName.D1, agent);
        LogEntry setToValueBeliefLogEntry = new BeliefUpdateLogEntry(BeliefUpdateType.SET_TO, 0.2F, BeliefName.B1,
                "test message from KT", agent);

        // Call the method for each log to write to the doc

        reportService.formatLogEntryForReport(doc, KtMessageLogEntry, agent,
                false, false, false, false, false);

        // Call case where desire is updated but should not be shown
        reportService.formatLogEntryForReport(doc, desireUpdateLogEntry, agent,
                false, false, false, false, false);
        reportService.formatLogEntryForReport(doc, LiloMessageLogEntry, agent,
                false, false, false, false, false);
        reportService.formatLogEntryForReport(doc, increaseBeliefLogEntry, agent,
                false, false, false, false, false);

        // Call case where belief is set to a value but should not be shown
        reportService.formatLogEntryForReport(doc, setToValueBeliefLogEntry, agent,
                false, false, false, false, false);

        reportService.formatLogEntryForReport(doc, decreaseBeliefLogEntry, agent,
                false, false, false, false, false);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.write(outputStream);

        // Expected results
        String expectedKtMessage = "KT: test message from KT\n";
        String expectedIntentionAndLiloReply = """
                Intentie:	Desire number one
                                
                Lilo: test reply
                """;
        String expectedIncreaseBelief = "Overtuiging: ↑\tBelief number one\n";
        String expectedDecreaseBelief = "Overtuiging: ↓\tBelief number one\n";

        // Get content of the document as a human-readable string
        List<XWPFParagraph> paragraphs = doc.getParagraphs();

        String receivedKtMessage = paragraphs.get(0).getText().replaceAll("\\R", "\n");
        String receivedIntentionAndLiloReply = paragraphs.get(1).getText().replaceAll("\\R", "\n");
        String receivedIncreaseBelief = paragraphs.get(2).getText().replaceAll("\\R", "\n");
        String receivedDecreaseBelief = paragraphs.get(3).getText().replaceAll("\\R", "\n");

        // Assert content of documents is what we expect
        assertEquals(expectedKtMessage, receivedKtMessage);
        assertEquals(expectedIntentionAndLiloReply, receivedIntentionAndLiloReply);
        assertEquals(expectedIncreaseBelief, receivedIncreaseBelief);
        assertEquals(expectedDecreaseBelief, receivedDecreaseBelief);

        doc.close();
        outputStream.close();
    }

    @Test
    public void testFormatLogEntryForAdvancedReport() throws IOException {
        // Create a sample XWPFDocument, Agent, Belief and Desire
        XWPFDocument doc = new XWPFDocument();
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, null, false, null);
        Belief belief = new Belief(0L, agent, "B1", "Belief number one", "", 0.3f);
        Desire desire = new Desire(0L, agent, "D1", "Desire number one", false, null);

        when(beliefService.getByAgentIdAndName(1L, "B1")).thenReturn(belief);
        when(desireService.getByAgentIdAndName(1L, "D1")).thenReturn(desire);

        // Create log entries that should be written to te doc
        LogEntry LiloMessageLogEntry = new MessageLogEntry("test reply", false, agent, DesireName.D1);
        LogEntry increaseBeliefLogEntry = new BeliefUpdateLogEntry(BeliefUpdateType.INCREASE, 0.5F, BeliefName.B1,
                "test message from KT", agent);
        LogEntry decreaseBeliefLogEntry = new BeliefUpdateLogEntry(BeliefUpdateType.DECREASE, 0.2F, BeliefName.B1,
                "test message from KT", agent);
        LogEntry desireUpdateTrueLogEntry = new DesireUpdateLogEntry(true, DesireName.D1, agent);
        LogEntry desireUpdateFalseLogEntry = new DesireUpdateLogEntry(false, DesireName.D1, agent);
        LogEntry setToValueBeliefLogEntry = new BeliefUpdateLogEntry(BeliefUpdateType.SET_TO, 0.2F, BeliefName.B1,
                "test message from KT", agent);

        // Call case where desire is updated and should be shown without abbreviation
        reportService.formatLogEntryForReport(doc, desireUpdateTrueLogEntry, agent,
                false, true, false, false, false);

        // Call case where desire is updated and should be shown with abbreviation
        reportService.formatLogEntryForReport(doc, desireUpdateFalseLogEntry, agent,
                true, true, false, false, false);

        // Call case where intention should be shown with an abbreviation
        reportService.formatLogEntryForReport(doc, LiloMessageLogEntry, agent,
                true, false, false, false, false);

        // Call case where belief should be shown with an abbreviation
        reportService.formatLogEntryForReport(doc, increaseBeliefLogEntry, agent,
                true, false, false, false, false);

        // Call case where belief should be shown with the update cause
        reportService.formatLogEntryForReport(doc, increaseBeliefLogEntry, agent,
                false, false, false, true, false);

        // Call case where belief is set to a value and should be shown
        reportService.formatLogEntryForReport(doc, setToValueBeliefLogEntry, agent,
                false, false, true, false, false);

        // Call case where belief is updated and the new value should be shown
        reportService.formatLogEntryForReport(doc, decreaseBeliefLogEntry, agent,
                false, false, false, false, true);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.write(outputStream);

        // Expected results
        String expectedDesireUpdateTrue = "Verlangen: ↑\tDesire number one\n";
        String expectedDesireFalseAbbreviation = "Verlangen: ↓\tD1: Desire number one\n";
        String expectedIntentionAbbreviation = """
                Intentie:	D1: Desire number one
                                
                Lilo: test reply
                """;
        String expectedBeliefAbbreviation = "Overtuiging: ↑\tB1: Belief number one\n";
        String expectedBeliefWithCause = "Overtuiging: ↑\tBelief number one\n(\"test message from KT\")\n";
        String expectedBeliefSetTo = "Overtuiging: →\tBelief number one\n";
        String expectedBeliefNewValue = "Overtuiging: ↓ (0.20)\tBelief number one\n";


        // Get content of the document as a human-readable string
        List<XWPFParagraph> paragraphs = doc.getParagraphs();

        String receivedDesireUpdateTrue = paragraphs.get(0).getText().replaceAll("\\R", "\n");
        String receivedDesireFalseAbbreviation = paragraphs.get(1).getText().replaceAll("\\R", "\n");
        String receivedIntentionAbbreviation = paragraphs.get(2).getText().replaceAll("\\R", "\n");
        String receivedBeliefAbbreviation = paragraphs.get(3).getText().replaceAll("\\R", "\n");
        String receivedBeliefWithCause = paragraphs.get(4).getText().replaceAll("\\R", "\n");
        String receivedBeliefSetTo = paragraphs.get(5).getText().replaceAll("\\R", "\n");
        String receivedBeliefNewValue = paragraphs.get(6).getText().replaceAll("\\R", "\n");

        // Assert content of documents is what we expect
        assertEquals(expectedDesireUpdateTrue, receivedDesireUpdateTrue);
        assertEquals(expectedDesireFalseAbbreviation, receivedDesireFalseAbbreviation);
        assertEquals(expectedIntentionAbbreviation, receivedIntentionAbbreviation);
        assertEquals(expectedBeliefAbbreviation, receivedBeliefAbbreviation);
        assertEquals(expectedBeliefWithCause, receivedBeliefWithCause);
        assertEquals(expectedBeliefSetTo, receivedBeliefSetTo);
        //assertEquals(expectedBeliefNewValue, receivedBeliefNewValue);

        doc.close();
        outputStream.close();
    }

    @Test
    public void testNonMessageCausesForAdvancedReport() throws IOException {
        // Create a sample XWPFDocument, Agent, Belief and Desire
        XWPFDocument doc = new XWPFDocument();
        Agent agent = new Agent(1L, "testId", "test", null, null, null, 0L, "", true, 0L, 0.0f, null, false, null);
        Belief belief = new Belief(0L, agent, "B1", "Belief number one", "", 0.3f);

        when(beliefService.getByAgentIdAndName(1L, "B1")).thenReturn(belief);

        LogEntry triggerBeliefUpdateLog = new BeliefUpdateLogEntry(BeliefUpdateType.INCREASE, 0.5F, BeliefName.B1,
                null, agent);
        LogEntry manualBeliefUpdateLog = new BeliefUpdateLogEntry(BeliefUpdateType.DECREASE, 0.2F, BeliefName.B1,
                "Handmatige update", agent, true);


        // Call case where belief is set to a value and should be shown
        reportService.formatLogEntryForReport(doc, triggerBeliefUpdateLog, agent,
                false, false, false, true, false);

        // Call case where belief is updated and the new value should be shown
        reportService.formatLogEntryForReport(doc, manualBeliefUpdateLog, agent,
                false, false, false, true, false);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.write(outputStream);

        // Expected results
        String expectedTriggerCause = "Overtuiging: ↑\tBelief number one\n(Trigger)\n";
        String expectedManualCause = "Overtuiging: ↓\tBelief number one\n(Handmatige update)\n";


        // Get content of the document as a human-readable string
        List<XWPFParagraph> paragraphs = doc.getParagraphs();

        String receivedTriggerCause = paragraphs.get(0).getText().replaceAll("\\R", "\n");
        String receivedManualCause = paragraphs.get(1).getText().replaceAll("\\R", "\n");

        // Assert content of documents is what we expect
        assertEquals(expectedTriggerCause, receivedTriggerCause);
        assertEquals(expectedManualCause, receivedManualCause);

        doc.close();
        outputStream.close();
    }

}
