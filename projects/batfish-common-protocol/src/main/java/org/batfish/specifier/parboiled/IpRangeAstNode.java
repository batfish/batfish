package org.batfish.specifier.parboiled;

import java.util.Objects;
import org.batfish.datamodel.Ip;

final class IpRangeAstNode implements IpSpaceAstNode {
  private final Ip _low;
  private final Ip _high;

  IpRangeAstNode(AstNode low, AstNode right) {
    _low = ((IpAstNode) low).getIp();
    _high = ((IpAstNode) right).getIp();
  }

  IpRangeAstNode(Ip low, Ip high) {
    _low = low;
    _high = high;
  }

  IpRangeAstNode(String low, String high) {
    _low = Ip.parse(low);
    _high = Ip.parse(high);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitIpRangeAstNode(this);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIpRangeAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpRangeAstNode)) {
      return false;
    }
    IpRangeAstNode that = (IpRangeAstNode) o;
    return Objects.equals(_low, that._low) && Objects.equals(_high, that._high);
  }

  public Ip getLow() {
    return _low;
  }

  public Ip getHigh() {
    return _high;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_low, _high);
  }
}
