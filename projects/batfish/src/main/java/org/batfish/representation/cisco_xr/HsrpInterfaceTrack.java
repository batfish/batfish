package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Settings at the {@code router hsrp > interface > address-family > hsrp > track} level. */
@ParametersAreNonnullByDefault
public final class HsrpInterfaceTrack implements Serializable {
  public HsrpInterfaceTrack(String name) {
    _name = name;
  }

  public @Nullable Integer getDecrementPriority() {
    return _decrementPriority;
  }

  public void setDecrementPriority(@Nullable Integer decrementPriority) {
    _decrementPriority = decrementPriority;
  }

  public @Nonnull String getName() {
    return _name;
  }

  private final @Nonnull String _name;
  private @Nullable Integer _decrementPriority;
}
