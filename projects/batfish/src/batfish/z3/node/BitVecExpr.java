package batfish.z3.node;

public class BitVecExpr extends CollapsedComplexExpr {

   public BitVecExpr(int size) {
      _subExpressions.add(new IdExpr("_"));
      _subExpressions.add(new IdExpr("BitVec"));
      _subExpressions.add(new IdExpr("_"));
      _subExpressions.add(new IdExpr(Integer.toString(size)));
   }
}
