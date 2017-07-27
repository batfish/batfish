package org.batfish.datamodel.routing_policy.expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Result;

public enum IntComparator {
  EQ,
  GE,
  GT,
  LE,
  LT;

  public Result apply(int lhs, int rhs) {
    Result result = new Result();
    boolean b;
    switch (this) {
      case EQ:
        b = (lhs == rhs);
        break;
      case GE:
        b = (lhs >= rhs);
        break;
      case GT:
        b = (lhs > rhs);
        break;
      case LE:
        b = (lhs <= rhs);
        break;
      case LT:
        b = (lhs < rhs);
        break;
      default:
        throw new BatfishException("Invalid " + IntComparator.class.getSimpleName());
    }
    result.setBooleanValue(b);
    return result;
  }
}
