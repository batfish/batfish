package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix6Space;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ExplicitPrefix6Set implements PrefixSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Prefix6Space _prefix6Space;

   @JsonCreator
   public ExplicitPrefix6Set() {
   }

   public ExplicitPrefix6Set(Prefix6Space prefix6Space) {
      _prefix6Space = prefix6Space;
   }

   public Prefix6Space getPrefix6Space() {
      return _prefix6Space;
   }

   @Override
   public boolean matches(Environment environment, Route route) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public void setPrefix6Space(Prefix6Space prefix6Space) {
      _prefix6Space = prefix6Space;
   }

}
