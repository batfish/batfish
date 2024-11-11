package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from destination-address */
public final class FwFromDestinationAddress implements FwFrom {

  private final @Nonnull String _description;
  private final @Nonnull IpWildcard _ipWildcard;

  /**
   * Creates a new {@link FwFromDestinationAddress}, matching the given {@link IpWildcard} and using
   * the given {@code description} for the {@link TraceElement}.
   */
  public FwFromDestinationAddress(@Nonnull IpWildcard ipWildcard, @Nonnull String description) {
    _ipWildcard = ipWildcard;
    _description = description;
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public Field getField() {
    return Field.DESTINATION;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return AclLineMatchExprs.matchDst(_ipWildcard.toIpSpace(), getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched destination-address %s", _description));
  }
}
