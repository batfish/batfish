package org.batfish.representation.cisco;

import javax.annotation.ParametersAreNonnullByDefault;

/** A visitor of {@link IpSla} that returns a generic value of type {@code T}. */
@ParametersAreNonnullByDefault
public interface IpSlaVisitor<T> {
  default T visit(IpSla ipSla) {
    return ipSla.accept(this);
  }

  T visitIcmpEchoSla(IcmpEchoSla icmpEchoSla);
}
