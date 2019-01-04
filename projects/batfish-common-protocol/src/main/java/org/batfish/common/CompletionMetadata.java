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
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Grouping of various snapshot properties used for autocomplete */
@ParametersAreNonnullByDefault
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
      @Nullable @JsonProperty(PROP_ADDRESS_BOOKS) Set<String> addressBooks,
      @Nullable @JsonProperty(PROP_ADDRESS_GROUPS) Set<String> addressGroups,
      @Nullable @JsonProperty(PROP_FILTER_NAMES) Set<String> filterNames,
      @Nullable @JsonProperty(PROP_INTERFACES) Set<NodeInterfacePair> interfaces,
      @Nullable @JsonProperty(PROP_IPS) Set<String> ips,
      @Nullable @JsonProperty(PROP_PREFIXES) Set<String> prefixes,
      @Nullable @JsonProperty(PROP_STRUCTURE_NAMES) Set<String> structureNames,
      @Nullable @JsonProperty(PROP_VRFS) Set<String> vrfs,
      @Nullable @JsonProperty(PROP_ZONES) Set<String> zones) {
    return new CompletionMetadata(
        firstNonNull(addressBooks, ImmutableSet.of()),
        firstNonNull(addressGroups, ImmutableSet.of()),
        firstNonNull(filterNames, ImmutableSet.of()),
        firstNonNull(interfaces, ImmutableSet.of()),
        firstNonNull(ips, ImmutableSet.of()),
        firstNonNull(prefixes, ImmutableSet.of()),
        firstNonNull(structureNames, ImmutableSet.of()),
        firstNonNull(vrfs, ImmutableSet.of()),
        firstNonNull(zones, ImmutableSet.of()));
  }

  public CompletionMetadata(
      Set<String> addressBooks,
      Set<String> addressGroups,
      Set<String> filterNames,
      Set<NodeInterfacePair> interfaces,
      Set<String> ips,
      Set<String> prefixes,
      Set<String> structureNames,
      Set<String> vrfs,
      Set<String> zones) {
    _addressBooks = addressBooks;
    _addressGroups = addressGroups;
    _filterNames = filterNames;
    _interfaces = interfaces;
    _ips = ips;
    _prefixes = prefixes;
    _structureNames = structureNames;
    _vrfs = vrfs;
    _zones = zones;
  }

  public CompletionMetadata() {
    this(
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of());
  }

  @JsonProperty(PROP_ADDRESS_BOOKS)
  @Nonnull
  public Set<String> getAddressBooks() {
    return _addressBooks;
  }

  @JsonProperty(PROP_ADDRESS_GROUPS)
  @Nonnull
  public Set<String> getAddressGroups() {
    return _addressGroups;
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

  @JsonProperty(PROP_PREFIXES)
  @Nonnull
  public Set<String> getPrefixes() {
    return _prefixes;
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
