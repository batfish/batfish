package org.batfish.vendor.a10.representation;

/** A visitor of {@link ServerTarget} that returns a generic value. */
public interface ServerTargetVisitor<T> {
  default T visit(ServerTarget target) {
    return target.accept(this);
  }

  T visitAddress(ServerTargetAddress address);
}
