package org.batfish.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OriginType {
   EGP("egp"),
   IGP("igp"),
   INCOMPLETE("incomplete");

   private final static Map<String, OriginType> _map = buildMap();

   private static Map<String, OriginType> buildMap() {
      Map<String, OriginType> map = new HashMap<>();
      for (OriginType value : OriginType.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static OriginType fromString(String name) {
      OriginType instance = _map.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException("Not a valid OriginType: \"" + name + "\"");
      }
      return instance;
   }

   private String _name;

   private OriginType(String originType) {
      _name = originType;
   }

   @JsonValue
   public String getOriginTypeName() {
      return _name;
   }

}
