package org.batfish.datamodel.vendor_family.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for a virtual-address. */
@ParametersAreNonnullByDefault
public final class VirtualAddress implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable Ip _address;
  private @Nullable Ip6 _address6;
  private @Nullable Ip _mask;
  private @Nullable Ip6 _mask6;
  private final @Nonnull String _name;
  private @Nullable RouteAdvertisementMode _routeAdvertisementMode;

  public VirtualAddress(String name) {
    _name = name;
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  public @Nullable Ip getMask() {
    return _mask;
  }

  public @Nullable Ip6 getMask6() {
    return _mask6;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable RouteAdvertisementMode getRouteAdvertisementMode() {
    return _routeAdvertisementMode;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  public void setAddress6(@Nullable Ip6 address6) {
    _address6 = address6;
  }

  public void setMask(@Nullable Ip mask) {
    _mask = mask;
  }

  public void setMask6(@Nullable Ip6 mask6) {
    _mask6 = mask6;
  }

  public void setRouteAdvertisementMode(@Nullable RouteAdvertisementMode routeAdvertisementMode) {
    _routeAdvertisementMode = routeAdvertisementMode;
  }
}
