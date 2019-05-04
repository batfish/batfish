package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class AddressGroupIpSpaceAstNode implements IpSpaceAstNode {
  private final String _addressGroup;
  private final String _referenceBook;

  AddressGroupIpSpaceAstNode(AstNode referenceBook, AstNode addressGroup) {
    checkArgument(referenceBook instanceof StringAstNode, "referenceBook must be a string");
    checkArgument(addressGroup instanceof StringAstNode, "addressGroup must be a string");
    _addressGroup = ((StringAstNode) addressGroup).getStr();
    _referenceBook = ((StringAstNode) referenceBook).getStr();
  }

  AddressGroupIpSpaceAstNode(String referenceBook, String addressGroup) {
    _addressGroup = addressGroup;
    _referenceBook = referenceBook;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitAddressGroupIpSpaceAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitAddressGroupAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AddressGroupIpSpaceAstNode)) {
      return false;
    }
    AddressGroupIpSpaceAstNode that = (AddressGroupIpSpaceAstNode) o;
    return Objects.equals(_addressGroup, that._addressGroup)
        && Objects.equals(_referenceBook, that._referenceBook);
  }

  String getAddressGroup() {
    return _addressGroup;
  }

  String getReferenceBook() {
    return _referenceBook;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_addressGroup, _referenceBook);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("referenceBook", _referenceBook)
        .add("addressGroup", _addressGroup)
        .toString();
  }
}
