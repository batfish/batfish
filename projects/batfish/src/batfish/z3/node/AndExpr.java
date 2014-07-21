package batfish.z3.node;

public class AndExpr extends ExpandedComplexExpr {

   public AndExpr() {
      _subExpressions.add(new IdExpr("and"));
   }

}
