package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Disjunction;

public final class PsFromAsPathGroup extends PsFrom {
  private final String _asPathGroupName;

  public PsFromAsPathGroup(String asPathGroupName) {
    _asPathGroupName = asPathGroupName;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    Map<String, AsPathGroup> asPathGroups = jc.getMasterLogicalSystem().getAsPathGroups();
    return toBooleanExpr(asPathGroups.get(_asPathGroupName), warnings);
  }

  @VisibleForTesting
  static BooleanExpr toBooleanExpr(@Nullable AsPathGroup asPathGroup, Warnings w) {
    if (asPathGroup == null) {
      // Undefined reference, return false.
      return BooleanExprs.FALSE;
    }
    List<BooleanExpr> asPaths = new ArrayList<>();
    for (NamedAsPath namedAsPath : asPathGroup.getAsPaths().values()) {
      try {
        BooleanExpr booleanExpr =
            AsPathMatchExprParser.convertToBooleanExpr(namedAsPath.getRegex());
        asPaths.add(booleanExpr);
      } catch (Exception e) {
        w.redFlagf(
            "Error converting Juniper as-path-group regex %s, will assume no paths match instead:"
                + " %s.",
            asPathGroup.getName(), e.getMessage());
        asPaths.add(BooleanExprs.FALSE);
      }
    }
    return new Disjunction(asPaths);
  }
}
