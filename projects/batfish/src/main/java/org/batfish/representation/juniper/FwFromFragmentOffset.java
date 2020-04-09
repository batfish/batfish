package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from fragment-offset with/without except */
public class FwFromFragmentOffset implements FwFrom {

  private boolean _except;

  private SubRange _offsetRange;

  public FwFromFragmentOffset(SubRange offsetRange, boolean except) {
    _offsetRange = offsetRange;
    _except = except;
  }

  @Override
  public Field getField() {
    return _except ? Field.FRAGMENT_OFFSET_EXCEPT : Field.FRAGMENT_OFFSET;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  @VisibleForTesting
  HeaderSpace toHeaderspace() {
    return _except
        ? HeaderSpace.builder().setNotFragmentOffsets(ImmutableSet.of(_offsetRange)).build()
        : HeaderSpace.builder().setFragmentOffsets(ImmutableSet.of(_offsetRange)).build();
  }

  private TraceElement getTraceElement() {
    return _except
        ? TraceElement.of(String.format("Matched fragment-offset %s except", _offsetRange))
        : TraceElement.of(String.format("Matched fragment-offset %s", _offsetRange));
  }
}
