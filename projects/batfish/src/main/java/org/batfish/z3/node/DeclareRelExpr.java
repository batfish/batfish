package org.batfish.z3.node;

import com.microsoft.z3.BitVecSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.List;

public class DeclareRelExpr extends Statement implements ComplexExpr {

  private String _name;
  private List<Integer> _sizes;
  private List<Expr> _subExpressions;

  public DeclareRelExpr(String name, List<Integer> sizes) {
    _name = name;
    _sizes = sizes;
    _subExpressions = new ArrayList<>();
    _subExpressions.add(new IdExpr("declare-rel"));
    _subExpressions.add(new IdExpr(name));
    ListExpr listExpression = new CollapsedListExpr();
    _subExpressions.add(listExpression);
    for (int size : sizes) {
      listExpression.addSubExpression(new BitVecExpr(size));
    }
    _printer = new CollapsedComplexExprPrinter(this);
  }

  @Override
  public List<Expr> getSubExpressions() {
    return _subExpressions;
  }

  public FuncDecl toFuncDecl(Context ctx) throws Z3Exception {
    List<BitVecSort> argTypes = new ArrayList<>();
    for (int size : _sizes) {
      argTypes.add(ctx.mkBitVecSort(size));
    }
    BitVecSort[] argTypesArray = argTypes.toArray(new BitVecSort[] {});
    FuncDecl output = ctx.mkFuncDecl(_name, argTypesArray, ctx.mkBoolSort());
    return output;
  }
}
