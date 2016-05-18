package org.batfish.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NodeType {
   BGP("bgp"),
   ISIS("isis"),
   OSPF("ospf");

   private final static Map<String, NodeType> _map = buildMap();

   private static Map<String, NodeType> buildMap() {
      Map<String, NodeType> map = new HashMap<String, NodeType>();
      for (NodeType value : NodeType.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static NodeType fromName(String name) {
      NodeType instance = _map.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException("Not a valid NodeType: \"" + name + "\"");
      }
      return instance;
   }

   private final String _name;

   private NodeType(String name) {
      _name = name;
   }

   public String nodeTypeName() {
      return _name;
   }
}
