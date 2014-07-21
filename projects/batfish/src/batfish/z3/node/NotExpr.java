package batfish.z3.node;

public class NotExpr extends CollapsedComplexExpr {

   public NotExpr() {
      _subExpressions.add(new IdExpr("not"));
   }

}
