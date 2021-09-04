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

  @Override
  public <T> T accept(AddressSpaceVisitor<T> visitor) {
    return visitor.visitNetwork(this);
  }

  @JsonCreator
  private static @Nonnull Network create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_NAT_SETTINGS) @Nullable NatSettings natSettings,
      @JsonProperty(PROP_SUBNET4) @Nullable Ip subnet4,
      @JsonProperty(PROP_SUBNET_MASK) @Nullable Ip subnetMask,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(natSettings != null, "Missing %s", PROP_NAT_SETTINGS);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new Network(name, natSettings, subnet4, subnetMask, uid);
  }

  @VisibleForTesting
  public Network(
      String name,
      NatSettings natSettings,
      @Nullable Ip subnet4,
      @Nullable Ip subnetMask,
      Uid uid) {
    super(name, uid);
    _natSettings = natSettings;
    _subnet4 = subnet4;
    _subnetMask = subnetMask;
  }

  public @Nonnull NatSettings getNatSettings() {
    return _natSettings;
  }

  public @Nullable Ip getSubnet4() {
    return _subnet4;
  }

  public @Nullable Ip getSubnetMask() {
    return _subnetMask;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    Network network = (Network) o;
    return _natSettings.equals(network._natSettings)
        && Objects.equals(_subnet4, network._subnet4)
        && Objects.equals(_subnetMask, network._subnetMask);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _natSettings, _subnet4, _subnetMask);
  }

  @Override
  public String toString() {
    return baseToStringHelper()
        .add(PROP_NAT_SETTINGS, _natSettings)
        .add(PROP_SUBNET4, _subnet4)
        .add(PROP_SUBNET_MASK, _subnetMask)
        .toString();
  }

  private static final String PROP_NAT_SETTINGS = "nat-settings";
  private static final String PROP_SUBNET4 = "subnet4";
  private static final String PROP_SUBNET_MASK = "subnet-mask";

  private final @Nonnull NatSettings _natSettings;
  private final @Nullable Ip _subnet4;
  private final @Nullable Ip _subnetMask;
}
