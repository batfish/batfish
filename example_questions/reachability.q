/**
 * A generic reachability query to find packet(s) with headers matching provided constraints exhibiting desired behavior.
 * @param ingress_node A Java regex (specified as 'regex&lt;...&gt;' where the regex replaces the ellipsis) specifying from which nodes desired packet(s) may originate. If left unspecified, such nodes are unconstrained.
 * @param final_node A Java regex (specified as 'regex&lt;...&gt;' where the regex replaces the ellipsis) specifying at which nodes desired packet(s) may be accepted/dropped/leave the network under test. If left unspecified, such nodes are unconstrained.
 * @param action The final disposition of the packet(s) (in multipath routing, there may be more than one final disposition). Choose a value from {accept, drop}. Default is accept.
 @param src_ip The space of source IP addresses for desired packet(s), specified as an IP address (e.g. 10.5.6.7), an IP prefix (e.g. 192.168.9.0/24), or a union of IP addressses and/or prefixes (e.g. {10.5.6.7, 192.168.9.0/24, ...})
 @param dst_ip The space of destination IP addresses for desired packet(s), specified as an IP address (e.g. 10.5.6.7), an IP prefix (e.g. 192.168.9.0/24), or a union of IP addressses and/or prefixes (e.g. {10.5.6.7, 192.168.9.0/24, ...})
 @param src_port For TCP or UDP packets, the space of acceptable source ports, specified as a single number (e.g. 22), a range (e.g. 5000-6000), or a union of of numbers and/or ranges (e.g. {22, 5000-6000, ...}).
 @param dst_port For TCP or UDP packets, the space of acceptable destination ports, specified as a single number (e.g. 22), a range (e.g. 5000-6000), or a union of of numbers and/or ranges (e.g. {22, 5000-6000, ...}).
 @param ip_protocol The IP protocol number for desired packet(s), specified as a single number (e.g. 6), a range (e.g. 11-50), or a union of numbers and/or ranges (e.g. {6, 11-50, ...}).
 */
defaults {
   $ingress_node = regex<.*>;
   $final_node = regex<.*>;
   $action = accept;
   $src_ip = 0.0.0.0/0;
   $dst_ip = 0.0.0.0/0;
   $src_port = 0-65535;
   $dst_port = 0-65535;
   $ip_protocol = 0-255;
}
reachability {
   ingress_node = $ingress_node,
   final_node = $final_node,
   action = $action,
   src_ip = $src_ip,
   dst_ip = $dst_ip,
   src_port = $src_port,
   dst_port = $dst_port,
   ip_protocol = $ip_protocol
}
