package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetLocalPreference extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntExpr _localPreference;

   @JsonCreator
   public SetLocalPreference() {
   }

   public SetLocalPreference(IntExpr localPreference) {
      _localPreference = localPreference;
   }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      BgpRoute.Builder bgpBuilder = (BgpRoute.Builder) environment
            .getOutputRoute();
      int localPreference = _localPreference.evaluate(environment);
      bgpBuilder.setLocalPreference(localPreference);
      return result;
   }

   public IntExpr getLocalPreference() {
      return _localPreference;
   }

   public void setLocalPreference(IntExpr localPreference) {
      _localPreference = localPreference;
   }

}
