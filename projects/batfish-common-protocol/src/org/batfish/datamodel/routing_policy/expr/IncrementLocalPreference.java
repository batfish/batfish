package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class IncrementLocalPreference extends IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _addend;

   @JsonCreator
   private IncrementLocalPreference() {
   }

   public IncrementLocalPreference(int addend) {
      _addend = addend;
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
      IncrementLocalPreference other = (IncrementLocalPreference) obj;
      if (_addend != other._addend) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _addend;
      return result;
   }

   public void setAddend(int addend) {
      _addend = addend;
   }

}
