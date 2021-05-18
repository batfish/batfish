package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConversions.computeDedupedAsPathMatchExprName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.computeOriginalAsPathMatchExprName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toDedupedAsPathMatchExpr;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toOriginalAsPathMatchExpr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprReference;
import org.batfish.datamodel.routing_policy.as_path.DedupedAsPath;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Disjunction;

public class RoutePolicyBooleanAsPathIn extends RoutePolicyBoolean {

  private final AsPathSetExpr _asExpr;

  public RoutePolicyBooleanAsPathIn(AsPathSetExpr expr) {
    _asExpr = expr;
  }

  public AsPathSetExpr getAsPathSetExpr() {
    return _asExpr;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    if (_asExpr instanceof AsPathSetReference) {
      String name = ((AsPathSetReference) _asExpr).getName();
      if (!cc.getAsPathSets().containsKey(name)) {
        // Undefined, return false.
        return BooleanExprs.FALSE;
      }
      return new Disjunction(
          MatchAsPath.of(
              DedupedAsPath.of(InputAsPath.instance()),
              AsPathMatchExprReference.of(computeDedupedAsPathMatchExprName(name))),
          MatchAsPath.of(
              InputAsPath.instance(),
              AsPathMatchExprReference.of(computeOriginalAsPathMatchExprName(name))));
    } else if (_asExpr instanceof InlineAsPathSet) {
      BooleanExpr dedupedMatches =
          MatchAsPath.of(
              DedupedAsPath.of(InputAsPath.instance()),
              toDedupedAsPathMatchExpr(((InlineAsPathSet) _asExpr).getAsPathSet()));
      BooleanExpr originalMatches =
          MatchAsPath.of(
              InputAsPath.instance(),
              toOriginalAsPathMatchExpr(((InlineAsPathSet) _asExpr).getAsPathSet()));
      return new Disjunction(dedupedMatches, originalMatches);
    } else {
      assert _asExpr instanceof AsPathSetVariable;
      // TODO: implement route-policy variables
      return BooleanExprs.FALSE;
    }
  }
}
