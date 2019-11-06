package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface AccessListServiceSpecifier extends Serializable {

  @Nonnull
  AclLineMatchExpr toAclLineMatchExpr(Map<String, ObjectGroup> objectGroups);
}
