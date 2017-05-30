package org.batfish.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CompositeBatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.main.Driver;
import org.batfish.main.Settings;

public class BatfishJobExecutor<Job extends BatfishJob<JobResult>, AE extends AnswerElement, JobResult extends BatfishJobResult<Output, AE>, Output> {

   private static final long JOB_POLLING_PERIOD_MS = 1000l;

   private final String _description;

   private final boolean _haltOnProcessingError;

   private final BatfishLogger _logger;

   private final Settings _settings;

   public BatfishJobExecutor(Settings settings, BatfishLogger logger,
         boolean haltOnProcessingError, String description) {
      _settings = settings;
      _logger = logger;
      _haltOnProcessingError = haltOnProcessingError;
      _description = description;
   }

   public void executeJobs(List<Job> jobs, Output output, AE answerElement) {
      ExecutorService pool;
      boolean shuffle;
      if (!_settings.getSequential()) {
         int maxConcurrentThreads = Runtime.getRuntime().availableProcessors();
         int numConcurrentThreads = Math.min(maxConcurrentThreads,
               _settings.getJobs());
         pool = Executors.newFixedThreadPool(numConcurrentThreads);
         shuffle = _settings.getShuffleJobs();
      }
      else {
         pool = Executors.newSingleThreadExecutor();
         shuffle = false;
      }
      if (shuffle) {
         Collections.shuffle(jobs);
      }
      List<Future<JobResult>> futures = new ArrayList<>();
      for (Job job : jobs) {
         Future<JobResult> future = pool.submit(job);
         futures.add(future);
      }
      boolean processingError = false;
      int finishedJobs = 0;
      int totalJobs = jobs.size();
      AtomicInteger completed = Driver.newBatch(_settings, _description,
            totalJobs);
      double finishedPercent;
      List<BatfishException> failureCauses = new ArrayList<>();
      while (!futures.isEmpty()) {
         List<Future<JobResult>> currentFutures = new ArrayList<>();
         currentFutures.addAll(futures);
         for (Future<JobResult> future : currentFutures) {
            if (future.isDone()) {
               futures.remove(future);
               finishedJobs++;
               completed.incrementAndGet();
               finishedPercent = 100 * ((double) finishedJobs) / totalJobs;
               JobResult result = null;
               try {
                  result = future.get();
               }
               catch (InterruptedException | ExecutionException e) {
                  throw new BatfishException("Error executing job", e);
               }
               String time = CommonUtil.getTime(result.getElapsedTime());
               Throwable failureCause = result.getFailureCause();
               if (failureCause == null) {
                  result.applyTo(output, _logger, answerElement);
                  _logger.infof(
                        "Job terminated successfully with result: %s after elapsed time: %s - %d/%d (%.1f%%) complete\n",
                        result.toString(), time, finishedJobs, totalJobs,
                        finishedPercent);
               }
               else {
                  String failureMessage = "Failure running job after elapsed time: "
                        + time + "\n-----BEGIN JOB LOG-----\n"
                        + result.getHistory()
                              .toString(BatfishLogger
                                    .getLogLevel(_settings.getLogLevel()))
                        + "\n-----END JOB LOG-----";
                  BatfishException bfc = new BatfishException(failureMessage,
                        failureCause);
                  if (_settings.getExitOnFirstError()) {
                     result.appendHistory(_logger);
                     throw bfc;
                  }
                  else {
                     processingError = true;
                     result.appendHistory(_logger);
                     _logger.error(failureMessage + ":\n\t"
                           + ExceptionUtils.getStackTrace(failureCause));
                     failureCauses.add(bfc);
                     if (!_haltOnProcessingError) {
                        result.applyTo(output, _logger, answerElement);
                     }
                  }
               }
            }
            else {
               continue;
            }
         }
         if (!futures.isEmpty()) {
            try {
               Thread.sleep(JOB_POLLING_PERIOD_MS);
            }
            catch (InterruptedException e) {
               throw new BatfishException("interrupted while sleeping", e);
            }
         }
      }
      pool.shutdown();
      if (processingError) {
         int numJobs = jobs.size();
         int numFailed = numJobs - failureCauses.size();
         int numSucceeded = numJobs - numFailed;
         if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
            _logger.infof("%d jobs succeeded; %d jobs failed\n", numSucceeded,
                  numFailed);
         }
         if (_haltOnProcessingError) {
            throw new CompositeBatfishException(
                  new BatfishException(
                        "Fatal exception due to failure of at least one job"),
                  failureCauses);
         }
      }
      else if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
         _logger.info("All jobs executed successfully\n");
      }

   }

}
