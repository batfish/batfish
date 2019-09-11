package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/** A way to render a {@link org.batfish.datamodel.bgp.community.Community} as a string. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface CommunityRendering extends Serializable {

  <T> T accept(CommunityRenderingVisitor<T> visitor);
}
