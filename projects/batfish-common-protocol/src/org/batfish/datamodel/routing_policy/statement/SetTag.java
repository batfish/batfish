package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetTag extends AbstractStatement {

   /**
   *
   */
  private static final long serialVersionUID = 1L;

  private IntExpr _tag;

  @JsonCreator
  public SetTag() {
  }

  public SetTag(IntExpr expr) {
     _tag = expr;
  }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      int tag = _tag.evaluate(environment);
      environment.getOutputRoute().setTag(tag);
      return result;
   }

   public void setTag(IntExpr tag) {
      _tag = tag;
   }
   
   public IntExpr getTag() {
      return _tag;
   }

}
