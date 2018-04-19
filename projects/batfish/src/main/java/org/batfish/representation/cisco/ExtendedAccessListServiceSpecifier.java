package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface ExtendedAccessListServiceSpecifier {

  @Nonnull
  AclLineMatchExpr toAclLineMatchExpr();
}
