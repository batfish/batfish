package org.batfish.z3.node;

import java.util.Collections;
import java.util.Set;

public class VarIntExpr extends IntExpr {

   private String _var;

   public VarIntExpr(String var) {
      _var = var;
      _printer = new SimpleExprPrinter(_var);
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof VarIntExpr) {
         VarIntExpr rhs = (VarIntExpr) o;
         return _var.equals(rhs._var);
      }
      return false;
   }

   public String getVariable() {
      return _var;
   }

   @Override
   public Set<String> getVariables() {
      return Collections.singleton(_var);
   }

   @Override
   public int hashCode() {
      return _var.hashCode();
   }

}
