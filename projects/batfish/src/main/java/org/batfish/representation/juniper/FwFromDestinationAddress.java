package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from destination-address */
public final class FwFromDestinationAddress implements FwFrom {

  @Nonnull private final String _description;
  @Nonnull private final IpWildcard _ipWildcard;

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
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched destination-address %s", _description));
  }

  @VisibleForTesting
  HeaderSpace toHeaderspace() {
    return HeaderSpace.builder().setDstIps(_ipWildcard.toIpSpace()).build();
  }
}
