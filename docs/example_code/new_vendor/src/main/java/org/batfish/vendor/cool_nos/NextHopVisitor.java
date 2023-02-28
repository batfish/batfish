package org.batfish.vendor.cool_nos;

/** Visitor of {@link NextHop} that returns a generic value of type {@code T}. */
public interface NextHopVisitor<T> {

  default T visit(NextHop nextHop) {
    return nextHop.accept(this);
  }

  T visitNextHopDiscard(NextHopDiscard nextHopDiscard);

  T visitNextHopGateway(NextHopGateway nextHopGateway);

  T visitNextHopInterface(NextHopInterface nextHopInterface);
}
