package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** BGP neighbor settings. This class implements the union of all address families. */
public class AristaBgpNeighborAddressFamily implements Serializable {
  private @Nullable Boolean _activate;
  private @Nullable AristaBgpAdditionalPathsConfig _additionalPaths;
  private @Nullable AristaBgpDefaultOriginate _defaultOriginate;
  private @Nullable Boolean _nextHopUnchanged;
  private @Nullable String _prefixListIn;
  private @Nullable String _prefixListOut;
  private @Nullable String _routeMapIn;
  private @Nullable String _routeMapOut;
  private @Nullable Integer _weight;

  public @Nullable Boolean getActivate() {
    return _activate;
  }

  public void setActivate(@Nullable Boolean activate) {
    _activate = activate;
  }

  public @Nullable AristaBgpAdditionalPathsConfig getAdditionalPaths() {
    return _additionalPaths;
  }

  public @Nonnull AristaBgpAdditionalPathsConfig getOrCreateAdditionalPaths() {
    if (_additionalPaths == null) {
      _additionalPaths = new AristaBgpAdditionalPathsConfig();
    }
    return _additionalPaths;
  }

  public @Nullable AristaBgpDefaultOriginate getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable AristaBgpDefaultOriginate defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  public @Nullable Boolean getNextHopUnchanged() {
    return _nextHopUnchanged;
  }

  public void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
    _nextHopUnchanged = nextHopUnchanged;
  }

  public @Nullable String getPrefixListIn() {
    return _prefixListIn;
  }

  public void setPrefixListIn(@Nullable String prefixListIn) {
    _prefixListIn = prefixListIn;
  }

  public @Nullable String getPrefixListOut() {
    return _prefixListOut;
  }

  public void setPrefixListOut(@Nullable String prefixListOut) {
    _prefixListOut = prefixListOut;
  }

  public @Nullable String getRouteMapIn() {
    return _routeMapIn;
  }

  public void setRouteMapIn(@Nullable String routeMapIn) {
    _routeMapIn = routeMapIn;
  }

  public @Nullable String getRouteMapOut() {
    return _routeMapOut;
  }

  public void setRouteMapOut(@Nullable String routeMapOut) {
    _routeMapOut = routeMapOut;
  }

  public @Nullable Integer getWeight() {
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
