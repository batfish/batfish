package org.batfish.z3.node;

public class CollapsedListExpr extends ListExpr {

   public CollapsedListExpr() {
      super();
      _printer = new CollapsedComplexExprPrinter(this);
   }

}
