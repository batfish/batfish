package org.batfish.role.addressbook;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.role.addressbook.Library.checkDuplicates;
import static org.batfish.role.addressbook.Library.checkValidName;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class Book implements Comparable<Book> {

  private static final String PROP_ADDRESS_GROUPS = "addressGroups";
  private static final String PROP_NAME = "name";
  private static final String PROP_SERVICE_ENDPOINTS = "serviceEndpoints";
  private static final String PROP_SERVICE_OBJECT_GROUPS = "serviceObjectGroups";
  private static final String PROP_SERVICE_OBJECTS = "serviceObjects";

  @Nonnull private SortedSet<AddressGroup> _addressGroups;
  @Nonnull private String _name;
  @Nonnull private SortedSet<ServiceEndpoint> _serviceEndpoints;
  @Nonnull private SortedSet<ServiceObjectGroup> _serviceObjectGroups;
  @Nonnull private SortedSet<ServiceObject> _serviceObjects;

  public Book(
      @JsonProperty(PROP_ADDRESS_GROUPS) List<AddressGroup> addressGroups,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_SERVICE_ENDPOINTS) List<ServiceEndpoint> serviceEndpoints,
      @JsonProperty(PROP_SERVICE_OBJECT_GROUPS) List<ServiceObjectGroup> serviceObjectGroups,
      @JsonProperty(PROP_SERVICE_OBJECTS) List<ServiceObject> serviceObjects) {
    checkArgument(name != null, "Address book name cannot be null");
    checkValidName(name, "book");

    // make things non-null for easier follow on code
    addressGroups = firstNonNull(addressGroups, ImmutableList.of());
    serviceEndpoints = firstNonNull(serviceEndpoints, ImmutableList.of());
    serviceObjectGroups = firstNonNull(serviceObjectGroups, ImmutableList.of());
    serviceObjects = firstNonNull(serviceObjects, ImmutableList.of());

    // collect names for sanity checking
    List<String> addressGroupNames =
        addressGroups.stream().map(AddressGroup::getName).collect(Collectors.toList());
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
    checkDuplicates("address group", addressGroupNames);
    checkDuplicates("service endpoint", serviceEndpointNames);
    checkDuplicates("service object group", serviceObjectGroupNames);
    checkDuplicates("service objects", serviceObjectNames);
    checkDuplicates("service object or group", allServiceNames);

    // check that there are no dangling pointers to non-existent names
    serviceEndpoints.forEach(s -> s.checkUndefinedReferences(addressGroupNames, allServiceNames));
    serviceObjectGroups.forEach(s -> s.checkUndefinedReferences(allServiceNames));

    // TODO: figure out what to do about circular pointers in service names

    _addressGroups = ImmutableSortedSet.copyOf(addressGroups);
    _name = name;
    _serviceEndpoints = ImmutableSortedSet.copyOf(serviceEndpoints);
    _serviceObjectGroups = ImmutableSortedSet.copyOf(serviceObjectGroups);
    _serviceObjects = ImmutableSortedSet.copyOf(serviceObjects);
  }

  @Override
  public int compareTo(Book o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Book)) {
      return false;
    }
    return Objects.equals(_addressGroups, ((Book) o)._addressGroups)
        && Objects.equals(_serviceEndpoints, ((Book) o)._serviceEndpoints)
        && Objects.equals(_serviceObjectGroups, ((Book) o)._serviceObjectGroups)
        && Objects.equals(_serviceObjects, ((Book) o)._serviceObjects);
  }

  @JsonProperty(PROP_ADDRESS_GROUPS)
  public SortedSet<AddressGroup> getAddressGroups() {
    return _addressGroups;
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
    return Objects.hash(_addressGroups, _serviceEndpoints, _serviceObjectGroups, _serviceObjects);
  }
}
