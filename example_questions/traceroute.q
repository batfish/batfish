/*
 * Trace a flow with parameters ($src_ip, $dst_ip, $src_port, $dst_port, $ip_protocol) that originates from $ingress_node.
 * MAKE SURE to supply parameter $ingress_node in parameters file or replace it below.
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
}
traceroute {
   flow(
      ingress_node = $ingress_node,
      src_ip=$src_ip,
      dst_ip=$dst_ip,
      src_port=$src_port,
      dst_port=$dst_port,
      ip_protocol=$ip_protocol
   );
}
