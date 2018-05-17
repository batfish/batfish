package org.batfish.datamodel;

public class DstIpHeaderSpaceConstraint implements HeaderSpaceConstraint {
  private final IpSpace _dstIpSpace;

  public DstIpHeaderSpaceConstraint(IpSpace dstIpSpace) {
    _dstIpSpace = dstIpSpace;
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitDstIpHeaderSpaceConstraint(this);
  }

  public IpSpace getDstIpSpace() {
    return _dstIpSpace;
  }
}
