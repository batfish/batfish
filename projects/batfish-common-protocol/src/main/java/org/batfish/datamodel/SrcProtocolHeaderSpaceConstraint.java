package org.batfish.datamodel;

import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;

public class SrcProtocolHeaderSpaceConstraint implements HeaderSpaceConstraint {
  private final SortedSet<Protocol> _srcProtocols;

  public SrcProtocolHeaderSpaceConstraint(SortedSet<Protocol> srcProtocols) {
    _srcProtocols = ImmutableSortedSet.copyOf(srcProtocols);
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitSrcProtocolHeaderSpaceConstraint(this);
  }

  public SortedSet<Protocol> getSrcProtocols() {
    return _srcProtocols;
  }
}
