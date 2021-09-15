package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatHideBehind} */
public interface NatHideBehindVisitor<T> {
  default T visit(NatHideBehind natHideBehind) {
    return natHideBehind.accept(this);
  }

  T visitNatHideBehindGateway(NatHideBehindGateway natHideBehindGateway);

  T visitNatHideBehindIp(NatHideBehindIp natHideBehindIp);
}
