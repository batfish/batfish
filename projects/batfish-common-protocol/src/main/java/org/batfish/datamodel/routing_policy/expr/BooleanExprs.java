package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public final class BooleanExprs {
  public enum StaticExpressionType {
    CallExprContext,
    CallStatementContext,
    False,
    True,
  }

  public static final StaticBooleanExpr CALL_EXPR_CONTEXT =
      new StaticBooleanExpr(StaticExpressionType.CallExprContext);
  public static final StaticBooleanExpr CALL_STATEMENT_CONTEXT =
      new StaticBooleanExpr(StaticExpressionType.CallStatementContext);
  public static final StaticBooleanExpr FALSE = new StaticBooleanExpr(StaticExpressionType.False);
  public static final StaticBooleanExpr TRUE = new StaticBooleanExpr(StaticExpressionType.True);

  public static class StaticBooleanExpr extends BooleanExpr {
    /** */
    private static final long serialVersionUID = 1L;

    private static final String PROP_TYPE = "type";

    private StaticExpressionType _type;

    private StaticBooleanExpr(StaticExpressionType type) {
      _type = type;
    }

    @JsonCreator
    static StaticBooleanExpr create(@JsonProperty(PROP_TYPE) StaticExpressionType type) {
      switch (type) {
        case CallExprContext:
          return CALL_EXPR_CONTEXT;

        case CallStatementContext:
          return CALL_STATEMENT_CONTEXT;

        case False:
          return FALSE;

        case True:
          return TRUE;

        default:
          throw new BatfishException(
              "Unhandled " + StaticBooleanExpr.class.getCanonicalName() + ": " + type);
      }
    }

    @Override
    public boolean equals(Object rhs) {
      return rhs instanceof StaticBooleanExpr && _type == ((StaticBooleanExpr) rhs)._type;
    }

    @Override
    public Result evaluate(Environment environment) {
      Result result = new Result();
      switch (_type) {
        case CallExprContext:
          result.setBooleanValue(environment.getCallExprContext());
          break;
        case CallStatementContext:
          result.setBooleanValue(environment.getCallStatementContext());
          break;
        case False:
          result.setBooleanValue(false);
          break;
        case True:
          result.setBooleanValue(true);
          break;
        default:
          throw new BatfishException(
              "Unhandled " + StaticBooleanExpr.class.getCanonicalName() + ": " + _type);
      }
      return result;
    }

    @JsonProperty(PROP_TYPE)
    public StaticExpressionType getType() {
      return _type;
    }

    @Override
    public int hashCode() {
      return _type.ordinal();
    }
  }
}
