package org.batfish.representation.juniper;

public interface BridgeDomainVlanIdVoidVisitor {

  default void visit(BridgeDomainVlanId bridgeDomainVlanId) {
    bridgeDomainVlanId.accept(this);
  }

  void visitBridgeDomainVlanIdAll();

  void visitBridgeDomainVlanIdNone();

  void visitBridgeDomainVlanIdNumber(BridgeDomainVlanIdNumber bridgeDomainVlanIdNumber);
}
