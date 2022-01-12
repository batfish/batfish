package org.batfish.datamodel.tracking;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/** An action to take when a {@link TrackMethod} has been triggered (is down). */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface TrackAction extends Serializable {
  void accept(GenericTrackActionVisitor visitor);

  /**
   * Whether the action should be performed when evaluation of the {@link TrackMethod} returns
   * {@code false} rather than {@code true}.
   */
  boolean getNegateTrack();

  String PROP_NEGATE_TRACK = "negateTrack";
}
