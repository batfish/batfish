package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from packet-length */
public class FwFromPacketLength implements FwFrom {

  private boolean _except;

  private final List<SubRange> _range;

  public FwFromPacketLength(List<SubRange> range, boolean except) {
    _range = range;
    _except = except;
  }

  @Override
  public Field getField() {
    return _except ? Field.FRAGMENT_OFFSET_EXCEPT : Field.PACKET_LENGTH;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private HeaderSpace toHeaderspace() {
    return _except
        ? HeaderSpace.builder().setNotPacketLengths(_range).build()
        : HeaderSpace.builder().setPacketLengths(_range).build();
  }

  private TraceElement getTraceElement() {
    String rangeString =
        String.join(
            " ",
            _range.stream()
                .map(
                    r ->
                        r.getStart() == r.getEnd()
                            ? String.valueOf(r.getStart())
                            : String.format("%d-%d", r.getStart(), r.getEnd()))
                .collect(ImmutableList.toImmutableList()));

    return _except
        ? TraceElement.of(String.format("Matched packet-length %s except", rangeString))
        : TraceElement.of(String.format("Matched packet-length %s", rangeString));
  }
}
