package batfish.z3.node;

public class ExtractExpr extends CollapsedComplexExpr {

   public ExtractExpr(String var, int high, int low) {
      ListExpr listExpr = new ListExpr();
      listExpr._subExpressions.add(new IdExpr("_"));
      listExpr._subExpressions.add(new IdExpr("extract"));
      listExpr._subExpressions.add(new IdExpr(Integer.toString(high)));
      listExpr._subExpressions.add(new IdExpr(Integer.toString(low)));
      _subExpressions.add(listExpr);
      _subExpressions.add(new IdExpr(var));
   }
   
}
