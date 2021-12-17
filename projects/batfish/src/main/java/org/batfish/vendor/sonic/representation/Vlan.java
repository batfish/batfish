package org.batfish.vendor.sonic.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents the settings of a VLAN: https://github.com/Azure/SONiC/wiki/Configuration#vlan */
public class Vlan implements Serializable {
  private static final String PROP_MEMBERS = "members";
  private static final String PROP_VLANID = "vlanid";

  private @Nonnull final List<String> _members;
  private @Nullable final Integer _vlanId;

  public @Nonnull List<String> getMembers() {
    return _members;
  }

  public @Nonnull Optional<Integer> getVlanId() {
    return Optional.ofNullable(_vlanId);
  }

  @JsonCreator
  private @Nonnull static Vlan create(
      @Nullable @JsonProperty(PROP_MEMBERS) ImmutableList<String> members,
      @Nullable @JsonProperty(PROP_VLANID) Integer vlanId) {
    // dhcp_servers are ignored
    return Vlan.builder().setMembers(members).setVlanId(vlanId).build();
  }

  private Vlan(List<String> members, @Nullable Integer vlanId) {
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
    return _members.equals(that._members) && Objects.equals(_vlanId, that._vlanId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_members, _vlanId);
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("members", _members)
        .add("vlanid", _vlanId)
        .toString();
  }

  public @Nonnull static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ImmutableList<String> _members;
    private Integer _vlanId;

    public @Nonnull Builder setMembers(@Nullable ImmutableList<String> members) {
      this._members = members;
      return this;
    }

    public @Nonnull Builder setVlanId(@Nullable Integer vlanId) {
      this._vlanId = vlanId;
      return this;
    }

    public @Nonnull Vlan build() {
      return new Vlan(firstNonNull(_members, ImmutableList.of()), _vlanId);
    }
  }
}
