package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class IncrementLocalPreference implements IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _addend;

   @JsonCreator
   public IncrementLocalPreference() {
   }

   public IncrementLocalPreference(int addend) {
      _addend = addend;
   }

   @Override
   public int evaluate(Environment environment) {
      BgpRoute oldRoute = (BgpRoute) environment.getOriginalRoute();
      int oldLp = oldRoute.getLocalPreference();
      int newVal = oldLp + _addend;
      return newVal;
   }

   public int getAddend() {
      return _addend;
   }

   public void setAddend(int addend) {
      _addend = addend;
   }

}
