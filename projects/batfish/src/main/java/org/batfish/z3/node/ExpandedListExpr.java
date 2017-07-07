package org.batfish.z3.node;

public class ExpandedListExpr extends ListExpr {

   public ExpandedListExpr() {
      super();
      _printer = new ExpandedComplexExprPrinter(this);
      _subExpressions.add(new IdExpr(""));
   }

}
