package org.batfish.question.interface_expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Interface;
import org.batfish.question.Environment;

public class VarInterfaceExpr extends BaseInterfaceExpr {

   private final String _variable;

   public VarInterfaceExpr(String variable) {
      _variable = variable;
   }

   @Override
   public Interface evaluate(Environment environment) {
      Interface value = environment.getInterfaces().get(_variable);
      if (value == null) {
         throw new BatfishException(
               "Reference to undefined interface variable: \"" + _variable
                     + "\"");
      }
      else {
         return value;
      }
   }

}
