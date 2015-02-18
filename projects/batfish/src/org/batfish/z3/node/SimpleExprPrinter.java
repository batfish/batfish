package org.batfish.z3.node;

public class SimpleExprPrinter extends ExprPrinter {

   private String _text;

   public SimpleExprPrinter(String text) {
      _text = text;
   }

   @Override
   public void print(StringBuilder sb, int indent) {
      sb.append(_text);
   }

}
