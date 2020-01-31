package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public final class FwFromApplicationOrApplicationSet implements FwFromApplicationSetMember {

  private final String _applicationOrApplicationSetName;

  public FwFromApplicationOrApplicationSet(String applicationOrApplicationSetName) {
    _applicationOrApplicationSetName = applicationOrApplicationSetName;
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<? super ExprAclLine> lines,
      Warnings w) {
    ApplicationSetMember application =
        jc.getMasterLogicalSystem().getApplications().get(_applicationOrApplicationSetName);
    if (application == null) {
      application =
          jc.getMasterLogicalSystem().getApplicationSets().get(_applicationOrApplicationSetName);
    }
    if (application != null) {
      application.applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
    }
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Warnings w) {
    ApplicationSetMember application =
        jc.getMasterLogicalSystem().getApplications().containsKey(_applicationOrApplicationSetName)
            ? jc.getMasterLogicalSystem().getApplications().get(_applicationOrApplicationSetName)
            : jc.getMasterLogicalSystem()
                .getApplicationSets()
                .get(_applicationOrApplicationSetName);

    if (application == null) {
      w.redFlag(
          String.format(
              "Reference to undefined application/application-set: %s",
              _applicationOrApplicationSetName));

      // match nothing
      return new MatchHeaderSpace(
          HeaderSpace.builder().setSrcIps(EmptyIpSpace.INSTANCE).build(),
          ApplicationSetMember.getTraceElement(
              jc.getFilename(),
              JuniperStructureType.APPLICATION,
              _applicationOrApplicationSetName));
    }

    return application.toAclLineMatchExpr(jc, w);
  }
}
