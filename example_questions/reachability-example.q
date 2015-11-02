reachability {
   ingress_node = regex<.*>,
   final_node = regex<.*>,
   action = accept,
   src_ip = { 2.128.0.101, 1.0.0.0/8 },
   dst_ip = 0.0.0.0/0,
   src_port = { 1-10, 20-30 },
   ip_protocol = 6,
   dst_port = 1-1000
}
