package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

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
      w.redFlag("Reference to undefined application: \"" + _junosApplication.name() + "\"");
    } else {
      _junosApplication.applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
    }
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Warnings w) {
    if (!_junosApplication.hasDefinition()) {
      w.redFlag("Reference to undefined application: \"" + _junosApplication.name() + "\"");
      // match nothing
      return new MatchHeaderSpace(
          HeaderSpace.builder().setSrcIps(EmptyIpSpace.INSTANCE).build(),
          ApplicationSetMember.getTraceElement(
              jc.getFilename(), JuniperStructureType.APPLICATION, _junosApplication.name()));
    }
    return _junosApplication.toAclLineMatchExpr(jc, w);
  }
}
