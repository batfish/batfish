package batfish.representation.juniper;

// Let r=incoming address and p=destination-prefix (source-prefix) in route-filter (source-address-filter)

public enum FilterMatchType {
   ADDRESS_MASK, 
   // r && ADDRESS_MASK = p && ADDRESS_MASK, length (r) = length(p)
   
   EXACT, 
   // length (r) = length(p), r/length = p/length
   // e.g. 192.168.0.0/16 matches: 192.168.0.0/16
   
   LONGER, 
   // r/length(p) = p/length(p), length(r) > length(p)
   // e.g. 192.168.0.0/16 matches: 192.168.*.*/[>16]
   
   ORLONGER, 
   // r/length(p) = p/length(p), length(r) >= length(p)
   // e.g. 192.168.0.0/16 matches: 192.168.*.*/[>=16]
   
   PREFIX_LENGTH_RANGE, 
   // r/length(p) = p/length(p), length(p2)<=length(r)<=length(p3)
   // e.g. 192.168.0.0/16, /18 /20 matches: 192.168.*.*/[18-20]
   
   THROUGH, 
   // r/length(p) = p/length(p), r/length(p2) = p2/length(p2), length(r) <= length(p2)
   // e.g. 192.168.0.0/16, 192.168.128.0/19 matches: 192.168.0.0/16,192.168.128.0/17,192.168.128.0/18,192.168.128.0/19
   
   UPTO
   // r/length(p) = p/length(p), length(p)<=length(r)<=length(p2)
   // e.g. 192.168.0.0/16, /20 matches: 192.168.*.*/[16-20]
}
   
