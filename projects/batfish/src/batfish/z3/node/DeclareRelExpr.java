package batfish.z3.node;

public class DeclareRelExpr extends CollapsedComplexExpr {

   public DeclareRelExpr(String name, int... sizes) {
      _subExpressions.add(new IdExpr("declare-rel"));
      _subExpressions.add(new IdExpr(name));
      ListExpr listExpression = new ListExpr();
      _subExpressions.add(new ListExpr());
      for (int size : sizes) {
         listExpression._subExpressions.add(new BitVecExpr(size));
      }
   }

}
