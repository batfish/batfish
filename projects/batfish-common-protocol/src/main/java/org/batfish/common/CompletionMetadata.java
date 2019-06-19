package org.batfish.common;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Grouping of various snapshot properties used for autocomplete */
@ParametersAreNonnullByDefault
public final class CompletionMetadata implements Serializable {

  public static final class Builder {

    private Set<String> _filterNames;

    private Set<NodeInterfacePair> _interfaces;

    private Set<String> _ips;

    private Set<String> _nodes;

    private Set<String> _prefixes;

    private Set<String> _routingPolicyNames;

    private Set<String> _structureNames;

    private Set<String> _vrfs;

    private Set<String> _zones;

    private Builder() {}

    public @Nonnull CompletionMetadata build() {
      return CompletionMetadata.create(
          null,
          null,
          _filterNames,
          _interfaces,
          _ips,
          _nodes,
          _prefixes,
          _routingPolicyNames,
          _structureNames,
          _vrfs,
          _zones);
    }

    public @Nonnull Builder setFilterNames(Set<String> filterNames) {
      _filterNames = ImmutableSet.copyOf(filterNames);
      return this;
    }

    public @Nonnull Builder setInterfaces(Set<NodeInterfacePair> interfaces) {
      _interfaces = ImmutableSet.copyOf(interfaces);
      return this;
    }

    public @Nonnull Builder setIps(Set<String> ips) {
      _ips = ImmutableSet.copyOf(ips);
      return this;
    }

    public @Nonnull Builder setNodes(Set<String> nodes) {
      _nodes = ImmutableSet.copyOf(nodes);
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
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PREFIXES = "prefixes";
  private static final String PROP_ROUTING_POLICY_NAMES = "routingPolicyNames";
  private static final String PROP_STRUCTURE_NAMES = "structureNames";
  private static final String PROP_VRFS = "vrfs";
  private static final String PROP_ZONES = "zones";

  private static final long serialVersionUID = 1L;

  private final Set<String> _filterNames;

  private final Set<NodeInterfacePair> _interfaces;

  private final Set<String> _ips;

  private final Set<String> _nodes;

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
  private static @Nonnull CompletionMetadata create(
      @Nullable @JsonProperty(PROP_ADDRESS_BOOKS) Set<String> addressBooks,
      @Nullable @JsonProperty(PROP_ADDRESS_GROUPS) Set<String> addressGroups,
      @Nullable @JsonProperty(PROP_FILTER_NAMES) Set<String> filterNames,
      @Nullable @JsonProperty(PROP_INTERFACES) Set<NodeInterfacePair> interfaces,
      @Nullable @JsonProperty(PROP_IPS) Set<String> ips,
      @Nullable @JsonProperty(PROP_NODES) Set<String> nodes,
      @Nullable @JsonProperty(PROP_PREFIXES) Set<String> prefixes,
      @Nullable @JsonProperty(PROP_ROUTING_POLICY_NAMES) Set<String> routingPolicyNames,
      @Nullable @JsonProperty(PROP_STRUCTURE_NAMES) Set<String> structureNames,
      @Nullable @JsonProperty(PROP_VRFS) Set<String> vrfs,
      @Nullable @JsonProperty(PROP_ZONES) Set<String> zones) {
    return new CompletionMetadata(
        firstNonNull(filterNames, ImmutableSet.of()),
        firstNonNull(interfaces, ImmutableSet.of()),
        firstNonNull(ips, ImmutableSet.of()),
        firstNonNull(nodes, ImmutableSet.of()),
        firstNonNull(prefixes, ImmutableSet.of()),
        firstNonNull(routingPolicyNames, ImmutableSet.of()),
        firstNonNull(structureNames, ImmutableSet.of()),
        firstNonNull(vrfs, ImmutableSet.of()),
        firstNonNull(zones, ImmutableSet.of()));
  }

  public CompletionMetadata(
      Set<String> filterNames,
      Set<NodeInterfacePair> interfaces,
      Set<String> ips,
      Set<String> nodes,
      Set<String> prefixes,
      Set<String> routingPolicyNames,
      Set<String> structureNames,
      Set<String> vrfs,
      Set<String> zones) {
    _filterNames = filterNames;
    _interfaces = interfaces;
    _ips = ips;
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
  public Set<String> getIps() {
    return _ips;
  }

  @JsonProperty(PROP_NODES)
  @Nonnull
  public Set<String> getNodes() {
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
        .add(PROP_NODES, _nodes)
        .add(PROP_PREFIXES, _prefixes)
        .add(PROP_ROUTING_POLICY_NAMES, _routingPolicyNames)
        .add(PROP_STRUCTURE_NAMES, _structureNames)
        .add(PROP_VRFS, _vrfs)
        .add(PROP_ZONES, _zones)
        .toString();
  }
}
