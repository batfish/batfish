package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

/** Represents a policy-statement "from" line in a {@link PsTerm} */
public abstract class PsFrom implements Serializable {

  private static final long serialVersionUID = 1L;

  public abstract BooleanExpr toBooleanExpr(
      JuniperConfiguration jc, Configuration c, Warnings warnings);
}
