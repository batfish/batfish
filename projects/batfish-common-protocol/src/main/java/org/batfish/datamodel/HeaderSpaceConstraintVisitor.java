package org.batfish.datamodel;

public interface HeaderSpaceConstraintVisitor<T> {
  T visitAndHeaderSpaceConstraint(AndHeaderSpaceConstraint andHeaderSpaceConstraint);

  T visitDstIpHeaderSpaceConstraint(DstIpHeaderSpaceConstraint dstIpHeaderSpaceConstraint);

  T visitDstPortHeaderSpaceConstraint(DstPortHeaderSpaceConstraint dstPortHeaderSpaceConstraint);

  T visitDstProtocolHeaderSpaceConstraint(
      DstProtocolHeaderSpaceConstraint dstProtocolHeaderSpaceConstraint);

  T visitHeaderFieldHeaderSpaceConstraint(
      HeaderFieldsHeaderSpaceConstraint headerFieldsHeaderSpaceConstraint);

  T visitNotHeaderSpaceConstraint(NotHeaderSpaceConstraint notHeaderSpaceConstraint);

  T visitOrHeaderSpaceConstraint(OrHeaderSpaceConstraint orHeaderSpaceConstraint);

  T visitSrcIpHeaderSpaceConstraint(SrcIpHeaderSpaceConstraint srcIpHeaderSpaceConstraint);

  T visitSrcPortHeaderSpaceConstraint(SrcPortHeaderSpaceConstraint srcPortHeaderSpaceConstraint);

  T visitSrcProtocolHeaderSpaceConstraint(
      SrcProtocolHeaderSpaceConstraint srcProtocolHeaderSpaceConstraint);

  T visitTrueHeaderSpaceConstraint();
}
