package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.IsisLevel;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IsisLevelExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetIsisLevel extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IsisLevelExpr _level;

   @JsonCreator
   public SetIsisLevel() {
   }

   public SetIsisLevel(IsisLevelExpr level) {
      _level = level;
   }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      IsisLevel level = _level.evaluate(environment);
      IsisRoute.Builder isisRouteBuilder = (IsisRoute.Builder) environment
            .getOutputRoute();
      isisRouteBuilder.setLevel(level);
      return result;
   }

   public IsisLevelExpr getLevel() {
      return _level;
   }

   public void setLevel(IsisLevelExpr level) {
      _level = level;
   }

}
