package org.batfish.datamodel.tracking;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.Configuration;

/**
 * An object "monitor" which can be used to trigger one or more {@link TrackAction} when the tracked
 * object state changes. Can track things like interface active state or ICMP reachability.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface TrackMethod extends Serializable {

  /** Evaluate if this track method is triggered on the specified {@link Configuration}. */
  <R> R accept(GenericTrackMethodVisitor<R> visitor);
}
