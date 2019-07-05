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
    switch (this) {
      case EQ:
        return new Result(lhs == rhs);
      case GE:
        return new Result(lhs >= rhs);
      case GT:
        return new Result(lhs > rhs);
      case LE:
        return new Result(lhs <= rhs);
      case LT:
        return new Result(lhs < rhs);
      default:
        throw new BatfishException("Invalid " + IntComparator.class.getSimpleName());
    }
  }

  public Result apply(long lhs, long rhs) {
    switch (this) {
      case EQ:
        return new Result(lhs == rhs);
      case GE:
        return new Result(lhs >= rhs);
      case GT:
        return new Result(lhs > rhs);
      case LE:
        return new Result(lhs <= rhs);
      case LT:
        return new Result(lhs < rhs);
      default:
        throw new BatfishException("Invalid " + IntComparator.class.getSimpleName());
    }
  }
}
