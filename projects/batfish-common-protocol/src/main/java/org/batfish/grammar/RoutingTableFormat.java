package org.batfish.grammar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoutingTableFormat {

   EMPTY("empty"),
   EOS("eos"),
   NXOS("nxos"),
   UNKNOWN("unknown");

   private final static Map<String, RoutingTableFormat> _map = buildMap();

   private synchronized static Map<String, RoutingTableFormat> buildMap() {
      Map<String, RoutingTableFormat> map = new HashMap<>();
      for (RoutingTableFormat value : RoutingTableFormat.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static RoutingTableFormat fromName(String name) {
      RoutingTableFormat instance = _map.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException(
               "No " + RoutingTableFormat.class.getSimpleName()
                     + " with name: '" + name + "'");
      }
      return instance;
   }

   private final String _name;

   private RoutingTableFormat(String name) {
      _name = name;
   }

   @JsonValue
   public String routingTableFormatName() {
      return _name;
   }

}
