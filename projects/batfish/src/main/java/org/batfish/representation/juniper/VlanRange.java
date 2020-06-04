package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;

@ParametersAreNonnullByDefault
public final class VlanRange implements VlanMember {

  public VlanRange(IntegerSpace range) {
    _range = range;
  }

  public @Nonnull IntegerSpace getRange() {
    return _range;
  }

  @Override
  public @Nonnull String toString() {
    return _range.toString();
  }

  private final @Nonnull IntegerSpace _range;
}
