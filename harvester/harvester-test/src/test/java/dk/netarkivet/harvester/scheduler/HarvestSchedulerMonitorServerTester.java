/*
 * #%L
 * Netarchivesuite - harvester - test
 * %%
 * Copyright (C) 2005 - 2014 The Royal Danish Library, the Danish State and University Library,
 *             the National Library of France and the Austrian National Library.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

package dk.netarkivet.harvester.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import javax.jms.JMSException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.distribute.Channels;
import dk.netarkivet.common.distribute.JMSConnectionFactory;
import dk.netarkivet.common.distribute.JMSConnectionMockupMQ;
import dk.netarkivet.common.distribute.NetarkivetMessage;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.RememberNotifications;
import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.harvester.datamodel.Constants;
import dk.netarkivet.harvester.datamodel.DatabaseTestUtils;
import dk.netarkivet.harvester.datamodel.Domain;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
import dk.netarkivet.harvester.datamodel.DomainDAO;
import dk.netarkivet.harvester.datamodel.DomainDAOTester;
import dk.netarkivet.harvester.datamodel.FullHarvest;
import dk.netarkivet.harvester.datamodel.HarvestChannel;
import dk.netarkivet.harvester.datamodel.HarvestDAOUtils;
import dk.netarkivet.harvester.datamodel.HarvestDefinition;
import dk.netarkivet.harvester.datamodel.HarvestDefinitionDAO;
import dk.netarkivet.harvester.datamodel.HarvestDefinitionDAOTester;
import dk.netarkivet.harvester.datamodel.HarvestInfo;
import dk.netarkivet.harvester.datamodel.Job;
import dk.netarkivet.harvester.datamodel.JobDAO;
import dk.netarkivet.harvester.datamodel.JobStatus;
import dk.netarkivet.harvester.datamodel.ScheduleDAOTester;
import dk.netarkivet.harvester.datamodel.StopReason;
import dk.netarkivet.harvester.datamodel.TemplateDAOTester;
import dk.netarkivet.harvester.datamodel.dao.DAOProviderFactory;
import dk.netarkivet.harvester.harvesting.HeritrixFiles;
import dk.netarkivet.harvester.harvesting.JobInfoTestImpl;
import dk.netarkivet.harvester.harvesting.distribute.CrawlStatusMessage;
import dk.netarkivet.harvester.harvesting.report.AbstractHarvestReport;
import dk.netarkivet.harvester.harvesting.report.LegacyHarvestReport;
import dk.netarkivet.testutils.LogbackRecorder;
import dk.netarkivet.testutils.TestFileUtils;
import dk.netarkivet.testutils.preconfigured.ReloadSettings;

/**
 * Tests of the class HarvestSchedulerMonitorServer.
 */
@SuppressWarnings({ "unused"})
@Ignore("binary derby database not converted to scripts yet")
public class HarvestSchedulerMonitorServerTester {

    public static JobDAO theDAO;
    public static final File BASEDIR = new File("tests/dk/netarkivet/harvester/scheduler/data/");
    public static final File ORIGINALS = new File(BASEDIR, "originals");
    public static final File WORKING = new File(BASEDIR, "working");
    private static final File CRAWL_REPORT = new File(WORKING, "harvestreports/crawl.log");
    private static final File STOP_REASON_CRAWL_REPORT = new File(WORKING, "harvestreports/stop-reason-crawl.log");
    private static final StopReason DEFAULT_STOPREASON = StopReason.DOWNLOAD_COMPLETE;
    ReloadSettings rs = new ReloadSettings();
    private HarvestSchedulerMonitorServer hsms;

    private static final HeritrixFiles HX_FILES =
        new HeritrixFiles(new File(WORKING, "harvestreports"), new JobInfoTestImpl(1L, 1L));

    /**
     * setUp method for this set of unit tests.
     */
    @Before
    public void setUp() throws IOException, SQLException,
            IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        rs.setUp();

        JMSConnectionMockupMQ.useJMSConnectionMockupMQ();
        HarvestDAOUtils.resetDAOs();
        Settings.set(CommonSettings.REMOTE_FILE_CLASS,
                     "dk.netarkivet.common.distribute.TestRemoteFile");
        FileUtils.removeRecursively(WORKING);
        TestFileUtils.copyDirectoryNonCVS(ORIGINALS, WORKING);
        //JobDAO.reset();
        Settings.set(CommonSettings.DB_BASE_URL, "jdbc:derby:"
                + WORKING.getCanonicalPath() + "/fullhddb");
        DatabaseTestUtils.getHDDB("/" + BASEDIR + "/fullhddb.sql",
                "fullhddb", WORKING);
        theDAO = JobDAO.getInstance();
        Settings.set(CommonSettings.NOTIFICATIONS_CLASS,
                     RememberNotifications.class.getName());
        hsms = new HarvestSchedulerMonitorServer();
        hsms.start();
    }
    /**
     * tearDown method for this set of unit tests.
     */
    @After
    public void tearDown() throws SQLException, IllegalAccessException, NoSuchFieldException {
        HarvestDAOUtils.resetDAOs();
        FileUtils.removeRecursively(WORKING);
        JMSConnectionMockupMQ.clearTestQueues();
        hsms.shutdown();
        DatabaseTestUtils.dropHDDB();
        JobDAO.reset();
        HarvestDefinitionDAOTester.resetDAO();
        DomainDAOTester.resetDomainDAO();
        ScheduleDAOTester.resetDAO();
        TemplateDAOTester.resetTemplateDAO();
        rs.tearDown();
    }

    /** Tests that default onMessage is used.
     * @throws JMSException
     */
    @Test
    public void testOnMessageUsesUnpack() throws JMSException {
        NetarkivetMessage nmsg = new CrawlStatusMessage(1, JobStatus.STARTED);
        JMSConnectionMockupMQ.TestObjectMessage omsg
                = (JMSConnectionMockupMQ.TestObjectMessage)
                JMSConnectionMockupMQ.getObjectMessage(nmsg);
        omsg.id = "IDXXX";
        hsms.onMessage(omsg);
        assertEquals("NetarchiveMessage should have the same ID as JMS message",
                     "IDXXX", nmsg.getID());
    }

    /**
     * Test that HSMS actually listens to THE_SCHED (see bug 203).
     */
    @Test
    public void testListens() {
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory
                .getInstance();
        assertEquals("Should be exactly one listener to the THE_SCHED queue ",
                     1, con.getListeners(Channels.getTheSched()).size());
        hsms.shutdown();
        assertEquals("Should have removed listener to the THE_SCHED queue ",
                     0, con.getListeners(Channels.getTheSched()).size());
    }

    /**
     * Test that we can call onMessage with the expected sequence of messages
     * for a successful crawl job.
     */
    @Test
    public void testOnMessageGoodJob() {
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory
                .getInstance();
        Job j1 = dk.netarkivet.harvester.scheduler.TestInfo.getJob();
        theDAO.create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Set job status to submitted
        j1.setStatus(JobStatus.SUBMITTED);
        theDAO.update(j1);

        // Send a message job-started message to onMessage
        CrawlStatusMessage csm_start = new
                CrawlStatusMessage(j1ID, JobStatus.STARTED);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_start));
        // Send a job-done message
        AbstractHarvestReport hhr = new LegacyHarvestReport(HX_FILES);
        CrawlStatusMessage csm_done = new
                CrawlStatusMessage(j1ID, JobStatus.DONE, hhr);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_done));
        // Job should now have status "done"
        j1 = theDAO.read(Long.valueOf(j1ID));
        assertEquals("Job should have status DONE: ",
                JobStatus.DONE, j1.getStatus());
        //Look for the domain persistence
        Domain nk_domain = DomainDAO.getInstance().read("netarkivet.dk");
        Iterator<HarvestInfo> hist = nk_domain.getHistory().getHarvestInfo();
        assertTrue("Should have one harvest remembered", hist.hasNext());
        HarvestInfo dh = (HarvestInfo) hist.next();
        assertEquals("Unexpected number of objects retireved",
                22, dh.getCountObjectRetrieved());
        assertEquals("Unexpected total size of harvest",
                270, dh.getSizeDataRetrieved());
        assertFalse("Should NOT have two harvests remembered", hist.hasNext());
    }

    /**
     * Tests what happens if a FAILED message arrives with a crawl report. The
     * behavior should be identical to the case with a DONE message.
     */
    @Test
    public void testOnMessageFailedJobWithReport() {
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory
                .getInstance();
        Job j1 = TestInfo.getJob();
        JobDAO.getInstance().create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Set job status to submitted
        j1.setStatus(JobStatus.SUBMITTED);
        theDAO.update(j1);

        // Send a message job-started message to onMessage
        CrawlStatusMessage csm_start = new
                CrawlStatusMessage(j1ID, JobStatus.STARTED);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_start));

        // Send a job-failed message
        AbstractHarvestReport hhr = new LegacyHarvestReport(HX_FILES);
        CrawlStatusMessage csm_failed = new
                CrawlStatusMessage(j1ID, JobStatus.FAILED, hhr);
        csm_failed.setNotOk("Simulated failed message");
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_failed));
        // Job should now have status "done"
        j1 = theDAO.read(Long.valueOf(j1ID));
        assertEquals("Job should have status FAILED: ",
                JobStatus.FAILED, j1.getStatus());
        //Look for the domain persistence
        Domain nk_domain = DomainDAO.getInstance().read("netarkivet.dk");
        Iterator<HarvestInfo> hist = nk_domain.getHistory().getHarvestInfo();
        assertTrue("Should have one harvest remembered", hist.hasNext());
        HarvestInfo dh = (HarvestInfo) hist.next();
        assertEquals("Unexpected number of objects retireved",
                22, dh.getCountObjectRetrieved());
        assertFalse("Should NOT have two harvests remembered", hist.hasNext());
    }

    /**
     * Test that we can do a failed job with no (ie NULL_REMOTE_FILE) crawl
     * report returned.
     */
    @Test
    public void testOnMessageFailedJobNoReport() {
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory
                .getInstance();

        Job j1 = TestInfo.getJob();
        JobDAO.getInstance().create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Set job status to submitted
        j1.setStatus(JobStatus.SUBMITTED);
        theDAO.update(j1);

        // Send a message job-started message to onMessage
        CrawlStatusMessage csm_start = new
                CrawlStatusMessage(j1ID,
                                   JobStatus.STARTED);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_start));
        // Send a job-failed message
        CrawlStatusMessage csm_failed = new
                CrawlStatusMessage(j1ID,
                                   JobStatus.FAILED, null);
        csm_failed.setNotOk("Simulated failed message");
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_failed));
        // Job should now have status "failed"
        j1 = theDAO.read(Long.valueOf(j1ID));
        assertEquals("Job should have status FAILED: ", JobStatus.FAILED,
                     j1.getStatus());
    }

    //
    // Out of sequence message tests:
    // Make it a contractual requirement that status must be set to
    // SUBMITTED before the job is sent so any message received for a
    // job while it still has status NEW should throw an exception.
    //
    // STARTED messages are purely informational. If a DONE or FAILED message
    // arrives before a STARTED, process the DONE or FAILED and ignore the
    // STARTED.
    //
    // A FAILED overrides a DONE. A DONE does not override a FAILED
    //

    /**
     * Test that receiving a message on a NEW job results in an exception.
     */
    @Test
    public void testMessageWhileNew() {
        Job j1 = TestInfo.getJob();
        JobDAO.getInstance().create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Send a message job-started message to onMessage
        CrawlStatusMessage csm_start = new
                CrawlStatusMessage(j1ID,
                                   JobStatus.STARTED);
        hsms.visit(csm_start);
        j1 = theDAO.read(j1.getJobID());
        assertEquals("Status should now be started: ",
                     JobStatus.STARTED, j1.getStatus());
    }

    /**
     * Send a STARTED message after a DONE message. The STARTED message should
     * be ignored.
     */
    @Test
    public void testStartedAfterDone() {
    	LogbackRecorder lr = LogbackRecorder.startRecorder();
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory.getInstance();
        Job j1 = TestInfo.getJob();
        JobDAO.getInstance().create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Set job status to submitted
        j1.setStatus(JobStatus.SUBMITTED);
        theDAO.update(j1);
        //
        // Send the "done" message
        //
        AbstractHarvestReport hhr = new LegacyHarvestReport(HX_FILES);
        CrawlStatusMessage csm_done = new
                CrawlStatusMessage(j1ID, JobStatus.DONE, hhr);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_done));
        //
        // Send the STARTED message
        //
        CrawlStatusMessage csm_start = new
                CrawlStatusMessage(j1ID,
                                   JobStatus.STARTED);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_start));
        //
        //All usual tests should work and job status should still be done
        //
        j1 = theDAO.read(Long.valueOf(j1ID));
        assertEquals("Job should have status DONE: ",
                JobStatus.DONE, j1.getStatus());
        //Look for the domain persistence
        Domain nk_domain = DomainDAO.getInstance().read("netarkivet.dk");
        Iterator<HarvestInfo> hist = nk_domain.getHistory().getHarvestInfo();
        assertTrue("Should have one harvest remembered", hist.hasNext());
        HarvestInfo dh = (HarvestInfo) hist.next();
        assertEquals("Unexpected number of objects retrieved",
                22, dh.getCountObjectRetrieved());
        assertFalse("Should NOT have two harvests remembered", hist.hasNext());
        // Check log
        lr.assertLogContains("Failed to log out of order messages", "tried to update");
        lr.stopRecorder();
    }

    /**
     * Send a STARTED CrawlStatusMessage after a Failed message.
     * This STARTED message should be ignored.
     */
    @Test
    public void testStartedAfterFailed() {
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory.getInstance();
        Job j1 = TestInfo.getJob();
        JobDAO.getInstance().create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Set job status to submitted
        j1.setStatus(JobStatus.SUBMITTED);
        theDAO.update(j1);
        //
        // Send the "failed" message
        //
        AbstractHarvestReport hhr = new LegacyHarvestReport(HX_FILES);
        CrawlStatusMessage csm_failed = new
                CrawlStatusMessage(j1ID, JobStatus.FAILED, hhr);
        csm_failed.setNotOk("Simulated failed message");

        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_failed));
        //
        // Send the STARTED message
        //
        CrawlStatusMessage csm_start = new
                CrawlStatusMessage(j1ID,
                                   JobStatus.STARTED);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_start));
        //
        //All usual tests should work and job status should still be done
        //
        j1 = theDAO.read(Long.valueOf(j1ID));
        assertEquals("Job should have status Failed: ",
                JobStatus.FAILED, j1.getStatus());
        //Look for the domain persistence
        Domain nk_domain = DomainDAO.getInstance().read("netarkivet.dk");
        Iterator<HarvestInfo> hist = nk_domain.getHistory().getHarvestInfo();
        assertTrue("Should have one harvest remembered", hist.hasNext());
        HarvestInfo dh = (HarvestInfo) hist.next();
        assertEquals("Unexpected number of objects retrieved",
                22, dh.getCountObjectRetrieved());
        assertFalse("Should NOT have two harvests remembered", hist.hasNext());
    }

    /**
     * If FAILED arrives after DONE, the job is marked as FAILED.
     */
    @Test
    public void testFailedAfterDone() {
    	LogbackRecorder lr = LogbackRecorder.startRecorder();
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory.getInstance();
        Job j1 = TestInfo.getJob();
        theDAO.create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Set job status to submitted
        j1.setStatus(JobStatus.SUBMITTED);
        theDAO.update(j1);

        // Send a message job-started message to onMessage
        CrawlStatusMessage csm_start = new
                CrawlStatusMessage(j1ID, JobStatus.STARTED);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_start));
        // Send a job-done message
        AbstractHarvestReport hhr = new LegacyHarvestReport(HX_FILES);
        CrawlStatusMessage csm_done = new
                CrawlStatusMessage(j1ID, JobStatus.DONE, hhr);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_done));
        // Send a job-failed message
        CrawlStatusMessage csm_failed = new
                CrawlStatusMessage(j1ID,
                                   JobStatus.FAILED, null);
        csm_failed.setNotOk("Failed");
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_failed));
        // Job should now have status "failed"
        j1 = theDAO.read(Long.valueOf(j1ID));
        assertEquals("Job should have status Failed: ", JobStatus.FAILED, j1.getStatus());
        //Look for the domain persistence
        Domain nk_domain = DomainDAO.getInstance().read("netarkivet.dk");
        Iterator<HarvestInfo> hist = nk_domain.getHistory().getHarvestInfo();
        assertTrue("Should have one harvest remembered", hist.hasNext());
        HarvestInfo dh = (HarvestInfo) hist.next();
        assertFalse("Should NOT have two harvests remembered", hist.hasNext());
        assertEquals("Unexpected number of objects retrieved", 22, 
                dh.getCountObjectRetrieved());
        assertEquals("Unexpected total size of harvest", 270, 
                dh.getSizeDataRetrieved());
        // Check log
        lr.assertLogContains("Failed to log out of order messages", "Marking job FAILED");
        lr.stopRecorder();
    }

    /**
     * If DONE arrives after FAILED, the job should be marked FAILED.
     */
    @Test
     public void testDoneAfterFailed() {
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory.getInstance();
        Job j1 = TestInfo.getJob();
        theDAO.create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Set job status to submitted
        j1.setStatus(JobStatus.SUBMITTED);
        theDAO.update(j1);

        // Send a message job-started message to onMessage
        CrawlStatusMessage csm_start = new
                CrawlStatusMessage(j1ID, JobStatus.STARTED);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_start));
         // Send a job-failed message
        CrawlStatusMessage csm_failed = new
                CrawlStatusMessage(j1ID,
                                   JobStatus.FAILED, null);
        csm_failed.setNotOk("Simulated failed message");

        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_failed));
        // Send a job-done message
        AbstractHarvestReport hhr = new LegacyHarvestReport(HX_FILES);
        CrawlStatusMessage csm_done = new
                CrawlStatusMessage(j1ID, JobStatus.DONE, hhr);
        hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csm_done));
        // Job should now have status "failed"
        j1 = theDAO.read(Long.valueOf(j1ID));
        assertEquals("Job should have status Failed: ", JobStatus.FAILED, 
                j1.getStatus());
        //Look for the domain persistence
        Domain nk_domain = DomainDAO.getInstance().read("netarkivet.dk");
        Iterator<HarvestInfo> hist = nk_domain.getHistory().getHarvestInfo();
        assertTrue("Should have one harvest remembered", hist.hasNext());
        HarvestInfo dh = (HarvestInfo) hist.next();
        assertFalse("Should NOT have two harvests remembered", hist.hasNext());
        assertEquals("Unexpected number of objects retrieved", 22, 
                dh.getCountObjectRetrieved());
        assertEquals("Unexpected total size of harvest", 270, 
                dh.getSizeDataRetrieved());
    }

     /**
      * Test that receiving a "DONE" directly after a "SUBMITTED" runs ok
      * but is logged.
      */
    @Test
    public void testDoneAfterSubmitted() {
    	LogbackRecorder lr = LogbackRecorder.startRecorder();
        JMSConnectionMockupMQ con = (JMSConnectionMockupMQ) JMSConnectionFactory.getInstance();
        Job j1 = TestInfo.getJob();
        theDAO.create(j1);
        j1.setStatus(JobStatus.NEW);
        theDAO.update(j1);
        long j1ID = j1.getJobID().longValue();
        // Set job status to submitted
        j1.setStatus(JobStatus.SUBMITTED);
        theDAO.update(j1);


        // Send a job-done message
        AbstractHarvestReport hhr = new LegacyHarvestReport(HX_FILES);
        CrawlStatusMessage csmDone = new
                CrawlStatusMessage(j1ID, JobStatus.DONE, hhr);
         hsms.onMessage(JMSConnectionMockupMQ.getObjectMessage(csmDone));
        // Job should now have status "done"
        j1 = theDAO.read(Long.valueOf(j1ID));
        assertEquals("Job should have status DONE: ", JobStatus.DONE, 
                j1.getStatus());
        //Look for the domain persistence
        Domain nk_domain = DomainDAO.getInstance().read("netarkivet.dk");
        Iterator<HarvestInfo> hist = nk_domain.getHistory().getHarvestInfo();
        assertTrue("Should have one harvest remembered", hist.hasNext());
        HarvestInfo dh = (HarvestInfo) hist.next();
        assertFalse("Should NOT have two harvests remembered", hist.hasNext());
        assertEquals("Unexpected number of objects retrieved", 22, 
                dh.getCountObjectRetrieved());
        assertEquals("Unexpected total size of harvest", 270, 
                dh.getSizeDataRetrieved());
        // Check log
        lr.assertLogContains("Failed to log out of order messages", "unexpected state");
        lr.stopRecorder();
    }

    /** Test that the stop reason is set correctly.
     *
     * There are the following cases:
     * Completed domains are set as completed
     * Domains reaching object limit should be set as object limit reached
     * For domains reaching the byte limit there are the following cases:
     * - We reached the harvest byte limit but not yet the config limit
     * - We reached the config limit
     *
     * We use a crawl log with the following characteristics:
     * - statsbiblioteket.dk reached object limit
     * - dr.dk reached byte limit
     * - kb.dk harvested complete
     *
     * (netarkivet.dk we fiddle with harvest and config limit to see
     * results but use the same crawllog)
     *
      */
    @Test
    public void testStopReasonSetCorrectly() {
        //Three domains to test on
        DomainDAO.getInstance().create(Domain.getDefaultDomain("kb.dk"));
        DomainDAO.getInstance().create(Domain.getDefaultDomain(
                "statsbiblioteket.dk"));
        DomainDAO.getInstance().create(Domain.getDefaultDomain("dr.dk"));

        //The host report we use
        AbstractHarvestReport hhr = new LegacyHarvestReport(HX_FILES);

        //A harvest definition with no limit
        HarvestDefinition snapshot =
            new FullHarvest("TestHarvest", "", null,
                    Constants.HERITRIX_MAXOBJECTS_INFINITY,
                    Constants.HERITRIX_MAXBYTES_INFINITY,
                    Constants.HERITRIX_MAXJOBRUNNINGTIME_INFINITY, false,
                    DAOProviderFactory.getHarvestDefinitionDAOProvider(),
                    DAOProviderFactory.getJobDAOProvider(),
                    DAOProviderFactory.getExtendedFieldDAOProvider(),
                    DAOProviderFactory.getDomainDAOProvider());
        HarvestDefinitionDAO.getInstance().create(snapshot);

        //A job from that harvest
        Domain dom = DomainDAO.getInstance().read("kb.dk");
        DomainConfiguration conf = dom.getDefaultConfiguration();
        Job job = Job.createSnapShotJob(
        		snapshot.getOid(),
        		new HarvestChannel("test", false, true, ""),
        		conf,
                Constants.HERITRIX_MAXOBJECTS_INFINITY,
                Constants.HERITRIX_MAXBYTES_INFINITY, 
                Constants.HERITRIX_MAXJOBRUNNINGTIME_INFINITY,
                0);
        dom = DomainDAO.getInstance().read("statsbiblioteket.dk");
        conf = dom.getDefaultConfiguration();
        job.addConfiguration(conf);
        dom = DomainDAO.getInstance().read("dr.dk");
        conf = dom.getDefaultConfiguration();
        job.addConfiguration(conf);
        job.setStatus(JobStatus.STARTED);
        JobDAO.getInstance().create(job);

        //A crawl status message received with the host report
        CrawlStatusMessage csmDone = new
                CrawlStatusMessage(job.getJobID(), JobStatus.DONE, hhr);

        //Receive the message
        hsms.visit(csmDone);

        //Check correct historyinfo for kb.dk: complete
        dom = DomainDAO.getInstance().read("kb.dk");
        HarvestInfo dh = dom.getHistory().getSpecifiedHarvestInfo(
                snapshot.getOid(),
                dom.getDefaultConfiguration().getName());
        assertEquals("Should have expected number of objects retrieved",
                1, dh.getCountObjectRetrieved());
        assertEquals("Should have expected total size of harvest",
                521, dh.getSizeDataRetrieved());
        assertEquals("Should be marked as complete",
                StopReason.DOWNLOAD_COMPLETE, dh.getStopReason());

        //Check correct historyinfo for statsbiblioteket.dk: object limit
        dom = DomainDAO.getInstance().read("statsbiblioteket.dk");
        dh = dom.getHistory().getSpecifiedHarvestInfo(snapshot.getOid(),
                dom.getDefaultConfiguration().getName());
        assertEquals("Should have expected number of objects retrieved",
                0, dh.getCountObjectRetrieved());
        assertEquals("Should have expected total size of harvest",
                0, dh.getSizeDataRetrieved());
        assertEquals("Should be marked as stopped due to config object limit",
                StopReason.CONFIG_OBJECT_LIMIT, dh.getStopReason());

        //Check correct historyinfo for dr.dk: size limit - config limit
        // is lowest, so this should be a size_limit
        dom = DomainDAO.getInstance().read("dr.dk");
        dh = dom.getHistory().getSpecifiedHarvestInfo(snapshot.getOid(),
                dom.getDefaultConfiguration().getName());
        assertEquals("Should have expected number of objects retrieved",
                2, dh.getCountObjectRetrieved());
        assertEquals("Should have expected total size of harvest",
                580, dh.getSizeDataRetrieved());
        assertEquals("Should be marked as stopped due to size limit",
                StopReason.CONFIG_SIZE_LIMIT, dh.getStopReason());

        //A harvest definition with low byte limit
        snapshot = new FullHarvest("TestHarvest2", "", null,
                Constants.HERITRIX_MAXOBJECTS_INFINITY, 
                10L,
                Constants.DEFAULT_MAX_JOB_RUNNING_TIME, false,
                DAOProviderFactory.getHarvestDefinitionDAOProvider(),
                DAOProviderFactory.getJobDAOProvider(),
                DAOProviderFactory.getExtendedFieldDAOProvider(),
                DAOProviderFactory.getDomainDAOProvider());
        HarvestDefinitionDAO.getInstance().create(snapshot);

        //A job from that harvest (note: conf is the dr.dk config)
        job = Job.createSnapShotJob(
        		snapshot.getOid(),
        		new HarvestChannel("test", false, true, ""),
        		conf,
                Constants.HERITRIX_MAXOBJECTS_INFINITY, 10L, 
                Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
        job.setStatus(JobStatus.STARTED);
        JobDAO.getInstance().create(job);

        //A message with a host report
        csmDone = new
                CrawlStatusMessage(job.getJobID(), JobStatus.DONE, hhr);

        //Receive the message
        hsms.visit(csmDone);

        //Check correct historyinfo for dr.dk: Harvest limit is lowest and wins
        dom = DomainDAO.getInstance().read("dr.dk");
        dh = dom.getHistory().getSpecifiedHarvestInfo(snapshot.getOid(),
                dom.getDefaultConfiguration().getName());
        assertEquals("Should have expected number of objects retrieved",
                2, dh.getCountObjectRetrieved());
        assertEquals("Should have expected total size of harvest",
                580, dh.getSizeDataRetrieved());
        assertEquals("Should be marked as size limit reached",
                StopReason.SIZE_LIMIT, dh.getStopReason());
    }
}