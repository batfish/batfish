package org.batfish.common.datamodel.questions;

import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

public enum VariableType {
   ACTION("action"),
   BGP_ADVERTISEMENT("bgp_advertisement"),
   BGP_NEIGHBOR("bgp_neighbor"),
   BOOLEAN("boolean"),
   GENERATED_ROUTE("generated_route"),
   INT("int"),
   INTERFACE("interface"),
   IP("ip"),
   IPSEC_VPN("ipsec_vpn"),
   MAP("map"),
   NEIGHBOR_TYPE("neighbor_type"),
   NODE("node"),
   NODE_TYPE("node_type"),
   POLICY_MAP("policy_map"),
   POLICY_MAP_CLAUSE("policy_map_clause"),
   PREFIX("prefix"),
   PREFIX_SPACE("prefix_space"),
   PROTOCOL("protocol"),
   RANGE("range"),
   REGEX("regex"),
   ROUTE("route"),
   ROUTE_FILTER("route_filter"),
   ROUTE_FILTER_LINE("route_filter_line"),
   SET_BGP_ADVERTISEMENT("set<bgp_advertisement>"),
   SET_BGP_NEIGHBOR("set<bgp_neighbor>"),
   SET_INT("set<int>"),
   SET_INTERFACE("set<integer>"),
   SET_IP("set<ip>"),
   SET_IPSEC_VPN("set<ipsec_vpn>"),
   SET_NODE("set<node>"),
   SET_POLICY_MAP("set<policy_map>"),
   SET_POLICY_MAP_CLAUSE("set<policy_map_clause>"),
   SET_PREFIX("set<prefix>"),
   SET_PREFIX_SPACE("set<prefix_space>"),
   SET_ROUTE("set<route>"),
   SET_ROUTE_FILTER("set<route_filter>"),
   SET_ROUTE_FILTER_LINE("set<route_filter_line>"),
   SET_STATIC_ROUTE("set<static_route>"),
   SET_STRING("set<string>"),
   STATIC_ROUTE("static_route"),
   STRING("string");

   private static final Map<String, VariableType> _nameMap = initNameMap();

   public static VariableType fromString(String typeStr) {
      VariableType value = _nameMap.get(typeStr);
      if (value == null) {
         throw new BatfishException("invalid "
               + VariableType.class.getSimpleName() + " name");
      }
      return value;
   }

   private static Map<String, VariableType> initNameMap() {
      Map<String, VariableType> nameMap = new HashMap<String, VariableType>();
      for (VariableType type : values()) {
         nameMap.put(type._name, type);
      }
      return nameMap;
   }

   private final String _name;

   private VariableType(String name) {
      _name = name;
   }

   public VariableType elementType() {
      String prefix = "set<";
      if (!_name.startsWith(prefix)) {
         throw new BatfishException("Not a set type: " + this);
      }
      String elementTypeName = _name.substring(prefix.length(),
            _name.length() - 1);
      return fromString(elementTypeName);
   }

   public String getName() {
      return _name;
   }
}
