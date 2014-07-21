package batfish.z3.node;

public class RuleExpr extends ExpandedComplexExpr {

   private IfExpr _ifExpression;
   
   public RuleExpr() {
      _subExpressions.add(new IdExpr("rule"));
      _ifExpression = new IfExpr();
      _subExpressions.add(_ifExpression);
   }
   
   @Override
   public void addSubExpression(Expr subExpression) {
      _ifExpression._subExpressions.add(subExpression);
   }
   
}
