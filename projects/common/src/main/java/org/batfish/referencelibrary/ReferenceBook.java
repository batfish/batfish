package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;

/** Represents a reference book which contains multiple types of named constructs */
@ParametersAreNonnullByDefault
public class ReferenceBook implements Comparable<ReferenceBook>, Serializable {

  @ParametersAreNonnullByDefault
  public static class Builder {

    private List<AddressGroup> _addressGroups;
    private List<FilterGroup> _filterGroups;
    private List<InterfaceGroup> _interfaceGroups;
    private String _name;
    private List<ServiceEndpoint> _serviceEndpoints;
    private List<ServiceObjectGroup> _serviceObjectGroups;
    private List<ServiceObject> _serviceObjects;

    private Builder(String name) {
      _name = name;
    }

    public ReferenceBook build() {
      return new ReferenceBook(
          _name,
          firstNonNull(_addressGroups, ImmutableList.of()),
          firstNonNull(_filterGroups, ImmutableList.of()),
          firstNonNull(_interfaceGroups, ImmutableList.of()),
          firstNonNull(_serviceEndpoints, ImmutableList.of()),
          firstNonNull(_serviceObjectGroups, ImmutableList.of()),
          firstNonNull(_serviceObjects, ImmutableList.of()));
    }

    public Builder setAddressGroups(List<AddressGroup> addressGroups) {
      _addressGroups = addressGroups;
      return this;
    }

    public Builder setFilterGroups(List<FilterGroup> filterGroups) {
      _filterGroups = filterGroups;
      return this;
    }

    public Builder setInterfaceGroups(List<InterfaceGroup> interfaceGroups) {
      _interfaceGroups = interfaceGroups;
      return this;
    }

    public Builder setServiceEndpoints(List<ServiceEndpoint> serviceEndpoints) {
      _serviceEndpoints = serviceEndpoints;
      return this;
    }

    public Builder setServiceObjectGroups(List<ServiceObjectGroup> serviceObjectGroups) {
      _serviceObjectGroups = serviceObjectGroups;
      return this;
    }

    public Builder setServiceObjects(List<ServiceObject> serviceObjects) {
      _serviceObjects = serviceObjects;
      return this;
    }
  }

  private static final String PROP_ADDRESS_GROUPS = "addressGroups";
  private static final String PROP_FILTER_GROUPS = "filterGroups";
  private static final String PROP_INTERFACE_GROUPS = "interfaceGroups";
  private static final String PROP_NAME = "name";
  private static final String PROP_SERVICE_ENDPOINTS = "serviceEndpoints";
  private static final String PROP_SERVICE_OBJECT_GROUPS = "serviceObjectGroups";
  private static final String PROP_SERVICE_OBJECTS = "serviceObjects";

  private final @Nonnull SortedSet<AddressGroup> _addressGroups;
  private final @Nonnull SortedSet<FilterGroup> _filterGroups;
  private final @Nonnull SortedSet<InterfaceGroup> _interfaceGroups;
  private final @Nonnull String _name;
  private final @Nonnull SortedSet<ServiceEndpoint> _serviceEndpoints;
  private final @Nonnull SortedSet<ServiceObjectGroup> _serviceObjectGroups;
  private final @Nonnull SortedSet<ServiceObject> _serviceObjects;

  @JsonCreator
  private static ReferenceBook create(
      @JsonProperty(PROP_ADDRESS_GROUPS) @Nullable List<AddressGroup> addressGroups,
      @JsonProperty(PROP_FILTER_GROUPS) @Nullable List<FilterGroup> filterGroups,
      @JsonProperty(PROP_INTERFACE_GROUPS) @Nullable List<InterfaceGroup> interfaceGroups,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_SERVICE_ENDPOINTS) @Nullable List<ServiceEndpoint> serviceEndpoints,
      @JsonProperty(PROP_SERVICE_OBJECT_GROUPS) @Nullable
          List<ServiceObjectGroup> serviceObjectGroups,
      @JsonProperty(PROP_SERVICE_OBJECTS) @Nullable List<ServiceObject> serviceObjects) {
    checkArgument(name != null, "Reference book name cannot be null");

    return new ReferenceBook(
        name,
        firstNonNull(addressGroups, ImmutableList.of()),
        firstNonNull(filterGroups, ImmutableList.of()),
        firstNonNull(interfaceGroups, ImmutableList.of()),
        firstNonNull(serviceEndpoints, ImmutableList.of()),
        firstNonNull(serviceObjectGroups, ImmutableList.of()),
        firstNonNull(serviceObjects, ImmutableList.of()));
  }

  private ReferenceBook(
      String name,
      List<AddressGroup> addressGroups,
      List<FilterGroup> filterGroups,
      List<InterfaceGroup> interfaceGroups,
      List<ServiceEndpoint> serviceEndpoints,
      List<ServiceObjectGroup> serviceObjectGroups,
      List<ServiceObject> serviceObjects) {
    Names.checkName(name, "book", Type.REFERENCE_OBJECT);

    // collect names for sanity checking
    List<String> addressGroupNames =
        addressGroups.stream().map(AddressGroup::getName).collect(Collectors.toList());
    List<String> filterGroupNames =
        filterGroups.stream().map(FilterGroup::getName).collect(Collectors.toList());
    List<String> interfaceGroupNames =
        interfaceGroups.stream().map(InterfaceGroup::getName).collect(Collectors.toList());
    List<String> serviceEndpointNames =
        serviceEndpoints.stream().map(ServiceEndpoint::getName).collect(Collectors.toList());
    List<String> serviceObjectGroupNames =
        serviceObjectGroups.stream().map(ServiceObjectGroup::getName).collect(Collectors.toList());
    List<String> serviceObjectNames =
        serviceObjects.stream().map(ServiceObject::getName).collect(Collectors.toList());
    List<String> allServiceNames =
        Stream.concat(serviceObjectNames.stream(), serviceObjectGroupNames.stream())
            .collect(Collectors.toList());

    // check for duplicate names
    ReferenceLibrary.checkDuplicates("address group", addressGroupNames);
    ReferenceLibrary.checkDuplicates("filter group", filterGroupNames);
    ReferenceLibrary.checkDuplicates("interface group", interfaceGroupNames);
    ReferenceLibrary.checkDuplicates("service endpoint", serviceEndpointNames);
    ReferenceLibrary.checkDuplicates("service object group", serviceObjectGroupNames);
    ReferenceLibrary.checkDuplicates("service objects", serviceObjectNames);
    ReferenceLibrary.checkDuplicates("service object or group", allServiceNames);

    // check that address group children do not have dangling pointers
    Set<String> extraNames =
        Sets.difference(
            addressGroups.stream()
                .flatMap(g -> g.getChildGroupNames().stream())
                .collect(ImmutableSet.toImmutableSet()),
            addressGroups.stream()
                .map(AddressGroup::getName)
                .collect(ImmutableSet.toImmutableSet()));
    checkArgument(
        extraNames.isEmpty(),
        "Following child address group names are not defined: %s",
        extraNames);

    // check that there are no dangling pointers to non-existent names
    serviceEndpoints.forEach(s -> s.checkUndefinedReferences(addressGroupNames, allServiceNames));
    serviceObjectGroups.forEach(s -> s.checkUndefinedReferences(allServiceNames));

    // TODO: figure out what to do about circular pointers in service names

    _addressGroups = ImmutableSortedSet.copyOf(addressGroups);
    _filterGroups = ImmutableSortedSet.copyOf(filterGroups);
    _interfaceGroups = ImmutableSortedSet.copyOf(interfaceGroups);
    _name = name;
    _serviceEndpoints = ImmutableSortedSet.copyOf(serviceEndpoints);
    _serviceObjectGroups = ImmutableSortedSet.copyOf(serviceObjectGroups);
    _serviceObjects = ImmutableSortedSet.copyOf(serviceObjects);
  }

  public static Builder builder(String name) {
    return new Builder(name);
  }

  @Override
  public int compareTo(ReferenceBook o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ReferenceBook)) {
      return false;
    }
    return Objects.equals(_addressGroups, ((ReferenceBook) o)._addressGroups)
        && Objects.equals(_filterGroups, ((ReferenceBook) o)._filterGroups)
        && Objects.equals(_interfaceGroups, ((ReferenceBook) o)._interfaceGroups)
        && Objects.equals(_name, ((ReferenceBook) o)._name)
        && Objects.equals(_serviceEndpoints, ((ReferenceBook) o)._serviceEndpoints)
        && Objects.equals(_serviceObjectGroups, ((ReferenceBook) o)._serviceObjectGroups)
        && Objects.equals(_serviceObjects, ((ReferenceBook) o)._serviceObjects);
  }

  /** Return the {@link AddressGroup} with name {@code groupName} */
  public Optional<AddressGroup> getAddressGroup(String groupName) {
    return _addressGroups.stream().filter(group -> group.getName().equals(groupName)).findAny();
  }

  @JsonProperty(PROP_ADDRESS_GROUPS)
  public @Nonnull SortedSet<AddressGroup> getAddressGroups() {
    return _addressGroups;
  }

  /** Return the {@link FilterGroup} with name {@code groupName} */
  public Optional<FilterGroup> getFilterGroup(String groupName) {
    return _filterGroups.stream().filter(group -> group.getName().equals(groupName)).findAny();
  }

  @JsonProperty(PROP_FILTER_GROUPS)
  public @Nonnull SortedSet<FilterGroup> getFilterGroups() {
    return _filterGroups;
  }

  /** Return the {@link InterfaceGroup} with name {@code groupName} */
  public Optional<InterfaceGroup> getInterfaceGroup(String groupName) {
    return _interfaceGroups.stream().filter(group -> group.getName().equals(groupName)).findAny();
  }

  @JsonProperty(PROP_INTERFACE_GROUPS)
  public @Nonnull SortedSet<InterfaceGroup> getInterfaceGroups() {
    return _interfaceGroups;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_SERVICE_ENDPOINTS)
  public @Nonnull SortedSet<ServiceEndpoint> getServiceEndpoints() {
    return _serviceEndpoints;
  }

  @JsonProperty(PROP_SERVICE_OBJECT_GROUPS)
  public @Nonnull SortedSet<ServiceObjectGroup> getServiceObjectGroups() {
    return _serviceObjectGroups;
  }

  @JsonProperty(PROP_SERVICE_OBJECTS)
  public @Nonnull SortedSet<ServiceObject> getServiceObjects() {
    return _serviceObjects;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _addressGroups,
        _filterGroups,
        _interfaceGroups,
        _name,
        _serviceEndpoints,
        _serviceObjectGroups,
        _serviceObjects);
  }

  /**
   * Get the set of addresses contained in {@code addressGroupName} after recursively resolving the
   * sub groups.
   *
   * <p>The implementation is robust to cycles among groups.
   */
  public Set<IpWildcard> getAddressesRecursive(String addressGroupName) {
    Set<String> allGroupNames = new HashSet<>();
    addGroupAndDescendantNames(addressGroupName, allGroupNames);
    return allGroupNames.stream()
        // get() is safe because we got names from the book itself
        .flatMap(groupName -> getAddressGroup(groupName).get().getAddresses().stream())
        .map(IpWildcard::parse)
        .collect(ImmutableSet.toImmutableSet());
  }

  /** Collects all group names in {@code allGroupNames} */
  @VisibleForTesting
  void addGroupAndDescendantNames(String groupName, Set<String> allGroupNames) {
    allGroupNames.add(groupName);
    // get() is safe because the constructor checks for dangling names
    for (String childName : getAddressGroup(groupName).get().getChildGroupNames()) {
      if (!allGroupNames.contains(childName)) {
        addGroupAndDescendantNames(childName, allGroupNames);
      }
    }
  }
}
