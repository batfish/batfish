/**
 * A generic query to list nodes in the testrig.
 * @param node_type A string that specifies the type of neighbor relationships to query. Value should be one of any, bgp, isis, ospf.
 * @param node A Java regex (specified as 'regex&lt;...&gt;' where the regex replaces the ellipsis) specifying which nodes should be listed. If left unspecified, such nodes are unconstrained.
 */
defaults {
   $node_type = any;
   $node = regex<.*>;
}
nodes {
   node_type = $node_type,
   node = $node
}
