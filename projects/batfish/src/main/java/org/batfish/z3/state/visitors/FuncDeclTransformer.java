package org.batfish.z3.state.visitors;

import com.microsoft.z3.BitVecSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import java.util.List;
import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.StateExpr.State;
import org.batfish.z3.expr.TransformationStateExpr;

public class FuncDeclTransformer implements GeneralStateVisitor {

  private final BitVecSort[] _basicStateVariableSorts;

  private final Context _ctx;

  private FuncDecl _funcDecl;

  private String _name;

  public FuncDeclTransformer(Context ctx, List<BitVecSort> basicStateVariableSorts) {
    _ctx = ctx;
    _basicStateVariableSorts = basicStateVariableSorts.stream().toArray(BitVecSort[]::new);
  }

  public FuncDecl toFuncDecl(String name, State state) {
    _name = name;
    state.accept(this);
    return _funcDecl;
  }

  @Override
  public void visitBasicStateExpr(BasicStateExpr.State basicState) {
    _funcDecl = _ctx.mkFuncDecl(_name, _basicStateVariableSorts, _ctx.mkBoolSort());
  }

  @Override
  public void visitTransformationStateExpr(TransformationStateExpr.State transformationState) {
    _funcDecl = _ctx.mkFuncDecl(_name, _basicStateVariableSorts, _ctx.mkBoolSort());
  }
}
