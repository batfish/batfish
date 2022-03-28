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

/** Class for firewall filter from icmp-code-except */
@ParametersAreNonnullByDefault
public class FwFromIcmpCodeExcept implements FwFrom {

  private final @Nonnull SubRange _icmpCodeRange;

  public FwFromIcmpCodeExcept(@Nonnull SubRange icmpCodeRange) {
    _icmpCodeRange = icmpCodeRange;
  }

  public @Nonnull SubRange getIcmpCodeRange() {
    return _icmpCodeRange;
  }

  @Override
  public Field getField() {
    return Field.ICMP_CODE_EXCEPT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder()
        .setIpProtocols(IpProtocol.ICMP)
        .setNotIcmpCodes(ImmutableList.of(_icmpCodeRange))
        .build();
  }

  private TraceElement getTraceElement() {
    return _icmpCodeRange.getStart() == _icmpCodeRange.getEnd()
        ? TraceElement.of(String.format("Matched icmp-code-except %d", _icmpCodeRange.getStart()))
        : TraceElement.of(
            String.format(
                "Matched icmp-code-except %d-%d",
                _icmpCodeRange.getStart(), _icmpCodeRange.getEnd()));
  }
}
