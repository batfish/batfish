package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

class AddressGroupAstNode implements IpSpaceAstNode {
  private final String _addressGroup;
  private final String _referenceBook;

  AddressGroupAstNode(AstNode addressGroup, AstNode referenceBook) {
    checkArgument(addressGroup instanceof StringAstNode, "addressGroup must be a string");
    checkArgument(referenceBook instanceof StringAstNode, "referenceBook must be a string");
    _addressGroup = ((StringAstNode) addressGroup).getStr();
    _referenceBook = ((StringAstNode) referenceBook).getStr();
  }

  AddressGroupAstNode(String addressGroup, String referenceBook) {
    _addressGroup = addressGroup;
    _referenceBook = referenceBook;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitAddressGroupAstNode(this);
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
    if (!(o instanceof AddressGroupAstNode)) {
      return false;
    }
    AddressGroupAstNode that = (AddressGroupAstNode) o;
    return Objects.equals(_addressGroup, that._addressGroup)
        && Objects.equals(_referenceBook, that._referenceBook);
  }

  String getAddressGroup() {
    return _addressGroup;
  }

  String getAddressBook() {
    return _referenceBook;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_addressGroup, _referenceBook);
  }
}
