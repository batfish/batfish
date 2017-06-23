package org.batfish.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ForwardingAction {
   ACCEPT("accept"),
   DEBUG("debug"),
   DROP("drop"),
   DROP_ACL("drop_acl"),
   DROP_ACL_IN("drop_acl_in"),
   DROP_ACL_OUT("drop_acl_out"),
   DROP_NO_ROUTE("drop_no_route"),
   DROP_NULL_ROUTE("drop_null_route"),
   FORWARD("forward");

   private final static Map<String, ForwardingAction> _map = buildMap();

   private static Map<String, ForwardingAction> buildMap() {
      Map<String, ForwardingAction> map = new HashMap<>();
      for (ForwardingAction value : ForwardingAction.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static ForwardingAction fromName(String name) {
      ForwardingAction instance = _map.get(name.toLowerCase());
      if (instance == null) {
         throw new BatfishException(
               "No ForwardingAction with name: \"" + name + "\"");
      }
      return instance;
   }

   private final String _name;

   private ForwardingAction(String name) {
      _name = name;
   }

   @JsonValue
   public String neighborTypeName() {
      return _name;
   }
}
