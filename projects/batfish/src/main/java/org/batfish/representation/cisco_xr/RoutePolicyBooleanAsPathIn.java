package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConversions.AS_PATH_SET_ELEM_CONVERTER;

import com.google.common.collect.ImmutableList;
import java.util.Objects;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.ExplicitAsPathSet;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;

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
      if (!c.getAsPathAccessLists().containsKey(name)) {
        // Undefined, return false.
        return BooleanExprs.FALSE;
      }
      return new LegacyMatchAsPath(
          new org.batfish.datamodel.routing_policy.expr.NamedAsPathSet(name));
    } else if (_asExpr instanceof InlineAsPathSet) {
      return new LegacyMatchAsPath(
          new ExplicitAsPathSet(
              ((InlineAsPathSet) _asExpr)
                  .getAsPathSet().getElements().stream()
                      .map(elem -> elem.accept(AS_PATH_SET_ELEM_CONVERTER))
                      .filter(Objects::nonNull)
                      .collect(ImmutableList.toImmutableList())));
    } else {
      assert _asExpr instanceof AsPathSetVariable;
      // TODO: implement route-policy variables
      return BooleanExprs.FALSE;
    }
  }
}
