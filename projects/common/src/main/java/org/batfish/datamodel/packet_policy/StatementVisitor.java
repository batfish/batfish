package org.batfish.datamodel.packet_policy;

/**
 * All evaluators of {@link PacketPolicy} must implement this interface to correctly handle
 * available statement types.
 */
public interface StatementVisitor<T> {
  default T visit(Statement step) {
    return step.accept(this);
  }

  T visitApplyFilter(ApplyFilter applyFilter);

  T visitApplyTransformation(ApplyTransformation transformation);

  T visitIf(If ifStmt);

  T visitReturn(Return returnStmt);
}
