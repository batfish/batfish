package org.batfish.representation.cisco;

import java.io.Serializable;

public class RouteMapContinue implements Serializable {

  private final int _statementLine;

  private final Integer _target;

  public RouteMapContinue(Integer target, int statementLine) {
    _target = target;
    _statementLine = statementLine;
  }

  public int getStatementLine() {
    return _statementLine;
  }

  public Integer getTarget() {
    return _target;
  }
}
