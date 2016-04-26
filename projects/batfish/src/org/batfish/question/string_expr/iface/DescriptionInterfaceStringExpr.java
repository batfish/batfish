package org.batfish.question.string_expr.iface;

import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.representation.Interface;

public final class DescriptionInterfaceStringExpr extends InterfaceStringExpr {

   public DescriptionInterfaceStringExpr(InterfaceExpr caller) {
      super(caller);
   }

   @Override
   public String evaluate(Environment environment) {
      Interface caller = _caller.evaluate(environment);
      String description = caller.getDescription();
      if (description == null) {
         return "";
      }
      else {
         return description;
      }
   }

}
