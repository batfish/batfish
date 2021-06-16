package org.batfish.representation.cisco_nxos;

import com.google.common.collect.Range;
import java.io.Serializable;
import org.batfish.datamodel.IntegerSpace;

/** Represents object tracking configuration. */
public interface Track extends Serializable {
  IntegerSpace TRACK_OBJECT_ID_RANGE = IntegerSpace.of(Range.closed(1, 500));
}
