package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Configuration of BGP routing-options within a virtual-router. Config at {@code network
 * virtual-router NAME protocol bgp routing-options}.
 */
public class BgpVrRoutingOptions implements Serializable {
  /** From PAN admin UI and verified. */
  private static final Long DEFAULT_DEFAULT_LOCAL_PREFERENCE = 100L;
  /** From PAN admin UI and verified. */
  private static final AsFormat DEFAULT_AS_FORMAT = AsFormat.TWO_BYTE_AS;

  /** What size ASNs this VR supports. */
  public enum AsFormat {
    TWO_BYTE_AS,
    FOUR_BYTE_AS,
  }

  public BgpVrRoutingOptions() {
    _asFormat = DEFAULT_AS_FORMAT;
    _defaultLocalPreference = DEFAULT_DEFAULT_LOCAL_PREFERENCE;
  }

  public @Nullable Boolean getAggregateMed() {
    return _aggregateMed;
  }

  public void setAggregateMed(@Nullable Boolean aggregateMed) {
    _aggregateMed = aggregateMed;
  }

  public @Nullable Boolean getAlwaysCompareMed() {
    return _alwaysCompareMed;
  }

  public void setAlwaysCompareMed(@Nullable Boolean alwaysCompareMed) {
    _alwaysCompareMed = alwaysCompareMed;
  }

  public @Nonnull AsFormat getAsFormat() {
    return _asFormat;
  }

  public void setAsFormat(@Nonnull AsFormat asFormat) {
    _asFormat = asFormat;
  }

  public long getDefaultLocalPreference() {
    return _defaultLocalPreference;
  }

  public void setDefaultLocalPreference(long defaultLocalPreference) {
    _defaultLocalPreference = defaultLocalPreference;
  }

  public @Nullable Boolean getDeterministicMedComparison() {
    return _deterministicMedComparison;
  }

  public void setDeterministicMedComparison(@Nullable Boolean deterministicMedComparison) {
    _deterministicMedComparison = deterministicMedComparison;
  }

  public @Nullable Boolean getGracefulRestartEnable() {
    return _gracefulRestartEnable;
  }

  public void setGracefulRestartEnable(@Nullable Boolean gracefulRestartEnable) {
    _gracefulRestartEnable = gracefulRestartEnable;
  }

  public @Nullable Ip getReflectorClusterId() {
    return _reflectorClusterId;
  }

  public void setReflectorClusterId(@Nullable Ip reflectorClusterId) {
    _reflectorClusterId = reflectorClusterId;
  }
  // private implementation details

  private @Nullable Boolean _aggregateMed;
  private @Nonnull AsFormat _asFormat;
  private @Nullable Boolean _alwaysCompareMed;
  private long _defaultLocalPreference;
  private @Nullable Boolean _deterministicMedComparison;
  private @Nullable Boolean _gracefulRestartEnable;
  private @Nullable Ip _reflectorClusterId;
}
