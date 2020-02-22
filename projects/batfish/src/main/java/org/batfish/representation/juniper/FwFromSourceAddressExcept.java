package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from source-address except */
public final class FwFromSourceAddressExcept implements FwFrom {

  @Nonnull private final String _description;
  @Nonnull private final IpWildcard _ipWildcard;

  /**
   * Creates a new {@link FwFromSourceAddressExcept}, matching everything except the given {@link
   * IpWildcard} and using the given {@code description} for the {@link TraceElement}.
   */
  public FwFromSourceAddressExcept(@Nonnull IpWildcard ipWildcard, @Nonnull String description) {
    _description = description;
    _ipWildcard = ipWildcard;
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public Field getField() {
    return Field.SOURCE_EXCEPT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched source-address %s except", _description));
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder().setNotSrcIps(_ipWildcard.toIpSpace()).build();
  }
}
