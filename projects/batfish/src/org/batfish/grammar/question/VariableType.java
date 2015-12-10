package org.batfish.grammar.question;

import org.batfish.common.BatfishException;

public enum VariableType {
   ACTION,
   BGP_NEIGHBOR,
   BOOLEAN,
   INT,
   INTERFACE,
   IP,
   IPSEC_VPN,
   NODE,
   POLICY_MAP,
   POLICY_MAP_CLAUSE,
   PREFIX,
   PREFIX_SPACE,
   RANGE,
   REGEX,
   ROUTE_FILTER,
   ROUTE_FILTER_LINE,
   SET_INT,
   SET_IP,
   SET_PREFIX,
   SET_ROUTE_FILTER,
   SET_STRING,
   STATIC_ROUTE,
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

      case "bgp_neighbor":
         return BGP_NEIGHBOR;

      case "boolean":
         return BOOLEAN;

      case "int":
         return INT;

      case "interface":
         return INTERFACE;

      case "ip":
         return IP;

      case "node":
         return NODE;

      case "policy_map":
         return POLICY_MAP;

      case "policy_map_clause":
         return POLICY_MAP_CLAUSE;

      case "prefix":
         return PREFIX;

      case "prefix_space":
         return PREFIX_SPACE;

      case "range":
         return RANGE;

      case "regex":
         return REGEX;

      case "route_filter":
         return ROUTE_FILTER;

      case "route_filter_line":
         return ROUTE_FILTER_LINE;

      case "static_route":
         return STATIC_ROUTE;

      case "string":
         return STRING;

      default:
         throw new BatfishException("Invalid variable type string: \""
               + typeStr + "\"");
      }

   }
}
