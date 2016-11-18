package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;

import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface CommunitySetExpr extends Serializable {

   CommunitySet communities(Environment environment);

   boolean matchSingleCommunity(Environment environment,
         CommunitySet communities);

}
