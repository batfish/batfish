package batfish.z3.node;

public class EqExpr extends CollapsedComplexExpr {

   public EqExpr() {
      _subExpressions.add(new IdExpr("="));
   }
   
}
