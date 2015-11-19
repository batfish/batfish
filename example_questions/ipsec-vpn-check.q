verify {
   $gateway_addresses:map<ip, ipsec_vpn>;
   foreach node {
      foreach ipsec_vpn {
         $remote_ipsec_vpn = ipsec_vpn_from_listen_address(ipsec_vpn.gateway_address);
         assert {
            ipsec_vpn.compatible_ipsec_proposals($remote_ipsec_vpn)
         }
         onfailure {
            printf("Ipsec VPNs '%s' on node '%s' with Ipsec policy '%s' and '%s' on node '%s' with Ipsec policy '%s' do not have compatible Ipsec proposals.\n",
               ipsec_vpn.name,
               node.name,
               ipsec_vpn.ipsec_policy_name,
               $remote_ipsec_vpn.name,
               $remote_ipsec_vpn.owner_name,
               $remote_ipsec_vpn.ipsec_policy_name);
         }
         assert {
            ipsec_vpn.compatible_ike_proposals($remote_ipsec_vpn)
         }
         onfailure {
            printf("Ipsec VPNs '%s' on node '%s' with IKE policy '%s' on gateway '%s' and '%s' on node '%s' with IKE policy '%s' on gateway '%s' do not have compatible IKE proposals.\n",
               ipsec_vpn.name, node.name,
               ipsec_vpn.ike_policy_name,
               ipsec.ike_gateway_name,
               $remote_ipsec_vpn.name,
               $remote_ipsec_vpn.ike_policy_name,
               $remote_ipsec_vpn.ike_gateway_name,
               $remote_ipsec_vpn.owner_name);
         }
         assert {
            ipsec_vpn.pre_shared_key == $remote_ipsec_vpn.pre_shared_key
         }
         onfailure {
            printf("Ipsec VPNs '%s' on node '%s' and '%s' on node '%s' do not have the same pre-shared-key.\n",
               ipsec_vpn.name,
               node.name,
               $remote_ipsec_vpn.name,
               $remote_ipsec_vpn.owner_name);
         }
      }
   }
}