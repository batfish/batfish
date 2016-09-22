package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetLocalPreference extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private int _localPreference;

   @JsonCreator
   public SetLocalPreference() {
   }

   public SetLocalPreference(int localPreference) {
      _localPreference = localPreference;
   }

   @Override
   public Result execute(Environment environment,
         AbstractRouteBuilder<?> route) {
      Result result = new Result();
      BgpRoute.Builder bgpBuilder = (BgpRoute.Builder) route;
      bgpBuilder.setLocalPreference(_localPreference);
      return result;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   public void setLocalPreference(int localPreference) {
      _localPreference = localPreference;
   }

}
