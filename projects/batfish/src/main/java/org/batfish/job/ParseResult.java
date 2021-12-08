package org.batfish.job;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.job.ParseVendorConfigurationJob.FileResult;
import org.batfish.vendor.VendorConfiguration;

/** An intermediate class that holds a cacheable result of parsing input configuration files. */
@ParametersAreNonnullByDefault
public class ParseResult implements Serializable {

  @Nullable private final VendorConfiguration _config;
  @Nullable private final Throwable _failureCause;
  @Nonnull private final Map<String, FileResult> _fileResults;
  @Nonnull private final ConfigurationFormat _format;
  @Nonnull private final Warnings _warnings;

  public ParseResult(
      @Nullable VendorConfiguration config,
      @Nullable Throwable failureCause,
      Map<String, FileResult> fileResults,
      ConfigurationFormat format,
      Warnings warnings) {
    checkArgument(
        fileResults.values().stream().noneMatch(fr -> fr.getParseStatus() == null),
        "ParseStatus is not set for some files");
    _config = config;
    _failureCause = failureCause;
    _fileResults = ImmutableMap.copyOf(fileResults);
    _format = format;
    _warnings = warnings;
  }

  @Nullable
  public VendorConfiguration getConfig() {
    return _config;
  }

  @Nullable
  public Throwable getFailureCause() {
    return _failureCause;
  }

  /**
   * Get results for individual files. File-level warnings and parse trees are contained in this
   * map. Warnings not specific to a file are in {@link #getWarnings()}
   */
  @Nonnull
  public Map<String, FileResult> getFileResults() {
    return _fileResults;
  }

  @Nonnull
  public ConfigurationFormat getFormat() {
    return _format;
  }

  /**
   * Get job-level (not file-specific) warnings. File-specific warnings (e.g., parse warnings) can
   * be accessed via {@link #getFileResults()}.
   */
  @Nonnull
  public Warnings getWarnings() {
    return _warnings;
  }
}
