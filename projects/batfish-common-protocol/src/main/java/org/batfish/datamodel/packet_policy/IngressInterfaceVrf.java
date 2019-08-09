package org.batfish.datamodel.packet_policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;

/** Represents VRF of the ingress interface, resolved at the time of policy evaluation */
public final class IngressInterfaceVrf implements VrfExpr {
  private static final IngressInterfaceVrf INSTANCE = new IngressInterfaceVrf();

  private IngressInterfaceVrf() {}

  @JsonCreator
  public static IngressInterfaceVrf instance() {
    return INSTANCE;
  }

  @Override
  public <T> T accept(VrfExprVisitor<T> visitor) {
    return visitor.visitIngressInterfaceVrf(this);
  }

  @Override
  public int hashCode() {
    return 0x3e0583f8; // randomly generated
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof IngressInterfaceVrf;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(IngressInterfaceVrf.class).toString();
  }
}
