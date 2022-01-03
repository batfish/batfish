package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.MoreObjects;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.referencelibrary.AddressGroup;

/**
 * An {@link IpSpaceAssignmentSpecifier} that looks up an {@link AddressGroup} in a {@link
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
  public IpSpace resolve(SpecifierContext ctxt) {
    return computeIpSpace(_addressGroupName, _bookName, ctxt);
  }

  /**
   * Computes the IpSpace in the address group. Returns {@link EmptyIpSpace} if the addressgroup is
   * empty.
   *
   * @throws NoSuchElementException if {@code bookName} does not exist or if {@code
   *     addressGroupName} or one of its descendants do not exist in the Reference Book.
   */
  public static IpSpace computeIpSpace(
      String addressGroupName, String bookName, SpecifierContext ctxt) {
    return firstNonNull(
        AclIpSpace.union(
            ctxt
                .getReferenceBook(bookName)
                .orElseThrow(
                    () -> new NoSuchElementException("ReferenceBook '" + bookName + "' not found"))
                .getAddressesRecursive(addressGroupName)
                .stream()
                .map(IpWildcard::toIpSpace)
                .collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("addressGroup", _addressGroupName)
        .add("referenceBook", _bookName)
        .toString();
  }
}
