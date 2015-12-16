defaults {
   $no_check_endpoint=false;
   $no_check_unique=false;
   $no_check_ike=false;
   $no_check_ipsec=false;
   $no_check_psk=false;
}
verify {
   $num_ipsec_vpns := 0;
   $num_ipsec_vpns_with_nondeterministic_endpoint := 0;
   $num_matched_ipsec_vpns := 0;
   $num_incompatible_ike_proposals := 0;
   $num_incompatible_ipsec_proposals := 0;
   $num_pre_shared_key_mismatches := 0;
   foreach node {
      foreach ipsec_vpn {
         $num_ipsec_vpns := $num_ipsec_vpns + 1;
         unless ($no_check_endpoint){
            assert {
               ipsec_vpn.has_remote_ipsec_vpn
            }
            onfailure {
               printf("MISSING_ENDPOINT: Could not determine remote Ipsec VPN for Ipsec VPN '%s' on node '%s'\n",
                  ipsec_vpn.name,
                  node.name);
            }
         }
         if (ipsec_vpn.has_remote_ipsec_vpn){
            $num_matched_ipsec_vpns := $num_matched_ipsec_vpns + 1;
            unless ($no_check_unique){
               assert {
                  ipsec_vpn.has_single_remote_ipsec_vpn
               }
               onfailure {
                  $num_ipsec_vpns_with_nondeterministic_endpoint := $num_ipsec_vpns_with_nondeterministic_endpoint + 1;
                  printf("NON_UNIQUE_ENDPOINT: Could not uniquely determine remote VPN for Ipsec VPN '%s' on node '%s' from candidate remote VPNs:\n",
                     ipsec_vpn.name,
                     node.name);
                  foreach remote_ipsec_vpn {
                     printf("\tCANDIDATE REMOTE VPN: '%s' on node '%s'\n",
                        remote_ipsec_vpn.name,
                        remote_ipsec_vpn.owner_name);
                  }
               }
            }
            unless ($no_check_ipsec){
               assert {
                  ipsec_vpn.compatible_ipsec_proposals
               }
               onfailure {
                  $num_incompatible_ipsec_proposals := $num_incompatible_ipsec_proposals + 1;
                  printf("INCOMPATIBLE_IPSEC_PROPOSALS: Ipsec VPNs '%s' on node '%s' with Ipsec policy '%s' and '%s' on node '%s' with Ipsec policy '%s' do not have compatible Ipsec proposals.\n",
                     ipsec_vpn.name,
                     node.name,
                     ipsec_vpn.ipsec_policy_name,
                     ipsec_vpn.remote_ipsec_vpn.name,
                     ipsec_vpn.remote_ipsec_vpn.owner.name,
                     ipsec_vpn.remote_ipsec_vpn.ipsec_policy_name);
               }
            }
            unless ($no_check_ike){
               assert {
                  ipsec_vpn.compatible_ike_proposals
               }
               onfailure {
                  $num_incompatible_ike_proposals := $num_incompatible_ike_proposals + 1;
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
            }
            unless ($no_check_psk){
               assert {
                  ipsec_vpn.pre_shared_key_hash == ipsec_vpn.remote_ipsec_vpn.pre_shared_key_hash
               }
               onfailure {
                  $num_pre_shared_key_mismatches := $num_pre_shared_key_mismatches + 1;
                  printf("PRE_SHARED_KEY_MISMATCH: Ipsec VPNs '%s' on node '%s' and '%s' on node '%s' do not have the same pre-shared-key.\n",
                     ipsec_vpn.name,
                     node.name,
                     ipsec_vpn.remote_ipsec_vpn.name,
                     ipsec_vpn.remote_ipsec_vpn.owner_name);
                  printf("local VPNs '%s' remote vpn '%s' reciprocal vpn '%s' .\n",
                     ipsec_vpn.name,
                     ipsec_vpn.remote_ipsec_vpn.name,
                     ipsec_vpn.remote_ipsec_vpn.remote_ipsec_vpn.name);
               }
            }
         }
      }
   }
   $num_missing_endpoints := $num_ipsec_vpns - $num_matched_ipsec_vpns;
   printf("****Summary****\n");
   unless ($no_check_endpoint){
      printf("MISSING_ENDPOINT: %s/%s\n", $num_missing_endpoints, $num_ipsec_vpns);
   }
   unless ($no_check_unique){
      printf("NON_UNIQUE_ENDPOINT: %s/%s\n", $num_ipsec_vpns_with_nondeterministic_endpoint, $num_matched_ipsec_vpns);
   }
   unless ($no_check_ipsec){
      printf("INCOMPATIBLE_IPSEC_PROPOSALS: %s/%s\n", $num_incompatible_ipsec_proposals, $num_matched_ipsec_vpns);
   }
   unless ($no_check_ike){
      printf("INCOMPATIBLE_IKE_PROPOSALS: %s/%s\n", $num_incompatible_ike_proposals, $num_matched_ipsec_vpns);
   }
   unless ($no_check_psk){
      printf("PRE_SHARED_KEY_MISMATCH: %s/%s\n", $num_pre_shared_key_mismatches, $num_matched_ipsec_vpns);
   }
}
