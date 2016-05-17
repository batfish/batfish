package org.batfish.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NamedStructType {
   ANY("any"),
   ACL("acl"),
   PREFIX_LIST("prefix_list"),
   ROUTE_POLICY("route_policy");

   private final static Map<String, NamedStructType> _map = buildMap();

   private static Map<String, NamedStructType> buildMap() {
      Map<String, NamedStructType> map = new HashMap<String, NamedStructType>();
      for (NamedStructType value : NamedStructType.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static NamedStructType fromName(String name) {
      NamedStructType instance = _map.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException("Not a valid NamedStructType: \"" + name
               + "\"");
      }
      return instance;
   }

   private final String _name;

   private NamedStructType(String name) {
      _name = name;
   }

   public String namedStructTypeName() {
      return _name;
   }
}
