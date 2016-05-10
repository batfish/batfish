package org.batfish.common.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

public enum NamedStructType {
   ANY("any"),
   ACL("acl"),
   PREFIX_LIST("prefix_list"),
   ROUTE_POLICY("route_policy");
   
      private final static Map<String, NamedStructType> _map = buildMap();

      private static Map<String, NamedStructType> buildMap() {
         Map<String, NamedStructType> map = new HashMap<String, NamedStructType>();
         for (NamedStructType ntype : NamedStructType.values()) {
            String ntypeName = ntype._ntypeName;
            map.put(ntypeName, ntype);
         }
         return Collections.unmodifiableMap(map);
      }

      public static NamedStructType fromName(String name) {
         NamedStructType ntype = _map.get(name);
         if (ntype == null) {
            throw new BatfishException("Not a valid named struct type: \"" + name
                  + "\"");
         }
         return ntype;
      }

      private final String _ntypeName;

      private NamedStructType(String ntypeName) {
         _ntypeName = ntypeName;
      }

      public String namedStructTypeName() {
         return _ntypeName;
      }
}
