package org.batfish.datamodel;

import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;

public class DstPortHeaderSpaceConstraint implements HeaderSpaceConstraint {
  private final SortedSet<SubRange> _dstPorts;

  public DstPortHeaderSpaceConstraint(SortedSet<SubRange> dstPorts) {
    _dstPorts = ImmutableSortedSet.copyOf(dstPorts);
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitDstPortHeaderSpaceConstraint(this);
  }

  public SortedSet<SubRange> getDstPorts() {
    return _dstPorts;
  }
}
