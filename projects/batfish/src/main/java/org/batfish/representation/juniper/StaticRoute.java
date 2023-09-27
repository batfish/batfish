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
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.community.Community;

public class StaticRoute implements Serializable {

  /* https://www.juniper.net/documentation/en_US/junos/topics/reference/general/routing-protocols-default-route-preference-values.html */
  private static final int DEFAULT_ADMIN_DISTANCE = 5;

  private Set<Community> _communities;

  private int _distance;

  private boolean _drop;

  private int _metric;

  private @Nonnull Set<String> _nextHopInterface;

  private @Nonnull Set<Ip> _nextHopIp;

  private List<String> _policies;

  private Prefix _prefix;

  private @Nullable String _nextTable;

  private @Nullable Boolean _resolve;

  /**
   * Each qualified next hop will produce a separate static route using properties of the static
   * route and overriding with properties of {@link QualifiedNextHop}
   */
  private Map<NextHop, QualifiedNextHop> _qualifiedNextHops;

  private Long _tag;

  private @Nullable Boolean _noInstall;

  public StaticRoute(Prefix prefix) {
    _communities = new TreeSet<>();
    _prefix = prefix;
    _policies = new ArrayList<>();
    // default admin costs for static routes in Juniper
    _distance = DEFAULT_ADMIN_DISTANCE;
    _qualifiedNextHops = new HashMap<>();
    _nextHopInterface = new HashSet<>();
    _nextHopIp = new HashSet<>();
  }

  public Set<Community> getCommunities() {
    return _communities;
  }

  public int getDistance() {
    return _distance;
  }

  public boolean getDrop() {
    return _drop;
  }

  public int getMetric() {
    return _metric;
  }

  public Set<String> getNextHopInterface() {
    return _nextHopInterface;
  }

  public Set<Ip> getNextHopIp() {
    return _nextHopIp;
  }

  public @Nullable Boolean getNoInstall() {
    return _noInstall;
  }

  public List<String> getPolicies() {
    return _policies;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public QualifiedNextHop getOrCreateQualifiedNextHop(NextHop nextHop) {
    return _qualifiedNextHops.computeIfAbsent(nextHop, QualifiedNextHop::new);
  }

  public Map<NextHop, QualifiedNextHop> getQualifiedNextHops() {
    return _qualifiedNextHops;
  }

  public Long getTag() {
    return _tag;
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
  public void addNextHopIp(Ip nextHopIp) {
    _nextTable = null;
    _drop = false;
    _nextHopIp.add(nextHopIp);
  }

  public void setNoInstall(@Nullable Boolean noInstall) {
    _noInstall = noInstall;
  }

  public void setTag(long tag) {
    _tag = tag;
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
}
