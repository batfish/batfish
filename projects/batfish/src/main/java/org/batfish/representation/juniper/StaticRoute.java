package org.batfish.representation.juniper;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;

public abstract class StaticRoute<T> implements Serializable {
  /* https://www.juniper.net/documentation/en_US/junos/topics/reference/general/routing-protocols-default-route-preference-values.html */
  private static final int DEFAULT_ADMIN_DISTANCE = 5;

  private static final int DEFAULT_METRIC = 0;

  private Set<Community> _communities;

  // Null distinguishes "unset" (so a value can be inherited from static defaults) from an explicit
  // value. Unset reads as the Junos default via the getter.
  private @Nullable Integer _distance;

  private boolean _drop;

  private @Nullable Integer _metric;

  private @Nonnull Set<String> _nextHopInterface;

  private @Nonnull Set<T> _nextHopIp;

  private List<String> _policies;

  private @Nullable String _nextTable;

  private @Nullable Boolean _install;

  private @Nullable Boolean _readvertise;

  /**
   * Each qualified next hop will produce a separate static route using properties of the static
   * route and overriding with properties of {@link QualifiedNextHop}
   */
  private Map<NextHop, QualifiedNextHop> _qualifiedNextHops;

  private @Nullable Boolean _resolve;

  private Long _tag;

  private @Nullable Long _tag2;

  public StaticRoute() {
    _communities = new TreeSet<>();
    _policies = new ArrayList<>();
    _qualifiedNextHops = new HashMap<>();
    _nextHopInterface = new HashSet<>();
    _nextHopIp = new HashSet<>();
  }

  public Set<Community> getCommunities() {
    return _communities;
  }

  /** The route's administrative distance (Junos "preference"), or the Junos default if unset. */
  public int getDistance() {
    return firstNonNull(_distance, DEFAULT_ADMIN_DISTANCE);
  }

  public boolean getDrop() {
    return _drop;
  }

  public @Nullable Boolean getInstall() {
    return _install;
  }

  public void setInstall(@Nullable Boolean install) {
    _install = install;
  }

  /** The route's metric, or the Junos default if unset. */
  public int getMetric() {
    return firstNonNull(_metric, DEFAULT_METRIC);
  }

  public Set<String> getNextHopInterface() {
    return _nextHopInterface;
  }

  public Set<T> getNextHopIp() {
    return _nextHopIp;
  }

  public List<String> getPolicies() {
    return _policies;
  }

  public QualifiedNextHop getOrCreateQualifiedNextHop(NextHop nextHop) {
    return _qualifiedNextHops.computeIfAbsent(nextHop, QualifiedNextHop::new);
  }

  public Map<NextHop, QualifiedNextHop> getQualifiedNextHops() {
    return _qualifiedNextHops;
  }

  public @Nullable Boolean getReadvertise() {
    return _readvertise;
  }

  public void setReadvertise(@Nullable Boolean readvertise) {
    _readvertise = readvertise;
  }

  public Long getTag() {
    return _tag;
  }

  public void setTag(long tag) {
    _tag = tag;
  }

  public @Nullable Long getTag2() {
    return _tag2;
  }

  public void setTag2(@Nullable Long tag2) {
    _tag2 = tag2;
  }

  public void setDistance(int distance) {
    _distance = distance;
  }

  /** Clears other next hops and sets discard/drop for the route. */
  public void setDrop() {
    clearNextHops();
    _drop = true;
  }

  public void setMetric(int metric) {
    _metric = metric;
  }

  /** Adds a next hop interface for the route. Also clears the next table and discard property. */
  public void addNextHopInterface(String nextHopInterface) {
    _nextTable = null;
    _drop = false;
    _nextHopInterface.add(nextHopInterface);
  }

  /** Adds a next hop IP for the route. Also clears the next table and discard property. */
  public void addNextHopIp(T nextHopIp) {
    _nextTable = null;
    _drop = false;
    _nextHopIp.add(nextHopIp);
  }

  public @Nullable Boolean getResolve() {
    return _resolve;
  }

  public void setResolve(@Nullable Boolean resolve) {
    _resolve = resolve;
  }

  /**
   * The name of a delegate routing instance/table that should traffic matching this route in its
   * own routing instance.
   */
  public @Nullable String getNextTable() {
    return _nextTable;
  }

  /** Clears other next hops and sets next table for the route. */
  public void setNextTable(@Nullable String nextTable) {
    clearNextHops();
    _nextTable = nextTable;
  }

  /**
   * Clear existing next hops. Used to make sure only one next hop field is set at any given time.
   */
  private void clearNextHops() {
    _nextHopIp.clear();
    _nextHopInterface.clear();
    _nextTable = null;
    _drop = false;
  }

  /**
   * Inherit attributes from {@code defaults} (the RIB's {@code static defaults} block) for any
   * attribute this route does not set explicitly. A route's own value always wins; this only fills
   * in unset fields.
   */
  public void inheritUnsetFields(StaticRoute<T> defaults) {
    if (_distance == null) {
      _distance = defaults._distance;
    }
    if (_metric == null) {
      _metric = defaults._metric;
    }
    if (_tag == null) {
      _tag = defaults._tag;
    }
    if (_tag2 == null) {
      _tag2 = defaults._tag2;
    }
    if (_install == null) {
      _install = defaults._install;
    }
    if (_readvertise == null) {
      _readvertise = defaults._readvertise;
    }
    if (_resolve == null) {
      _resolve = defaults._resolve;
    }
    if (_communities.isEmpty()) {
      _communities.addAll(defaults._communities);
    }
  }
}
