package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SubRange;

/** Represents an individual line of a {@link PortObjectGroupPortSpec} */
@ParametersAreNonnullByDefault
public final class PortObjectGroupLine implements Serializable {

  private @Nonnull final List<SubRange> _ranges;

  public PortObjectGroupLine(Iterable<SubRange> ranges) {
    _ranges = ImmutableList.copyOf(ranges);
  }

  public @Nonnull List<SubRange> getRanges() {
    return _ranges;
  }
}
