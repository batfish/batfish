package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS port, keyed by its slot/mda/connector path string (e.g. {@code 1/1/c1} for a connector
 * or {@code 1/1/c1/1} for a breakout sub-port). Holds the admin-state and, for connectors, the
 * breakout setting.
 */
public final class Port implements Serializable {

  public Port(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** The {@code admin-state} ({@code enable}/{@code disable}), or {@code null} if unset. */
  public @Nullable Boolean getAdminStateEnable() {
    return _adminStateEnable;
  }

  public void setAdminStateEnable(@Nullable Boolean adminStateEnable) {
    _adminStateEnable = adminStateEnable;
  }

  /** The {@code connector breakout} setting (e.g. {@code c1-100g}), or {@code null} if unset. */
  public @Nullable String getBreakout() {
    return _breakout;
  }

  public void setBreakout(@Nullable String breakout) {
    _breakout = breakout;
  }

  private final @Nonnull String _name;
  private @Nullable Boolean _adminStateEnable;
  private @Nullable String _breakout;
}
