package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchLocalPreference extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _localPreference;

   public MatchLocalPreference(int localPreference) {
      _localPreference = localPreference;
   }

   @Override
   public Result evaluate(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   public void setLocalPreference(int localPreference) {
      _localPreference = localPreference;
   }

}
