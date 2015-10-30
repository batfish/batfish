defaults {
   $src_ip=0.0.0.0;
   $dst_ip=0.0.0.0;
   $src_port=0;
   $dst_port=0;
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
