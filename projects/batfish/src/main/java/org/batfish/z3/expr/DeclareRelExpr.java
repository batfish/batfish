package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import com.microsoft.z3.BitVecSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Z3Exception;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.batfish.z3.HeaderField;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class DeclareRelExpr extends Statement {

  public static final List<BitVecExpr> ARGUMENTS =
      Arrays.stream(HeaderField.values())
          .map(HeaderField::getSize)
          .map(BitVecExpr::new)
          .collect(ImmutableList.toImmutableList());

  private String _name;

  public DeclareRelExpr(String name) {
    _name = name;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitDeclareRelExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_name, ((DeclareRelExpr) e)._name);
  }

  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }

  public FuncDecl toFuncDecl(Context ctx) throws Z3Exception {
    BitVecSort[] argTypesArray =
        ARGUMENTS
            .stream()
            .map(BitVecExpr::getSize)
            .map(ctx::mkBitVecSort)
            .toArray(size -> new BitVecSort[size]);
    FuncDecl output = ctx.mkFuncDecl(_name, argTypesArray, ctx.mkBoolSort());
    return output;
  }
}
