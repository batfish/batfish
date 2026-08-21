package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Configuration for FastPath's in-memory buffered log, set via {@code logging buffered [severity]}
 * and {@code logging buffered wrap}.
 */
public final class LoggingBuffered implements Serializable {

  /** Whether in-memory buffered logging is enabled. */
  public @Nullable Boolean getEnabled() {
    return _enabled;
  }

  public void setEnabled(@Nullable Boolean enabled) {
    _enabled = enabled;
  }

  /** Whether the in-memory log wraps ({@code logging buffered wrap}) when full. */
  public @Nullable Boolean getWrap() {
    return _wrap;
  }

  public void setWrap(@Nullable Boolean wrap) {
    _wrap = wrap;
  }

  /**
   * The minimum severity (0-7) for buffered logging, or {@code null} if not configured. Set via
   * {@code logging buffered <severity>} (a form devices accept despite the CLI guide showing no
   * argument).
   */
  public @Nullable Integer getSeverity() {
    return _severity;
  }

  public void setSeverity(@Nullable Integer severity) {
    _severity = severity;
  }

  private @Nullable Boolean _enabled;
  private @Nullable Boolean _wrap;
  private @Nullable Integer _severity;
}
