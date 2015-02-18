package org.batfish.z3.node;

public class IdExpr extends Expr {

   private String _id;

   public IdExpr(String id) {
      _id = id;
      _printer = new SimpleExprPrinter(_id);
   }

   public String getId() {
      return _id;
   }

}
