package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface AccessListServiceSpecifier extends Serializable {

  @Nonnull
  AclLineMatchExpr toAclLineMatchExpr();
}
