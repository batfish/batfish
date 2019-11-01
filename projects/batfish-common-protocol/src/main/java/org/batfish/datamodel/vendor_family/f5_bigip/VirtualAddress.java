package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for a virtual-address. */
public final class VirtualAddress implements Serializable {

  public static class Builder {

    public @Nonnull VirtualAddress build() {
      checkArgument(_name != null, "Missing name");
      return new VirtualAddress(
          _address,
          _address6,
          _arpDisabled,
          _icmpEchoDisabled,
          _mask,
          _mask6,
          _name,
          _routeAdvertisementMode);
    }

    public @Nonnull Builder setAddress(@Nullable Ip address) {
      _address = address;
      return this;
    }

    public @Nonnull Builder setAddress6(@Nullable Ip6 address6) {
      _address6 = address6;
      return this;
    }

    public @Nonnull Builder setArpDisabled(@Nullable Boolean arpDisabled) {
      _arpDisabled = arpDisabled;
      return this;
    }

    public @Nonnull Builder setIcmpEchoDisabled(@Nullable Boolean icmpEchoDisabled) {
      _icmpEchoDisabled = icmpEchoDisabled;
      return this;
    }

    public @Nonnull Builder setMask(@Nullable Ip mask) {
      _mask = mask;
      return this;
    }

    public @Nonnull Builder setMask6(@Nullable Ip6 mask6) {
      _mask6 = mask6;
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setRouteAdvertisementMode(
        @Nullable RouteAdvertisementMode routeAdvertisementMode) {
      _routeAdvertisementMode = routeAdvertisementMode;
      return this;
    }

    private @Nullable Ip _address;
    private @Nullable Ip6 _address6;
    private @Nullable Boolean _arpDisabled;
    private @Nullable Boolean _icmpEchoDisabled;
    private @Nullable Ip _mask;
    private @Nullable Ip6 _mask6;
    private @Nullable String _name;
    private @Nullable RouteAdvertisementMode _routeAdvertisementMode;

    private Builder() {}
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  public @Nullable Boolean getArpDisabled() {
    return _arpDisabled;
  }

  public @Nullable Boolean getIcmpEchoDisabled() {
    return _icmpEchoDisabled;
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

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VirtualAddress)) {
      return false;
    }
    VirtualAddress rhs = (VirtualAddress) obj;
    return Objects.equals(_address, rhs._address)
        && Objects.equals(_address6, rhs._address6)
        && Objects.equals(_arpDisabled, rhs._arpDisabled)
        && Objects.equals(_icmpEchoDisabled, rhs._icmpEchoDisabled)
        && Objects.equals(_mask, rhs._mask)
        && Objects.equals(_mask6, rhs._mask6)
        && _name.equals(rhs._name)
        && Objects.equals(_routeAdvertisementMode, rhs._routeAdvertisementMode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _address,
        _address6,
        _arpDisabled,
        _icmpEchoDisabled,
        _mask,
        _mask6,
        _name,
        _routeAdvertisementMode != null ? _routeAdvertisementMode.ordinal() : null);
  }

  private final @Nullable Ip _address;
  private final @Nullable Ip6 _address6;
  private final @Nullable Boolean _arpDisabled;
  private final @Nullable Boolean _icmpEchoDisabled;
  private final @Nullable Ip _mask;
  private final @Nullable Ip6 _mask6;
  private final @Nonnull String _name;
  private final @Nullable RouteAdvertisementMode _routeAdvertisementMode;

  private VirtualAddress(
      @Nullable Ip address,
      @Nullable Ip6 address6,
      @Nullable Boolean arpDisabled,
      @Nullable Boolean icmpEchoDisabled,
      @Nullable Ip mask,
      @Nullable Ip6 mask6,
      String name,
      @Nullable RouteAdvertisementMode routeAdvertisementMode) {
    _address = address;
    _address6 = address6;
    _arpDisabled = arpDisabled;
    _icmpEchoDisabled = icmpEchoDisabled;
    _mask = mask;
    _mask6 = mask6;
    _name = name;
    _routeAdvertisementMode = routeAdvertisementMode;
  }
}
