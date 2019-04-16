package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.referencelibrary.AddressGroup;

/** A bean for {@link AddressGroup} */
public class AddressGroupBean {

  /** Names of sub groups in this group */
  public Set<String> addressGroups;

  /**
   * The set of address in this address group. An address can be an IP address, prefix, or
   * address:mask
   */
  public Set<String> addresses;

  /** The name of this address group */
  public String name;

  @JsonCreator
  private AddressGroupBean() {}

  public AddressGroupBean(AddressGroup addressGroup) {
    name = addressGroup.getName();
    addresses = ImmutableSet.copyOf(addressGroup.getAddresses());
    addressGroups = ImmutableSet.copyOf(addressGroup.getAddressGroups());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AddressGroupBean)) {
      return false;
    }
    return Objects.equals(addressGroups, ((AddressGroupBean) o).addressGroups)
        && Objects.equals(addresses, ((AddressGroupBean) o).addresses)
        && Objects.equals(name, ((AddressGroupBean) o).name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addressGroups, addresses, name);
  }

  public AddressGroup toAddressGroup() {
    return new AddressGroup(
        name,
        ImmutableSortedSet.copyOf(firstNonNull(addresses, ImmutableSet.of())),
        ImmutableSortedSet.copyOf(firstNonNull(addressGroups, ImmutableSet.of())));
  }
}
