package org.batfish.common;

import static com.google.common.base.Functions.constant;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.autocomplete.IpCompletionMetadata;
import org.batfish.common.autocomplete.LocationCompletionMetadata;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.specifier.Location;

/** Grouping of various snapshot properties used for autocomplete */
@ParametersAreNonnullByDefault
public final class CompletionMetadata implements Serializable {

  /** Builder for {@link CompletionMetadata} */
  public static final class Builder {

    private Set<String> _filterNames;

    private Set<NodeInterfacePair> _interfaces;

    private Map<Ip, IpCompletionMetadata> _ips;

    private Set<LocationCompletionMetadata> _locations;

    private Set<String> _mlagIds;

    // mapping: hostname --> humanName
    private Map<String, NodeCompletionMetadata> _nodes;

    private Set<String> _prefixes;

    private Set<String> _routingPolicyNames;

    private Set<String> _structureNames;

    private Set<String> _vrfs;

    private Set<String> _zones;

    private Builder() {}

    public @Nonnull CompletionMetadata build() {
      return new CompletionMetadata(
          firstNonNull(_filterNames, ImmutableSet.of()),
          firstNonNull(_interfaces, ImmutableSet.of()),
          firstNonNull(_ips, ImmutableMap.of()),
          firstNonNull(_locations, ImmutableSet.of()),
          firstNonNull(_mlagIds, ImmutableSet.of()),
          firstNonNull(_nodes, ImmutableMap.of()),
          firstNonNull(_prefixes, ImmutableSet.of()),
          firstNonNull(_routingPolicyNames, ImmutableSet.of()),
          firstNonNull(_structureNames, ImmutableSet.of()),
          firstNonNull(_vrfs, ImmutableSet.of()),
          firstNonNull(_zones, ImmutableSet.of()));
    }

    public @Nonnull Builder setFilterNames(Set<String> filterNames) {
      _filterNames = ImmutableSet.copyOf(filterNames);
      return this;
    }

    public @Nonnull Builder setInterfaces(Set<NodeInterfacePair> interfaces) {
      _interfaces = ImmutableSet.copyOf(interfaces);
      return this;
    }

    public @Nonnull Builder setIps(Set<Ip> ips) {
      setIps(
          ips.stream()
              .collect(ImmutableMap.toImmutableMap(identity(), ip -> new IpCompletionMetadata())));
      return this;
    }

    public @Nonnull Builder setIps(Map<Ip, IpCompletionMetadata> ips) {
      _ips = ImmutableMap.copyOf(ips);
      return this;
    }

    public @Nonnull Builder setLocations(Set<LocationCompletionMetadata> locations) {
      _locations = ImmutableSet.copyOf(locations);
      return this;
    }

    public @Nonnull Builder setMlagIds(Set<String> mlagIds) {
      _mlagIds = ImmutableSet.copyOf(mlagIds);
      return this;
    }

    public @Nonnull Builder setNodes(Set<String> nodes) {
      return setNodes(
          nodes.stream()
              .collect(
                  ImmutableMap.toImmutableMap(
                      identity(), constant(new NodeCompletionMetadata(null)))));
    }

    public @Nonnull Builder setNodes(Map<String, NodeCompletionMetadata> nodes) {
      _nodes = ImmutableMap.copyOf(nodes);
      return this;
    }

    public @Nonnull Builder setPrefixes(Set<String> prefixes) {
      _prefixes = ImmutableSet.copyOf(prefixes);
      return this;
    }

    public @Nonnull Builder setRoutingPolicyNames(Set<String> routingPolicyNames) {
      _routingPolicyNames = ImmutableSet.copyOf(routingPolicyNames);
      return this;
    }

    public @Nonnull Builder setStructureNames(Set<String> structureNames) {
      _structureNames = ImmutableSet.copyOf(structureNames);
      return this;
    }

    public @Nonnull Builder setVrfs(Set<String> vrfs) {
      _vrfs = ImmutableSet.copyOf(vrfs);
      return this;
    }

    public @Nonnull Builder setZones(Set<String> zones) {
      _zones = ImmutableSet.copyOf(zones);
      return this;
    }
  }

  /**
   * Address books and groups do not belong here as they are network-wide. Leaving these properties
   * in place to be able to de-serialize old data. Should be removed at some point.
   */
  @Deprecated private static final String PROP_ADDRESS_BOOKS = "addressBooks";

  @Deprecated private static final String PROP_ADDRESS_GROUPS = "addressGroups";
  private static final String PROP_FILTER_NAMES = "filterNames";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_IPS = "ips";
  private static final String PROP_LOCATIONS = "locations";
  // deprecated location information
  private static final String PROP_SOURCE_LOCATIONS = "locationInfo";
  private static final String PROP_MLAG_IDS = "mlagIds";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PREFIXES = "prefixes";
  private static final String PROP_ROUTING_POLICY_NAMES = "routingPolicyNames";
  private static final String PROP_STRUCTURE_NAMES = "structureNames";
  private static final String PROP_VRFS = "vrfs";
  private static final String PROP_ZONES = "zones";

  private final Set<String> _filterNames;

  private final Set<NodeInterfacePair> _interfaces;

  private final Map<Ip, IpCompletionMetadata> _ips;

  private final Set<LocationCompletionMetadata> _locations;

  private final Set<String> _mlagIds;

  private final Map<String, NodeCompletionMetadata> _nodes;

  private final Set<String> _prefixes;

  private final Set<String> _routingPolicyNames;

  private final Set<String> _structureNames;

  private final Set<String> _vrfs;

  private final Set<String> _zones;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static final CompletionMetadata EMPTY = builder().build();

  @JsonCreator
  private static @Nonnull CompletionMetadata jsonCreator(
      @Nullable @JsonProperty(PROP_ADDRESS_BOOKS) Set<String> addressBooks,
      @Nullable @JsonProperty(PROP_ADDRESS_GROUPS) Set<String> addressGroups,
      @Nullable @JsonProperty(PROP_FILTER_NAMES) Set<String> filterNames,
      @Nullable @JsonProperty(PROP_INTERFACES) Set<NodeInterfacePair> interfaces,
      @Nullable @JsonProperty(PROP_IPS) Map<Ip, IpCompletionMetadata> ips,
      @Nullable @JsonProperty(PROP_LOCATIONS) Set<LocationCompletionMetadata> locations,
      // deprecated; included for backward compatibility Nov 9, 2020
      @Nullable @JsonProperty(PROP_SOURCE_LOCATIONS) Set<Location> sourceLocations,
      @Nullable @JsonProperty(PROP_MLAG_IDS) Set<String> mlagIds,
      @Nullable @JsonProperty(PROP_NODES) Map<String, NodeCompletionMetadata> nodes,
      @Nullable @JsonProperty(PROP_PREFIXES) Set<String> prefixes,
      @Nullable @JsonProperty(PROP_ROUTING_POLICY_NAMES) Set<String> routingPolicyNames,
      @Nullable @JsonProperty(PROP_STRUCTURE_NAMES) Set<String> structureNames,
      @Nullable @JsonProperty(PROP_VRFS) Set<String> vrfs,
      @Nullable @JsonProperty(PROP_ZONES) Set<String> zones) {
    checkArgument(
        locations == null || sourceLocations == null,
        "At most one of %s or %s must be present",
        PROP_LOCATIONS,
        PROP_SOURCE_LOCATIONS);
    return new CompletionMetadata(
        firstNonNull(filterNames, ImmutableSet.of()),
        firstNonNull(interfaces, ImmutableSet.of()),
        firstNonNull(ips, ImmutableMap.of()),
        firstNonNull(
            locations,
            firstNonNull(sourceLocations, ImmutableSet.<Location>of()).stream()
                .map(loc -> new LocationCompletionMetadata(loc, true))
                .collect(ImmutableSet.toImmutableSet()),
            ImmutableSet.of()),
        firstNonNull(mlagIds, ImmutableSet.of()),
        firstNonNull(nodes, ImmutableMap.of()),
        firstNonNull(prefixes, ImmutableSet.of()),
        firstNonNull(routingPolicyNames, ImmutableSet.of()),
        firstNonNull(structureNames, ImmutableSet.of()),
        firstNonNull(vrfs, ImmutableSet.of()),
        firstNonNull(zones, ImmutableSet.of()));
  }

  public CompletionMetadata(
      Set<String> filterNames,
      Set<NodeInterfacePair> interfaces,
      Map<Ip, IpCompletionMetadata> ips,
      Set<LocationCompletionMetadata> locations,
      Set<String> mlagIds,
      Map<String, NodeCompletionMetadata> nodes,
      Set<String> prefixes,
      Set<String> routingPolicyNames,
      Set<String> structureNames,
      Set<String> vrfs,
      Set<String> zones) {
    _filterNames = filterNames;
    _interfaces = interfaces;
    _ips = ips;
    _locations = locations;
    _mlagIds = mlagIds;
    _nodes = nodes;
    _prefixes = prefixes;
    _routingPolicyNames = routingPolicyNames;
    _structureNames = structureNames;
    _vrfs = vrfs;
    _zones = zones;
  }

  @JsonProperty(PROP_FILTER_NAMES)
  @Nonnull
  public Set<String> getFilterNames() {
    return _filterNames;
  }

  @JsonProperty(PROP_INTERFACES)
  @Nonnull
  public Set<NodeInterfacePair> getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_IPS)
  @Nonnull
  public Map<Ip, IpCompletionMetadata> getIps() {
    return _ips;
  }

  @JsonProperty(PROP_LOCATIONS)
  @Nonnull
  public Set<LocationCompletionMetadata> getLocations() {
    return _locations;
  }

  /** Returns the full set of MLAG domain ids in the snapshot */
  @JsonProperty(PROP_MLAG_IDS)
  @Nonnull
  public Set<String> getMlagIds() {
    return _mlagIds;
  }

  @JsonProperty(PROP_NODES)
  @Nonnull
  public Map<String, NodeCompletionMetadata> getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_PREFIXES)
  @Nonnull
  public Set<String> getPrefixes() {
    return _prefixes;
  }

  @JsonProperty(PROP_ROUTING_POLICY_NAMES)
  @Nonnull
  public Set<String> getRoutingPolicyNames() {
    return _routingPolicyNames;
  }

  @JsonProperty(PROP_STRUCTURE_NAMES)
  @Nonnull
  public Set<String> getStructureNames() {
    return _structureNames;
  }

  @JsonProperty(PROP_VRFS)
  @Nonnull
  public Set<String> getVrfs() {
    return _vrfs;
  }

  @JsonProperty(PROP_ZONES)
  @Nonnull
  public Set<String> getZones() {
    return _zones;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CompletionMetadata)) {
      return false;
    }
    CompletionMetadata rhs = (CompletionMetadata) obj;
    return _filterNames.equals(rhs._filterNames)
        && _interfaces.equals(rhs._interfaces)
        && _ips.equals(rhs._ips)
        && _locations.equals(rhs._locations)
        && _mlagIds.equals(rhs._mlagIds)
        && _nodes.equals(rhs._nodes)
        && _prefixes.equals(rhs._prefixes)
        && _routingPolicyNames.equals(rhs._routingPolicyNames)
        && _structureNames.equals(rhs._structureNames)
        && _vrfs.equals(rhs._vrfs)
        && _zones.equals(rhs._zones);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _filterNames,
        _interfaces,
        _ips,
        _locations,
        _mlagIds,
        _nodes,
        _prefixes,
        _routingPolicyNames,
        _structureNames,
        _vrfs,
        _zones);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_FILTER_NAMES, _filterNames)
        .add(PROP_INTERFACES, _interfaces)
        .add(PROP_IPS, _ips)
        .add(PROP_LOCATIONS, _locations)
        .add(PROP_MLAG_IDS, _mlagIds)
        .add(PROP_NODES, _nodes)
        .add(PROP_PREFIXES, _prefixes)
        .add(PROP_ROUTING_POLICY_NAMES, _routingPolicyNames)
        .add(PROP_STRUCTURE_NAMES, _structureNames)
        .add(PROP_VRFS, _vrfs)
        .add(PROP_ZONES, _zones)
        .toString();
  }
}
