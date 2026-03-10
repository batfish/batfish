package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from ttl */
@ParametersAreNonnullByDefault
public class FwFromTtl implements FwFrom {

  private final boolean _except;

  private final @Nonnull SubRange _range;

  public FwFromTtl(SubRange range, boolean except) {
    _range = range;
    _except = except;
  }

  public boolean getExcept() {
    return _except;
  }

  public @Nonnull SubRange getRange() {
    return _range;
  }

  @Override
  public Field getField() {
    return _except ? Field.TTL_EXCEPT : Field.TTL;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    // TODO: support TTL matching in vendor-independent model
    return AclLineMatchExprs.FALSE;
  }
}
