package org.batfish.coordinator;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishWorkerService;
import org.batfish.common.BfConsts;
import org.batfish.common.LaunchResult;

/**
 * {@link WorkExecutor} implementation that delegates directly to a {@link BatfishWorkerService}.
 */
@ParametersAreNonnullByDefault
public class BatfishWorkerServiceWorkExecutor implements WorkExecutor {

  public BatfishWorkerServiceWorkExecutor(
      BatfishLogger logger, BatfishWorkerService batfishWorkerService) {
    _logger = logger;
    _batfishWorkerService = batfishWorkerService;
  }

  @Override
  public SubmissionResult submit(QueuedWork work) {
    String taskId = work.getId().toString();
    Map<String, String> params = new HashMap<>(work.resolveRequestParams());
    params.put(
        BfConsts.ARG_STORAGE_BASE,
        Main.getSettings().getContainersLocation().toAbsolutePath().toString());
    _logger.infof("BFS:runTask(%s,%s)\n", taskId, params);
    assert !Strings.isNullOrEmpty(taskId);
    try {
      ImmutableList.Builder<String> argsBuilder = ImmutableList.builder();
      params.forEach(
          (k, v) -> {
            argsBuilder.add("-" + k);
            if (!Strings.isNullOrEmpty(v)) {
              argsBuilder.add(v);
            }
          });
      List<String> argsList = argsBuilder.build();
      String[] args = argsBuilder.build().toArray(new String[argsList.size()]);
      _logger.infof("Will run with args: %s\n", Arrays.toString(args));

      LaunchResult lr = _batfishWorkerService.runTask(taskId, args);
      switch (lr.getType()) {
        case LAUNCHED:
          _logger.info(String.format("Work submitted with ID: %s\n", taskId));
          return SubmissionResult.success(() -> _batfishWorkerService.getTaskStatus(taskId));
        case BUSY:
          _logger.warn(String.format("Work with ID: %s requeued because worker is busy\n", taskId));
          return SubmissionResult.failure();
        case ERROR:
          _logger.error(String.format("Error submitting work: %s\n", lr.getMessage()));
          return SubmissionResult.error();
        default:
          throw new IllegalArgumentException(
              String.format("Invalid LaunchResult.Type: %s", lr.getType()));
      }
    } catch (Exception e) {
      _logger.error(
          String.format("Exception submitting work: %s\n", Throwables.getStackTraceAsString(e)));
      return SubmissionResult.error();
    }
  }

  private final @Nonnull BatfishLogger _logger;
  private final @Nonnull BatfishWorkerService _batfishWorkerService;
}
