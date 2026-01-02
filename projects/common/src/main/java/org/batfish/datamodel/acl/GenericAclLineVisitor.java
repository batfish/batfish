package org.batfish.datamodel.acl;

import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;

/** Visitor for {@link AclLine} */
public interface GenericAclLineVisitor<R> {

  default R visit(AclLine line) {
    return line.accept(this);
  }

  R visitAclAclLine(AclAclLine aclAclLine);

  R visitExprAclLine(ExprAclLine exprAclLine);
}
