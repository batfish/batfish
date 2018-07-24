package org.batfish.specifier;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.role.addressbook.AddressGroup;

/** An {@link IpSpaceSpecifier} that is looks up the IpSpace from the address book. */
public final class AddressBookIpSpaceSpecifier implements IpSpaceSpecifier {
  private final String _addressGroupName;
  private final String _bookName;

  public AddressBookIpSpaceSpecifier(String addressGroupname, String bookName) {
    _addressGroupName = addressGroupname;
    _bookName = bookName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AddressBookIpSpaceSpecifier)) {
      return false;
    }
    return Objects.equals(_addressGroupName, ((AddressBookIpSpaceSpecifier) o)._addressGroupName)
        && Objects.equals(_bookName, ((AddressBookIpSpaceSpecifier) o)._addressGroupName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_addressGroupName, _bookName);
  }

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    AddressGroup addressGroup =
        ctxt.getAddressBook(_bookName)
            .orElseThrow(
                () -> new NoSuchElementException("AddressBook '" + _bookName + "' not found"))
            .getAddressGroup(_addressGroupName)
            .orElseThrow(
                () -> new NoSuchElementException("AddressGroup '" + _addressGroupName + "' found"));

    return IpSpaceAssignment.builder()
        .assign(
            locations,
            AclIpSpace.union(
                addressGroup
                    .getAddresses()
                    .stream()
                    .map(add -> new IpWildcard(add).toIpSpace())
                    .collect(Collectors.toList())))
        .build();
  }
}
