package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.referencelibrary.AddressGroup;

/**
 * An {@link IpSpaceSpecifier} that looks up an {@link AddressGroup} in a {@link
 * org.batfish.referencelibrary.ReferenceBook}.
 */
public final class ReferenceAddressGroupIpSpaceSpecifier implements IpSpaceSpecifier {
  private final String _addressGroupName;
  private final String _bookName;

  public ReferenceAddressGroupIpSpaceSpecifier(String addressGroupName, String bookName) {
    _addressGroupName = addressGroupName;
    _bookName = bookName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReferenceAddressGroupIpSpaceSpecifier)) {
      return false;
    }
    ReferenceAddressGroupIpSpaceSpecifier other = (ReferenceAddressGroupIpSpaceSpecifier) o;
    return Objects.equals(_addressGroupName, other._addressGroupName)
        && Objects.equals(_bookName, other._bookName);
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
                () ->
                    new NoSuchElementException(
                        "AddressGroup '"
                            + _addressGroupName
                            + "' not found in ReferenceBook '"
                            + _bookName
                            + "'"));

    return IpSpaceAssignment.builder()
        .assign(
            locations,
            firstNonNull(
                AclIpSpace.union(
                    addressGroup
                        .getAddresses()
                        .stream()
                        .map(add -> new IpWildcard(add).toIpSpace())
                        .collect(Collectors.toList())),
                EmptyIpSpace.INSTANCE))
        .build();
  }
}
