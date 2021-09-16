package org.batfish.vendor.check_point_management;

/** Indicates a NAT hide rule should use its gateway's IP. */
public class NatHideBehindGateway extends NatHideBehind {
  public static final NatHideBehindGateway INSTANCE = new NatHideBehindGateway();

  @Override
  <T> T accept(NatHideBehindVisitor<T> visitor) {
    return visitor.visitNatHideBehindGateway(this);
  }

  private NatHideBehindGateway() {}

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NatHideBehindGateway;
  }

  @Override
  public int hashCode() {
    return 0x6E677E37; // randomly generated
  }
}
