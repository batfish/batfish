package org.batfish.job;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.vendor.VendorConfiguration;

/** An intermediate class that holds a cacheable result of parsing input configuration files. */
public class ParseResult implements Serializable {
  private static final long serialVersionUID = 0L;

  @Nullable private final VendorConfiguration _config;
  @Nonnull private final ParseTreeSentences _parseTreeSentences;
  @Nullable private final Throwable _failureCause;
  @Nonnull private final ParseStatus _status;
  @Nonnull private final Warnings _warnings;

  public ParseResult(
      @Nullable VendorConfiguration config,
      @Nullable Throwable failureCause,
      @Nonnull ParseTreeSentences parseTreeSentences,
      @Nonnull ParseStatus status,
      @Nonnull Warnings warnings) {
    _config = config;
    _failureCause = failureCause;
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
