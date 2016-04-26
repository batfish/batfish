package org.batfish.question.string_expr.iface;

import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.representation.Interface;

public final class NameInterfaceStringExpr extends InterfaceStringExpr {

   public NameInterfaceStringExpr(InterfaceExpr caller) {
      super(caller);
   }

   @Override
   public String evaluate(Environment environment) {
      Interface caller = _caller.evaluate(environment);
      return caller.getName();
   }

}
