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
public final class Host extends TypedManagementObject
    implements HasNatSettings, Machine, NatTranslatedDestination, NatTranslatedSource {

  @Override
  public <T> T accept(AddressSpaceVisitor<T> visitor) {
    return visitor.visitHost(this);
  }

  @Override
  public <T> T accept(HasNatSettingsVisitor<T> visitor) {
    return visitor.visitHost(this);
  }

  @Override
  public <T> T accept(MachineVisitor<T> visitor) {
    return visitor.visitHost(this);
  }

  @Override
  public <T> T accept(NatTranslatedDestinationVisitor<T> visitor) {
    return visitor.visitHost(this);
  }

  @Override
  public <T> T accept(NatTranslatedSourceVisitor<T> visitor) {
    return visitor.visitHost(this);
  }

  @JsonCreator
  private static @Nonnull Host create(
      @JsonProperty(PROP_IPV4_ADDRESS) @Nullable Ip ipv4Address,
      @JsonProperty(PROP_NAT_SETTINGS) @Nullable NatSettings natSettings,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(natSettings != null, "Missing %s", PROP_NAT_SETTINGS);
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new Host(ipv4Address, natSettings, name, uid);
  }

  @VisibleForTesting
  public Host(@Nullable Ip ipv4Address, NatSettings natSettings, String name, Uid uid) {
    super(name, uid);
    _ipv4Address = ipv4Address;
    _natSettings = natSettings;
  }

  public @Nullable Ip getIpv4Address() {
    return _ipv4Address;
  }

  @Override
  public @Nonnull NatSettings getNatSettings() {
    return _natSettings;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    Host host = (Host) o;
    return Objects.equals(_ipv4Address, host._ipv4Address)
        && _natSettings.equals(host._natSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _ipv4Address, _natSettings);
  }

  @Override
  public String toString() {
    return baseToStringHelper()
        .add(PROP_IPV4_ADDRESS, _ipv4Address)
        .add(PROP_NAT_SETTINGS, _natSettings)
        .toString();
  }

  private static final String PROP_IPV4_ADDRESS = "ipv4-address";
  private static final String PROP_NAT_SETTINGS = "nat-settings";

  private final @Nullable Ip _ipv4Address;
  private final @Nonnull NatSettings _natSettings;
}
