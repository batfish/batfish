package org.batfish.job;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.vendor.VendorConfiguration;

/** An intermediate class that holds a cacheable result of parsing input configuration files. */
@ParametersAreNonnullByDefault
public class ParseResult implements Serializable {

  @Nullable private final VendorConfiguration _config;
  @Nonnull private final ParseTreeSentences _parseTreeSentences;
  @Nullable private final Throwable _failureCause;
  @Nonnull private final String _filename;
  @Nonnull private final ConfigurationFormat _format;
  @Nonnull private final ParseStatus _status;
  @Nonnull private final Warnings _warnings;

  public ParseResult(
      @Nullable VendorConfiguration config,
      @Nullable Throwable failureCause,
      String filename,
      ConfigurationFormat format,
      ParseTreeSentences parseTreeSentences,
      ParseStatus status,
      Warnings warnings) {
    _config = config;
    _failureCause = failureCause;
    _filename = filename;
    _format = format;
    _parseTreeSentences = parseTreeSentences;
    _status = status;
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

  @Nonnull
  public String getFilename() {
    return _filename;
  }

  @Nonnull
  public ConfigurationFormat getFormat() {
    return _format;
  }

  @Nonnull
  public ParseTreeSentences getParseTreeSentences() {
    return _parseTreeSentences;
  }

  @Nonnull
  public ParseStatus getStatus() {
    return _status;
  }

  @Nonnull
  public Warnings getWarnings() {
    return _warnings;
  }
}
