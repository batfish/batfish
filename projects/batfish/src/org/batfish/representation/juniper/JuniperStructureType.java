package org.batfish.representation.juniper;

import org.batfish.representation.StructureType;

public enum JuniperStructureType implements StructureType {
   BGP_GROUP("bgp group"),
   FIREWALL_FILTER("firewall filter"),
   IKE_GATEWAY("ike gateway"),
   IKE_POLICY("ike policy"),
   IKE_PROPOSAL("ike proposal"),
   INTERFACE("interface"),
   IPSEC_POLICY("ipsec policy"),
   IPSEC_PROPOSAL("ipsec proposal"),
   POLICY_STATEMENT("policy-statement"),
   PREFIX_LIST("prefix-list"),
   SECURE_TUNNEL_INTERFACE("secure tunnel interface");

   private final String _description;

   private JuniperStructureType(String description) {
      _description = description;
   }

   @Override
   public String getDescription() {
      return _description;
   }

}
