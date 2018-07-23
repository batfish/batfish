package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.role.addressbook.AddressGroup;

public class AddressGroupBean {

  public Set<String> addresses;
  public String name;

  @JsonCreator
  private AddressGroupBean() {}

  public AddressGroupBean(AddressGroup addressGroup) {
    name = addressGroup.getName();
    addresses = ImmutableSet.copyOf(addressGroup.getAddresses());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AddressGroupBean)) {
      return false;
    }
    return Objects.equals(addresses, ((AddressGroupBean) o).addresses)
        && Objects.equals(name, ((AddressGroupBean) o).name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addresses, name);
  }

  public AddressGroup toAddressGroup() {
    return new AddressGroup(ImmutableSortedSet.copyOf(addresses), name);
  }
}
