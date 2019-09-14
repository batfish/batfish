package org.batfish.representation.cisco.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** BGP neighbor settings. This class implements the union of all address families. */
public final class AristaBgpNeighborAddressFamily implements Serializable {
  @Nullable private Boolean _activate;
  @Nullable private AristaBgpAdditionalPathsConfig _additionalPaths;
  private boolean _defaultActivate = false;
  @Nullable private AristaBgpDefaultOriginate _defaultOriginate;
  @Nullable private Boolean _nextHopUnchanged;
  @Nullable private String _prefixListIn;
  @Nullable private String _prefixListOut;
  @Nullable private String _routeMapIn;
  @Nullable private String _routeMapOut;
  @Nullable private Integer _weight;

  @Nullable
  public final Boolean getActivate() {
    return _activate;
  }

  public final void setActivate(@Nullable Boolean activate) {
    _activate = activate;
  }

  @Nullable
  public final AristaBgpAdditionalPathsConfig getAdditionalPaths() {
    return _additionalPaths;
  }

  public final void setAdditionalPaths(@Nullable AristaBgpAdditionalPathsConfig additionalPaths) {
    _additionalPaths = additionalPaths;
  }

  /** Whether the neighbors by default are activated in this address family */
  public final boolean isDefaultActivate() {
    return _defaultActivate;
  }

  public final void setDefaultActivate(boolean defaultActivate) {
    _defaultActivate = defaultActivate;
  }

  @Nullable
  public AristaBgpDefaultOriginate getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable AristaBgpDefaultOriginate defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  @Nullable
  public Boolean getNextHopUnchanged() {
    return _nextHopUnchanged;
  }

  public final void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
    _nextHopUnchanged = nextHopUnchanged;
  }

  @Nullable
  public String getPrefixListIn() {
    return _prefixListIn;
  }

  public void setPrefixListIn(@Nullable String prefixListIn) {
    _prefixListIn = prefixListIn;
  }

  @Nullable
  public String getPrefixListOut() {
    return _prefixListOut;
  }

  public void setPrefixListOut(@Nullable String prefixListOut) {
    _prefixListOut = prefixListOut;
  }

  @Nullable
  public String getRouteMapIn() {
    return _routeMapIn;
  }

  public final void setRouteMapIn(@Nullable String routeMapIn) {
    _routeMapIn = routeMapIn;
  }

  @Nullable
  public final String getRouteMapOut() {
    return _routeMapOut;
  }

  public final void setRouteMapOut(@Nullable String routeMapOut) {
    _routeMapOut = routeMapOut;
  }

  @Nullable
  public final Integer getWeight() {
    return _weight;
  }

  public final void setWeight(@Nullable Integer weight) {
    _weight = weight;
  }

  public void inheritFrom(AristaBgpNeighborAddressFamily other) {
    if (_activate == null) {
      _activate = other._activate;
    }
    if (_additionalPaths == null) {
      _additionalPaths = other._additionalPaths;
    }
    // how does defaultActivate play in?
    if (_nextHopUnchanged == null) {
      _nextHopUnchanged = other._nextHopUnchanged;
    }
    if (_routeMapIn == null) {
      _routeMapIn = other._routeMapIn;
    }
    if (_routeMapOut == null) {
      _routeMapOut = other._routeMapOut;
    }
    if (_weight == null) {
      _weight = other._weight;
    }
  }
}
