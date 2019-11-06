package org.batfish.representation.cisco_xr.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** BGP neighbor settings. This class implements the union of all address families. */
public class AristaBgpNeighborAddressFamily implements Serializable {
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
  public Boolean getActivate() {
    return _activate;
  }

  public void setActivate(@Nullable Boolean activate) {
    _activate = activate;
  }

  @Nullable
  public AristaBgpAdditionalPathsConfig getAdditionalPaths() {
    return _additionalPaths;
  }

  public void setAdditionalPaths(@Nullable AristaBgpAdditionalPathsConfig additionalPaths) {
    _additionalPaths = additionalPaths;
  }

  /** Whether the neighbors by default are activated in this address family */
  public boolean isDefaultActivate() {
    return _defaultActivate;
  }

  public void setDefaultActivate(boolean defaultActivate) {
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

  public void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
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

  public void setRouteMapIn(@Nullable String routeMapIn) {
    _routeMapIn = routeMapIn;
  }

  @Nullable
  public String getRouteMapOut() {
    return _routeMapOut;
  }

  public void setRouteMapOut(@Nullable String routeMapOut) {
    _routeMapOut = routeMapOut;
  }

  @Nullable
  public Integer getWeight() {
    return _weight;
  }

  public void setWeight(@Nullable Integer weight) {
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
