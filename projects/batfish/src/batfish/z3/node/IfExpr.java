package batfish.z3.node;

public class IfExpr extends ExpandedComplexExpr {
   
   public IfExpr() {
      _subExpressions.add(new IdExpr("=>"));
   }
   
}
