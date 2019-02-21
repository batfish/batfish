package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.ExplicitAsPathSet;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.RegexAsPathSetElem;
import org.batfish.grammar.flatjuniper.AsPathRegex;

/** Represents a "from as-path" line in a {@link PsTerm} */
public class PsFromAsPath extends PsFrom {

  private static final long serialVersionUID = 1L;

  private String _asPathName;

  public PsFromAsPath(String asPathName) {
    _asPathName = asPathName;
  }

  public String getAsPathName() {
    return _asPathName;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    AsPath asPath = jc.getMasterLogicalSystem().getAsPaths().get(_asPathName);
    if (asPath == null) {
      // Undefined reference, return false.
      return BooleanExprs.FALSE;
    }
    try {
      String javaRegex = AsPathRegex.convertToJavaRegex(asPath.getRegex());
      return new MatchAsPath(new ExplicitAsPathSet(new RegexAsPathSetElem(javaRegex)));
    } catch (Exception e) {
      warnings.redFlag(
          String.format(
              "Error converting Juniper as-path regex %s, will assume no paths match instead: %s.",
              asPath.getRegex(), e.getMessage()));
      /* Handle error, return false instead. */
      return BooleanExprs.FALSE;
    }
  }
}
