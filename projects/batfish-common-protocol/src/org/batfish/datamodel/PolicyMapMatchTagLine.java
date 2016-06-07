package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchTagLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private static final String TAGS_VAR = "tags";

   private final Set<Integer> _tags;

   @JsonCreator
   public PolicyMapMatchTagLine(@JsonProperty(TAGS_VAR) Set<Integer> tags) {
      _tags = tags;
   }

   @JsonProperty(TAGS_VAR)
   public Set<Integer> getTags() {
      return _tags;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.TAG;
   }

}
