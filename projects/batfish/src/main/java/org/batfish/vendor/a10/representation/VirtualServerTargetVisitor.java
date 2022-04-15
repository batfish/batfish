package org.batfish.vendor.a10.representation;

/** A visitor of {@link VirtualServerTarget} that returns a generic value. */
public interface VirtualServerTargetVisitor<T> {
  default T visit(VirtualServerTarget target) {
    return target.accept(this);
  }

  T visitVirtualServerTargetAddress(VirtualServerTargetAddress virtualServerTargetAddress);

  T visitVirtualServerTargetAddress6(VirtualServerTargetAddress6 virtualServerTargetAddress6);
}
