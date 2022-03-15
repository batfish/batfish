package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from icmp-type-except */
@ParametersAreNonnullByDefault
public class FwFromIcmpTypeExcept implements FwFrom {

  private final @Nonnull SubRange _icmpTypeRange;

  public FwFromIcmpTypeExcept(@Nonnull SubRange icmpTypeRange) {
    _icmpTypeRange = icmpTypeRange;
  }

  public @Nonnull SubRange getIcmpTypeRange() {
    return _icmpTypeRange;
  }

  @Override
  public Field getField() {
    return Field.ICMP_TYPE_EXCEPT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder()
        .setIpProtocols(IpProtocol.ICMP)
        .setNotIcmpTypes(ImmutableList.of(_icmpTypeRange))
        .build();
  }

  private TraceElement getTraceElement() {
    return _icmpTypeRange.getStart() == _icmpTypeRange.getEnd()
        ? TraceElement.of(String.format("Matched icmp-type-except %d", _icmpTypeRange.getStart()))
        : TraceElement.of(
            String.format(
                "Matched icmp-type-except %d-%d",
                _icmpTypeRange.getStart(), _icmpTypeRange.getEnd()));
  }
}
