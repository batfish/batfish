package org.batfish.z3.node;

import org.batfish.representation.Prefix;
import org.batfish.z3.NodProgram;
import org.batfish.z3.Synthesizer;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class PrefixMatchExpr extends BooleanExpr {

   private BooleanExpr _expr;

   public PrefixMatchExpr(String var, Prefix prefix) {

      VarIntExpr varExpr = new VarIntExpr(var);
      int length = prefix.getPrefixLength();
      if (length == 0) {
         _expr = TrueExpr.INSTANCE;
      }
      else if (length == Synthesizer.IP_BITS) {
         _expr = new EqExpr(varExpr, new LitIntExpr(prefix.getAddress()));
      }
      else {
         int low = Synthesizer.IP_BITS - length;
         int high = Synthesizer.IP_BITS - 1;
         IntExpr lhs = new ExtractExpr(var, low, high);
         IntExpr rhs = new LitIntExpr(prefix.getAddress().asLong(), low, high);
         _expr = new EqExpr(lhs, rhs);
      }
   }

   @Override
   public void print(StringBuilder sb, int indent) {
      _expr.print(sb, indent);
   }

   @Override
   public BooleanExpr simplify() {
      return _expr.simplify();
   }

   @Override
   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      return _expr.toBoolExpr(nodProgram);
   }

   @Override
   public String toString() {
      return _expr.toString();
   }

}
