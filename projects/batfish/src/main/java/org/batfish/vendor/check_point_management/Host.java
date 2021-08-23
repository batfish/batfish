package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** A single host address. */
public final class Host extends AddressSpace {

  @JsonCreator
  private static @Nonnull Host create(
      @JsonProperty(PROP_IPV4_ADDRESS) @Nullable Ip ipv4Address,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(ipv4Address != null, "Missing %s", PROP_IPV4_ADDRESS);
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new Host(ipv4Address, name, uid);
  }

  @VisibleForTesting
  Host(Ip ipv4Address, String name, Uid uid) {
    super(name, uid);
    _ipv4Address = ipv4Address;
  }

  public @Nonnull Ip getIpv4Address() {
    return _ipv4Address;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    Host host = (Host) o;
    return _ipv4Address.equals(host._ipv4Address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _ipv4Address);
  }

  @Override
  public String toString() {
    return baseToStringHelper().add(PROP_IPV4_ADDRESS, _ipv4Address).toString();
  }

  private static final String PROP_IPV4_ADDRESS = "ipv4-address";

  private final @Nonnull Ip _ipv4Address;
}
