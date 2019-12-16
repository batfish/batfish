package org.batfish.datamodel.acl;

import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;

/** Visitor for {@link ExprAclLine} */
public interface GenericAclLineVisitor<R> {

  default R visit(AclLine line) {
    return line.accept(this);
  }

  R visitExprAclLine(ExprAclLine exprAclLine);
}
