package org.batfish.datamodel.tracking;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.Configuration;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface TrackMethod extends Serializable {

  /** Evaluate if this track method is triggered on the specified {@link Configuration}. */
  boolean evaluate(Configuration configuration);
}
