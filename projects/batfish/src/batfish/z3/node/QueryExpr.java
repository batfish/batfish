package batfish.z3.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import batfish.collections.VarIndexMap;

public class QueryExpr extends Statement implements ComplexExpr {

   private BooleanExpr _subExpression;
   private List<Expr> _subExpressions;

   public QueryExpr(BooleanExpr expr) {
      _subExpression = expr;
      init();
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }
   
   @Override
   public Set<String> getVariables() {
      return _subExpression.getVariables();
   }

   public VarIndexMap getVarIndices(List<String> declaredVars) {
      VarIndexMap absoluteVarIndices = new VarIndexMap();
      VarIndexMap relativeVarIndices = new VarIndexMap();
      Set<String> vars = getVariables();
      for (String var : vars) {
         int index = Collections.binarySearch(declaredVars, var);
         if (index < 0) {
            throw new Error("variable does not exist");
         }
         absoluteVarIndices.put(index, var);
      }
      int i = 0;
      for (String var : absoluteVarIndices.values()) {
         relativeVarIndices.put(i, var);
         i++;
      }
      return relativeVarIndices;
   }

   private void init() {
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr("query"));
      _subExpressions.add(_subExpression);
      _printer = new CollapsedComplexExprPrinter(this);
   }

}
