package org.batfish.representation.arista.eos;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** BGP neighbor settings. This class implements the union of all address families. */
public class AristaBgpNeighborAddressFamily implements Serializable {
  @Nullable private Boolean _activate;
  @Nullable private AristaBgpAdditionalPathsConfig _additionalPaths;
  @Nullable private AristaBgpNeighborDefaultOriginate _defaultOriginate;
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

  @Nonnull
  public AristaBgpAdditionalPathsConfig getOrCreateAdditionalPaths() {
    if (_additionalPaths == null) {
      _additionalPaths = new AristaBgpAdditionalPathsConfig();
    }
    return _additionalPaths;
  }

  @Nullable
  public AristaBgpNeighborDefaultOriginate getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable AristaBgpNeighborDefaultOriginate defaultOriginate) {
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
    if (_prefixListIn == null) {
      _prefixListIn = other._prefixListIn;
    }
    if (_prefixListOut == null) {
      _prefixListOut = other._prefixListOut;
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
