package org.batfish.grammar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BgpTableFormat {

   EMPTY("empty"),
   EOS("eos"),
   EOS_DETAIL("eos_detail"),
   UNKNOWN("unknown");

   private final static Map<String, BgpTableFormat> _map = buildMap();

   private synchronized static Map<String, BgpTableFormat> buildMap() {
      Map<String, BgpTableFormat> map = new HashMap<>();
      for (BgpTableFormat value : BgpTableFormat.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static BgpTableFormat fromName(String name) {
      BgpTableFormat instance = _map.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException("No " + BgpTableFormat.class.getSimpleName()
               + " with name: '" + name + "'");
      }
      return instance;
   }

   private final String _name;

   private BgpTableFormat(String name) {
      _name = name;
   }

   @JsonValue
   public String bgpTableFormatName() {
      return _name;
   }

}
