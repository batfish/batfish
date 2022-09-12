package org.batfish.coordinator;

import java.util.function.BiFunction;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.config.Settings;

/**
 * Function that creates a {@link org.batfish.coordinator.WorkExecutor} based on given {@link
 * org.batfish.common.BatfishLogger} and {@link org.batfish.coordinator.config.Settings}.
 */
@FunctionalInterface
public interface WorkExecutorCreator extends BiFunction<BatfishLogger, Settings, WorkExecutor> {}
