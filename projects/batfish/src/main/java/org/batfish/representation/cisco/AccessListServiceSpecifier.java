package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface AccessListServiceSpecifier extends Serializable {

  @Nonnull
  AclLineMatchExpr toAclLineMatchExpr();
}
