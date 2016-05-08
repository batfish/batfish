package org.batfish.common.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

public enum NodeType {
	ANY("any"),
	BGP("bgp"),
	ISIS("isis"),
	OSPF("ospf");
	
	   private final static Map<String, NodeType> _map = buildMap();

	   private static Map<String, NodeType> buildMap() {
	      Map<String, NodeType> map = new HashMap<String, NodeType>();
	      for (NodeType ntype : NodeType.values()) {
	         String ntypeName = ntype._ntypeName;
	         map.put(ntypeName, ntype);
	      }
	      return Collections.unmodifiableMap(map);
	   }

	   public static NodeType fromName(String name) {
	      NodeType ntype = _map.get(name);
	      if (ntype == null) {
	         throw new BatfishException("Not a valid node type: \"" + name
	               + "\"");
	      }
	      return ntype;
	   }

	   private final String _ntypeName;

	   private NodeType(String ntypeName) {
	      _ntypeName = ntypeName;
	   }

	   public String nodeTypeName() {
	      return _ntypeName;
	   }
}
