package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface ExtendedAccessListServiceSpecifier extends Serializable {

  @Nonnull
  AclLineMatchExpr toAclLineMatchExpr();
}
