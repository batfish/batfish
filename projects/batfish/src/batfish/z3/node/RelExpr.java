package batfish.z3.node;

public class RelExpr extends CollapsedComplexExpr {
   
   public RelExpr(String name) {
      _subExpressions.add(new IdExpr(name));
   }
   
}
