package org.batfish.vendor.arista.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/** Arista does not let you filter on services in Standard ACL lines. */
public class StandardAccessListServiceSpecifier implements AccessListServiceSpecifier {
  public static final StandardAccessListServiceSpecifier INSTANCE =
      new StandardAccessListServiceSpecifier();

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(HeaderSpace.builder().build());
  }

  private StandardAccessListServiceSpecifier() {} // prevent instantiation
}
