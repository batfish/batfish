package org.batfish.representation.juniper;

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

  /**
   * The enclosing {@code static {}} block's {@code defaults} route, whose attributes this route
   * inherits for any field it does not set itself, or null if this route IS the defaults route.
   * Resolving inheritance lazily in the getters (rather than copying fields in) keeps a route's own
   * unset/explicit distinction intact and means callers need no separate inheritance step.
   */
  private final @Nullable StaticRoute<T> _defaults;

  private final Set<Community> _communities;

  // Null distinguishes "unset" (inherit from defaults) from an explicit value. An unset field with
  // no defaults to inherit reads as the Junos default via the getter.
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

  private @Nullable Long _tag;

  private @Nullable Long _tag2;

  public StaticRoute(@Nullable StaticRoute<T> defaults) {
    _defaults = defaults;
    _communities = new TreeSet<>();
    _policies = new ArrayList<>();
    _qualifiedNextHops = new HashMap<>();
    _nextHopInterface = new HashSet<>();
    _nextHopIp = new HashSet<>();
  }

  /**
   * The route's communities: its own if it sets any, otherwise those inherited from the {@code
   * defaults} block. Junos replaces (does not merge) the defaults' communities when a route sets
   * its own.
   */
  public Set<Community> getCommunities() {
    if (_communities.isEmpty() && _defaults != null) {
      return _defaults.getCommunities();
    }
    return _communities;
  }

  /** Adds a community to this route's own set (used while parsing). */
  public void addCommunity(Community community) {
    _communities.add(community);
  }

  /**
   * The route's administrative distance (Junos "preference"): its own if set, else inherited from
   * the {@code defaults} block, else the Junos default.
   */
  public int getDistance() {
    if (_distance != null) {
      return _distance;
    }
    return _defaults != null ? _defaults.getDistance() : DEFAULT_ADMIN_DISTANCE;
  }

  public boolean getDrop() {
    return _drop;
  }

  public @Nullable Boolean getInstall() {
    return _install != null ? _install : (_defaults != null ? _defaults.getInstall() : null);
  }

  public void setInstall(@Nullable Boolean install) {
    _install = install;
  }

  /** The route's metric: its own if set, else inherited from the {@code defaults} block, else 0. */
  public int getMetric() {
    if (_metric != null) {
      return _metric;
    }
    return _defaults != null ? _defaults.getMetric() : DEFAULT_METRIC;
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
    return _readvertise != null
        ? _readvertise
        : (_defaults != null ? _defaults.getReadvertise() : null);
  }

  public void setReadvertise(@Nullable Boolean readvertise) {
    _readvertise = readvertise;
  }

  public @Nullable Long getTag() {
    return _tag != null ? _tag : (_defaults != null ? _defaults.getTag() : null);
  }

  public void setTag(long tag) {
    _tag = tag;
  }

  public @Nullable Long getTag2() {
    return _tag2 != null ? _tag2 : (_defaults != null ? _defaults.getTag2() : null);
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
    return _resolve != null ? _resolve : (_defaults != null ? _defaults.getResolve() : null);
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
}
