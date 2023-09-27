package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from address */
public final class FwFromAddress implements FwFrom {

  private final @Nonnull String _description;
  private final @Nonnull IpWildcard _ipWildcard;

  /**
   * Creates a new {@link FwFromAddress}, matching the given {@link IpWildcard} and using the given
   * {@code description} for the {@link TraceElement}.
   */
  public FwFromAddress(@Nonnull IpWildcard ipWildcard, @Nonnull String description) {
    _ipWildcard = ipWildcard;
    _description = description;
  }

  public @Nonnull IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public Field getField() {
    return Field.ADDRESS;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    AclLineMatchExpr matchSrc =
        matchSrc(
            _ipWildcard.toIpSpace(),
            TraceElement.of(String.format("Matched source-address %s", _description)));
    AclLineMatchExpr matchDst =
        matchDst(
            _ipWildcard.toIpSpace(),
            TraceElement.of(String.format("Matched destination-address %s", _description)));
    return or(String.format("Matched address %s", _description), matchSrc, matchDst);
  }
}
