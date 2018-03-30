package org.batfish.z3.expr;

import java.util.Set;

/** A statement describing the preconditions necessary to reach a specified target state */
public abstract class RuleStatement extends Statement {
  public abstract StateExpr getPostconditionState();

  public abstract Set<StateExpr> getPreconditionStates();
}
