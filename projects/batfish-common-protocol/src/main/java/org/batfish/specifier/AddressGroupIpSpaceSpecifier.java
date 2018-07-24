package org.batfish.specifier;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.referencelibrary.AddressGroup;

/** An {@link IpSpaceSpecifier} that is looks up the IpSpace from the reference book. */
public final class AddressGroupIpSpaceSpecifier implements IpSpaceSpecifier {
  private final String _addressGroupName;
  private final String _bookName;

  public AddressGroupIpSpaceSpecifier(String addressGroupname, String bookName) {
    _addressGroupName = addressGroupname;
    _bookName = bookName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AddressGroupIpSpaceSpecifier)) {
      return false;
    }
    return Objects.equals(_addressGroupName, ((AddressGroupIpSpaceSpecifier) o)._addressGroupName)
        && Objects.equals(_bookName, ((AddressGroupIpSpaceSpecifier) o)._addressGroupName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_addressGroupName, _bookName);
  }

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    AddressGroup addressGroup =
        ctxt.getReferenceBook(_bookName)
            .orElseThrow(
                () -> new NoSuchElementException("ReferenceBook '" + _bookName + "' not found"))
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
