package org.batfish.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CompositeBatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.main.Driver;

/**
 * Class to execute a list of jobs in a thread pool of adaptable size using {@link Executors}.
 * The execution can be forced to be sequential by setting the appropriate property in {@link
 * Settings}
 *
 * @param <JobT> Type of job submitted to the executor
 * @param <AnswerElementT> Type of {@link AnswerElement} to which job result will be applied
 * @param <JobResultT> Type of {@link BatfishJobResult} which will contain the result of the job
 * @param <OutputT> Type of data structure to which job result will be applied
 */
public class BatfishJobExecutor<
    JobT extends BatfishJob<JobResultT>,
    AnswerElementT extends AnswerElement,
    JobResultT extends BatfishJobResult<OutputT, AnswerElementT>,
    OutputT> {

  private final String _description;

  private final boolean _haltOnProcessingError;

  private final BatfishLogger _logger;

  private final Settings _settings;

  private int _finishedJobs;

  private int _totalJobs;

  private AtomicInteger _completed;

  private double _finishedPercent;

  public BatfishJobExecutor(
      Settings settings, BatfishLogger logger, boolean haltOnProcessingError, String description) {
    _settings = settings;
    _logger = logger;
    _haltOnProcessingError = haltOnProcessingError;
    _description = description;
  }

  /**
   * Executes jobs in a thread pool or in a single thread executor depending on settings
   *
   * @param jobs Jobs to be executed
   * @param output Data structure to which the result of the job will be applied, it should be of
   *     generic type {@link OutputT}
   * @param answerElement {@link AnswerElement} containing the detail of the jobs executed
   */
  public void executeJobs(List<JobT> jobs, OutputT output, AnswerElementT answerElement) {
    //Initializing executors
    ExecutorService pool = createExecutorService();
    ExecutorCompletionService<JobResultT> completionService = new ExecutorCompletionService<>(pool);

    if (!_settings.getSequential() && _settings.getShuffleJobs()) {
      Collections.shuffle(jobs);
    }

    for (JobT job : jobs) {
      completionService.submit(job);
    }

    initializeJobsStats(jobs);
    boolean processingError = false;
    List<BatfishException> failureCauses = new ArrayList<>();

    for (int i = 0; i < jobs.size(); i++) {

      JobResultT result = null;
      try {
        // getting the result of the job
        result = completionService.take().get();
      } catch (InterruptedException e) {
        pool.shutdown();
        throw new BatfishException("Job didn't finish", e);
      } catch (ExecutionException e) {
        pool.shutdown();
        throw new BatfishException("Error executing job", e);
      }

      markJobCompleted();
      boolean jobResultError = handleJobResult(result, output, answerElement, failureCauses);
      if (jobResultError) {
        processingError = true;
      }
    }
    pool.shutdown();
    if (processingError) {
      handleProcessingError(jobs, failureCauses);
    } else if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
      _logger.info("All jobs executed successfully\n");
    }
  }

  ExecutorService createExecutorService() {
    ExecutorService pool;
    if (_settings.getSequential()) {
      return Executors.newSingleThreadExecutor();
    } else {
      int maxConcurrentThreads = Runtime.getRuntime().availableProcessors();
      int numConcurrentThreads = Math.min(maxConcurrentThreads, _settings.getJobs());
      return Executors.newFixedThreadPool(numConcurrentThreads);
    }
  }

  String getFailureMessage(JobResultT result) {
    String time = CommonUtil.getTime(result.getElapsedTime());
    String failureMessage =
        String.format(
            "Failure running job after elapsed time: %s\n-----"
                + "BEGIN JOB LOG-----\n%s\n-----END JOB LOG-----",
            time, result.getHistory().toString(BatfishLogger.getLogLevel(_settings.getLogLevel())));
    return failureMessage;
  }

  String getSuccessMessage(JobResultT result) {
    String time = CommonUtil.getTime(result.getElapsedTime());
    String successMessage =
        String.format(
            "Job terminated successfully with result: %s after elapsed time: %s - %d/%d "
                + "(%.1f%%) complete\n",
            result, time, _finishedJobs, _totalJobs, _finishedPercent);
    return successMessage;
  }

  /**
   * Handles the Job result appropriately in case of success/error, and updates logs
   *
   * @param result Result of Job
   * @param output Data structure to which the result of the job will be applied, it should be of
   *     generic type {@link OutputT}
   * @param answerElement {@link AnswerElement} for the execution of jobs
   * @param failureCauses List of failure causes for all jobs
   * @return {@code true} if there is a processing error
   */
  boolean handleJobResult(
      JobResultT result,
      OutputT output,
      AnswerElementT answerElement,
      List<BatfishException> failureCauses) {
    Throwable failureCause = result.getFailureCause();
    if (failureCause == null) {
      result.applyTo(output, _logger, answerElement);
      _logger.info(getSuccessMessage(result));
      return false;
    } else {
      String failureMessage = getFailureMessage(result);
      BatfishException bfc = new BatfishException(failureMessage, failureCause);
      if (_settings.getExitOnFirstError()) {
        result.appendHistory(_logger);
        throw bfc;
      } else {
        result.appendHistory(_logger);
        _logger.errorf("%s:\n\t%s", failureMessage, ExceptionUtils.getStackTrace(failureCause));
        failureCauses.add(bfc);
        if (!_haltOnProcessingError) {
          result.applyTo(output, _logger, answerElement);
        }
        return true;
      }
    }
  }

  void handleProcessingError(List<JobT> jobs, List<BatfishException> failureCauses) {
    int numJobs = jobs.size();
    int numFailed = failureCauses.size();
    int numSucceeded = numJobs - numFailed;
    if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
      _logger.infof("%d jobs succeeded; %d jobs failed\n", numSucceeded, numFailed);
    }
    if (_haltOnProcessingError) {
      throw new HandleProcessingErrorException(failureCauses);
    }
  }

  void initializeJobsStats(List<JobT> jobs) {
    _finishedJobs = 0;
    _totalJobs = jobs.size();
    _completed = Driver.newBatch(_settings, _description, _totalJobs);
    _finishedPercent = 0;
  }

  void markJobCompleted() {
    _finishedJobs++;
    _completed.incrementAndGet();
    _finishedPercent = 100 * ((double) _finishedJobs) / _totalJobs;
  }

  static final class HandleProcessingErrorException extends CompositeBatfishException {

    private static final long serialVersionUID = 1L;

    private static final String PROCESSING_ERROR_MESSAGE =
        "Fatal exception due to failure of at least one job";

    public HandleProcessingErrorException(List<BatfishException> failureCauses) {
      super(new BatfishException(PROCESSING_ERROR_MESSAGE), failureCauses);
    }
  }
}
