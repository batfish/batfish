package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Prefix6Space;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ExplicitPrefix6Set extends Prefix6SetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Prefix6Space _prefix6Space;

   @JsonCreator
   private ExplicitPrefix6Set() {
   }

   public ExplicitPrefix6Set(Prefix6Space prefix6Space) {
      _prefix6Space = prefix6Space;
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
      ExplicitPrefix6Set other = (ExplicitPrefix6Set) obj;
      if (_prefix6Space == null) {
         if (other._prefix6Space != null) {
            return false;
         }
      }
      else if (!_prefix6Space.equals(other._prefix6Space)) {
         return false;
      }
      return true;
   }

   public Prefix6Space getPrefix6Space() {
      return _prefix6Space;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((_prefix6Space == null) ? 0 : _prefix6Space.hashCode());
      return result;
   }

   @Override
   public boolean matches(Prefix6 prefix6, Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public void setPrefix6Space(Prefix6Space prefix6Space) {
      _prefix6Space = prefix6Space;
   }

}
