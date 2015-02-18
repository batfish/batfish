package org.batfish.collections;

import java.util.TreeMap;

public class NodeRoleMap extends TreeMap<String, RoleSet> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public RoleNodeMap toRoleNodeMap() {
      RoleNodeMap roleNodeMap = new RoleNodeMap();
      for (java.util.Map.Entry<String, RoleSet> nodeRoleEntry : entrySet()) {
         String node = nodeRoleEntry.getKey();
         RoleSet nodeRoles = nodeRoleEntry.getValue();
         for (String role : nodeRoles) {
            NodeSet roleNodes = roleNodeMap.get(role);
            if (roleNodes == null) {
               roleNodes = new NodeSet();
               roleNodeMap.put(role, roleNodes);
            }
            roleNodes.add(node);
         }
      }
      return roleNodeMap;
   }

}
