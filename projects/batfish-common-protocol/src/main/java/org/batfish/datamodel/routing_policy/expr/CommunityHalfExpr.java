package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface CommunityHalfExpr extends Serializable {

  boolean dynamicMatchCommunity();

  boolean matches(int communityHalf);
}
