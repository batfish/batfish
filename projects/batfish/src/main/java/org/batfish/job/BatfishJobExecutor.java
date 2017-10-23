package org.batfish.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CompositeBatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.main.Driver;

public class BatfishJobExecutor<
    JobT extends BatfishJob<JobResultT>,
    AnswerElementT extends AnswerElement,
    JobResultT extends BatfishJobResult<OutputT, AnswerElementT>,
    OutputT> {

  private static final long JOB_POLLING_PERIOD_MS = 1000L;

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
   * @param output Data Structure which will contain the output of the jobs
   * @param answerElement Answer Element containing the detail of the jobs executed
   */
  public void executeJobs(List<JobT> jobs, OutputT output, AnswerElementT answerElement) {
    //Initializing executors
    ExecutorService pool = getExecutorService();
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
      try {
        // getting the next completed task
        Future<JobResultT> currentDoneFuture = completionService.take();
        updateJobsStats();

        JobResultT result = null;
        try {
          // getting the result of the job
          result = currentDoneFuture.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new BatfishException("Error executing job", e);
        }
        boolean jobResultError = handleJobResult(result, output, answerElement, failureCauses);
        if (jobResultError) {
          processingError = true;
        }

      } catch (InterruptedException e) {
        throw new BatfishException("interrupted while waiting for jobs to complete", e);
      }
    }
    pool.shutdown();
    if (processingError) {
      handleProcessingError(jobs, failureCauses);
    } else if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
      _logger.info("All jobs executed successfully\n");
    }
  }

  ExecutorService getExecutorService() {
    ExecutorService pool;
    if (!_settings.getSequential()) {
      int maxConcurrentThreads = Runtime.getRuntime().availableProcessors();
      int numConcurrentThreads = Math.min(maxConcurrentThreads, _settings.getJobs());
      pool = Executors.newFixedThreadPool(numConcurrentThreads);
    } else {
      pool = Executors.newSingleThreadExecutor();
    }
    return pool;
  }

  /**
   * Handles the Job result appropriately in case of success/error, and updates logs
   *
   * @param result Result of Job
   * @param output Output comprising results of all jobs
   * @param answerElement Answer Element for the execution of jobs
   * @param failureCauses List of failure causes for all jobs
   * @return True if there is a processing error else False
   */
  boolean handleJobResult(
      JobResultT result,
      OutputT output,
      AnswerElementT answerElement,
      List<BatfishException> failureCauses) {
    boolean processingError = false;
    String time = CommonUtil.getTime(result.getElapsedTime());
    Throwable failureCause = result.getFailureCause();
    if (failureCause == null) {
      result.applyTo(output, _logger, answerElement);
      _logger.infof(
          "Job terminated successfully with result: %s after elapsed time: %s - %d/%d "
              + "(%.1f%%) complete\n",
          result.toString(), time, _finishedJobs, _totalJobs, _finishedPercent);
    } else {
      String failureMessage =
          String.format(
              "Failure running job after elapsed time: %s\n-----"
                  + "BEGIN JOB LOG-----\n%s\n-----END JOB LOG-----",
              time,
              result.getHistory().toString(BatfishLogger.getLogLevel(_settings.getLogLevel())));
      BatfishException bfc = new BatfishException(failureMessage, failureCause);
      if (_settings.getExitOnFirstError()) {
        result.appendHistory(_logger);
        throw bfc;
      } else {
        processingError = true;
        result.appendHistory(_logger);
        _logger.error(failureMessage + ":\n\t" + ExceptionUtils.getStackTrace(failureCause));
        failureCauses.add(bfc);
        if (!_haltOnProcessingError) {
          result.applyTo(output, _logger, answerElement);
        }
      }
    }
    return processingError;
  }

  void handleProcessingError(List<JobT> jobs, List<BatfishException> failureCauses) {
    int numJobs = jobs.size();
    int numFailed = numJobs - failureCauses.size();
    int numSucceeded = numJobs - numFailed;
    if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
      _logger.infof("%d jobs succeeded; %d jobs failed\n", numSucceeded, numFailed);
    }
    if (_haltOnProcessingError) {
      throw new CompositeBatfishException(
          new BatfishException("Fatal exception due to failure of at least one job"),
          failureCauses);
    }
  }

  void initializeJobsStats(List<JobT> jobs) {
    _finishedJobs = 0;
    _totalJobs = jobs.size();
    _completed = Driver.newBatch(_settings, _description, _totalJobs);
    _finishedPercent = 0;
  }

  void updateJobsStats() {
    _finishedJobs++;
    _completed.incrementAndGet();
    _finishedPercent = 100 * ((double) _finishedJobs) / _totalJobs;
  }
}
