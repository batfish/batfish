package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Result;

public enum IntComparator {
  EQ,
  GE,
  GT,
  LE,
  LT;

  public Result apply(int lhs, int rhs) {
    return switch (this) {
      case EQ -> new Result(lhs == rhs);
      case GE -> new Result(lhs >= rhs);
      case GT -> new Result(lhs > rhs);
      case LE -> new Result(lhs <= rhs);
      case LT -> new Result(lhs < rhs);
    };
  }

  public Result apply(long lhs, long rhs) {
    return switch (this) {
      case EQ -> new Result(lhs == rhs);
      case GE -> new Result(lhs >= rhs);
      case GT -> new Result(lhs > rhs);
      case LE -> new Result(lhs <= rhs);
      case LT -> new Result(lhs < rhs);
    };
  }
}
