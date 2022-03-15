package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from packet-length */
@ParametersAreNonnullByDefault
public class FwFromPacketLength implements FwFrom {

  private final boolean _except;

  private final @Nonnull SubRange _range;

  public FwFromPacketLength(SubRange range, boolean except) {
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
    return _except ? Field.PACKET_LENGTH_EXCEPT : Field.PACKET_LENGTH;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private HeaderSpace toHeaderspace() {
    return _except
        ? HeaderSpace.builder().setNotPacketLengths(ImmutableList.of(_range)).build()
        : HeaderSpace.builder().setPacketLengths(ImmutableList.of(_range)).build();
  }

  private TraceElement getTraceElement() {
    String rangeString =
        _range.getStart() == _range.getEnd()
            ? String.valueOf(_range.getStart())
            : String.format("%d-%d", _range.getStart(), _range.getEnd());

    return _except
        ? TraceElement.of(String.format("Matched packet-length %s except", rangeString))
        : TraceElement.of(String.format("Matched packet-length %s", rangeString));
  }
}
