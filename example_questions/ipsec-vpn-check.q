verify {
   foreach node {
      foreach ipsec_vpn {
         assert {
            ipsec_vpn.has_remote_ipsec_vpn
         }
         onfailure {
            printf("MISSING_ENDPOINT: Could not determine remote Ipsec VPN for Ipsec VPN '%s' on node '%s'\n",
               ipsec_vpn.name,
               node.name);
         }
         if (ipsec_vpn.has_remote_ipsec_vpn) then {
            assert {
               ipsec_vpn.compatible_ipsec_proposals
            }
            onfailure {
               printf("INCOMPATIBLE_IPSEC_PROPOSALS: Ipsec VPNs '%s' on node '%s' with Ipsec policy '%s' and '%s' on node '%s' with Ipsec policy '%s' do not have compatible Ipsec proposals.\n",
                  ipsec_vpn.name,
                  node.name,
                  ipsec_vpn.ipsec_policy_name,
                  ipsec_vpn.remote_ipsec_vpn.name,
                  ipsec_vpn.remote_ipsec_vpn.owner_name,
                  ipsec_vpn.remote_ipsec_vpn.ipsec_policy_name);
            }
            assert {
               ipsec_vpn.compatible_ike_proposals
            }
            onfailure {
               printf("INCOMPATIBLE_IKE_PROPOSALS: Ipsec VPNs '%s' on node '%s' with IKE policy '%s' on gateway '%s' and '%s' on node '%s' with IKE policy '%s' on gateway '%s' do not have compatible IKE proposals.\n",
                  ipsec_vpn.name,
                  node.name,
                  ipsec_vpn.ike_policy_name,
                  ipsec_vpn.ike_gateway_name,
                  ipsec_vpn.remote_ipsec_vpn.name,
                  ipsec_vpn.remote_ipsec_vpn.ike_policy_name,
                  ipsec_vpn.remote_ipsec_vpn.ike_gateway_name,
                  ipsec_vpn.remote_ipsec_vpn.owner_name);
            }
            assert {
               ipsec_vpn.pre_shared_key == ipsec_vpn.remote_ipsec_vpn.pre_shared_key
            }
            onfailure {
               printf("PRE_SHARED_KEY_MISMATCH: Ipsec VPNs '%s' on node '%s' and '%s' on node '%s' do not have the same pre-shared-key.\n",
                  ipsec_vpn.name,
                  node.name,
                  ipsec_vpn.remote_ipsec_vpn.name,
                  ipsec_vpn.remote_ipsec_vpn.owner_name);
            }
         }
      }
   }
}