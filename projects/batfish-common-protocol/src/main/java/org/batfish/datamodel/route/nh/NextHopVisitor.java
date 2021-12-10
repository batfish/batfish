package org.batfish.datamodel.route.nh;

/** Visitor interface that evaluates a generic {@link NextHop} */
public interface NextHopVisitor<T> {
  default T visit(NextHop nextHop) {
    return nextHop.accept(this);
  }

  T visitNextHopIp(NextHopIp nextHopIp);

  T visitNextHopInterface(NextHopInterface nextHopInterface);

  T visitNextHopDiscard(NextHopDiscard nextHopDiscard);

  T visitNextHopVrf(NextHopVrf nextHopVrf);

  T visitNextHopVtep(NextHopVtep nextHopVtep);
}
