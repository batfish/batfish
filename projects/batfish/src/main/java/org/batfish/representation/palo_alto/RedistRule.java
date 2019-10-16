package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.OriginType;

/** Rules used for redisribution, refers to a {@link RedistProfile} */
public final class RedistRule implements Serializable {
  public enum AddressFamilyIdentifier {
    IPV4,
    IPV6
  }

  public enum RouteTableType {
    BOTH,
    MULTICAST,
    UNICAST
  }

  public @Nullable AddressFamilyIdentifier getAddressFamilyIdentifier() {
    return _addressFamilyIdentifier;
  }

  public void setAddressFamilyIdentifier(
      @Nullable AddressFamilyIdentifier addressFamilyIdentifier) {
    _addressFamilyIdentifier = addressFamilyIdentifier;
  }

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public void setEnable(@Nullable Boolean enable) {
    _enable = enable;
  }

  public @Nullable OriginType getOrigin() {
    return _origin;
  }

  public void setOrigin(@Nullable OriginType origin) {
    _origin = origin;
  }

  public @Nullable RouteTableType getRouteTableType() {
    return _routeTableType;
  }

  public void setRouteTableType(@Nullable RouteTableType routeTableType) {
    _routeTableType = routeTableType;
  }

  private @Nullable AddressFamilyIdentifier _addressFamilyIdentifier;
  private @Nullable Boolean _enable;
  private @Nullable OriginType _origin;
  private @Nullable RouteTableType _routeTableType;
}
