package org.batfish.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
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

  private final String _description;

  private final boolean _haltOnProcessingError;

  private final BatfishLogger _logger;

  private final Settings _settings;

  public BatfishJobExecutor(
      Settings settings, BatfishLogger logger, boolean haltOnProcessingError, String description) {
    _settings = settings;
    _logger = logger;
    _haltOnProcessingError = haltOnProcessingError;
    _description = description;
  }

  public void executeJobs(List<JobT> jobs, OutputT output, AnswerElementT answerElement) {
    ExecutorService pool;
    boolean shuffle;
    if (!_settings.getSequential()) {
      int maxConcurrentThreads = Runtime.getRuntime().availableProcessors();
      int numConcurrentThreads = Math.min(maxConcurrentThreads, _settings.getJobs());
      pool = Executors.newFixedThreadPool(numConcurrentThreads);
      shuffle = _settings.getShuffleJobs();
    } else {
      pool = Executors.newSingleThreadExecutor();
      shuffle = false;
    }
    if (shuffle) {
      Collections.shuffle(jobs);
    }
    List<Future<JobResultT>> futures = new ArrayList<>();
    Semaphore semaphore = new Semaphore(0);
    for (JobT job : jobs) {
      job.setSemaphore(semaphore);
      Future<JobResultT> future = pool.submit(job);
      futures.add(future);
    }
    AtomicBoolean processingError = new AtomicBoolean(false);
    int totalJobs = jobs.size();
    AtomicInteger finishedJobs = Driver.newBatch(_settings, _description, totalJobs);
    List<BatfishException> failureCauses = new ArrayList<>();
    try {
      while (!futures.isEmpty()) {
        semaphore.acquire();
        semaphore.release();
        List<Future<JobResultT>> currentFutures = new ArrayList<>();
        currentFutures.addAll(futures);
        for (Future<JobResultT> future : currentFutures) {
          if (future.isDone()) {
            semaphore.acquire();
            futures.remove(future);
            processCompletedFuture(
                output,
                answerElement,
                processingError,
                finishedJobs,
                totalJobs,
                failureCauses,
                future);
          } else {
            continue;
          }
        }
      }
    } catch (InterruptedException e) {
      throw new BatfishException("interrupted while sleeping", e);
    }
    pool.shutdown();
    if (processingError.get()) {
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
    } else if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
      _logger.info("All jobs executed successfully\n");
    }
  }

  private void processCompletedFuture(
      OutputT output,
      AnswerElementT answerElement,
      AtomicBoolean processingError,
      AtomicInteger finishedJobs,
      int totalJobs,
      List<BatfishException> failureCauses,
      Future<JobResultT> future) {
    int newFinishedJobs = finishedJobs.incrementAndGet();
    double finishedPercent = 100 * ((double) newFinishedJobs) / totalJobs;
    JobResultT result = null;
    try {
      result = future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new BatfishException("Error executing job", e);
    }
    String time = CommonUtil.getTime(result.getElapsedTime());
    Throwable failureCause = result.getFailureCause();
    if (failureCause == null) {
      result.applyTo(output, _logger, answerElement);
      _logger.infof(
          "Job terminated successfully with result: %s after elapsed time: %s - %d/%d "
              + "(%.1f%%) complete\n",
          result.toString(), time, newFinishedJobs, totalJobs, finishedPercent);
    } else {
      String failureMessage =
          "Failure running job after elapsed time: "
              + time
              + "\n-----BEGIN JOB LOG-----\n"
              + result.getHistory().toString(BatfishLogger.getLogLevel(_settings.getLogLevel()))
              + "\n-----END JOB LOG-----";
      BatfishException bfc = new BatfishException(failureMessage, failureCause);
      if (_settings.getExitOnFirstError()) {
        result.appendHistory(_logger);
        throw bfc;
      } else {
        processingError.set(true);
        result.appendHistory(_logger);
        _logger.error(failureMessage + ":\n\t" + ExceptionUtils.getStackTrace(failureCause));
        failureCauses.add(bfc);
        if (!_haltOnProcessingError) {
          result.applyTo(output, _logger, answerElement);
        }
      }
    }
  }
}
