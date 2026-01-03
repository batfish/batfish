package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* High-availability group used to calculate failover score. */
public final class HaGroup implements Serializable {

  public static final class Builder {

    public @Nonnull HaGroup build() {
      checkArgument(_name != null, "Missing name");
      return new HaGroup(_activeBonus, _name, _pools.build(), _trunks.build());
    }

    public @Nonnull Builder setActiveBonus(@Nullable Integer activeBonus) {
      _activeBonus = activeBonus;
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder addPool(HaGroupPool pool) {
      _pools.put(pool.getName(), pool);
      return this;
    }

    public @Nonnull Builder setPools(Map<String, HaGroupPool> pools) {
      _pools = ImmutableMap.<String, HaGroupPool>builder().putAll(pools);
      return this;
    }

    public @Nonnull Builder addTrunk(HaGroupTrunk trunk) {
      _trunks.put(trunk.getName(), trunk);
      return this;
    }

    public @Nonnull Builder setTrunks(Map<String, HaGroupTrunk> trunks) {
      _trunks = ImmutableMap.<String, HaGroupTrunk>builder().putAll(trunks);
      return this;
    }

    private @Nullable Integer _activeBonus;
    private @Nullable String _name;
    private @Nonnull ImmutableMap.Builder<String, HaGroupPool> _pools;
    private @Nonnull ImmutableMap.Builder<String, HaGroupTrunk> _trunks;

    private Builder() {
      _pools = ImmutableMap.builder();
      _trunks = ImmutableMap.builder();
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public @Nullable Integer getActiveBonus() {
    return _activeBonus;
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

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HaGroup)) {
      return false;
    }
    HaGroup rhs = (HaGroup) obj;
    return Objects.equals(_activeBonus, rhs._activeBonus)
        && _name.equals(rhs._name)
        && _pools.equals(rhs._pools)
        && _trunks.equals(rhs._trunks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_activeBonus, _name, _pools, _trunks);
  }

  private final @Nullable Integer _activeBonus;
  private final @Nonnull String _name;
  private final @Nonnull Map<String, HaGroupPool> _pools;
  private final @Nonnull Map<String, HaGroupTrunk> _trunks;

  private HaGroup(
      @Nullable Integer activeBonus,
      String name,
      Map<String, HaGroupPool> pools,
      Map<String, HaGroupTrunk> trunks) {
    _activeBonus = activeBonus;
    _name = name;
    _pools = pools;
    _trunks = trunks;
  }
}
