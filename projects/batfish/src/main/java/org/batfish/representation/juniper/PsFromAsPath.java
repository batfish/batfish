package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/** Represents a "from as-path" line in a {@link PsTerm} */
public final class PsFromAsPath extends PsFrom {

  private final String _asPathName;

  public PsFromAsPath(String asPathName) {
    _asPathName = asPathName;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    Map<String, AsPath> asPaths = jc.getMasterLogicalSystem().getAsPaths();
    return toBooleanExpr(asPaths.get(_asPathName), warnings);
  }

  @VisibleForTesting
  static BooleanExpr toBooleanExpr(@Nullable AsPath asPath, Warnings w) {
    if (asPath == null) {
      // Undefined reference, return false.
      return BooleanExprs.FALSE;
    }
    try {
      return AsPathMatchExprParser.convertToBooleanExpr(asPath.getRegex());
    } catch (Exception e) {
      w.redFlagf(
          "Error converting Juniper as-path regex %s, will assume no paths match instead: %s.",
          asPath.getRegex(), e.getMessage());
      /* Handle error, return false instead. */
      return BooleanExprs.FALSE;
    }
  }
}
