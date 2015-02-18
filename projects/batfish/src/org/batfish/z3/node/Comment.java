package org.batfish.z3.node;

public class Comment extends Statement {

   private String[] _lines;

   public Comment(String... lines) {
      _lines = lines;
   }

   @Override
   public void print(StringBuilder sb, int indent) {
      sb.append("\n");
      for (String line : _lines) {
         sb.append(";;; ");
         sb.append(line);
         sb.append("\n");
      }
   }

}
