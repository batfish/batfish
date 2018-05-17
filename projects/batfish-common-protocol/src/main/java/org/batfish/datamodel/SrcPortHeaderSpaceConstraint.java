package org.batfish.datamodel;

import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;

public class SrcPortHeaderSpaceConstraint implements HeaderSpaceConstraint {
  private final SortedSet<SubRange> _srcPorts;

  public SrcPortHeaderSpaceConstraint(SortedSet<SubRange> srcPorts) {
    _srcPorts = ImmutableSortedSet.copyOf(srcPorts);
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitSrcPortHeaderSpaceConstraint(this);
  }

  public SortedSet<SubRange> getSrcPorts() {
    return _srcPorts;
  }
}
