package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.transformation.Transformation.RuleAction;

public abstract class CiscoNat implements Serializable {
  private static final long serialVersionUID = 1L;

  protected RuleAction _action;

  public RuleAction getAction() {
    return _action;
  }

  public void setAction(RuleAction action) {
    _action = action;
  }

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();
}
