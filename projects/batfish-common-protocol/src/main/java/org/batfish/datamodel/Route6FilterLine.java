package org.batfish.datamodel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("A line in an Route6FilterList")
public class Route6FilterLine implements Serializable {

   private static final String ACTION_VAR = "action";

   private static final String LENGTH_RANGE_VAR = "lengthRange";

   private static final String PREFIX_VAR = "prefix";

   private static final long serialVersionUID = 1L;

   private final LineAction _action;

   private final SubRange _lengthRange;

   private final Prefix6 _prefix;

   @JsonCreator
   public Route6FilterLine(@JsonProperty(ACTION_VAR) LineAction action,
         @JsonProperty(PREFIX_VAR) Prefix6 prefix,
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
      Route6FilterLine other = (Route6FilterLine) obj;
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
   @JsonPropertyDescription("The action the underlying access-list will take when this line matches an IPV6 route.")
   public LineAction getAction() {
      return _action;
   }

   @JsonProperty(LENGTH_RANGE_VAR)
   @JsonPropertyDescription("The range of acceptable prefix-lengths for a route.")
   public SubRange getLengthRange() {
      return _lengthRange;
   }

   @JsonProperty(PREFIX_VAR)
   @JsonPropertyDescription("The bits against which to compare a route's prefix. The length of this prefix is used to determine how many leading bits must match.")
   public Prefix6 getPrefix() {
      return _prefix;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _action.ordinal();
      result = prime * result + _lengthRange.hashCode();
      result = prime * result + _prefix.hashCode();
      return result;
   }

   public String toCompactString() {
      StringBuilder sb = new StringBuilder();
      sb.append(_action.toString() + " ");
      sb.append(_prefix.toString() + " ");
      sb.append(_lengthRange.toString() + " ");
      return sb.toString();

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
