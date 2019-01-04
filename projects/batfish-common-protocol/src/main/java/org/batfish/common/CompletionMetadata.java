package org.batfish.common;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Grouping of various snapshot properties used for autocomplete */
public final class CompletionMetadata {

  private static final String PROP_ADDRESS_BOOKS = "addressBooks";
  private static final String PROP_ADDRESS_GROUPS = "addressGroups";
  private static final String PROP_FILTER_NAMES = "filterNames";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_IPS = "ips";
  private static final String PROP_PREFIXES = "prefixes";
  private static final String PROP_STRUCTURE_NAMES = "structureNames";
  private static final String PROP_VRFS = "vrfs";
  private static final String PROP_ZONES = "zones";

  private final Set<String> _addressBooks;

  private final Set<String> _addressGroups;

  private final Set<String> _filterNames;

  private final Set<NodeInterfacePair> _interfaces;

  private final Set<String> _ips;

  private final Set<String> _prefixes;

  private final Set<String> _structureNames;

  private final Set<String> _vrfs;

  private final Set<String> _zones;

  @JsonCreator
  private static @Nonnull CompletionMetadata create(
      @JsonProperty(PROP_ADDRESS_BOOKS) Set<String> addressBooks,
      @JsonProperty(PROP_ADDRESS_GROUPS) Set<String> addressGroups,
      @JsonProperty(PROP_FILTER_NAMES) Set<String> filterNames,
      @JsonProperty(PROP_INTERFACES) Set<NodeInterfacePair> interfaces,
      @JsonProperty(PROP_IPS) Set<String> ips,
      @JsonProperty(PROP_PREFIXES) Set<String> prefixes,
      @JsonProperty(PROP_STRUCTURE_NAMES) Set<String> structureNames,
      @JsonProperty(PROP_VRFS) Set<String> vrfs,
      @JsonProperty(PROP_ZONES) Set<String> zones) {
    return new CompletionMetadata(
        addressBooks,
        addressGroups,
        filterNames,
        interfaces,
        ips,
        prefixes,
        structureNames,
        vrfs,
        zones);
  }

  public CompletionMetadata(
      @Nullable Set<String> addressBooks,
      @Nullable Set<String> addressGroups,
      @Nullable Set<String> filterNames,
      @Nullable Set<NodeInterfacePair> interfaces,
      @Nullable Set<String> ips,
      @Nullable Set<String> prefixes,
      @Nullable Set<String> structureNames,
      @Nullable Set<String> vrfs,
      @Nullable Set<String> zones) {
    _addressBooks = firstNonNull(addressBooks, ImmutableSet.of());
    _addressGroups = firstNonNull(addressGroups, ImmutableSet.of());
    _filterNames = firstNonNull(filterNames, ImmutableSet.of());
    _interfaces = firstNonNull(interfaces, ImmutableSet.of());
    _ips = firstNonNull(ips, ImmutableSet.of());
    _prefixes = firstNonNull(prefixes, ImmutableSet.of());
    _structureNames = firstNonNull(structureNames, ImmutableSet.of());
    _vrfs = firstNonNull(vrfs, ImmutableSet.of());
    _zones = firstNonNull(zones, ImmutableSet.of());
  }

  public CompletionMetadata() {
    this(null, null, null, null, null, null, null, null, null);
  }

  @JsonProperty(PROP_ADDRESS_BOOKS)
  public Set<String> getAddressBooks() {
    return _addressBooks;
  }

  @JsonProperty(PROP_ADDRESS_GROUPS)
  public Set<String> getAddressGroups() {
    return _addressGroups;
  }

  @JsonProperty(PROP_FILTER_NAMES)
  public Set<String> getFilterNames() {
    return _filterNames;
  }

  @JsonProperty(PROP_INTERFACES)
  public Set<NodeInterfacePair> getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_IPS)
  public Set<String> getIps() {
    return _ips;
  }

  @JsonProperty(PROP_PREFIXES)
  public Set<String> getPrefixes() {
    return _prefixes;
  }

  @JsonProperty(PROP_STRUCTURE_NAMES)
  public Set<String> getStructureNames() {
    return _structureNames;
  }

  @JsonProperty(PROP_VRFS)
  public Set<String> getVrfs() {
    return _vrfs;
  }

  @JsonProperty(PROP_ZONES)
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
    return _addressBooks.equals(rhs._addressBooks)
        && _addressGroups.equals(rhs._addressGroups)
        && _filterNames.equals(rhs._filterNames)
        && _interfaces.equals(rhs._interfaces)
        && _ips.equals(rhs._ips)
        && _prefixes.equals(rhs._prefixes)
        && _structureNames.equals(rhs._structureNames)
        && _vrfs.equals(rhs._vrfs)
        && _zones.equals(rhs._zones);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _addressBooks,
        _addressGroups,
        _filterNames,
        _interfaces,
        _ips,
        _prefixes,
        _structureNames,
        _vrfs,
        _zones);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_ADDRESS_BOOKS, _addressBooks)
        .add(PROP_ADDRESS_GROUPS, _addressGroups)
        .add(PROP_FILTER_NAMES, _filterNames)
        .add(PROP_INTERFACES, _interfaces)
        .add(PROP_IPS, _ips)
        .add(PROP_PREFIXES, _prefixes)
        .add(PROP_STRUCTURE_NAMES, _structureNames)
        .add(PROP_VRFS, _vrfs)
        .add(PROP_ZONES, _zones)
        .toString();
  }
}
