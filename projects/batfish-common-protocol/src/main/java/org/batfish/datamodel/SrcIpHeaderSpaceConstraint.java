package org.batfish.datamodel;

public class SrcIpHeaderSpaceConstraint implements HeaderSpaceConstraint {
  private final IpSpace _srcIpSpace;

  public SrcIpHeaderSpaceConstraint(IpSpace srcIpSpace) {
    _srcIpSpace = srcIpSpace;
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitSrcIpHeaderSpaceConstraint(this);
  }

  public IpSpace getSrcIpSpace() {
    return _srcIpSpace;
  }
}
