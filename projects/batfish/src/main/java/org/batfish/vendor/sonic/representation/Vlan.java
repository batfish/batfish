package org.batfish.vendor.sonic.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.jackson.SingletonOrSingletonList;

/** Represents the settings of a VLAN: https://github.com/Azure/SONiC/wiki/Configuration#vlan */
public class Vlan implements Serializable {
  private static final String PROP_DHCP_SERVERS = "dhcp_servers";
  private static final String PROP_MEMBERS = "members";
  private static final String PROP_VLANID = "vlanid";

  private final @Nonnull List<String> _dhcpServers;
  private final @Nonnull List<String> _members;
  private final @Nullable Integer _vlanId;

  public @Nonnull List<String> getDhcpServers() {
    return _dhcpServers;
  }

  public @Nonnull List<String> getMembers() {
    return _members;
  }

  public @Nonnull Optional<Integer> getVlanId() {
    return Optional.ofNullable(_vlanId);
  }

  @JsonCreator
  private static @Nonnull Vlan create(
      @JsonProperty(PROP_DHCP_SERVERS) @Nullable List<String> dhcpServers,
      @JsonProperty(PROP_MEMBERS) @Nullable ImmutableList<String> members,
      @JsonProperty(PROP_VLANID) @Nullable JsonNode vlanIdInput) {
    Integer vlanId = SingletonOrSingletonList.deserialize(vlanIdInput, Integer.class);
    return Vlan.builder().setDhcpServers(dhcpServers).setMembers(members).setVlanId(vlanId).build();
  }

  private Vlan(List<String> dhcpServers, List<String> members, @Nullable Integer vlanId) {
    _dhcpServers = dhcpServers;
    _members = members;
    _vlanId = vlanId;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Vlan)) {
      return false;
    }
    Vlan that = (Vlan) o;
    return _dhcpServers.equals(that._dhcpServers)
        && _members.equals(that._members)
        && Objects.equals(_vlanId, that._vlanId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_dhcpServers, _members, _vlanId);
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("dhcp_servers", _dhcpServers)
        .add("members", _members)
        .add("vlanid", _vlanId)
        .toString();
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private List<String> _dhcpServers;
    private List<String> _members;
    private Integer _vlanId;

    public @Nonnull Builder setDhcpServers(@Nullable List<String> dhcpServers) {
      this._dhcpServers = dhcpServers;
      return this;
    }

    public @Nonnull Builder setMembers(@Nullable List<String> members) {
      this._members = members;
      return this;
    }

    public @Nonnull Builder setVlanId(@Nullable Integer vlanId) {
      this._vlanId = vlanId;
      return this;
    }

    public @Nonnull Vlan build() {
      return new Vlan(
          ImmutableList.copyOf(firstNonNull(_dhcpServers, ImmutableList.of())),
          ImmutableList.copyOf(firstNonNull(_members, ImmutableList.of())),
          _vlanId);
    }
  }
}
