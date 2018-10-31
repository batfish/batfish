package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class ReferenceBook implements Comparable<ReferenceBook> {

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
          _addressGroups,
          _filterGroups,
          _interfaceGroups,
          _name,
          _serviceEndpoints,
          _serviceObjectGroups,
          _serviceObjects);
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

  @Nonnull private final SortedSet<AddressGroup> _addressGroups;
  @Nonnull private final SortedSet<FilterGroup> _filterGroups;
  @Nonnull private final SortedSet<InterfaceGroup> _interfaceGroups;
  @Nonnull private final String _name;
  @Nonnull private final SortedSet<ServiceEndpoint> _serviceEndpoints;
  @Nonnull private final SortedSet<ServiceObjectGroup> _serviceObjectGroups;
  @Nonnull private final SortedSet<ServiceObject> _serviceObjects;

  @JsonCreator
  private ReferenceBook(
      @JsonProperty(PROP_ADDRESS_GROUPS) List<AddressGroup> addressGroups,
      @JsonProperty(PROP_FILTER_GROUPS) List<FilterGroup> filterGroups,
      @JsonProperty(PROP_INTERFACE_GROUPS) List<InterfaceGroup> interfaceGroups,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_SERVICE_ENDPOINTS) List<ServiceEndpoint> serviceEndpoints,
      @JsonProperty(PROP_SERVICE_OBJECT_GROUPS) List<ServiceObjectGroup> serviceObjectGroups,
      @JsonProperty(PROP_SERVICE_OBJECTS) List<ServiceObject> serviceObjects) {
    checkArgument(name != null, "Reference book name cannot be null");
    ReferenceLibrary.checkValidName(name, "book");

    // non-null versions for easier follow on code
    List<AddressGroup> nnAddressGroups = firstNonNull(addressGroups, ImmutableList.of());
    List<FilterGroup> nnFilterGroups = firstNonNull(filterGroups, ImmutableList.of());
    List<InterfaceGroup> nnInterfaceGroups = firstNonNull(interfaceGroups, ImmutableList.of());
    List<ServiceEndpoint> nnServiceEndpoints = firstNonNull(serviceEndpoints, ImmutableList.of());
    List<ServiceObjectGroup> nnServiceObjectGroups =
        firstNonNull(serviceObjectGroups, ImmutableList.of());
    List<ServiceObject> nnServiceObjects = firstNonNull(serviceObjects, ImmutableList.of());

    // collect names for sanity checking
    List<String> addressGroupNames =
        nnAddressGroups.stream().map(AddressGroup::getName).collect(Collectors.toList());
    List<String> filterGroupNames =
        nnFilterGroups.stream().map(FilterGroup::getName).collect(Collectors.toList());
    List<String> interfaceGroupNames =
        nnInterfaceGroups.stream().map(InterfaceGroup::getName).collect(Collectors.toList());
    List<String> serviceEndpointNames =
        nnServiceEndpoints.stream().map(ServiceEndpoint::getName).collect(Collectors.toList());
    List<String> serviceObjectGroupNames =
        nnServiceObjectGroups
            .stream()
            .map(ServiceObjectGroup::getName)
            .collect(Collectors.toList());
    List<String> serviceObjectNames =
        nnServiceObjects.stream().map(ServiceObject::getName).collect(Collectors.toList());
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

    // check that there are no dangling pointers to non-existent names
    nnServiceEndpoints.forEach(s -> s.checkUndefinedReferences(addressGroupNames, allServiceNames));
    nnServiceObjectGroups.forEach(s -> s.checkUndefinedReferences(allServiceNames));

    // TODO: figure out what to do about circular pointers in service names

    _addressGroups = ImmutableSortedSet.copyOf(nnAddressGroups);
    _filterGroups = ImmutableSortedSet.copyOf(nnFilterGroups);
    _interfaceGroups = ImmutableSortedSet.copyOf(nnInterfaceGroups);
    _name = name;
    _serviceEndpoints = ImmutableSortedSet.copyOf(nnServiceEndpoints);
    _serviceObjectGroups = ImmutableSortedSet.copyOf(nnServiceObjectGroups);
    _serviceObjects = ImmutableSortedSet.copyOf(nnServiceObjects);
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
  public SortedSet<AddressGroup> getAddressGroups() {
    return _addressGroups;
  }

  /** Return the {@link FilterGroup} with name {@code groupName} */
  public Optional<FilterGroup> getFilterGroup(String groupName) {
    return _filterGroups.stream().filter(group -> group.getName().equals(groupName)).findAny();
  }

  @JsonProperty(PROP_FILTER_GROUPS)
  public SortedSet<FilterGroup> getFilterGroups() {
    return _filterGroups;
  }

  /** Return the {@link InterfaceGroup} with name {@code groupName} */
  public Optional<InterfaceGroup> getInterfaceGroup(String groupName) {
    return _interfaceGroups.stream().filter(group -> group.getName().equals(groupName)).findAny();
  }

  @JsonProperty(PROP_INTERFACE_GROUPS)
  public SortedSet<InterfaceGroup> getInterfaceGroups() {
    return _interfaceGroups;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_SERVICE_ENDPOINTS)
  public SortedSet<ServiceEndpoint> getServiceEndpoints() {
    return _serviceEndpoints;
  }

  @JsonProperty(PROP_SERVICE_OBJECT_GROUPS)
  public SortedSet<ServiceObjectGroup> getServiceObjectGroups() {
    return _serviceObjectGroups;
  }

  @JsonProperty(PROP_SERVICE_OBJECTS)
  public SortedSet<ServiceObject> getServiceObjects() {
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
}
