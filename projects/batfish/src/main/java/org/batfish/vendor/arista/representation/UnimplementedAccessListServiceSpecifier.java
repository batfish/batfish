package org.batfish.vendor.arista.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;

/**
 * An {@link AccessListServiceSpecifier} that is used for unimplemented features that Cisco-like
 * configurations may match against. Can never match any line.
 */
public final class UnimplementedAccessListServiceSpecifier implements AccessListServiceSpecifier {

  public static final UnimplementedAccessListServiceSpecifier INSTANCE =
      new UnimplementedAccessListServiceSpecifier();

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr() {
    return FalseExpr.INSTANCE;
  }

  private UnimplementedAccessListServiceSpecifier() {} // prevent instantiation
}
