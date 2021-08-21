package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** An IPv4 network. */
public final class Network extends AddressSpace {

  @JsonCreator
  private static @Nonnull Network create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_SUBNET4) @Nullable Ip subnet4,
      @JsonProperty(PROP_SUBNET_MASK) @Nullable Ip subnetMask,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(subnet4 != null, "Missing %s", PROP_SUBNET4);
    checkArgument(subnetMask != null, "Missing %s", PROP_SUBNET_MASK);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new Network(name, subnet4, subnetMask, uid);
  }

  @VisibleForTesting
  Network(String name, Ip subnet4, Ip subnetMask, Uid uid) {
    super(name, uid);
    _subnet4 = subnet4;
    _subnetMask = subnetMask;
  }

  public @Nonnull Ip getSubnet4() {
    return _subnet4;
  }

  public @Nonnull Ip getSubnetMask() {
    return _subnetMask;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    Network network = (Network) o;
    return _subnet4.equals(network._subnet4) && _subnetMask.equals(network._subnetMask);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _subnet4, _subnetMask);
  }

  @Override
  public String toString() {
    return baseToStringHelper()
        .add(PROP_SUBNET4, _subnet4)
        .add(PROP_SUBNET_MASK, _subnetMask)
        .toString();
  }

  private static final String PROP_SUBNET4 = "subnet4";
  private static final String PROP_SUBNET_MASK = "subnet-mask";

  private final @Nonnull Ip _subnet4;
  private final @Nonnull Ip _subnetMask;
}
