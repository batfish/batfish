package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* High-availability group used to calculate failover score. */
public final class HaGroup implements Serializable {

  public HaGroup(String name) {
    _name = name;
    _pools = new HashMap<>();
    _trunks = new HashMap<>();
  }

  public @Nullable Integer getActiveBonus() {
    return _activeBonus;
  }

  public void setActiveBonus(@Nullable Integer activeBonus) {
    _activeBonus = activeBonus;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Map<String, HaGroupPool> getPools() {
    return _pools;
  }

  public @Nonnull Map<String, HaGroupTrunk> getTrunks() {
    return _trunks;
  }

  private @Nullable Integer _activeBonus;
  private final @Nonnull String _name;
  private final @Nonnull Map<String, HaGroupPool> _pools;
  private final @Nonnull Map<String, HaGroupTrunk> _trunks;
}
