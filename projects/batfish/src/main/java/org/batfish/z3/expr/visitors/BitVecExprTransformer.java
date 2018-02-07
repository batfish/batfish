package org.batfish.z3.expr.visitors;

import com.microsoft.z3.BitVecExpr;
import org.batfish.common.BatfishException;
import org.batfish.z3.HeaderField;
import org.batfish.z3.NodProgram;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.IntExprVisitor;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.VarIntExpr;

public class BitVecExprTransformer implements IntExprVisitor {

  public static BitVecExpr toBitVecExpr(IntExpr intExpr, NodProgram nodProgram) {
    BitVecExprTransformer bitVecExprTransformer = new BitVecExprTransformer(nodProgram);
    intExpr.accept(bitVecExprTransformer);
    return bitVecExprTransformer._bitVecExpr;
  }

  private BitVecExpr _bitVecExpr;

  private final NodProgram _nodProgram;

  private BitVecExprTransformer(NodProgram nodProgram) {
    _nodProgram = nodProgram;
  }

  @Override
  public void visitExtractExpr(ExtractExpr extractExpr) {
    _bitVecExpr =
        _nodProgram
            .getContext()
            .mkExtract(
                extractExpr.getHigh(),
                extractExpr.getLow(),
                toBitVecExpr(extractExpr.getVar(), _nodProgram));
  }

  @Override
  public void visitLitIntExpr(LitIntExpr litIntExpr) {
    _bitVecExpr = _nodProgram.getContext().mkBV(litIntExpr.getNum(), litIntExpr.getBits());
  }

  @Override
  public void visitVarIntExpr(VarIntExpr varIntExpr) {
    HeaderField headerField = varIntExpr.getHeaderField();
    _bitVecExpr = _nodProgram.getVariables().get(headerField);
    if (_bitVecExpr == null) {
      throw new BatfishException("nodProgram missing mapping for variable: '" + headerField + "'");
    }
  }
}
