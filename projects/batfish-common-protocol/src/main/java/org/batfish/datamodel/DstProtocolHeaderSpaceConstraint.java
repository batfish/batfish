package org.batfish.datamodel;

import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;

public class DstProtocolHeaderSpaceConstraint implements HeaderSpaceConstraint {
  private final SortedSet<Protocol> _dstProtocols;

  public DstProtocolHeaderSpaceConstraint(SortedSet<Protocol> dstProtocols) {
    _dstProtocols = ImmutableSortedSet.copyOf(dstProtocols);
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitDstProtocolHeaderSpaceConstraint(this);
  }

  public SortedSet<Protocol> getDstProtocols() {
    return _dstProtocols;
  }
}
