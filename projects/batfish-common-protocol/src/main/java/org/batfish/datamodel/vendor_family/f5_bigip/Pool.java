package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a pool of nodes. */
public final class Pool implements Serializable {

  public static final class Builder {

    public @Nonnull Pool build() {
      checkArgument(_name != null, "Missing %s", PROP_NAME);
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

  @JsonProperty(PROP_DESCRIPTION)
  public @Nullable String getDescription() {
    return _description;
  }

  @JsonIgnore
  public @Nonnull Map<String, PoolMember> getMembers() {
    return _members;
  }

  @JsonProperty(PROP_MEMBERS)
  private @Nonnull SortedMap<String, PoolMember> getMembersSorted() {
    return ImmutableSortedMap.copyOf(_members);
  }

  @JsonProperty(PROP_MONITORS)
  public @Nonnull List<String> getMonitors() {
    return _monitors;
  }

  @JsonProperty(PROP_NAME)
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

  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_MEMBERS = "members";
  private static final String PROP_MONITORS = "monitors";
  private static final String PROP_NAME = "name";

  @JsonCreator
  private static @Nonnull Pool create(
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_MEMBERS) @Nullable Map<String, PoolMember> members,
      @JsonProperty(PROP_MONITORS) @Nullable List<String> monitors,
      @JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(name != null, "Missing: %s", PROP_NAME);
    return new Pool(
        description,
        ImmutableMap.copyOf(firstNonNull(members, ImmutableMap.of())),
        ImmutableList.copyOf(firstNonNull(monitors, ImmutableList.of())),
        name);
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
