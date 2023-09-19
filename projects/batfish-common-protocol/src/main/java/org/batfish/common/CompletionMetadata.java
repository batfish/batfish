package org.batfish.common;

import static com.google.common.base.Functions.constant;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

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
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Grouping of various snapshot properties used for autocomplete */
@ParametersAreNonnullByDefault
public final class CompletionMetadata implements Serializable {

  /** Builder for {@link CompletionMetadata} */
  public static final class Builder {

    private Set<String> _filterNames;

    private Set<NodeInterfacePair> _interfaces;

    private PrefixTrieMultiMap<IpCompletionMetadata> _ips;

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
          firstNonNull(_ips, new PrefixTrieMultiMap<>()),
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
      PrefixTrieMultiMap<IpCompletionMetadata> trie = new PrefixTrieMultiMap<>();
      ips.forEach(ip -> trie.put(ip.toPrefix(), new IpCompletionMetadata()));
      setIps(trie);
      return this;
    }

    public @Nonnull Builder setIps(PrefixTrieMultiMap<IpCompletionMetadata> ips) {
      _ips = ips;
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

  private final Set<String> _filterNames;

  private final Set<NodeInterfacePair> _interfaces;

  private final PrefixTrieMultiMap<IpCompletionMetadata> _ips;

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

  public CompletionMetadata(
      Set<String> filterNames,
      Set<NodeInterfacePair> interfaces,
      PrefixTrieMultiMap<IpCompletionMetadata> ips,
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

  public @Nonnull Set<String> getFilterNames() {
    return _filterNames;
  }

  public @Nonnull Set<NodeInterfacePair> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull PrefixTrieMultiMap<IpCompletionMetadata> getIps() {
    return _ips;
  }

  public @Nonnull Set<LocationCompletionMetadata> getLocations() {
    return _locations;
  }

  /** Returns the full set of MLAG domain ids in the snapshot */
  public @Nonnull Set<String> getMlagIds() {
    return _mlagIds;
  }

  public @Nonnull Map<String, NodeCompletionMetadata> getNodes() {
    return _nodes;
  }

  public @Nonnull Set<String> getPrefixes() {
    return _prefixes;
  }

  public @Nonnull Set<String> getRoutingPolicyNames() {
    return _routingPolicyNames;
  }

  public @Nonnull Set<String> getStructureNames() {
    return _structureNames;
  }

  public @Nonnull Set<String> getVrfs() {
    return _vrfs;
  }

  public @Nonnull Set<String> getZones() {
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
        .add("filterNames", _filterNames)
        .add("interfaces", _interfaces)
        .add("ips", _ips)
        .add("locations", _locations)
        .add("mlagIds", _mlagIds)
        .add("nodes", _nodes)
        .add("prefixes", _prefixes)
        .add("routingPolicyNames", _routingPolicyNames)
        .add("structureNames", _structureNames)
        .add("vrfs", _vrfs)
        .add("zones", _zones)
        .toString();
  }
}
