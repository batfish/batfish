package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.vendor.VendorStructureId;

public interface ApplicationSetMember {
  void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<? super ExprAclLine> lines,
      Warnings w);

  AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Warnings w);

  static TraceElement getTraceElement(
      String filename, JuniperStructureType structureType, String structureName) {
    return TraceElement.builder()
        .add("Matched ")
        .add(
            structureName,
            new VendorStructureId(filename, structureType.getDescription(), structureName))
        .build();
  }
}
