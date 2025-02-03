package org.batfish.coordinator;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishWorkerService;
import org.batfish.common.BfConsts;
import org.batfish.common.LaunchResult;
import org.batfish.common.Task;

/**
 * {@link WorkExecutor} implementation that delegates directly to a {@link BatfishWorkerService}.
 */
@ParametersAreNonnullByDefault
public class BatfishWorkerServiceWorkExecutor implements WorkExecutor {

  public BatfishWorkerServiceWorkExecutor(
      BatfishLogger logger, Path containersLocation, BatfishWorkerService batfishWorkerService) {
    _logger = logger;
    _containersLocation = containersLocation;
    _batfishWorkerService = batfishWorkerService;
  }

  @Override
  public SubmissionResult submit(QueuedWork work) {
    try {
      return doSubmit(work);
    } catch (Exception e) {
      return SubmissionResult.error(
          String.format("Exception submitting work: %s\n", Throwables.getStackTraceAsString(e)));
    }
  }

  @VisibleForTesting
  @Nonnull
  SubmissionResult doSubmit(QueuedWork work) {
    String taskId = work.getId().toString();
    checkArgument(!Strings.isNullOrEmpty(taskId), "taskId must be non-empty");
    List<String> args = getRunArgs(work, _containersLocation);
    _logger.infof("Will run task %s with args: %s\n", taskId, args);
    LaunchResult lr = _batfishWorkerService.runTask(taskId, args.toArray(new String[] {}));
    return processLaunchResult(lr, taskId, _batfishWorkerService);
  }

  @VisibleForTesting
  static @Nonnull SubmissionResult processLaunchResult(
      LaunchResult lr, String taskId, BatfishWorkerService batfishWorkerService) {
    return switch (lr.getType()) {
      case LAUNCHED ->
          SubmissionResult.success(
              () ->
                  Optional.ofNullable(batfishWorkerService.getTaskStatus(taskId))
                      .orElse(Task.unknown()));
      case BUSY -> SubmissionResult.busy();
      case ERROR -> SubmissionResult.error(lr.getMessage());
    };
  }

  @VisibleForTesting
  static @Nonnull List<String> getRunArgs(QueuedWork work, Path containersLocation) {
    Map<String, String> params = new TreeMap<>(work.resolveRequestParams());
    params.put(BfConsts.ARG_STORAGE_BASE, containersLocation.toAbsolutePath().toString());
    ImmutableList.Builder<String> argsBuilder = ImmutableList.builder();
    params.forEach(
        (k, v) -> {
          argsBuilder.add("-" + k);
          if (!Strings.isNullOrEmpty(v)) {
            argsBuilder.add(v);
          }
        });
    return argsBuilder.build();
  }

  private final @Nonnull Path _containersLocation;
  private final @Nonnull BatfishLogger _logger;
  private final @Nonnull BatfishWorkerService _batfishWorkerService;
}
