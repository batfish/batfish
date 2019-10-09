package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a pool of nodes. */
public final class Pool implements Serializable {

  public Pool(String name) {
    _name = name;
    _members = new HashMap<>();
    _monitors = new LinkedList<>();
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nonnull Map<String, PoolMember> getMembers() {
    return _members;
  }

  public @Nullable List<String> getMonitors() {
    return _monitors;
  }

  public @Nonnull String getName() {
    return _name;
  }

  private @Nullable String _description;
  private final @Nonnull Map<String, PoolMember> _members;
  private final @Nonnull List<String> _monitors;
  private final @Nonnull String _name;
}
