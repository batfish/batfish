package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public final class FwFromJunosApplication implements FwFromApplicationSetMember {

  private final JunosApplication _junosApplication;

  public FwFromJunosApplication(JunosApplication junosApplication) {
    _junosApplication = junosApplication;
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<? super ExprAclLine> lines,
      Warnings w) {
    if (!_junosApplication.hasDefinition()) {
      w.redFlag(
          "Reference to unimplemented built-in application: \""
              + _junosApplication.getJuniperName()
              + "\"");
    } else {
      _junosApplication.applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
    }
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Warnings w) {
    return _junosApplication.toAclLineMatchExpr(jc, w);
  }
}
