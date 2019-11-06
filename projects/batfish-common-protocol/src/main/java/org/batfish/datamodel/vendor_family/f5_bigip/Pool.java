package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a pool of nodes. */
public final class Pool implements Serializable {

  public static final class Builder {

    public @Nonnull Pool build() {
      checkArgument(_name != null, "Missing name");
      return new Pool(_description, _members.build(), _monitors.build(), _name);
    }

    public @Nonnull Builder setDescription(String description) {
      _description = description;
      return this;
    }

    public @Nonnull Builder addMember(PoolMember member) {
      _members.put(member.getName(), member);
      return this;
    }

    public @Nonnull Builder setMembers(Map<String, PoolMember> members) {
      _members = ImmutableMap.<String, PoolMember>builder().putAll(members);
      return this;
    }

    public @Nonnull Builder addMonitor(String monitor) {
      _monitors.add(monitor);
      return this;
    }

    public @Nonnull Builder setMonitors(Iterable<String> monitors) {
      _monitors = ImmutableList.<String>builder().addAll(monitors);
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    private @Nullable String _description;
    private @Nonnull ImmutableMap.Builder<String, PoolMember> _members;
    private @Nonnull ImmutableList.Builder<String> _monitors;
    private @Nullable String _name;

    private Builder() {
      _members = ImmutableMap.builder();
      _monitors = ImmutableList.builder();
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nonnull Map<String, PoolMember> getMembers() {
    return _members;
  }

  public @Nonnull List<String> getMonitors() {
    return _monitors;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Pool)) {
      return false;
    }
    Pool rhs = (Pool) obj;
    return Objects.equals(_description, rhs._description)
        && _members.equals(rhs._members)
        && _monitors.equals(rhs._monitors)
        && _name.equals(rhs._name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_description, _members, _monitors, _name);
  }

  private Pool(
      @Nullable String description,
      Map<String, PoolMember> members,
      List<String> monitors,
      String name) {
    _description = description;
    _members = members;
    _monitors = monitors;
    _name = name;
  }

  private final @Nullable String _description;
  private final @Nonnull Map<String, PoolMember> _members;
  private final @Nonnull List<String> _monitors;
  private final @Nonnull String _name;
}
