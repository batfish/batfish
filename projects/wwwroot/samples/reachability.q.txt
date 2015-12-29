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
