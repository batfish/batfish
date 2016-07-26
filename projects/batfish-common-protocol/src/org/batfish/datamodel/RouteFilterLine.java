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
      if (_action != other._action) {
         return false;
      }
      if (!_lengthRange.equals(other._lengthRange)) {
         return false;
      }
      if (!_prefix.equals(other._prefix)) {
         return false;
      }
      return true;
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
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _action.hashCode();
      result = prime * result + _lengthRange.hashCode();
      result = prime * result + _prefix.hashCode();
      return result;
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
