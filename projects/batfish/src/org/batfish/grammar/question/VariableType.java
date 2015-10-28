package org.batfish.grammar.question;

import org.batfish.common.BatfishException;

public enum VariableType {
   INT,
   IP,
   ROUTE_FILTER,
   SET_INT,
   SET_IP,
   SET_ROUTE_FILTER,
   SET_STRING,
   STRING;

   public static VariableType fromString(String typeStr) {
      switch (typeStr) {
      case "set<route_filter>":
         return SET_ROUTE_FILTER;
      case "set<ip>":
         return SET_IP;

      case "set<string>":
         return SET_STRING;

      case "set<int>":
         return SET_INT;

      case "int":
         return INT;

      case "ip":
         return IP;

      case "route_filter":
         return ROUTE_FILTER;

      default:
         throw new BatfishException("Invalid variable type string: \""
               + typeStr + "\"");
      }

   }
}
