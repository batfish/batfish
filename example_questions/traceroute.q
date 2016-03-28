/**
 * Trace a flow with provided parameters. Be sure to provide a value for ingress_node to get any results
 * @param ingress_node The node at which the trace originates (REQUIRED) (MUST BE QUOTED)
 * @param src_ip The source IP address in the header. Defaults to 0.0.0.0
 * @param dst_ip The destination IP address in the header. Defaults to 0.0.0.0
 * @param src_port The source port in the header for TCP and UDP flows. Defaults to 0.
 * @param dst_port The destination port in the header for TCP and UDP flows. Defaults to 0.
 * @param ip_protocol The IP protocol number in the header (use 6 for TCP and 17 for UDP). Defaults to 0.
 * @param icmp_type The ICMP type for ICMP flows. Defaults to 255, a non-real-world value meaning unset or inapplicable.
 * @param icmp_code The ICMP code for ICMP flows. Defaults to 255, a non-real-world value meaning unset or inapplicable.
 * @param icmp_type The TCPFLAGS byte for TCP flows. Defaults to 255, a non-real-world value meaning unset or inapplicable.
 */
defaults {
   // The node at which the flow is created
   $ingress_node="_REPLACE_ME_IN_QUESTION_OR_PARAMETERS_FILE_";

   // The source IP in the header of the flow
   $src_ip=0.0.0.0;

   // The destination IP in the header of the flow
   $dst_ip=0.0.0.0;

   // The source port in the header of the flow. Put 0 for flows that are neither TCP nor UDP.
   $src_port=0;

   // The destination port in the header of the flow. Put 0 for flows that are neither TCP nor UDP.
   $dst_port=0;

   // The IP protocol number in the header of the flow.
   $ip_protocol=6;

   // The ICMP type attached to an ICMP packet (use 255 for unset or inapplicable)
   $icmp_type=255;

   // The ICMP code attached to an ICMP packet (use 255 for unset or inapplicable)
   $icmp_code=255;

   // The TCPFLAGS byte attached to a TCP packet (use 255 for unset or inapplicable)
   $tcp_flags=255;
}
traceroute {
   flow(
      ingress_node = $ingress_node,
      src_ip=$src_ip,
      dst_ip=$dst_ip,
      src_port=$src_port,
      dst_port=$dst_port,
      ip_protocol=$ip_protocol,
      icmp_type=$icmp_code,
      icmp_code=$icmp_code,
      tcp_flags=$tcp_flags
   );
}
