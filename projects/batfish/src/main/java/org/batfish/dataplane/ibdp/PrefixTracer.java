package org.batfish.dataplane.ibdp;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.Environment.Direction;

/** Keeps data about which prefixes where advertised to neighboring routers */
public final class PrefixTracer implements Serializable {
  static final String SENT = "sent";
  static final String FILTERED_OUT = "filtered_out";
  static final String FILTERED_IN = "filtered_in";
  static final String RECEIVED = "received";

  public static final class Neighbor implements Serializable {

    private final String _hostname;
    private final Ip _ip;
    @Nullable private final String _routingPolicy;
    private final String _vrfName;

    /**
     * A non-protocol-specific L3 data plane neighbor, identified by hostname, VRF name & IP
     * address. Optional routing policy can be specified as meta data to indicate a policy that
     * allowed or filtered the prefix.
     */
    Neighbor(String hostname, Ip ip, String vrfName, @Nullable String routingPolicy) {
      _hostname = hostname;
      _ip = ip;
      _routingPolicy = routingPolicy;
      _vrfName = vrfName;
    }

    /** Return the remote neighbor's hostname */
    public String getHostname() {
      return _hostname;
    }

    /** Return the remote neighbor's IP */
    public Ip getIp() {
      return _ip;
    }

    /** Return the routing policy (of the current router) */
    @Nullable
    public String getRoutingPolicy() {
      return _routingPolicy;
    }

    /** Return remote neighbor's VRF name */
    public String getVrfname() {
      return _vrfName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof Neighbor)) {
        return false;
      }
      Neighbor neighbor = (Neighbor) o;
      return _hostname.equals(neighbor._hostname)
          && _ip.equals(neighbor._ip)
          && _vrfName.equals(neighbor._vrfName)
          && Objects.equals(_routingPolicy, neighbor._routingPolicy);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_hostname, _vrfName, _ip, _routingPolicy);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("hostname", _hostname)
          .add("ip", _ip)
          .add("routingPolicy", _routingPolicy)
          .add("vrfName", _vrfName)
          .toString();
    }
  }

  private final Map<Prefix, Set<Neighbor>> _filteredOnImport;
  private final Map<Prefix, Set<Neighbor>> _filteredOnExport;
  private final Set<Prefix> _originated;
  private final Map<Prefix, Set<Neighbor>> _installed;
  private final Map<Prefix, Set<Neighbor>> _sent;

  /** Which prefixes should be traced. */
  private final PrefixSpace _prefixesToTrace;

  PrefixTracer() {
    this(DEFAULT_PREFIXES_TO_TRACE);
  }

  PrefixTracer(@Nonnull PrefixSpace prefixesToTrace) {
    _originated = Sets.newConcurrentHashSet();
    _installed = new ConcurrentHashMap<>();
    _sent = new ConcurrentHashMap<>();
    _filteredOnImport = new ConcurrentHashMap<>();
    _filteredOnExport = new ConcurrentHashMap<>();
    _prefixesToTrace = prefixesToTrace;
  }

  /** Note that we considered given prefix for origination */
  void originated(Prefix prefix) {
    if (!_prefixesToTrace.containsPrefix(prefix)) {
      return;
    }
    _originated.add(prefix);
  }

  /** Note that we exported given prefix to a given {@link Neighbor} */
  void sentTo(
      Prefix prefix,
      String neighborHostname,
      Ip neighborIp,
      String neighborVrf,
      @Nullable String exportPolicy) {
    if (!_prefixesToTrace.containsPrefix(prefix)) {
      return;
    }
    Set<Neighbor> set = _sent.computeIfAbsent(prefix, p -> Sets.newConcurrentHashSet());
    set.add(new Neighbor(neighborHostname, neighborIp, neighborVrf, exportPolicy));
  }

  /** Note that we installed a prefix received from a given {@link Neighbor} */
  void installed(
      Prefix prefix,
      String neighborHostname,
      Ip neighborIp,
      String neighborVrf,
      @Nullable String importPolicy) {
    if (!_prefixesToTrace.containsPrefix(prefix)) {
      return;
    }
    Set<Neighbor> set = _installed.computeIfAbsent(prefix, p -> Sets.newConcurrentHashSet());
    set.add(new Neighbor(neighborHostname, neighborIp, neighborVrf, importPolicy));
  }

  /**
   * Note that we did filtered a prefix when sending/receiving routes from given {@link Neighbor}.
   *
   * @param direction {@link Direction} indicating whether we were sending ({@link Direction#OUT})
   *     or receiving ({@link Direction#IN}) the prefix
   */
  void filtered(
      Prefix prefix,
      String neighborHostname,
      Ip neighborIp,
      String neighborVrf,
      @Nullable String policyName,
      Direction direction) {
    if (!_prefixesToTrace.containsPrefix(prefix)) {
      return;
    }
    if (direction == Direction.IN) {
      Set<Neighbor> set =
          _filteredOnImport.computeIfAbsent(prefix, p -> Sets.newConcurrentHashSet());
      set.add(new Neighbor(neighborHostname, neighborIp, neighborVrf, policyName));
    } else if (direction == Direction.OUT) {
      Set<Neighbor> set =
          _filteredOnExport.computeIfAbsent(prefix, p -> Sets.newConcurrentHashSet());
      set.add(new Neighbor(neighborHostname, neighborIp, neighborVrf, policyName));
    } else {
      throw new UnsupportedOperationException("Unknown filtering direction");
    }
  }

  /** Return the set of prefixes we attempted to originate */
  public SortedSet<Prefix> getOriginated() {
    return ImmutableSortedSet.copyOf(_originated);
  }

  /** Return the set of prefixes we successfully sent/exported. */
  public Map<Prefix, Set<Neighbor>> getSent() {
    return _sent;
  }

  /** Return the set of prefixes we received and installed. */
  public Map<Prefix, Set<Neighbor>> getInstalled() {
    return _installed;
  }

  /**
   * Return the set of prefixes we filtered (in a given direction)
   *
   * @param direction {@link Direction} indicating whether the prefixes were filtered on export
   *     ({@link Direction#OUT}) or import ({@link Direction#IN}).
   */
  public Map<Prefix, Set<Neighbor>> getFiltered(Direction direction) {
    if (direction == Direction.IN) {
      return _filteredOnImport;
    } else if (direction == Direction.OUT) {
      return _filteredOnExport;
    } else {
      throw new UnsupportedOperationException("Unknown filtering direction");
    }
  }

  /** Structure: prefix -&gt; action -&gt; set of hostnames */
  public Map<Prefix, Map<String, Set<String>>> summarize() {
    Map<Prefix, Map<String, Set<String>>> result = new HashMap<>();
    _sent.forEach(
        (prefix, neighbors) ->
            neighbors.forEach(
                neighbor ->
                    result
                        .computeIfAbsent(prefix, p -> new HashMap<>())
                        .computeIfAbsent(SENT, a -> new HashSet<>())
                        .add(neighbor.getHostname())));

    _filteredOnExport.forEach(
        (prefix, neighbors) ->
            neighbors.forEach(
                neighbor ->
                    result
                        .computeIfAbsent(prefix, p -> new HashMap<>())
                        .computeIfAbsent(FILTERED_OUT, a -> new HashSet<>())
                        .add(neighbor.getHostname())));
    _filteredOnImport.forEach(
        (prefix, neighbors) ->
            neighbors.forEach(
                neighbor ->
                    result
                        .computeIfAbsent(prefix, p -> new HashMap<>())
                        .computeIfAbsent(FILTERED_IN, a -> new HashSet<>())
                        .add(neighbor.getHostname())));
    _installed.forEach(
        (prefix, neighbors) ->
            neighbors.forEach(
                neighbor ->
                    result
                        .computeIfAbsent(prefix, p -> new HashMap<>())
                        .computeIfAbsent(RECEIVED, a -> new HashSet<>())
                        .add(neighbor.getHostname())));
    return result;
  }

  // If not explicitly provided, this is the space of prefixes that will be traced.
  private static final PrefixSpace DEFAULT_PREFIXES_TO_TRACE = new PrefixSpace(); // none.
}
