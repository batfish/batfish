package org.batfish.grammar.question;

import org.batfish.common.BatfishException;

public enum VariableType {
   ACTION,
   INT,
   IP,
   PREFIX,
   RANGE,
   REGEX,
   ROUTE_FILTER,
   SET_INT,
   SET_IP,
   SET_PREFIX,
   SET_ROUTE_FILTER,
   SET_STRING,
   STRING;

   public static VariableType fromString(String typeStr) {
      switch (typeStr) {
      case "set<route_filter>":
         return SET_ROUTE_FILTER;
      case "set<ip>":
         return SET_IP;

      case "set<prefix>":
         return SET_PREFIX;

      case "set<string>":
         return SET_STRING;

      case "set<int>":
         return SET_INT;

      case "action":
         return ACTION;

      case "int":
         return INT;

      case "ip":
         return IP;

      case "prefix":
         return PREFIX;

      case "range":
         return RANGE;

      case "regex":
         return REGEX;

      case "route_filter":
         return ROUTE_FILTER;

      default:
         throw new BatfishException("Invalid variable type string: \""
               + typeStr + "\"");
      }

   }
}
