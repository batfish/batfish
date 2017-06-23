package org.batfish.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NeighborType {
   EBGP("ebgp"),
   IBGP("ibgp"),
   LAN("lan"),
   OSPF("ospf");

   private final static Map<String, NeighborType> _map = buildMap();

   private synchronized static Map<String, NeighborType> buildMap() {
      Map<String, NeighborType> map = new HashMap<>();
      for (NeighborType value : NeighborType.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static NeighborType fromName(String name) {
      NeighborType instance = _map.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException("No " + NeighborType.class.getSimpleName()
               + " with name: '" + name + "'");
      }
      return instance;
   }

   private final String _name;

   private NeighborType(String name) {
      _name = name;
   }

   @JsonValue
   public String neighborTypeName() {
      return _name;
   }
}
