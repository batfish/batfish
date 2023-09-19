package org.batfish.representation.palo_alto;

import javax.annotation.Nullable;

/** Configurations specific to NSSA type OSPF area */
public class OspfAreaNssa implements OspfAreaTypeSettings {
  public enum DefaultRouteType {
    EXT_1,
    EXT_2
  }

  /** If not appearing in the config then default route is not disabled. */
  private static final boolean DEFAULT_DEFAULT_ROUTE_DISABLE = false;

  public OspfAreaNssa() {
    _defaultRouteDisable = DEFAULT_DEFAULT_ROUTE_DISABLE;
  }

  @Override
  public <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor) {
    return visitor.visitOspfAreaNssa(this);
  }

  /**
   * If set then default route will not be advertised from this area
   *
   * @return true if no default route will be advertised from this area
   */
  public boolean isDefaultRouteDisable() {
    return _defaultRouteDisable;
  }

  public void setDefaultRouteDisable(boolean defaultRouteDisable) {
    _defaultRouteDisable = defaultRouteDisable;
  }

  public @Nullable Integer getDefaultRouteMetric() {
    return _defaultRouteMetric;
  }

  public void setDefaultRouteMetric(@Nullable Integer defaultRouteMetric) {
    _defaultRouteMetric = defaultRouteMetric;
  }

  public @Nullable DefaultRouteType getDefaultRouteType() {
    return _defaultRouteType;
  }

  public void setDefaultRouteType(@Nullable DefaultRouteType defaultRouteType) {
    _defaultRouteType = defaultRouteType;
  }

  public @Nullable Boolean getAcceptSummary() {
    return _acceptSummary;
  }

  public void setAcceptSummary(@Nullable Boolean acceptSummary) {
    _acceptSummary = acceptSummary;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private boolean _defaultRouteDisable;
  private @Nullable Integer _defaultRouteMetric;
  private @Nullable DefaultRouteType _defaultRouteType;
  private @Nullable Boolean _acceptSummary;
}
