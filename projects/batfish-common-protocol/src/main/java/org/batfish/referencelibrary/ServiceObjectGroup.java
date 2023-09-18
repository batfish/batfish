package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;

public class ServiceObjectGroup implements Comparable<ServiceObjectGroup>, Serializable {

  private static final String PROP_NAME = "name";
  private static final String PROP_SERVICES = "services";

  private @Nonnull String _name;
  private @Nonnull SortedSet<String> _services;

  @JsonCreator
  public ServiceObjectGroup(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_SERVICES) SortedSet<String> services) {
    checkArgument(name != null, "Service object group name cannot be null");
    Names.checkName(name, "service object group", Type.REFERENCE_OBJECT);

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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ServiceObjectGroup)) {
      return false;
    }
    return Objects.equals(_name, ((ServiceObjectGroup) o)._name)
        && Objects.equals(_services, ((ServiceObjectGroup) o)._services);
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_SERVICES)
  public SortedSet<String> getServices() {
    return _services;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _services);
  }
}
