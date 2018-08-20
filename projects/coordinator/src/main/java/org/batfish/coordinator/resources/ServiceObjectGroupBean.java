package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.referencelibrary.ServiceObjectGroup;

public class ServiceObjectGroupBean {

  /** The name of this service object group */
  public String name;

  /** The set of names of service objects or service object groups in this service object group */
  public Set<String> services;

  @JsonCreator
  private ServiceObjectGroupBean() {}

  public ServiceObjectGroupBean(ServiceObjectGroup group) {
    name = group.getName();
    services = ImmutableSet.copyOf(group.getServices());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ServiceObjectGroupBean)) {
      return false;
    }
    return Objects.equals(name, ((ServiceObjectGroupBean) o).name)
        && Objects.equals(services, ((ServiceObjectGroupBean) o).services);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, services);
  }

  public ServiceObjectGroup toServiceObjectGroup() {
    return new ServiceObjectGroup(
        name, ImmutableSortedSet.copyOf(firstNonNull(services, ImmutableSet.of())));
  }
}
