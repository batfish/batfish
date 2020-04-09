package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.applications.IcmpTypeCodesApplication.isValidTypeCode;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
final class IcmpTypeCodeAppAstNode implements AppAstNode {

  private final int _type;

  private final int _code;

  IcmpTypeCodeAppAstNode(int type, int code) {
    checkArgument(
        isValidTypeCode(type, code), "Invalid ICMP type/code combination: %s/%s", type, code);
    _type = type;
    _code = code;
  }

  /** Creates from an AstNode that contains the type and the code */
  public static AstNode create(AstNode typeAstNode, int code) {
    return new IcmpTypeCodeAppAstNode(((IcmpTypeAppAstNode) typeAstNode).getType(), code);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIcmpTypeCodeAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitIcmpTypeCodeAppAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IcmpTypeCodeAppAstNode)) {
      return false;
    }
    IcmpTypeCodeAppAstNode that = (IcmpTypeCodeAppAstNode) o;
    return Objects.equals(_type, that._type) && Objects.equals(_code, that._code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _code);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add("type", _type)
        .add("code", _code)
        .toString();
  }

  public int getType() {
    return _type;
  }

  public int getCode() {
    return _code;
  }
}
