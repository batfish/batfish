package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a {@link Trunk} within a {@link HaGroup}. */
public final class HaGroupTrunk implements Serializable {

  public HaGroupTrunk(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getWeight() {
    return _weight;
  }

  public void setWeight(@Nullable Integer weight) {
    _weight = weight;
  }

  private final @Nonnull String _name;
  private @Nullable Integer _weight;
}
