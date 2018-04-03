package org.batfish.z3.expr;

import java.util.Set;

/** A statement describing the preconditions necessary to reach a specified target state */
public abstract class RuleStatement extends Statement {

  /** Return the postcondition state that this rule leads to */
  public abstract StateExpr getPostconditionState();

  /** Return the set of precondition states that must all be true for the rule to fire. */
  public abstract Set<StateExpr> getPreconditionStates();
}
