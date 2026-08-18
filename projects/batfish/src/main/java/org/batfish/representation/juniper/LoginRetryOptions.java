package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Vendor-specific model of the Junos {@code [edit system login retry-options]} block, which limits
 * SSH/Telnet login attempts.
 */
public class LoginRetryOptions implements Serializable {

  private @Nullable Integer _triesBeforeDisconnect;
  private @Nullable Integer _backoffThreshold;
  private @Nullable Integer _backoffFactor;
  private @Nullable Integer _minimumTime;
  private @Nullable Integer _maximumTime;
  private @Nullable Integer _lockoutPeriod;

  public @Nullable Integer getTriesBeforeDisconnect() {
    return _triesBeforeDisconnect;
  }

  public void setTriesBeforeDisconnect(@Nullable Integer triesBeforeDisconnect) {
    _triesBeforeDisconnect = triesBeforeDisconnect;
  }

  public @Nullable Integer getBackoffThreshold() {
    return _backoffThreshold;
  }

  public void setBackoffThreshold(@Nullable Integer backoffThreshold) {
    _backoffThreshold = backoffThreshold;
  }

  public @Nullable Integer getBackoffFactor() {
    return _backoffFactor;
  }

  public void setBackoffFactor(@Nullable Integer backoffFactor) {
    _backoffFactor = backoffFactor;
  }

  public @Nullable Integer getMinimumTime() {
    return _minimumTime;
  }

  public void setMinimumTime(@Nullable Integer minimumTime) {
    _minimumTime = minimumTime;
  }

  public @Nullable Integer getMaximumTime() {
    return _maximumTime;
  }

  public void setMaximumTime(@Nullable Integer maximumTime) {
    _maximumTime = maximumTime;
  }

  public @Nullable Integer getLockoutPeriod() {
    return _lockoutPeriod;
  }

  public void setLockoutPeriod(@Nullable Integer lockoutPeriod) {
    _lockoutPeriod = lockoutPeriod;
  }
}
