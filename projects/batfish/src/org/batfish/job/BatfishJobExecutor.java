package org.batfish.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.util.Util;

public class BatfishJobExecutor<Job extends BatfishJob<JobResult>, JobResult extends BatfishJobResult<Output>, Output> {

   private static final long JOB_POLLING_PERIOD_MS = 1000l;

   private final BatfishLogger _logger;

   private final Settings _settings;

   public BatfishJobExecutor(Settings settings, BatfishLogger logger) {
      _settings = settings;
      _logger = logger;
   }

   public Output executeJobs(List<Job> jobs, Output output) {
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
      List<Future<JobResult>> futures = new ArrayList<Future<JobResult>>();
      for (Job job : jobs) {
         Future<JobResult> future = pool.submit(job);
         futures.add(future);
      }
      boolean processingError = false;
      int finishedJobs = 0;
      int totalJobs = jobs.size();
      double finishedPercent;
      while (!futures.isEmpty()) {
         List<Future<JobResult>> currentFutures = new ArrayList<Future<JobResult>>();
         currentFutures.addAll(futures);
         for (Future<JobResult> future : currentFutures) {
            if (future.isDone()) {
               futures.remove(future);
               finishedJobs++;
               finishedPercent = 100 * ((double) finishedJobs) / totalJobs;
               JobResult result = null;
               try {
                  result = future.get();
               }
               catch (InterruptedException | ExecutionException e) {
                  throw new BatfishException("Error executing nod job", e);
               }
               String time = Util.getTime(result.getElapsedTime());
               Throwable failureCause = result.getFailureCause();
               if (failureCause == null) {
                  result.applyTo(output, _logger);
                  _logger
                        .infof(
                              "Job terminated successfully with result: %s after elapsed time: %s - %d/%d (%.1f%%) complete\n",
                              result.toString(), time, finishedJobs, totalJobs,
                              finishedPercent);
               }
               else {
                  String failureMessage = "Failure running job after elapsed time: "
                        + time;
                  if (_settings.getExitOnFirstError()) {
                     result.explainFailure(_logger);
                     throw new BatfishException(failureMessage, failureCause);
                  }
                  else {
                     processingError = true;
                     _logger.error(failureMessage + ":"
                           + ExceptionUtils.getStackTrace(failureCause));
                     result.explainFailure(_logger);
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
         throw new BatfishException(
               "Fatal exception due to failure of at least one nod job");
      }
      else {
         if (!_logger.isActive(BatfishLogger.LEVEL_INFO)) {
            _logger.info("All jobs executed successfully\n");
         }
         return output;
      }

   }

}
