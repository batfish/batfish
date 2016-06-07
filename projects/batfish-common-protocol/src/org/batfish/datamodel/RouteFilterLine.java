package org.batfish.datamodel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RouteFilterLine implements Serializable {

   private static final String ACTION_VAR = "action";

   private static final String LENGTH_RANGE_VAR = "lengthRange";

   private static final String PREFIX_VAR = "prefix";

   private static final long serialVersionUID = 1L;

   private final LineAction _action;

   private final SubRange _lengthRange;

   private final Prefix _prefix;

   @JsonCreator
   public RouteFilterLine(@JsonProperty(ACTION_VAR) LineAction action,
         @JsonProperty(PREFIX_VAR) Prefix prefix,
         @JsonProperty(LENGTH_RANGE_VAR) SubRange lengthRange) {
      _action = action;
      _prefix = prefix;
      _lengthRange = lengthRange;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      RouteFilterLine other = (RouteFilterLine) obj;
      if (other.toString().equals(this.toString())) {
         return true;
      }
      return false;
   }

   @JsonProperty(ACTION_VAR)
   public LineAction getAction() {
      return _action;
   }

   @JsonProperty(LENGTH_RANGE_VAR)
   public SubRange getLengthRange() {
      return _lengthRange;
   }

   @JsonProperty(PREFIX_VAR)
   public Prefix getPrefix() {
      return _prefix;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{ ");
      sb.append("Action=" + _action + " ");
      sb.append("Prefix=" + _prefix + " ");
      sb.append("LengthRange=" + _lengthRange + " ");
      sb.append("}");
      return sb.toString();
   }
}
