package org.batfish.role.addressbook;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.role.addressbook.AddressLibrary.checkDuplicates;
import static org.batfish.role.addressbook.AddressLibrary.checkValidName;

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

public class AddressBook implements Comparable<AddressBook> {

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

  public AddressBook(
      @JsonProperty(PROP_ADDRESS_GROUPS) List<AddressGroup> addressGroups,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_SERVICE_ENDPOINTS) List<ServiceEndpoint> serviceEndpoints,
      @JsonProperty(PROP_SERVICE_OBJECT_GROUPS) List<ServiceObjectGroup> serviceObjectGroups,
      @JsonProperty(PROP_SERVICE_OBJECTS) List<ServiceObject> serviceObjects) {
    checkArgument(name != null, "Address book name cannot be null");
    checkValidName(name, "book");

    // non-null versions for easier follow on code
    List<AddressGroup> nnAddressGroups = firstNonNull(addressGroups, ImmutableList.of());
    List<ServiceEndpoint> nnServiceEndpoints = firstNonNull(serviceEndpoints, ImmutableList.of());
    List<ServiceObjectGroup> nnServiceObjectGroups =
        firstNonNull(serviceObjectGroups, ImmutableList.of());
    List<ServiceObject> nnServiceObjects = firstNonNull(serviceObjects, ImmutableList.of());

    // collect names for sanity checking
    List<String> addressGroupNames =
        nnAddressGroups.stream().map(AddressGroup::getName).collect(Collectors.toList());
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
    checkDuplicates("address group", addressGroupNames);
    checkDuplicates("service endpoint", serviceEndpointNames);
    checkDuplicates("service object group", serviceObjectGroupNames);
    checkDuplicates("service objects", serviceObjectNames);
    checkDuplicates("service object or group", allServiceNames);

    // check that there are no dangling pointers to non-existent names
    nnServiceEndpoints.forEach(s -> s.checkUndefinedReferences(addressGroupNames, allServiceNames));
    nnServiceObjectGroups.forEach(s -> s.checkUndefinedReferences(allServiceNames));

    // TODO: figure out what to do about circular pointers in service names

    _addressGroups = ImmutableSortedSet.copyOf(nnAddressGroups);
    _name = name;
    _serviceEndpoints = ImmutableSortedSet.copyOf(nnServiceEndpoints);
    _serviceObjectGroups = ImmutableSortedSet.copyOf(nnServiceObjectGroups);
    _serviceObjects = ImmutableSortedSet.copyOf(nnServiceObjects);
  }

  @Override
  public int compareTo(AddressBook o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AddressBook)) {
      return false;
    }
    return Objects.equals(_addressGroups, ((AddressBook) o)._addressGroups)
        && Objects.equals(_serviceEndpoints, ((AddressBook) o)._serviceEndpoints)
        && Objects.equals(_serviceObjectGroups, ((AddressBook) o)._serviceObjectGroups)
        && Objects.equals(_serviceObjects, ((AddressBook) o)._serviceObjects);
  }

  /** Return the {@link AddressGroup} with name {@code groupName} */
  public Optional<AddressGroup> getAddressGroup(String groupName) {
    return _addressGroups.stream().filter(group -> group.getName().equals(groupName)).findAny();
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
