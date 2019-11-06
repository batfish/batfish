package org.batfish.representation.cisco_xr;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;

/**
 * An {@link AccessListServiceSpecifier} that is used for unimplemented features that CiscoXr-like
 * configurations may match against. Can never match any line.
 */
public final class UnimplementedAccessListServiceSpecifier implements AccessListServiceSpecifier {

  public static final UnimplementedAccessListServiceSpecifier INSTANCE =
      new UnimplementedAccessListServiceSpecifier();

  @Nonnull
  @Override
  public AclLineMatchExpr toAclLineMatchExpr(Map<String, ObjectGroup> objectGroups) {
    return FalseExpr.INSTANCE;
  }

  private UnimplementedAccessListServiceSpecifier() {} // prevent instantiation
}
