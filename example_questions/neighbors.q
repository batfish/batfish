/**
 * A generic query to find unidirectional neighbor relationships. 
 * @param neighbor_type A string that specifies the type of neighbor relationships to query. Value should be one of physical, ebgp, ibgp.
 * @param src_node A Java regex (specified as 'regex&lt;...&gt;' where the regex replaces the ellipsis) specifying which nodes should be sources for the neighbor relationship. If left unspecified, such nodes are unconstrained.
 * @param dst_node A Java regex (specified as 'regex&lt;...&gt;' where the regex replaces the ellipsis) specifying which nodes should be destinations for the neighbor relationship. If left unspecified, such nodes are unconstrained.
 */
defaults {
   $neighbor_type = physical;
   $src_node = regex<.*>;
   $dst_node = regex<.*>;
}
neighbors {
   neighbor_type = $neighbor_type
   dst_node = $dst_node,
   src_node = $src_node
}
