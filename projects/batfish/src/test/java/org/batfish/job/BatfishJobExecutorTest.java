package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.config.Settings;
import org.batfish.datamodel.answers.AnswerElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link BatfishJobExecutor}. */
class BatfishJobExecutorTest {

  private static final long TEST_ELAPSED_TIME = 1000L;
  private static final String TEST_EXECUTOR_DESC = "Test Job Executor";
  BatfishLogger _logger;

  @BeforeEach
  void setup() {
    _logger = new BatfishLogger(BatfishLogger.LEVELSTR_INFO, false);
  }

  @Test
  void testExecuteJobsResults() {
    Settings settings = new Settings();

    List<BfTestJob> jobs = new ArrayList<>();
    jobs.add(new BfTestJob(settings, "result1"));
    jobs.add(new BfTestJob(settings, "result2"));

    Set<String> output = new HashSet<>();
    BfTestAnswerElement ae = new BfTestAnswerElement();
    BatfishJobExecutor.runJobsInExecutor(
        settings, _logger, jobs, output, ae, false, TEST_EXECUTOR_DESC);

    // checking the outputs produced by the tasks
    assertEquals(output, Sets.newHashSet("result1", "result2"));
  }

  @Test
  void testHandleJobResultSuccess() {
    Settings settings = new Settings();

    // initializing executor
    BatfishJobExecutor executor = BatfishJobExecutor.getBatfishJobExecutor(settings, _logger);
    executor.initializeJobsStats(
        Lists.newArrayList(new BfTestJob(settings, "result1")), TEST_EXECUTOR_DESC);

    // Simulating finishing of a job and handling the result
    // initiating a separate logger from the Executor logger
    BatfishLogger jobLogger = new BatfishLogger(BatfishLogger.LEVELSTR_INFO, false);
    BfTestResult bfTestResult =
        new BfTestResult(TEST_ELAPSED_TIME, jobLogger.getHistory(), "result");
    Set<String> output = new HashSet<>();
    List<BatfishException> failureCauses = new ArrayList<>();
    BfTestAnswerElement ae = new BfTestAnswerElement();
    executor.markJobCompleted();
    executor.handleJobResult(bfTestResult, output, ae, failureCauses, false);

    // checking the log of the executor for the job finished
    assertEquals(
        _logger.getHistory().toString(BatfishLogger.LEVEL_INFO),
        executor.getSuccessMessage(bfTestResult));
  }

  @Test
  void testHandleJobResultFailure() {
    Settings settings = new Settings();

    // initializing executor
    BatfishJobExecutor executor = BatfishJobExecutor.getBatfishJobExecutor(settings, _logger);
    executor.initializeJobsStats(
        Lists.newArrayList(new BfTestJob(settings, "result1")), TEST_EXECUTOR_DESC);

    // Simulating failure of a job and handling the result
    // initiating a separate logger from the Executor logger
    BatfishLogger jobLogger = new BatfishLogger(BatfishLogger.LEVELSTR_INFO, false);
    BfTestResult bfTestResult =
        new BfTestResult(
            TEST_ELAPSED_TIME,
            jobLogger.getHistory(),
            new BatfishException("Test Job Failure Message"));

    Set<String> output = new HashSet<>();
    List<BatfishException> failureCauses = new ArrayList<>();
    BfTestAnswerElement ae = new BfTestAnswerElement();
    executor.markJobCompleted();
    executor.handleJobResult(bfTestResult, output, ae, failureCauses, false);

    // checking that correct failure message is written in the log
    assertEquals(failureCauses.get(0).getMessage(), executor.getFailureMessage(bfTestResult));
  }

  @Test
  void testHandleProcessingError() {
    Settings settings = new Settings();

    // initializing executor
    BatfishJobExecutor executor = BatfishJobExecutor.getBatfishJobExecutor(settings, _logger);
    List<BatfishException> failureCauses = new ArrayList<>();

    // checking if the exception thrown has correct class
    Throwable exception =
        assertThrows(
            BatfishException.class,
            () ->
                executor.handleProcessingError(
                    Lists.newArrayList(new BfTestJob(settings, "result1")), failureCauses, true));
    assertThat(exception.getMessage(), containsString(BatfishJobExecutor.JOB_FAILURE_MESSAGE));
  }

  /** Class for Batfish test job */
  private class BfTestJob extends BatfishJob<BfTestResult> {
    private String _testValue;

    public BfTestJob(Settings settings, String testValue) {
      super(settings);
      _testValue = testValue;
    }

    @Override
    public BfTestResult call() {
      long startTime = System.currentTimeMillis();
      return new BfTestResult(
          System.currentTimeMillis() - startTime, _logger.getHistory(), _testValue);
    }
  }

  private class BfTestResult extends BatfishJobResult<Set<String>, BfTestAnswerElement> {
    private String _result;

    public BfTestResult(long elapsedTime, BatfishLoggerHistory history, String result) {
      super(elapsedTime, history);
      _result = result;
    }

    public BfTestResult(long elapsedTime, BatfishLoggerHistory history, Throwable failureCause) {
      super(elapsedTime, history, failureCause);
    }

    @Override
    public void applyTo(
        Set<String> output, BatfishLogger logger, BfTestAnswerElement answerElement) {
      output.add(_result);
      answerElement.getOutputs().add(_result);
    }

    @Override
    public void appendHistory(BatfishLogger logger) {
      logger.append(_history, "");
    }
  }

  private class BfTestAnswerElement extends AnswerElement {
    private Set<String> _outputs;

    public BfTestAnswerElement() {
      _outputs = new HashSet<>();
    }

    public Set<String> getOutputs() {
      return _outputs;
    }
  }
}
