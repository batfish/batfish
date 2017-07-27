package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;

public class PsFromAsPath extends PsFrom {

  /** */
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
    return new MatchAsPath(new NamedAsPathSet(_asPathName));
  }
}
