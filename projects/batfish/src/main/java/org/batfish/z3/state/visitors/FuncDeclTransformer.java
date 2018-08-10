package org.batfish.z3.state.visitors;

import com.microsoft.z3.BitVecSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import java.util.List;
import org.batfish.z3.expr.StateExpr.State;

public class FuncDeclTransformer {

  private final BitVecSort[] _basicStateVariableSorts;

  private final Context _ctx;

  private String _name;

  public FuncDeclTransformer(Context ctx, List<BitVecSort> basicStateVariableSorts) {
    _ctx = ctx;
    _basicStateVariableSorts = basicStateVariableSorts.toArray(new BitVecSort[0]);
  }

  public FuncDecl toFuncDecl(String name, State state) {
    _name = name;
    return _ctx.mkFuncDecl(_name, _basicStateVariableSorts, _ctx.mkBoolSort());
  }
}
