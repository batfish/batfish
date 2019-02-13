package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

class AddressGroupAstNode implements NewIpSpaceAstNode {
  private final String _addressGroup;
  private final String _addressBook;

  AddressGroupAstNode(AstNode addressGroup, AstNode addressBook) {
    checkArgument(addressGroup instanceof StringAstNode, "addressGroup must be a string");
    checkArgument(addressBook instanceof StringAstNode, "addressBook must be a string");
    _addressGroup = ((StringAstNode) addressGroup).getStr();
    _addressBook = ((StringAstNode) addressBook).getStr();
  }

  AddressGroupAstNode(String addressGroup, String addressBook) {
    _addressGroup = addressGroup;
    _addressBook = addressBook;
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
        && Objects.equals(_addressBook, that._addressBook);
  }

  String getAddressGroup() {
    return _addressGroup;
  }

  String getAddressBook() {
    return _addressBook;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_addressGroup, _addressBook);
  }
}
