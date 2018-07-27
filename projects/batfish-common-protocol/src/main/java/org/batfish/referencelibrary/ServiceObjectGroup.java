package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.SortedSet;
import javax.annotation.Nonnull;

public class ServiceObjectGroup implements Comparable<ServiceObjectGroup> {

  private static final String PROP_NAME = "name";
  private static final String PROP_SERVICES = "services";

  @Nonnull private String _name;
  @Nonnull private SortedSet<String> _services;

  public ServiceObjectGroup(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_SERVICES) SortedSet<String> services) {
    checkArgument(name != null, "Service object group name cannot be null");
    ReferenceLibrary.checkValidName(name, "service object group");

    _name = name;
    _services = firstNonNull(services, ImmutableSortedSet.of());
  }

  public void checkUndefinedReferences(List<String> allServiceNames) {
    _services.forEach(
        s ->
            checkArgument(
                allServiceNames.contains(s),
                "Undefined service name '%s' in service object group '%s'",
                s,
                _name));
  }

  @Override
  public int compareTo(ServiceObjectGroup o) {
    return _name.compareTo(o._name);
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_SERVICES)
  public SortedSet<String> getServices() {
    return _services;
  }
}
