package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;
import java.util.SortedSet;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class CommunitySetExpr implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract SortedSet<Long> communities(Environment environment);

   public abstract SortedSet<Long> communities(Environment environment,
         SortedSet<Long> communityCandidates);

   @Override
   public abstract boolean equals(Object obj);

   @Override
   public abstract int hashCode();

   public abstract boolean matchSingleCommunity(Environment environment,
         SortedSet<Long> communities);

}
