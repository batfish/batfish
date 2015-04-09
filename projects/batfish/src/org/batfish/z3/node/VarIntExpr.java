package org.batfish.z3.node;

import java.util.Collections;
import java.util.Set;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.Z3Exception;

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

   @Override
   public com.microsoft.z3.BitVecExpr toBitVecExpr(NodProgram nodProgram)
         throws Z3Exception {
      return nodProgram.getVariables().get(_var);
   }

}
