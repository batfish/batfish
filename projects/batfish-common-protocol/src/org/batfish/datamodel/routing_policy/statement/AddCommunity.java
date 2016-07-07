package org.batfish.datamodel.routing_policy.statement;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;

public class AddCommunity extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedSet<Long> _communities;

   @JsonCreator
   public AddCommunity() {
      _communities = new TreeSet<Long>();
   }

   public AddCommunity(Set<Long> communities) {
      this();
      _communities.addAll(communities);
   }

   public SortedSet<Long> getCommunities() {
      return _communities;
   }

   public void setCommunities(SortedSet<Long> communities) {
      _communities = communities;
   }

}
