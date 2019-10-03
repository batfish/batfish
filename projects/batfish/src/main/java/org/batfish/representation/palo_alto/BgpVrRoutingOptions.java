package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Configuration of BGP routing-options within a virtual-router. Config at {@code network
 * virtual-router NAME protocol bgp routing-options}.
 */
public class BgpVrRoutingOptions implements Serializable {

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

  // private implementation details

  private @Nullable Boolean _aggregateMed;
  private @Nullable Boolean _alwaysCompareMed;
  private @Nullable Boolean _deterministicMedComparison;
  private @Nullable Boolean _gracefulRestartEnable;
}
