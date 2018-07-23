package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.role.addressbook.AddressBook;

@ParametersAreNonnullByDefault
public class AddressBookBean {
  public String name;
  public Set<AddressGroupBean> addressGroups;
  public Set<ServiceEndpointBean> serviceEndpoints;
  public Set<ServiceObjectGroupBean> serviceObjectGroups;
  public Set<ServiceObjectBean> serviceObjects;

  @JsonCreator
  private AddressBookBean() {}

  public AddressBookBean(AddressBook book) {
    name = book.getName();
    addressGroups =
        book.getAddressGroups()
            .stream()
            .map(ag -> new AddressGroupBean(ag))
            .collect(Collectors.toSet());
    serviceEndpoints =
        book.getServiceEndpoints()
            .stream()
            .map(se -> new ServiceEndpointBean(se))
            .collect(Collectors.toSet());
    serviceObjectGroups =
        book.getServiceObjectGroups()
            .stream()
            .map(sog -> new ServiceObjectGroupBean(sog))
            .collect(Collectors.toSet());
    serviceObjects =
        book.getServiceObjects()
            .stream()
            .map(so -> new ServiceObjectBean(so))
            .collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AddressBookBean)) {
      return false;
    }
    return Objects.equals(addressGroups, ((AddressBookBean) o).addressGroups)
        && Objects.equals(name, ((AddressBookBean) o).name)
        && Objects.equals(serviceEndpoints, ((AddressBookBean) o).serviceEndpoints)
        && Objects.equals(serviceObjectGroups, ((AddressBookBean) o).serviceObjectGroups)
        && Objects.equals(serviceObjects, ((AddressBookBean) o).serviceObjects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressGroups, name, serviceEndpoints, serviceObjectGroups, serviceObjects);
  }

  /** Creates {@link AddressBook} from this bean */
  public AddressBook toAddressBook() {
    return new AddressBook(
        addressGroups.stream().map(agb -> agb.toAddressGroup()).collect(Collectors.toList()),
        name,
        serviceEndpoints.stream().map(seb -> seb.toServiceEndpoint()).collect(Collectors.toList()),
        serviceObjectGroups
            .stream()
            .map(sogb -> sogb.toServiceObjectGroup())
            .collect(Collectors.toList()),
        serviceObjects.stream().map(seb -> seb.toServiceObject()).collect(Collectors.toList()));
  }
}
