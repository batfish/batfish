package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for a virtual-address. */
@ParametersAreNonnullByDefault
public final class VirtualAddress implements Serializable {
  private static final String PROP_ADDRESS = "address";
  private static final String PROP_ADDRESS6 = "address6";
  private static final String PROP_ARP_DISABLED = "arpDisabled";
  private static final String PROP_ICMP_ECHO_DISABLED = "icmpEchoDisabled";
  private static final String PROP_MASK = "mask";
  private static final String PROP_MASK6 = "mask6";
  private static final String PROP_NAME = "name";
  private static final String PROP_ROUTE_ADVERTISEMENT_MODE = "routeAdvertisementMode";
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull VirtualAddress create(@JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    return new VirtualAddress(name);
  }

  private @Nullable Ip _address;
  private @Nullable Ip6 _address6;
  private @Nullable Boolean _arpDisabled;
  private @Nullable Boolean _icmpEchoDisabled;
  private @Nullable Ip _mask;
  private @Nullable Ip6 _mask6;
  private final @Nonnull String _name;
  private @Nullable RouteAdvertisementMode _routeAdvertisementMode;

  public VirtualAddress(String name) {
    _name = name;
  }

  @JsonProperty(PROP_ADDRESS)
  public @Nullable Ip getAddress() {
    return _address;
  }

  @JsonProperty(PROP_ADDRESS6)
  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  @JsonProperty(PROP_ARP_DISABLED)
  public @Nullable Boolean getArpDisabled() {
    return _arpDisabled;
  }

  @JsonProperty(PROP_ICMP_ECHO_DISABLED)
  public @Nullable Boolean getIcmpEchoDisabled() {
    return _icmpEchoDisabled;
  }

  @JsonProperty(PROP_MASK)
  public @Nullable Ip getMask() {
    return _mask;
  }

  @JsonProperty(PROP_MASK6)
  public @Nullable Ip6 getMask6() {
    return _mask6;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_ROUTE_ADVERTISEMENT_MODE)
  public @Nullable RouteAdvertisementMode getRouteAdvertisementMode() {
    return _routeAdvertisementMode;
  }

  @JsonProperty(PROP_ADDRESS)
  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  @JsonProperty(PROP_ADDRESS6)
  public void setAddress6(@Nullable Ip6 address6) {
    _address6 = address6;
  }

  @JsonProperty(PROP_ARP_DISABLED)
  public void setArpDisabled(@Nullable Boolean arpDisabled) {
    _arpDisabled = arpDisabled;
  }

  public void setIcmpEchoDisabled(@Nullable Boolean icmpEchoDisabled) {
    _icmpEchoDisabled = icmpEchoDisabled;
  }

  @JsonProperty(PROP_MASK)
  public void setMask(@Nullable Ip mask) {
    _mask = mask;
  }

  @JsonProperty(PROP_MASK6)
  public void setMask6(@Nullable Ip6 mask6) {
    _mask6 = mask6;
  }

  @JsonProperty(PROP_ROUTE_ADVERTISEMENT_MODE)
  public void setRouteAdvertisementMode(@Nullable RouteAdvertisementMode routeAdvertisementMode) {
    _routeAdvertisementMode = routeAdvertisementMode;
  }
}
