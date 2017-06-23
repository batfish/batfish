package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ExplicitAs extends AsExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _as;

   @JsonCreator
   private ExplicitAs() {
   }

   public ExplicitAs(int as) {
      _as = as;
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
      ExplicitAs other = (ExplicitAs) obj;
      if (_as != other._as) {
         return false;
      }
      return true;
   }

   @Override
   public int evaluate(Environment environment) {
      return _as;
   }

   public int getAs() {
      return _as;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _as;
      return result;
   }

   public void setAs(int as) {
      _as = as;
   }

}
