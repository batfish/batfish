package org.batfish.datamodel.visitors;

import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/** Converts an {@link AclLine} to the {@link HeaderSpace} matching that line. */
public class HeaderSpaceConverter implements GenericAclLineVisitor<HeaderSpace> {

  private static final HeaderSpaceConverter INSTANCE = new HeaderSpaceConverter();
  private static final AclLineMatchExprToHeaderSpaceConverter MATCH_EXPR_CONVERTER =
      new AclLineMatchExprToHeaderSpaceConverter();

  public static HeaderSpace convert(AclLine line) {
    return line.accept(INSTANCE);
  }

  private HeaderSpaceConverter() {}

  @Override
  public HeaderSpace visitExprAclLine(ExprAclLine exprAclLine) {
    return MATCH_EXPR_CONVERTER.visit(exprAclLine.getMatchCondition());
  }

  private static class AclLineMatchExprToHeaderSpaceConverter
      implements GenericAclLineMatchExprVisitor<HeaderSpace> {

    @Override
    public HeaderSpace visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      throw new UnsupportedOperationException("no implementation for generated method");
    }

    @Override
    public HeaderSpace visitFalseExpr(FalseExpr falseExpr) {
      return HeaderSpace.builder().setNegate(true).build();
    }

    @Override
    public HeaderSpace visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return matchHeaderSpace.getHeaderspace();
    }

    @Override
    public HeaderSpace visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      throw new UnsupportedOperationException("no implementation for generated method");
    }

    @Override
    public HeaderSpace visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      throw new UnsupportedOperationException("no implementation for generated method");
    }

    @Override
    public HeaderSpace visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      throw new UnsupportedOperationException("no implementation for generated method");
    }

    @Override
    public HeaderSpace visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      throw new UnsupportedOperationException("no implementation for generated method");
    }

    @Override
    public HeaderSpace visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      throw new UnsupportedOperationException("no implementation for generated method");
    }

    @Override
    public HeaderSpace visitTrueExpr(TrueExpr trueExpr) {
      return HeaderSpace.builder().build();
    }
  }
}
