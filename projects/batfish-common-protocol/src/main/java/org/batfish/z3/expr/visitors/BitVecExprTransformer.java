package org.batfish.z3.expr.visitors;

import com.microsoft.z3.BitVecExpr;
import org.batfish.common.BatfishException;
import org.batfish.z3.NodContext;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.VarIntExpr;

public class BitVecExprTransformer implements GenericIntExprVisitor<BitVecExpr> {

  public static BitVecExpr toBitVecExpr(IntExpr intExpr, NodContext nodContext) {
    BitVecExprTransformer bitVecExprTransformer = new BitVecExprTransformer(nodContext);
    return intExpr.accept(bitVecExprTransformer);
  }

  private final NodContext _nodContext;

  private BitVecExprTransformer(NodContext nodContext) {
    _nodContext = nodContext;
  }

  @Override
  public BitVecExpr castToGenericIntExprVisitorReturnType(Object o) {
    return (BitVecExpr) o;
  }

  @Override
  public BitVecExpr visitExtractExpr(ExtractExpr extractExpr) {
    return _nodContext
        .getContext()
        .mkExtract(
            extractExpr.getHigh(),
            extractExpr.getLow(),
            toBitVecExpr(extractExpr.getVar(), _nodContext));
  }

  @Override
  public BitVecExpr visitLitIntExpr(LitIntExpr litIntExpr) {
    return _nodContext.getContext().mkBV(litIntExpr.getNum(), litIntExpr.getBits());
  }

  @Override
  public BitVecExpr visitTransformedVarIntExpr(TransformedVarIntExpr transformedVarIntExpr) {
    String fieldName = transformedVarIntExpr.getField().getName();
    BitVecExpr ret = _nodContext.getTransformedVariables().get(fieldName);
    if (ret == null) {
      throw new BatfishException(
          "nodContext missing mapping for transformed variable: '" + fieldName + "'");
    }
    return ret;
  }

  @Override
  public BitVecExpr visitVarIntExpr(VarIntExpr varIntExpr) {
    String fieldName = varIntExpr.getField().getName();
    BitVecExpr ret = _nodContext.getVariables().get(fieldName);
    if (ret == null) {
      throw new BatfishException("nodContext missing mapping for variable: '" + fieldName + "'");
    }
    return ret;
  }
}
