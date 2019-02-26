package org.batfish.datamodel.vendor_family.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a pool of nodes. */
@ParametersAreNonnullByDefault
public final class Pool implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Map<String, PoolMember> _members;

  private @Nullable String _monitor;

  private final @Nonnull String _name;

  public Pool(String name) {
    _name = name;
    _members = new HashMap<>();
  }

  public Map<String, PoolMember> getMembers() {
    return _members;
  }

  public @Nullable String getMonitor() {
    return _monitor;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setMonitor(@Nullable String monitor) {
    _monitor = monitor;
  }
}
