package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a pool of nodes. */
@ParametersAreNonnullByDefault
public final class Pool implements Serializable {

  public Pool(
      @Nullable String description,
      Map<String, PoolMember> members,
      List<String> monitors,
      String name) {
    _description = description;
    _members = members;
    _monitors = monitors;
    _name = name;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public @Nullable String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_MEMBERS)
  public Map<String, PoolMember> getMembers() {
    return _members;
  }

  @JsonProperty(PROP_MONITORS)
  public @Nullable List<String> getMonitors() {
    return _monitors;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
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

  private final @Nullable String _description;
  private final @Nonnull Map<String, PoolMember> _members;
  private final @Nullable List<String> _monitors;
  private final @Nonnull String _name;
}
