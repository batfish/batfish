package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a pool of nodes. */
@ParametersAreNonnullByDefault
public final class Pool implements Serializable {
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_MEMBERS = "members";
  private static final String PROP_MONITOR = "monitor";
  private static final String PROP_NAME = "name";
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull Pool create(
      @JsonProperty(PROP_MEMBERS) Map<String, PoolMember> members,
      @JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(name != null, "Missing: %s", PROP_NAME);
    return new Pool(ImmutableMap.copyOf(firstNonNull(members, ImmutableMap.of())), name);
  }

  private @Nullable String _description;
  private final @Nonnull Map<String, PoolMember> _members;
  private @Nullable String _monitor;
  private final @Nonnull String _name;

  private Pool(Map<String, PoolMember> members, String name) {
    _members = members;
    _name = name;
  }

  public Pool(String name) {
    _name = name;
    _members = new HashMap<>();
  }

  @JsonProperty(PROP_DESCRIPTION)
  public @Nullable String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_MEMBERS)
  public Map<String, PoolMember> getMembers() {
    return _members;
  }

  @JsonProperty(PROP_MONITOR)
  public @Nullable String getMonitor() {
    return _monitor;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(@Nullable String description) {
    _description = description;
  }

  @JsonProperty(PROP_MONITOR)
  public void setMonitor(@Nullable String monitor) {
    _monitor = monitor;
  }
}
