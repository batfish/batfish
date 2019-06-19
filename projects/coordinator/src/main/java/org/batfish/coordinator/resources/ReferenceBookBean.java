package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.referencelibrary.ReferenceBook;

@ParametersAreNonnullByDefault
public class ReferenceBookBean {
  /** The name of the reference book */
  public String name;

  /** The set of {@link AddressGroupBean}s in this book */
  public Set<AddressGroupBean> addressGroups;

  /** The set of {@link FilterGroupBean}s in this book */
  public Set<FilterGroupBean> filterGroups;

  /** The set of {@link InterfaceGroupBean}s in this book */
  public Set<InterfaceGroupBean> interfaceGroups;

  /** The set of {@link ServiceEndpointBean}s in this book */
  public Set<ServiceEndpointBean> serviceEndpoints;

  /** The set of {@link ServiceObjectGroupBean}s in this book */
  public Set<ServiceObjectGroupBean> serviceObjectGroups;

  /** The set of {@link ServiceObjectBean}s in this book */
  public Set<ServiceObjectBean> serviceObjects;

  @JsonCreator
  private ReferenceBookBean() {}

  public ReferenceBookBean(ReferenceBook book) {
    name = book.getName();
    addressGroups =
        book.getAddressGroups().stream()
            .map(ag -> new AddressGroupBean(ag))
            .collect(Collectors.toSet());
    filterGroups =
        book.getFilterGroups().stream()
            .map(fg -> new FilterGroupBean(fg))
            .collect(Collectors.toSet());
    interfaceGroups =
        book.getInterfaceGroups().stream()
            .map(ig -> new InterfaceGroupBean(ig))
            .collect(Collectors.toSet());
    serviceEndpoints =
        book.getServiceEndpoints().stream()
            .map(se -> new ServiceEndpointBean(se))
            .collect(Collectors.toSet());
    serviceObjectGroups =
        book.getServiceObjectGroups().stream()
            .map(sog -> new ServiceObjectGroupBean(sog))
            .collect(Collectors.toSet());
    serviceObjects =
        book.getServiceObjects().stream()
            .map(so -> new ServiceObjectBean(so))
            .collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ReferenceBookBean)) {
      return false;
    }
    return Objects.equals(addressGroups, ((ReferenceBookBean) o).addressGroups)
        && Objects.equals(filterGroups, ((ReferenceBookBean) o).filterGroups)
        && Objects.equals(interfaceGroups, ((ReferenceBookBean) o).interfaceGroups)
        && Objects.equals(name, ((ReferenceBookBean) o).name)
        && Objects.equals(serviceEndpoints, ((ReferenceBookBean) o).serviceEndpoints)
        && Objects.equals(serviceObjectGroups, ((ReferenceBookBean) o).serviceObjectGroups)
        && Objects.equals(serviceObjects, ((ReferenceBookBean) o).serviceObjects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        addressGroups,
        filterGroups,
        interfaceGroups,
        name,
        serviceEndpoints,
        serviceObjectGroups,
        serviceObjects);
  }

  /** Creates {@link ReferenceBook} from this bean */
  public ReferenceBook toAddressBook() {
    return ReferenceBook.builder(name)
        .setAddressGroups(
            firstNonNull(addressGroups, Collections.<AddressGroupBean>emptySet()).stream()
                .map(agb -> agb.toAddressGroup())
                .collect(Collectors.toList()))
        .setFilterGroups(
            firstNonNull(filterGroups, Collections.<FilterGroupBean>emptySet()).stream()
                .map(fgb -> fgb.toFilterGroup())
                .collect(Collectors.toList()))
        .setInterfaceGroups(
            firstNonNull(interfaceGroups, Collections.<InterfaceGroupBean>emptySet()).stream()
                .map(igb -> igb.toInterfaceGroup())
                .collect(Collectors.toList()))
        .setServiceEndpoints(
            firstNonNull(serviceEndpoints, Collections.<ServiceEndpointBean>emptySet()).stream()
                .map(seb -> seb.toServiceEndpoint())
                .collect(Collectors.toList()))
        .setServiceObjectGroups(
            firstNonNull(serviceObjectGroups, Collections.<ServiceObjectGroupBean>emptySet())
                .stream()
                .map(sogb -> sogb.toServiceObjectGroup())
                .collect(Collectors.toList()))
        .setServiceObjects(
            firstNonNull(serviceObjects, Collections.<ServiceObjectBean>emptySet()).stream()
                .map(seb -> seb.toServiceObject())
                .collect(Collectors.toList()))
        .build();
  }
}
