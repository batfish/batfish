package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DecrementLocalPreference implements IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _subtrahend;

   @JsonCreator
   public DecrementLocalPreference() {
   }

   public DecrementLocalPreference(int subtrahend) {
      _subtrahend = subtrahend;
   }

   @Override
   public int evaluate(Environment environment) {
      BgpRoute oldRoute = (BgpRoute) environment.getOriginalRoute();
      int oldLp = oldRoute.getLocalPreference();
      int newVal = oldLp - _subtrahend;
      return newVal;
   }

   public int getSubtrahend() {
      return _subtrahend;
   }

   public void setSubtrahend(int subtrahend) {
      _subtrahend = subtrahend;
   }

}
