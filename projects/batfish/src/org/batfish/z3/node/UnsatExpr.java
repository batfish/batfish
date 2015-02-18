package org.batfish.z3.node;

public class UnsatExpr extends Statement {
   @Override
   public void print(StringBuilder sb, int indent) {
      sb.append("unsat");
   }
}
