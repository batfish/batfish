package batfish.z3.node;

public class DeclareVarExpr extends CollapsedComplexExpr {

   public DeclareVarExpr(String name, int size) {
      _subExpressions.add(new IdExpr("declare-var"));
      _subExpressions.add(new IdExpr(name));
      _subExpressions.add(new BitVecExpr(size));
   }

}
