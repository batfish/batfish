package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for an {@code ip sla} object. */
@ParametersAreNonnullByDefault
public abstract class IpSla implements Serializable {

  public abstract <T> T accept(IpSlaVisitor<T> visitor);

  /**
   * Whether this SLA will still be alive after "infinite" time passes. If {@code false}, this SLA
   * is considered inactive.
   *
   * <p>Note that this is based on {@code ip sla schedule} configuration which only exists as long
   * as sla does, and cannot be configured before sla exists.
   */
  public final boolean getLivesForever() {
    return _livesForever;
  }

  public final void setLivesForever(boolean livesForever) {
    _livesForever = livesForever;
  }

  /**
   * Whether this SLA will ever start, assuming "infinite" time passes. If {@code false}, this SLA
   * is considered inactive.
   *
   * <p>Note that this is based on {@code ip sla schedule} configuration which only exists as long
   * as sla does, and cannot be configured before sla exists.
   */
  public final boolean getStartsEventually() {
    return _startsEventually;
  }

  public final void setStartsEventually(boolean startsEventually) {
    _startsEventually = startsEventually;
  }

  private boolean _livesForever;
  private boolean _startsEventually;
}
