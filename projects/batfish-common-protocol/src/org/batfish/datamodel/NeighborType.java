package org.batfish.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

public enum NeighborType {
   ANY("any"),
	EBGP("ebgp"),
	IBGP("ibgp"),
   PHYSICAL("physical");
	
	   private final static Map<String, NeighborType> _map = buildMap();

	   private static Map<String, NeighborType> buildMap() {
	      Map<String, NeighborType> map = new HashMap<String, NeighborType>();
	      for (NeighborType ntype : NeighborType.values()) {
	         String ntypeName = ntype._ntypeName;
	         map.put(ntypeName, ntype);
	      }
	      return Collections.unmodifiableMap(map);
	   }

	   public static NeighborType fromName(String name) {
	      NeighborType ntype = _map.get(name);
	      if (ntype == null) {
	         throw new BatfishException("No neighbor type with name: \"" + name
	               + "\"");
	      }
	      return ntype;
	   }

	   private final String _ntypeName;

	   private NeighborType(String ntypeName) {
	      _ntypeName = ntypeName;
	   }

	   public String neighborTypeName() {
	      return _ntypeName;
	   }
}
