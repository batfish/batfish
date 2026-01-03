package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/** A way to render a community as a string. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface CommunityRendering extends Serializable {

  <T, U> T accept(CommunityRenderingVisitor<T, U> visitor, U arg);
}
