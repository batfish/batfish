package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DecrementLocalPreference extends IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _subtrahend;

   @JsonCreator
   private DecrementLocalPreference() {
   }

   public DecrementLocalPreference(int subtrahend) {
      _subtrahend = subtrahend;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      DecrementLocalPreference other = (DecrementLocalPreference) obj;
      if (_subtrahend != other._subtrahend) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _subtrahend;
      return result;
   }

   public void setSubtrahend(int subtrahend) {
      _subtrahend = subtrahend;
   }

}
