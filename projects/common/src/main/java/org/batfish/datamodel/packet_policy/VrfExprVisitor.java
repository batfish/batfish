package org.batfish.datamodel.packet_policy;

public interface VrfExprVisitor<T> {
  default T visit(VrfExpr action) {
    return action.accept(this);
  }

  T visitLiteralVrfName(LiteralVrfName expr);

  T visitIngressInterfaceVrf(IngressInterfaceVrf expr);
}
