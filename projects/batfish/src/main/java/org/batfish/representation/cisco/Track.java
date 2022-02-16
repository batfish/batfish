package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Object tracking configuration. */
@ParametersAreNonnullByDefault
public interface Track extends Serializable {

  <T> T accept(TrackVisitor<T> visitor);
}
