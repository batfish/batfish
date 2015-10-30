traceroute {
   flow(
      ingress_node = "as3core1",
      dst_ip=2.128.0.101,
      ip_protocol=6
   );
   flow(
      ingress_node = "as1core1",
      dst_ip=2.128.0.101,
      ip_protocol=6
   );
}
