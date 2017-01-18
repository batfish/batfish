package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;

import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class CommunitySetExpr implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract CommunitySet communities(Environment environment);

   public abstract CommunitySet communities(Environment environment,
         CommunitySet communityCandidates);

   @Override
   public abstract boolean equals(Object obj);

   @Override
   public abstract int hashCode();

   public abstract boolean matchSingleCommunity(Environment environment,
         CommunitySet communities);

}
