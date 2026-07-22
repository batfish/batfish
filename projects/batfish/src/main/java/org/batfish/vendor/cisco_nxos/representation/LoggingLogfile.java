package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Configuration for the local logging logfile, set via {@code logging logfile <name>
 * <severity-level> [size <bytes>] [persistent threshold <percent>]}.
 */
public final class LoggingLogfile implements Serializable {

  public LoggingLogfile(String name, int severityLevel) {
    _name = name;
    _severityLevel = severityLevel;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public int getSeverityLevel() {
    return _severityLevel;
  }

  public @Nullable Integer getSizeBytes() {
    return _sizeBytes;
  }

  public void setSizeBytes(@Nullable Integer sizeBytes) {
    _sizeBytes = sizeBytes;
  }

  public @Nullable Integer getPersistentThreshold() {
    return _persistentThreshold;
  }

  public void setPersistentThreshold(@Nullable Integer persistentThreshold) {
    _persistentThreshold = persistentThreshold;
  }

  private final @Nonnull String _name;
  private final int _severityLevel;
  private @Nullable Integer _sizeBytes;
  private @Nullable Integer _persistentThreshold;
}
