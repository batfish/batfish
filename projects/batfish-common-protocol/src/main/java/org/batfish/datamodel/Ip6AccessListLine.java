package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("A line in an Ip6AccessList")
public final class Ip6AccessListLine extends Header6Space {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private String _name;

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      Ip6AccessListLine other = (Ip6AccessListLine) obj;
      if (!super.equals(obj)) {
         return false;
      }
      if (_action != other._action) {
         return false;
      }
      return true;
   }

   @JsonPropertyDescription("The action the underlying access-list will take when this line matches an IPV6 packet.")
   public LineAction getAction() {
      return _action;
   }

   @JsonSchemaDescription("The name of this line in the list")
   public String getName() {
      return _name;
   }

   @Override
   public int hashCode() {
      // TODO: implement better hashcode
      return 0;
   }

   public void setAction(LineAction action) {
      _action = action;
   }

   public void setName(String name) {
      _name = name;
   }

   @Override
   public String toString() {
      return "[Action:" + _action + ", Base: " + super.toString() + "]";
   }

}
