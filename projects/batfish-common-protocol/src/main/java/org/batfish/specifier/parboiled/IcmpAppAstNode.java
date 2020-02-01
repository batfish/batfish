package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
final class IcmpAppAstNode implements AppAstNode {

  @Nullable private final Integer _type;

  @Nullable private final Integer _code;

  IcmpAppAstNode() {
    this(null, null);
  }

  IcmpAppAstNode(Integer type) {
    this(type, null);
  }

  IcmpAppAstNode(@Nullable Integer type, @Nullable Integer code) {
    checkArgument(type == null || isValidType(type), "Invalid ICMP type: %s", type);
    if (code != null) {
      checkArgument(type != null, "ICMP type cannot be null when code is non-null");
      checkArgument(isValidCodeForType(code, type), "Invalid ICMP code %s for type %s", code, type);
    }

    _type = type;
    _code = code;
  }

  /** Creates from an AstNode that contains the type and the code */
  public static AstNode create(AstNode typeAstNode, int code) {
    return new IcmpAppAstNode(((IcmpAppAstNode) typeAstNode).getType(), code);
  }

  /**
   * Returns if the ICMP type is valid. Assumes that type is non-null and a non-negative number (as
   * parsed by {@link CommonParser#Number()}).
   */
  private static boolean isValidType(Integer type) {
    // allowing for unassigned and reserved values
    return type <= 255;
  }

  /**
   * Returns if the ICMP code is valid for the provided type. Assumes that type and code are
   * non-null and non-negative numbers (as parsed by {@link CommonParser#Number()}).
   *
   * <p>The classification below is based on
   * https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol#Control_messages
   */
  private static boolean isValidCodeForType(Integer code, Integer type) {
    // types with no valid codes
    if (type == 1
        || type == 2
        || type == 6
        || type == 7
        || (type >= 19 && type <= 29)
        || (type >= 31 && type <= 41)
        || type >= 44) {
      return false;
    }

    switch (type) {
      case 0: // echo request
      case 8:
      case 9: // router advertisement
      case 10: // router solicitation
      case 4: // source quench
      case 13: // Timestamp
      case 14:
      case 15:
      case 16:
      case 17:
      case 18: // Address Mask Reply
      case 30: // traceroute
      case 42:
        return code == 0;
      case 3: // destination unreachable
        return code <= 15;
      case 5: // redirect message
      case 43:
        return code <= 4;
      case 11: // time exceeded
        return code <= 1;
      case 12: // Parameter Problem: Bad IP header
        return code <= 2;
      default:
        throw new IllegalArgumentException("Invalid ICMP type " + type);
    }
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIcmpAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitIcmpAppAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IcmpAppAstNode)) {
      return false;
    }
    IcmpAppAstNode that = (IcmpAppAstNode) o;
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

  @Nullable
  public Integer getType() {
    return _type;
  }

  @Nullable
  public Integer getCode() {
    return _code;
  }
}
