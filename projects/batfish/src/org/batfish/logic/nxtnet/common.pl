function_sig('Network_end', 2).
function_sig('BgpAdvertisement_nextHopIp', 2).
function_sig('BgpNeighborGeneratedRoute_constructor', 4).
function_sig('Flow_tag', 2).
function_sig('AdministrativeDistance', 3).
function_sig('FlowOriginate', 20).
function_sig('RouteFilterFirstMatch', 3).
function_sig('MaxLocalPref', 3).
function_sig('Flow_srcIp', 2).
function_sig('Route_node', 2).
function_sig('OspfE2Route_constructor', 6).
function_sig('GeneratedRoute_type', 2).
function_sig('Flow_ipProtocol', 2).
function_sig('Route_protocol', 2).
function_sig('InstalledBgpAdvertisementRoute', 2).
function_sig('RouteDetails_nextHopIp', 2).
function_sig('MinContributingRouteAddress', 2).
function_sig('PolicyMapFirstMatchFlow', 3).
function_sig('MinOspfRouteCost', 3).
function_sig('Route_constructor', 5).
function_sig('Route_network', 2).
function_sig('MinOspfE2RouteCostToAdvertiser', 3).
function_sig('OspfRoute_advertiser', 2).
function_sig('MinAsPathSize', 3).
function_sig('AdvertisementPathSize', 2).
function_sig('Network_constructor', 4).
function_sig('LongestPrefixNetworkMatchPrefixLength', 3).
function_sig('Route_cost', 2).
function_sig('BgpAdvertisement_srcNode', 2).
function_sig('BgpAdvertisement_localPref', 2).
function_sig('RouteDetails_tag', 2).
function_sig('Flow_srcPort', 2).
function_sig('OspfRoute_advertiserIp', 2).
function_sig('MinOspfE1RouteCost', 3).
function_sig('MinBestGlobalGeneratedRoute_nextHopIpInt', 2).
function_sig('Flow_dstPort', 2).
function_sig('Flow_icmpCode', 2).
function_sig('Flow_icmpType', 2).
function_sig('Flow_tcpFlags', 2).
function_sig('Network_address', 2).
function_sig('OspfRoute_costToAdvertiser', 2).
function_sig('PolicyMapFirstMatchAdvert', 3).
function_sig('IpAccessListFirstMatch', 3).
function_sig('Route_admin', 2).
function_sig('InterfaceRoute_nextHopInt', 2).
function_sig('RouteDetails_admin', 2).
function_sig('MinIsisL2RouteCost', 3).
function_sig('InterfaceRoute_constructor', 5).
function_sig('SetIsisInterfaceCost', 3).
function_sig('GeneratedRoute_constructor', 4).
function_sig('BgpNeighborGeneratedRoute_neighborIp', 2).
function_sig('MinIsisL1RouteCost', 3).
function_sig('Flow_node', 2).
function_sig('SetOspfInterfaceCost', 3).
function_sig('IpCount', 2).
function_sig('VlanNumber', 2).
function_sig('BgpAdvertisement_originatorIp', 2).
function_sig('PolicyMapFirstMatchRoute', 3).
function_sig('RouteDetails_nextHopInt', 2).
function_sig('Flow_dstIp', 2).
function_sig('Route_tag', 2).
function_sig('MinCost', 3).
function_sig('CommunityListFirstMatch', 3).
function_sig('Network_prefix_length', 2).
function_sig('RouteDetails_cost', 2).
function_sig('BgpAdvertisement_dstIp', 2).
function_sig('BgpAdvertisement_dstNode', 2).
function_sig('BgpAdvertisement_network', 2).
function_sig('NetworkOf', 3).
function_sig('AsPathFirstMatchAdvert', 3).
function_sig('BgpAdvertisement_srcProtocol', 2).
function_sig('BgpAdvertisementRoute', 2).
function_sig('BgpAdvertisement_type', 2).
function_sig('MinOspfIARouteCost', 3).
function_sig('BgpAdvertisement_originType', 2).
function_sig('BgpAdvertisement_med', 2).
function_sig('SetLinkLoadLimitOut', 3).
function_sig('OspfE1Route_constructor', 5).
function_sig('MinAdmin', 3).
function_sig('Route_nextHopIp', 2).
function_sig('SetLinkLoadLimitIn', 3).
function_sig('BgpAdvertisement_constructor', 12).
function_sig('MinContributingRouteAdmin', 2).
function_sig('BgpAdvertisement_srcIp', 2).
function_sig('RouteDetails_nextHop', 2).


'AsPathDenyAdvert'(AsPath, Advert) :-
   'AsPathFirstMatchAdvert'(AsPath, Advert, Line),
   'SetAsPathLineDeny'(AsPath, Line).
'AsPathDenyAdvert'(AsPath, Advert) :-
   need_AsPathMatchAdvert(AsPath, Advert),
   \+ 'AsPathLineMatchAdvert'(AsPath, _, Advert).

'AsPathFirstMatchAdvert'(AsPath, Advert, MatchLine ):-
   agg(MatchLine = min(Line),(
      'AsPathLineMatchAdvert'(AsPath, Line, Advert))).

'AsPathLineMatchAdvert'(AsPath, Line, Advert) :-
   'AsPathLineMatchAs'(AsPath, Line, Advert) ;
   'AsPathLineMatchAsAtBeginning'(AsPath, Line, Advert) ;
   'AsPathLineMatchAsPair'(AsPath, Line, Advert) ;
   'AsPathLineMatchAsPairAtBeginning'(AsPath, Line, Advert) ;
   'AsPathLineMatchEmpty'(AsPath, Line, Advert).

'AsPathLineMatchAs'(AsPath, Line, Advert) :-
   need_AsPathMatchAdvert(AsPath, Advert),
   'SetAsPathLineMatchAs'(AsPath, Line, AsLow, AsHigh),
   'AdvertisementPath'(Advert, _, As),
   AsLow =< As,
   As =< AsHigh.

'AsPathLineMatchAsAtBeginning'(AsPath, Line, Advert) :-
   need_AsPathMatchAdvert(AsPath, Advert),
   'SetAsPathLineMatchAsAtBeginning'(AsPath, Line, AsLow, AsHigh),
   'AdvertisementPath'(Advert, Index, As),
   AsLow =< As,
   As =< AsHigh,
   'AdvertisementPathSize'(Advert, Size),
   Index = Size - 1.

'AsPathLineMatchAsPair'(AsPath, Line, Advert) :-
   need_AsPathMatchAdvert(AsPath, Advert),
   'SetAsPathLineMatchAsPair'(AsPath, Line, As1Low, As1High, As2Low, As2High),
   'AdvertisementPath'(Advert, Index1, As1),
   'AdvertisementPath'(Advert, Index2, As2),
   As1Low =< As1,
   As1 =< As1High,
   As2Low =< As2,
   As2 =< As2High,
   Index2 = Index1 - 1.

'AsPathLineMatchAsPairAtBeginning'(AsPath, Line, Advert) :-
   need_AsPathMatchAdvert(AsPath, Advert),
   'SetAsPathLineMatchAsPairAtBeginning'(AsPath, Line, As1Low, As1High, As2Low, As2High),
   'AdvertisementPath'(Advert, Index1, As1),
   'AdvertisementPath'(Advert, Index2, As2),
   As1Low =< As1,
   As1 =< As1High,
   As2Low =< As2,
   As2 =< As2High,
   Index2 = Index1 + 1,
   'AdvertisementPathSize'(Advert, Size),
   Index1 = Size - 1.

'AsPathLineMatchEmpty'(AsPath, Line, Advert) :-
   need_AsPathMatchAdvert(AsPath, Advert),
   'SetAsPathLineMatchEmpty'(AsPath, Line),
   'AdvertisementPathSize'(Advert, Size),
   Size = 0.

'AsPathPermitAdvert'(AsPath, Advert) :-
   'AsPathFirstMatchAdvert'(AsPath, Advert, Line),
   'SetAsPathLinePermit'(AsPath, Line).
'AdministrativeDistance'('arista', 'bgp', 200).
'AdministrativeDistance'('arista', 'ibgp', 200).
'AdministrativeDistance'('arista', 'connected', 0).
'AdministrativeDistance'('arista', 'ospf', 110).
'AdministrativeDistance'('arista', 'ospfIA', 110).
'AdministrativeDistance'('arista', 'ospfE1', 110).
'AdministrativeDistance'('arista', 'ospfE2', 110).

'AdministrativeDistance'('aws_vpc', 'bgp', 20).
'AdministrativeDistance'('aws_vpc', 'ibgp', 200).
'AdministrativeDistance'('aws_vpc', 'connected', 0).
'AdministrativeDistance'('aws_vpc', 'isisL1', 115).
'AdministrativeDistance'('aws_vpc', 'isisL2', 115).
'AdministrativeDistance'('aws_vpc', 'isisEL1', 115).
'AdministrativeDistance'('aws_vpc', 'isisEL2', 115).
'AdministrativeDistance'('aws_vpc', 'ospf', 110).
'AdministrativeDistance'('aws_vpc', 'ospfIA', 110).
'AdministrativeDistance'('aws_vpc', 'ospfE1', 110).
'AdministrativeDistance'('aws_vpc', 'ospfE2', 110).

'AdministrativeDistance'('cisco', 'bgp', 20).
'AdministrativeDistance'('cisco', 'ibgp', 200).
'AdministrativeDistance'('cisco', 'connected', 0).
'AdministrativeDistance'('cisco', 'isisL1', 115).
'AdministrativeDistance'('cisco', 'isisL2', 115).
'AdministrativeDistance'('cisco', 'isisEL1', 115).
'AdministrativeDistance'('cisco', 'isisEL2', 115).
'AdministrativeDistance'('cisco', 'ospf', 110).
'AdministrativeDistance'('cisco', 'ospfIA', 110).
'AdministrativeDistance'('cisco', 'ospfE1', 110).
'AdministrativeDistance'('cisco', 'ospfE2', 110).

'AdministrativeDistance'('juniper', 'bgp', 170).
'AdministrativeDistance'('juniper', 'ibgp', 170).
'AdministrativeDistance'('juniper', 'connected', 0).
'AdministrativeDistance'('juniper', 'isisL1', 15).
'AdministrativeDistance'('juniper', 'isisL2', 18).
'AdministrativeDistance'('juniper', 'isisEL1', 160).
'AdministrativeDistance'('juniper', 'isisEL2', 165).
'AdministrativeDistance'('juniper', 'ospf', 10).
'AdministrativeDistance'('juniper', 'ospfIA', 10).
'AdministrativeDistance'('juniper', 'ospfE1', 150).
'AdministrativeDistance'('juniper', 'ospfE2', 150).

'AdministrativeDistance'('vyos', 'bgp', 20).
'AdministrativeDistance'('vyos', 'ibgp', 200).
'AdministrativeDistance'('vyos', 'connected', 0).
'AdministrativeDistance'('vyos', 'isisL1', 115).
'AdministrativeDistance'('vyos', 'isisL2', 115).
'AdministrativeDistance'('vyos', 'isisEL1', 115).
'AdministrativeDistance'('vyos', 'isisEL2', 115).
'AdministrativeDistance'('vyos', 'ospf', 110).
'AdministrativeDistance'('vyos', 'ospfIA', 110).
'AdministrativeDistance'('vyos', 'ospfE1', 110).
'AdministrativeDistance'('vyos', 'ospfE2', 110).

'PolicyMap'(Map) :-
   'SetBgpExportPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map).

'PolicyMap'(Map) :-
   'SetBgpGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map).

'PolicyMap'(Map) :-
   'SetBgpImportPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map).

'PolicyMap'(Map) :-
   'SetBgpNeighborGeneratedRoutePolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Network_start, Network_end, Prefix_length, Map).

'PolicyMap'(Map) :-
   'SetBgpOriginationPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map).
% advertise a transit route received through ibgp or ebgp
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, Advert),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, LocalPref),
'BgpAdvertisement_med'(Advert, Med),
'BgpAdvertisement_network'(Advert, DstIpBlock),
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, OriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize)
:-
   Type = 'bgp',
   'Ip_NONE'(OriginatorIp),
   (
      'BgpAdvertisement_type'(PrevAdvert, 'bgp_ti' );
      'BgpAdvertisement_type'(PrevAdvert, 'ibgp_ti')
   ),
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, OrigNextHopIp),
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol),
   'BgpAdvertisement_dstNode'(PrevAdvert, SrcNode),
   'AdvertisementPathSize'(PrevAdvert, PrevPathSize),
   'InstalledBgpAdvertisement'(PrevAdvert),
   PathSize = PrevPathSize,
   'BestBgpAdvertisement'(PrevAdvert),
   (
      'BgpDefaultLocalPref'(SrcNode, DstIp, LocalPref);
      (
         \+ 'BgpDefaultLocalPref'(SrcNode, DstIp, _),
         LocalPref = 100
      )
   ),
   OriginType = 'egp',
   'BgpNeighborDefaultMetric'(SrcNode, DstIp, Med),
   NextHopIp = SrcIp,
   OrigNextHopIp \== DstIp,
   'BgpNeighbors'(SrcNode, SrcIp, DstNode, DstIp).
% advertise an internally received route
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, Network, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, Advert),
'AdvertisementPathSize'(Advert, PathSize),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, LocalPref),
'BgpAdvertisement_med'(Advert, Med),
'BgpAdvertisement_network'(Advert, Network),
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, OriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'OriginalBgpAdvertisementRoute'(Advert, Route),
'OriginatedBgpNetwork'(SrcNode, Network)
:-
   Type = 'bgp',
   PathSize = 0,
   NextHopIp = SrcIp,
   'Ip_NONE'(OriginatorIp),
   'BgpNeighbors'(SrcNode, SrcIp, DstNode, DstIp),
   'BgpNeighborIp'(SrcNode, DstIp),
   OriginType = 'igp',
   'BgpNeighborDefaultMetric'(SrcNode, DstIp, Med),
   (
      'BgpDefaultLocalPref'(SrcNode, DstIp, LocalPref) ;
      (
         \+ 'BgpDefaultLocalPref'(SrcNode, DstIp, _),
         LocalPref = 100
      )
   ),
   'Route_network'(Route, Network),
   'Route_protocol'(Route, SrcProtocol),
   'Route_node'(Route, SrcNode),
   (
      (
         'ActiveGeneratedRoute'(Route),
         'BgpGeneratedRoute'(Route)
      ) ;
      (
         'ActiveGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute_neighborIp'(Route, DstIp)
      ) ;
      (
         'InstalledRoute'(Route),
         SrcProtocol \== 'bgp',
         SrcProtocol \== 'ibgp',
         (
            \+ 'BgpOriginationPolicy'(SrcNode, DstIp, _);
            (
               'BgpOriginationPolicy'(SrcNode, DstIp, Map),
               'PolicyMapPermitRoute'(Map, _, Route)
            )
         )
      )
   ).

need_PolicyMapMatchRoute(Map, Route) :-
   (
      'InstalledRoute'(Route) ;
      'BgpGeneratedRoute'(Route) ;
      (
         'BgpNeighborGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute_neighborIp'(Route, NeighborIp)
      )
   ),
   'Route_node'(Route, Node),
   'BgpOriginationPolicy'(Node, NeighborIp, Map),
   'BgpNeighbors'(Node, _, _, NeighborIp).
% incoming transformation
'AdvertisementCommunity'(Advert, Community) :-
   PriorType = 'bgp_to',
   'BgpNeighborSendCommunity'(DstNode, SrcIp),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode),
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp),
   'ParentAdvertisement'(PrevAdvert, Advert),
   (
      (
         'BgpImportPolicy'(DstNode, SrcIp, Map),
         'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
         \+ 'SetPolicyMapClauseSetCommunityNone'(Map, Clause),
         (
            'SetPolicyMapClauseAddCommunity'(Map, Clause, Community) ;
            'SetPolicyMapClauseSetCommunity'(Map, Clause, Community) ;
            (
               'AdvertisementCommunity'(PrevAdvert, Community),
               \+ 'SetPolicyMapClauseSetCommunity'(Map, Clause, _),
               (
                  \+ 'SetPolicyMapClauseDeleteCommunity'(Map, Clause, _) ;
                  (
                     'SetPolicyMapClauseDeleteCommunity'(Map, Clause, DeleteList),
                     \+ 'CommunityListPermit'(DeleteList, _, Community)
                  )
               )
            )
         )
      ) ;
      (
         \+ 'BgpImportPolicy'(DstNode, SrcIp, _),
         'AdvertisementCommunity'(PrevAdvert, Community)
      )
   ).

'BestBgpRoute'(Route),
   'Route'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'BestBgpRouteNetwork'(Node, Network),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol),
   'BgpAdvertisementRoute'(Advert, Route)
:-
   Type = 'bgp_ti',
   Protocol = 'bgp',
   'BgpAdvertisement_type'(Advert, Type),
   'BgpAdvertisement_network'(Advert, Network),
   'BgpAdvertisement_nextHopIp'(Advert, NextHopIp),
   'BgpAdvertisement_dstNode'(Advert, Node),
   'BgpAdvertisement_med'(Advert, Cost),
   'BestBgpAdvertisement'(Advert),
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).

% ebgp transformed incoming
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, TNextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, TOriginType, TLocalPref, TMed, Advert),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, TLocalPref),
'BgpAdvertisement_med'(Advert, TMed),
'BgpAdvertisement_network'(Advert, DstIpBlock),
'BgpAdvertisement_nextHopIp'(Advert, TNextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, TOriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, TSrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize)
:-
   Type = 'bgp_ti',
   PriorType = 'bgp_to',
   \+ 'HasIp'(DstNode, OriginatorIp),
   'LocalAs'(DstNode, SrcIp, ReceiverAs),
   \+ 'AdvertisementPath'(PrevAdvert, _, ReceiverAs),
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp),
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode),
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref),
   'BgpAdvertisement_med'(PrevAdvert, Med),
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp),
   'BgpAdvertisement_originatorIp'(PrevAdvert, OriginatorIp),
   'BgpAdvertisement_originType'(PrevAdvert, OriginType),
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp),
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode),
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'AdvertisementPathSize'(PrevAdvert, PathSize),
   'BgpNeighborIp'(DstNode, SrcIp),
   (
      (
         'BgpImportPolicy'(DstNode, SrcIp, Map),
         'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
         'PolicyMapClauseTransformAdvert'(Map, Clause, PrevAdvert, TNextHopIp,
            TLocalPref, TOriginType, TMed, TSrcProtocol)
      );
      (
         \+ 'BgpImportPolicy'(DstNode, SrcIp, _),
         TNextHopIp = NextHopIp,
         TLocalPref = LocalPref,
         TOriginType = OriginType,
         TMed = Med,
         TSrcProtocol = SrcProtocol
      )
   ).

need_PolicyMapMatchAdvert(Map, Advert)
:-
   Type = 'bgp_to',
   'BgpAdvertisement_dstNode'(Advert, DstNode),
   'BgpAdvertisement_srcIp'(Advert, SrcIp),
   'BgpAdvertisement_type'(Advert, Type),
   'BgpImportPolicy'(DstNode, SrcIp, Map).
% append as to path for bgp route advertisement
'AdvertisementPath'(Advert, Index, As) :-
   'BgpAdvertisement_type'(Advert, Type),
   'BgpAdvertisement_dstNode'(Advert, DstNode),
   'BgpAdvertisement_srcIp'(Advert, SrcIp),
   Type = 'bgp_to',
   'ParentAdvertisement'(PrevAdvert, Advert),
   'AdvertisementPathSize'(PrevAdvert, PrevPathSize),
   Index = PrevPathSize,
   'RemoteAs'(DstNode, SrcIp, As).

% outgoing transformation
'AdvertisementCommunity'(Advert, Community) :-
   PriorType = 'bgp',
   'BgpNeighborSendCommunity'(SrcNode, DstIp),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode),
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp),
   'ParentAdvertisement'(PrevAdvert, Advert),
   (
      (
         'BgpExportPolicy'(SrcNode, DstIp, Map),
         'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
         \+ 'SetPolicyMapClauseSetCommunityNone'(Map, Clause),
         (
            (
               \+ 'SetPolicyMapClauseMatchPolicy'(Map, Clause, _),
               Clause = TransformingClause,
               Map = TransformingMap
            ) ;
            (
               'SetPolicyMapClauseMatchPolicy'(Map, Clause, TransformingMap),
               'PolicyMapPermitAdvert'(TransformingMap, TransformingClause, PrevAdvert)
            )
         ),
         (
            'SetPolicyMapClauseAddCommunity'(TransformingMap, TransformingClause, Community) ;
            'SetPolicyMapClauseSetCommunity'(TransformingMap, TransformingClause, Community) ;
            (
               'AdvertisementCommunity'(PrevAdvert, Community),
               \+ 'SetPolicyMapClauseSetCommunity'(TransformingMap, TransformingClause, _),
               (
                  \+ 'SetPolicyMapClauseDeleteCommunity'(TransformingMap, TransformingClause, _) ;
                  (
                     'SetPolicyMapClauseDeleteCommunity'(TransformingMap, TransformingClause, DeleteList),
                     \+ 'CommunityListPermit'(DeleteList, _, Community)
                  )
               )
            )
         )
      ) ;
      (
         \+ 'BgpExportPolicy'(SrcNode, DstIp, _),
         'AdvertisementCommunity'(PrevAdvert, Community)
      )
   ).

% ebgp transformed outgoing
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, TNextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, TOriginType, TLocalPref, TMed, Advert),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, TLocalPref),
'BgpAdvertisement_med'(Advert, TMed),
'BgpAdvertisement_network'(Advert, DstIpBlock),
'BgpAdvertisement_nextHopIp'(Advert, TNextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, TOriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, TSrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize)
:-
   Type = 'bgp_to',
   PriorType = 'bgp',
   PathSize = PrevPathSize + 1,
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp),
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode),
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref),
   'BgpAdvertisement_med'(PrevAdvert, Med),
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp),
   'BgpAdvertisement_originatorIp'(PrevAdvert, OriginatorIp),
   'BgpAdvertisement_originType'(PrevAdvert, OriginType),
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp),
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode),
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'AdvertisementPathSize'(PrevAdvert, PrevPathSize),
   'BgpNeighborIp'(SrcNode, DstIp),
   (
      (
         'BgpExportPolicy'(SrcNode, DstIp, Map),
         'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
         (
            (
               \+ 'SetPolicyMapClauseMatchPolicy'(Map, Clause, _),
               TransformingClause = Clause,
               TransformingMap = Map
            ) ;
            (
               'SetPolicyMapClauseMatchPolicy'(Map, Clause, TransformingMap),
               'PolicyMapPermitAdvert'(TransformingMap, TransformingClause, PrevAdvert)
            )
         ),
         'PolicyMapClauseTransformAdvert'(TransformingMap, TransformingClause, PrevAdvert, TNextHopIp,
            TLocalPref, TOriginType, TMed, TSrcProtocol)
      );
      (
         \+ 'BgpExportPolicy'(SrcNode, DstIp, _),
         TNextHopIp = NextHopIp,
         TLocalPref = LocalPref,
         TOriginType = OriginType,
         TMed = Med,
         TSrcProtocol = SrcProtocol
      )
   ).
'BgpGeneratedRoute'(Route),
   'Route'(Route),
   'GeneratedRoute_constructor'(Node, Network, Type, Route),
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   'SetBgpGeneratedRoute'(Node, Network),
   Type = 'GeneratedRouteType_BGP',
   Protocol = 'aggregate'.

'BgpNeighborGeneratedRoute'(Route),
   'Route'(Route),
   'BgpNeighborGeneratedRoute_constructor'(Node, Network, NeighborIp, Route),
   'BgpNeighborGeneratedRoute_neighborIp'(Route, NeighborIp),
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   'BgpNeighborIpGeneratedRoute'(Node, NeighborIp, Network),
   Type = 'GeneratedRouteType_BGP_NEIGHBOR',
   Protocol = 'aggregate'.

'BgpNeighborIpGeneratedRoute'(Node, NeighborIp, Network) :-
   'SetBgpNeighborGeneratedRoute'(Node, NeighborNetwork, Network),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'BgpNeighborGeneratedRoutePolicy'(Node, NeighborIp, Network, Map) :-
   'SetBgpNeighborGeneratedRoutePolicy'(Node, NeighborNetwork, Network, Map),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'GeneratedRoutePolicy'(Route, Policy) :-
   'BgpGeneratedRoute'(Route),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'SetBgpGeneratedRoutePolicy'(Node, Network, Policy).
'GeneratedRoutePolicy'(Route, Policy) :-
   'BgpNeighborGeneratedRoute'(Route),
   'BgpNeighborGeneratedRoute_neighborIp'(Route, NeighborIp),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'BgpNeighborGeneratedRoutePolicy'(Node, NeighborIp, Network, Policy).

need_PolicyMapMatchRoute(Map, Route) :-
   'InstalledRoute'(Route),
   'Route_node'(Route, Node),
   (
      'SetBgpGeneratedRoutePolicy'(Node, _, Map) ;
      'BgpNeighborGeneratedRoutePolicy'(Node, _, _, Map)
   ).

'SetBgpGeneratedRoute'(Node, Network) :-
   'SetBgpGeneratedRoute_flat'(Node, Network_start, Network_end, Prefix_length),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'SetBgpGeneratedRoutePolicy'(Node, Network, Map) :-
   'SetBgpGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'SetBgpNeighborGeneratedRoute'(Node, NeighborNetwork, Network) :-
   'SetBgpNeighborGeneratedRoute_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Network_start, Network_end, Prefix_length),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'SetBgpNeighborGeneratedRoutePolicy'(Node, NeighborNetwork, Network, Map) :-
   'SetBgpNeighborGeneratedRoutePolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).
% ibgp advertisement from bgp
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, Advert),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, LocalPref),
'BgpAdvertisement_med'(Advert, Med),
'BgpAdvertisement_network'(Advert, DstIpBlock),
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, OriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize)
:-
   Type = 'ibgp',
   PriorType = 'bgp_ti',
   'Ip_NONE'(OriginatorIp),
   'BgpAdvertisement_dstNode'(PrevAdvert, SrcNode),
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref),
   'BgpAdvertisement_med'(PrevAdvert, Med),
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp),
   'BgpAdvertisement_originType'(PrevAdvert, OriginType),
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'AdvertisementPathSize'(PrevAdvert, PathSize),
   'BestBgpAdvertisement'(PrevAdvert),
   'InstalledBgpAdvertisement'(PrevAdvert),
   'IbgpNeighbors'(SrcNode, SrcIp, DstNode, DstIp).
% advertise an internally received route
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, Network, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, Advert),
'AdvertisementPathSize'(Advert, PathSize),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, LocalPref),
'BgpAdvertisement_med'(Advert, Med),
'BgpAdvertisement_network'(Advert, Network),
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, OriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'OriginalBgpAdvertisementRoute'(Advert, Route),
'OriginatedBgpNetwork'(SrcNode, Network)
:-
   Type = 'ibgp',
   PathSize = 0,
   NextHopIp = SrcIp,
   OriginatorIp = SrcIp,
   'IbgpNeighbors'(SrcNode, SrcIp, DstNode, DstIp),
   'BgpNeighborIp'(SrcNode, DstIp),
   OriginType = 'igp',
   'BgpNeighborDefaultMetric'(SrcNode, DstIp, Med),
   (
      'BgpDefaultLocalPref'(SrcNode, DstIp, LocalPref) ;
      (
         \+ 'BgpDefaultLocalPref'(SrcNode, DstIp, _),
         LocalPref = 100
      )
   ),
   'Route_network'(Route, Network),
   'Route_protocol'(Route, SrcProtocol),
   'Route_node'(Route, SrcNode),
   (
      (
         'ActiveGeneratedRoute'(Route),
         'BgpGeneratedRoute'(Route)
      ) ;
      (
         'ActiveGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute_neighborIp'(Route, DstIp)
      ) ;
      (
         'InstalledRoute'(Route),
         SrcProtocol \== 'bgp',
         SrcProtocol \== 'ibgp',
         (
            \+ 'BgpOriginationPolicy'(SrcNode, DstIp, _);
            (
               'BgpOriginationPolicy'(SrcNode, DstIp, Map),
               'PolicyMapPermitRoute'(Map, _, Route)
            )
         )
      )
   ).

need_PolicyMapMatchRoute(Map, Route) :-
   (
      'InstalledRoute'(Route) ;
      'BgpGeneratedRoute'(Route) ;
      (
         'BgpNeighborGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute_neighborIp'(Route, NeighborIp)
      )
   ),
   'Route_node'(Route, Node),
   'BgpOriginationPolicy'(Node, NeighborIp, Map),
   'IbgpNeighbors'(Node, _, _, NeighborIp).
% incoming transformation
'AdvertisementCommunity'(Advert, Community) :-
   PriorType = 'ibgp_to',
   'BgpNeighborSendCommunity'(DstNode, SrcIp),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode),
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp),
   'ParentAdvertisement'(PrevAdvert, Advert),
   (
      (
         'BgpImportPolicy'(DstNode, SrcIp, Map),
         'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
         \+ 'SetPolicyMapClauseSetCommunityNone'(Map, Clause),
         (
            'SetPolicyMapClauseAddCommunity'(Map, Clause, Community) ;
            'SetPolicyMapClauseSetCommunity'(Map, Clause, Community) ;
            (
               'AdvertisementCommunity'(PrevAdvert, Community),
               \+ 'SetPolicyMapClauseSetCommunity'(Map, Clause, _),
               (
                  \+ 'SetPolicyMapClauseDeleteCommunity'(Map, Clause, _) ;
                  (
                     'SetPolicyMapClauseDeleteCommunity'(Map, Clause, DeleteList),
                     \+ 'CommunityListPermit'(DeleteList, _, Community)
                  )
               )
            )
         )
      ) ;
      (
         \+ 'BgpImportPolicy'(DstNode, SrcIp, _),
         'AdvertisementCommunity'(PrevAdvert, Community)
      )
   ).

'BestIbgpRoute'(Route),
   'Route'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol),
   'BgpAdvertisementRoute'(Advert, Route)
:-
   Type = 'ibgp_ti',
   Protocol = 'ibgp',
   'BgpAdvertisement_type'(Advert, Type),
   'BgpAdvertisement_network'(Advert, Network),
   'BgpAdvertisement_nextHopIp'(Advert, NextHopIp),
   'BgpAdvertisement_dstNode'(Advert, Node),
   'BgpAdvertisement_med'(Advert, Cost),
   'BestBgpAdvertisement'(Advert),
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin),
   \+ 'BestBgpRouteNetwork'(Node, Network).

% ibgp transformed incoming
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, TNextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, TOriginType, TLocalPref, TMed, Advert),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, TLocalPref),
'BgpAdvertisement_med'(Advert, TMed),
'BgpAdvertisement_network'(Advert, DstIpBlock),
'BgpAdvertisement_nextHopIp'(Advert, TNextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, TOriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, TSrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize)
:-
   Type = 'ibgp_ti',
   PriorType = 'ibgp_to',
   (
      \+ 'RouteReflectorClient'(DstNode, SrcIp, _) ;
      (
         'RouteReflectorClient'(DstNode, _, ClusterId),
         \+ 'AdvertisementClusterId'(PrevAdvert, ClusterId)
      )
   ),
   \+ 'HasIp'(DstNode, OriginatorIp),
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp),
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode),
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref),
   'BgpAdvertisement_med'(PrevAdvert, Med),
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp),
   'BgpAdvertisement_originatorIp'(PrevAdvert, OriginatorIp),
   'BgpAdvertisement_originType'(PrevAdvert, OriginType),
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp),
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode),
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'AdvertisementPathSize'(PrevAdvert, PathSize),
   'BgpNeighborIp'(DstNode, SrcIp),
   (
      (
         'BgpImportPolicy'(DstNode, SrcIp, Map),
         'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
         'PolicyMapClauseTransformAdvert'(Map, Clause, PrevAdvert, TNextHopIp,
            TLocalPref, TOriginType, TMed, TSrcProtocol)
      );
      (
         \+ 'BgpImportPolicy'(DstNode, SrcIp, _),
         TNextHopIp = NextHopIp,
         TLocalPref = LocalPref,
         TOriginType = OriginType,
         TMed = Med,
         TSrcProtocol = SrcProtocol
      )
   ).

need_PolicyMapMatchAdvert(Map, Advert)
:-
   Type = 'ibgp_to',
   'BgpAdvertisement_dstNode'(Advert, DstNode),
   'BgpAdvertisement_srcIp'(Advert, SrcIp),
   'BgpAdvertisement_type'(Advert, Type),
   'BgpImportPolicy'(DstNode, SrcIp, Map).

'IbgpNeighborTo'(Node, Neighbor, NeighborIp) :-
   'NetworkOf'(NeighborIp, Prefix_length, NeighborNetwork),
   'InstalledRoute'(Route),
   'Route_network'(Route, NeighborNetwork),
   'Route_node'(Route, Node),
   'IpReadyInt'(Neighbor, _, NeighborIp, Prefix_length),
   'RemoteAs'(Node, NeighborIp, As),
   'LocalAs'(Node, NeighborIp, As).

'IbgpNeighbors'(Node1, Ip1, Node2, Ip2) :-
   'IbgpNeighborTo'(Node1, Node2, Ip2),
   'IbgpNeighborTo'(Node2, Node1, Ip1).

% outgoing transformation
'AdvertisementCommunity'(Advert, Community) :-
   PriorType = 'ibgp',
   'BgpNeighborSendCommunity'(SrcNode, DstIp),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode),
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp),
   'ParentAdvertisement'(PrevAdvert, Advert),
   (
      (
         'BgpExportPolicy'(SrcNode, DstIp, Map),
         'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
         \+ 'SetPolicyMapClauseSetCommunityNone'(Map, Clause),
         (
            (
               \+ 'SetPolicyMapClauseMatchPolicy'(Map, Clause, _),
               Clause = TransformingClause,
               Map = TransformingMap
            ) ;
            (
               'SetPolicyMapClauseMatchPolicy'(Map, Clause, TransformingMap),
               'PolicyMapPermitAdvert'(TransformingMap, TransformingClause, PrevAdvert)
            )
         ),
         (
            'SetPolicyMapClauseAddCommunity'(TransformingMap, TransformingClause, Community) ;
            'SetPolicyMapClauseSetCommunity'(TransformingMap, TransformingClause, Community) ;
            (
               'AdvertisementCommunity'(PrevAdvert, Community),
               \+ 'SetPolicyMapClauseSetCommunity'(TransformingMap, TransformingClause, _),
               (
                  \+ 'SetPolicyMapClauseDeleteCommunity'(TransformingMap, TransformingClause, _) ;
                  (
                     'SetPolicyMapClauseDeleteCommunity'(TransformingMap, TransformingClause, DeleteList),
                     \+ 'CommunityListPermit'(DeleteList, _, Community)
                  )
               )
            )
         )
      ) ;
      (
         \+ 'BgpExportPolicy'(SrcNode, DstIp, _),
         'AdvertisementCommunity'(PrevAdvert, Community)
      )
   ).

% ibgp transformed outgoing
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, TNextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, TOriginType, TLocalPref, TMed, Advert),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, TLocalPref),
'BgpAdvertisement_med'(Advert, TMed),
'BgpAdvertisement_network'(Advert, DstIpBlock),
'BgpAdvertisement_nextHopIp'(Advert, TNextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, TOriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, TSrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize)
:-
   Type = 'ibgp_to',
   PriorType = 'ibgp',
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp),
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode),
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref),
   'BgpAdvertisement_med'(PrevAdvert, Med),
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp),
   'BgpAdvertisement_originatorIp'(PrevAdvert, OriginatorIp),
   'BgpAdvertisement_originType'(PrevAdvert, OriginType),
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp),
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode),
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'AdvertisementPathSize'(PrevAdvert, PathSize),
   'BgpNeighborIp'(SrcNode, DstIp),
   (
      (
         'BgpExportPolicy'(SrcNode, DstIp, Map),
         'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
         (
            (
               \+ 'SetPolicyMapClauseMatchPolicy'(Map, Clause, _),
               TransformingClause = Clause,
               TransformingMap = Map
            ) ;
            (
               'SetPolicyMapClauseMatchPolicy'(Map, Clause, TransformingMap),
               'PolicyMapPermitAdvert'(TransformingMap, TransformingClause, PrevAdvert)
            )
         ),
         'PolicyMapClauseTransformAdvert'(TransformingMap, TransformingClause, PrevAdvert, TNextHopIp,
            TLocalPref, TOriginType, TMed, TSrcProtocol)
      );
      (
         \+ 'BgpExportPolicy'(SrcNode, DstIp, _),
         TNextHopIp = NextHopIp,
         TLocalPref = LocalPref,
         TOriginType = OriginType,
         TMed = Med,
         TSrcProtocol = SrcProtocol
      )
   ).
'BgpOriginationPolicy'(Node, NeighborIp, Map) :-
   'SetBgpOriginationPolicy'(Node, NeighborNetwork, Map),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'SetBgpOriginationPolicy'(Node, NeighborNetwork, Map) :-
   'SetBgpOriginationPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).
'BgpImportPolicy'(Node, NeighborIp, Map) :-
   'SetBgpImportPolicy'(Node, NeighborNetwork, Map),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'SetBgpImportPolicy'(Node, NeighborNetwork, Map) :-
   'SetBgpImportPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).
'BgpExportPolicy'(Node, NeighborIp, Map) :-
   'SetBgpExportPolicy'(Node, NeighborNetwork, Map),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'SetBgpExportPolicy'(Node, NeighborNetwork, Map) :-
   'SetBgpExportPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).
'AdvertisementClusterId'(Advert, ClusterId) :-
   (
      PriorType = 'ibgp' ;
      PriorType = 'ibgp_ti' ;
      PriorType = 'ibgp_to'
   ),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'ParentAdvertisement'(PrevAdvert, Advert),
   'AdvertisementClusterId'(PrevAdvert, ClusterId).
'AdvertisementClusterId'(Advert, ClusterId) :-
   Type = 'ibgp',
   'BgpAdvertisement_type'(Advert, Type),
   'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
   'BgpAdvertisement_srcNode'(Advert, SrcNode),
   'BgpAdvertisement_dstIp'(Advert, DstIp),
   'Ip_NONE'(IpNone),
   OriginatorIp \== IpNone,
   'RouteReflectorClient'(SrcNode, DstIp, ClusterId).

% ibgp route reflection
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, Advert),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, LocalPref),
'BgpAdvertisement_med'(Advert, Med),
'BgpAdvertisement_network'(Advert, DstIpBlock),
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, OriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize)
:-
   Type = 'ibgp',
   PriorType = 'ibgp_ti',
   'BgpAdvertisement_dstNode'(PrevAdvert, SrcNode),
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref),
   'BgpAdvertisement_med'(PrevAdvert, Med),
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp),
   'BgpAdvertisement_originatorIp'(PrevAdvert, PrevOriginatorIp),
   'BgpAdvertisement_originType'(PrevAdvert, OriginType),
   'BgpAdvertisement_srcIp'(PrevAdvert, SenderIp),
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'AdvertisementPathSize'(PrevAdvert, PathSize),
   'InstalledBgpAdvertisement'(PrevAdvert),
   'BestBgpAdvertisement'(PrevAdvert),
   (
      (
         'Ip_NONE'(IpNone),
         IpNone \== PrevOriginatorIp,
         OriginatorIp = PrevOriginatorIp
      ) ;
      (
         'Ip_NONE'(PrevOriginatorIp),
         OriginatorIp = SenderIp
      )
   ),
   (
      'RouteReflectorClient'(SrcNode, SenderIp, _) ;
      (
         \+ 'RouteReflectorClient'(SrcNode, SenderIp, _),
         'RouteReflectorClient'(SrcNode, DstIp, _)
      )
   ),
   'IbgpNeighbors'(SrcNode, SrcIp, DstNode, DstIp).

'RouteReflectorClient'(Node, NeighborIp, ClusterId) :-
   'SetRouteReflectorClient'(Node, NeighborNetwork, ClusterId),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'SetRouteReflectorClient'(Node, NeighborNetwork, ClusterId) :-
   'SetRouteReflectorClient_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, ClusterId),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).
% copy old path elements
'AdvertisementPath'(Advert, Index, As) :-
   'ParentAdvertisement'(PrevAdvert, Advert),
   'AdvertisementPath'(PrevAdvert, Index, As).

% i/ebgp base advertisement inherits community from bgp and ibgp
'AdvertisementCommunity'(Advert, Community) :-
   (
      PriorType = 'bgp_ti' ;
      PriorType = 'ibgp_ti'
   ),
   'AdvertisementCommunity'(PrevAdvert, Community),
   'BgpAdvertisement_type'(PrevAdvert, PriorType),
   'ParentAdvertisement'(PrevAdvert, Advert).

'BestBgpAdvertisement'(Advert) :-
   'MinAsPathLengthBgpAdvertisement'(Advert).

'BestPerProtocolRoute'(Route) :-
   'BestBgpRoute'(Route) ;
   'BestIbgpRoute'(Route).

'BgpDefaultLocalPref'(Node, NeighborIp, LocalPref) :-
   'SetBgpDefaultLocalPref'(Node, NeighborNetwork, LocalPref),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'BgpNeighborDefaultMetric'(Node, NeighborIp, Metric) :-
   'SetBgpNeighborDefaultMetric'(Node, NeighborNetwork, Metric),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'BgpNeighborLocalIp'(Node, NeighborIp, LocalIp) :-
   'SetBgpNeighborLocalIp'(Node, NeighborNetwork, LocalIp),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'BgpMultihopNeighborIp'(Node, NeighborIp) :-
   'SetBgpMultihopNeighborNetwork'(Node, NeighborNetwork),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'BgpNeighborIp'(Node, NeighborIp) :-
   'SetBgpNeighborNetwork'(Node, NeighborNetwork),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'BgpNeighbors'(Node1, Ip1, Node2, Ip2) :-
   'SetExternalBgpRemoteIp'(Node2, Ip2),
   'BgpNeighborLocalIp'(Node1, Ip2, Ip1),
   'BgpNeighborIp'(Node1, Ip2).
'BgpNeighbors'(Node1, Ip1, Node2, Ip2) :-
   'BgpMultihopNeighborTo'(Node1, Node2, Ip2),
   'BgpMultihopNeighborTo'(Node2, Node1, Ip1).
'BgpNeighbors'(Node1, Ip1, Node2, Ip2) :-
   'IpReadyInt'(Node1, Int1, Ip1, _),
   'IpReadyInt'(Node2, Int2, Ip2, _),
   'LanAdjacent'(Node1, Int1, Node2, Int2),
   'BgpNeighborIp'(Node1, Ip2),
   'BgpNeighborIp'(Node2, Ip1).

'BgpMultihopNeighborTo'(Node, Neighbor, NeighborIp) :-
   'NetworkOf'(NeighborIp, Prefix_length, NeighborNetwork),
   'InstalledRoute'(Route),
   'Route_network'(Route, NeighborNetwork),
   'Route_node'(Route, Node),
   'IpReadyInt'(Neighbor, _, NeighborIp, Prefix_length),
   'BgpMultihopNeighborIp'(Node, NeighborIp),
   'RemoteAs'(Node, NeighborIp, RemoteAs),
   'LocalAs'(Node, NeighborIp, LocalAs),
   LocalAs \==  RemoteAs .

'BgpNeighborSendCommunity'(Node, NeighborIp) :-
   'SetBgpNeighborSendCommunity'(Node, NeighborNetwork),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'InstalledBgpAdvertisement'(Advert) :-
   'InstalledBgpAdvertisementRoute'(Advert, _).

'InstalledBgpAdvertisementRoute'(Advert, Route ):-
   'BgpAdvertisementRoute'(Advert, Route),
   'InstalledRoute'(Route).

'LocalAs'(Node, NeighborIp, LocalAs) :-
   'SetLocalAs'(Node, NeighborNetwork, LocalAs),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'MaxLocalPref'(Node, Network, MaxLocalPref ):-
   agg(MaxLocalPref = max(LocalPref),(
      'ReceivedBgpAdvertisement'(Advert),
      'BgpAdvertisement_dstNode'(Advert, Node),
      'BgpAdvertisement_localPref'(Advert, LocalPref),
      'BgpAdvertisement_network'(Advert, Network))).

'MaxLocalPrefBgpAdvertisement'(Advert) :-
   'ReceivedBgpAdvertisement'(Advert),
   \+ 'OriginatedBgpNetwork'(Node, Network),
   'BgpAdvertisement_dstNode'(Advert, Node),
   'BgpAdvertisement_localPref'(Advert, LocalPref),
   'BgpAdvertisement_network'(Advert, Network),
   'MaxLocalPref'(Node, Network, LocalPref).

'MinAsPathLengthBgpAdvertisement'(Advert) :-
   'MaxLocalPrefBgpAdvertisement'(Advert),
   'BgpAdvertisement_dstNode'(Advert, Node),
   'BgpAdvertisement_network'(Advert, Network),
   'AdvertisementPathSize'(Advert, BestAsPathSize),
   'MinAsPathSize'(Node, Network, BestAsPathSize).

'MinAsPathSize'(Node, Network, MinSize ):-
   agg(MinSize = min(Size),(
      'MaxLocalPrefBgpAdvertisement'(Advert),
      'BgpAdvertisement_dstNode'(Advert, Node),
      'BgpAdvertisement_network'(Advert, Network),
      'AdvertisementPathSize'(Advert, Size))).

need_PolicyMapMatchAdvert(Map, Advert)
:-
   (
      Type = 'bgp' ;
      Type = 'ibgp'
   ),
   'BgpAdvertisement_srcNode'(Advert, SrcNode),
   'BgpAdvertisement_dstIp'(Advert, DstIp),
   'BgpAdvertisement_type'(Advert, Type),
   'BgpExportPolicy'(SrcNode, DstIp, Map).

'OriginalBgpAdvertisement'(Advert) :-
   'OriginalBgpAdvertisementRoute'(Advert, _).

'ReceivedBgpAdvertisement'(Advert) :-
   'BgpAdvertisement_type'(Advert, Type),
   (
      Type = 'bgp_ti' ;
      Type = 'ibgp_ti'
   ).

'RemoteAs'(Node, NeighborIp, RemoteAs) :-
   'SetRemoteAs'(Node, NeighborNetwork, RemoteAs),
   'NetworkOf'(NeighborIp, _, NeighborNetwork).

'SetBgpDefaultLocalPref'(Node, NeighborNetwork, LocalPref) :-
   'SetBgpDefaultLocalPref_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, LocalPref),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).

'SetBgpNeighborDefaultMetric'(Node, NeighborNetwork, Metric) :-
   'SetBgpNeighborDefaultMetric_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Metric),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).

'SetBgpMultihopNeighborNetwork'(Node, NeighborNetwork) :-
   'SetBgpMultihopNeighborNetwork_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).

'SetBgpNeighborLocalIp'(Node, NeighborNetwork, LocalIp) :-
   'SetBgpNeighborLocalIp_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, LocalIp),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).

'SetBgpNeighborNetwork'(Node, NeighborNetwork) :-
   'SetBgpNeighborNetwork_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).

'SetBgpNeighborSendCommunity'(Node, NeighborNetwork) :-
   'SetBgpNeighborSendCommunity_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).

'SetLocalAs'(Node, NeighborNetwork, LocalAs) :-
   'SetLocalAs_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, LocalAs),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).

'SetRemoteAs'(Node, NeighborNetwork, RemoteAs) :-
   'SetRemoteAs_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, RemoteAs),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork).

'BgpAdvertisement_details'(Type, DstIpBlock, NextHopIp, SrcIp, DstIp, SrcProtocol, SrcNode, DstNode, LocalPref, Med, OriginatorIp, OriginType)
:-
   'BgpAdvertisement_constructor'(Type, DstIpBlock, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, Advert),
   'BgpAdvertisement_originatorIp'(Advert, OriginatorIp).

'CommunityListFirstMatch'(List, Community, FirstLine ):-
   agg(FirstLine = min(Line),( 'CommunityListMatch'(List, Line, Community))).

'CommunityListMatch'(List, Line, Community) :-
   'SetCommunityListLine'(List, Line, Community).
   %'Pneed_CommunityListMatch'(List, Line, Community). %TODO: Implement this

'CommunityListPermit'(List, Line, Community) :-
   'CommunityListFirstMatch'(List, Community, Line),
   'SetCommunityListLinePermit'(List, Line).
% ip is that of a connected neighbor
'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, NextHopIp),
   'FibNeighborIp'(Node, Ip)
:-
   \+ 'HasIp'(Node, Ip),
   'LanAdjacent'(Node, Interface, NextHop, NextHopInt),
   'ConnectedRoute'(Node, Network, Interface),
   'ConnectedRoute'(NextHop, Network, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, Ip, _),
   Ip = NextHopIp,
   Prefix_length = 32.
% drop connected route networks since we have more specific entries for connected neighbors (Or terminate flow sink)
'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, NextHopIp) :-
   \+ 'FibNeighborIp'(Node, Ip),
   'LongestPrefixNetworkMatch'(Node, Ip, MatchNet),
   Prefix_length < 32,
   'BestConnectedRoute'(Route),
   'ConnectedRoute'(Node, MatchNet, RouteInterface),
   'Route_network'(Route, MatchNet),
   'Network_prefix_length'(MatchNet, Prefix_length),
   'Route_node'(Route, Node),
   NextHop = '(none)',
   (
      (
         \+ 'SetFlowSinkInterface'(Node, RouteInterface),
         Interface = 'null_interface',
         NextHopInt = 'null_interface',
         NextHopIp = Ip
      ) ;
      (
         'SetFlowSinkInterface'(Node, RouteInterface),
         Interface = RouteInterface,
         NextHopInt = 'flow_sink_termination',
         'Ip_NONE'(NextHopIp)
      )
   ).
% static non-null interface-only route -- send to all adjacent neighbors who would route
%    the ip itself to an interface other than the one on which the packet was received
'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, NextHopIp) :-
   \+ 'HasIp'(Node, Ip),
   'LongestPrefixNetworkMatch'(Node, Ip, MatchNet),
   'InstalledRoute'(Route),
   'StaticIntRoute'(Route),
   'Route_network'(Route, MatchNet),
   'Network_prefix_length'(MatchNet, Prefix_length),
   'Route_node'(Route, Node),
   'Route_nextHopIp'(Route, RouteNextHopIp),
   'Ip_NONE'(RouteNextHopIp),
   'InterfaceRoute_nextHopInt'(Route, Interface),
   Interface \== 'null_interface',
   'LanAdjacent'(Node, Interface, NextHop, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   (
      'HasIp'(NextHop, Ip) ;
      (
         % proxy arp case -- must be enabled
         \+ 'HasIp'(NextHop, Ip),
         'Fib'(NextHop, Ip, _, NextHopOutInt, _, _, _),
         NextHopOutInt \== NextHopInt
      )
   ).
% static null interface-only route -- drop
'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, NextHopIp) :-
   \+ 'HasIp'(Node, Ip),
   'LongestPrefixNetworkMatch'(Node, Ip, MatchNet),
   'InstalledRoute'(Route),
   'StaticIntRoute'(Route),
   'Route_network'(Route, MatchNet),
   'Network_prefix_length'(MatchNet, Prefix_length),
   'Route_node'(Route, Node),
   'Route_nextHopIp'(Route, NextHopIp),
   NextHop = '(none)',
   'Ip_NONE'(NextHopIp),
   'InterfaceRoute_nextHopInt'(Route, Interface),
   Interface = 'null_interface',
   NextHopInt = 'null_interface'.
% static non-null interface+NextHopIp route -- send to all adjacent neighbors who would route
%    the declared nextHopIp to an interface other than the one on which the packet was received
'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, NextHopIp) :-
   \+ 'HasIp'(Node, Ip),
   'LongestPrefixNetworkMatch'(Node, Ip, MatchNet),
   'InstalledRoute'(Route),
   'StaticIntRoute'(Route),
   'Route_network'(Route, MatchNet),
   'Network_prefix_length'(MatchNet, Prefix_length),
   'Route_node'(Route, Node),
   'Route_nextHopIp'(Route, RouteNextHopIp),
   'Ip_NONE'(IpNone),
   NextHopIp \== IpNone,
   'InterfaceRoute_nextHopInt'(Route, Interface),
   'LanAdjacent'(Node, Interface, NextHop, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   (
      'HasIp'(NextHop, RouteNextHopIp) ;
      (
         % proxy arp case -- must be enabled
         \+ 'HasIp'(NextHop, RouteNextHopIp),
         'Fib'(NextHop, RouteNextHopIp, _, NextHopOutInt, _, _, _),
         NextHopOutInt \== NextHopInt
      )
   ).
% regular non-interface nextHopIp route or flow sink non-interface route
'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, NextHopIp) :-
   \+ 'HasIp'(Node, Ip),
   'LongestPrefixNetworkMatch'(Node, Ip, MatchNet),
   'InstalledRoute'(Route),
   \+ 'InterfaceRoute'(Route),
   'Route_network'(Route, MatchNet),
   'Network_prefix_length'(MatchNet, Prefix_length),
   'Route_node'(Route, Node),
   /*(
      NextHopInt = 'flow_sink_termination' ;
      (
         NextHopIp \== 'Ip_ZERO',

      )
   ),*/
   'Route_nextHopIp'(Route, RouteNextHopIp),
   'Fib'(Node, RouteNextHopIp, _, Interface, NextHop, NextHopInt, NextHopIp).

'FibNetwork'(Node, Network, Interface, NextHop, NextHopInt) :-
   'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, _),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node),
   'Route_network'(Route, Network),
   'Network_address'(Network, Ip),
   'NetworkOf'(Ip, Prefix_length, Network).

'FibNetwork'(Node, Network, Interface, NextHop, NextHopInt) :-
   'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, _),
   'FibNeighborIp'(Node, Ip),
   'Network_address'(Network, Ip),
   'NetworkOf'(Ip, Prefix_length, Network).

'FibDrop'(Node, Ip) :-
   'Fib'(Node, Ip, _, DropInterface, _, _, _),
   DropInterface = 'null_interface'.

'FibForward'(Node, Ip, Interface, NextHop, NextHopInt) :-
   'Fib'(Node, Ip, _, Interface, NextHop, NextHopInt, _),
   NextHopInt \== 'null_interface'.

'FibForwardPolicyRouteNextHopIp'(Node, Ip, Interface, NextHop, NextHopInt) :-
   'Fib'(Node, Ip, _, Interface, NextHop, NextHopInt, _),
   'SetPolicyMapClauseSetNextHopIp'(_, _, Ip).

'FibNeighborUnreachable'(Node, Ip, NeighborIp) :-
   'Fib'(Node, Ip, _, DropInterface, _, _, NeighborIp),
   DropInterface = 'null_interface',
   'Ip_NONE'(IpNone),
   NeighborIp \== IpNone.

'FibRoute'(Node, Ip) :-
   'FibForward'(Node, Ip, _, _, _) ;
   'FibDrop'(Node, Ip).
'ActiveGeneratedRoute'(Route),
   'ActiveGeneratedRouteContributor'(Route, ContributingRoute)
:-
   'GeneratedRoute'(Route),
   'InstalledRoute'(ContributingRoute),
   'Route_network'(ContributingRoute, ContributingNetwork),
   'Route_network'(Route, Network),
   'Route_node'(ContributingRoute, Node),
   'Route_node'(Route, Node),
   'Network_address'(ContributingNetwork, ContributingAddress),
   'Network_address'(Network, StartAddress),
   'Network_end'(Network, EndAddress),
   StartAddress < ContributingAddress,
   ContributingAddress =< EndAddress,
   (
      \+ 'GeneratedRoutePolicy'(Route, _) ;
      (
         'GeneratedRoutePolicy'(Route, Policy),
         'PolicyMapPermitRoute'(Policy, _, ContributingRoute)
      )
   ).

'BestGlobalGeneratedRoute'(Route),
   'InterfaceRoute'(Route),
   'InterfaceRoute_nextHopInt'(Route, NextHopInt),
   'Route_nextHopIp'(Route, NextHopIp)
:-
   'MinAdminContributingRoute'(Route, ContributingRoute),
   'SetGeneratedRouteDiscard'(Node, Network),
   NextHopInt = 'null_interface',
   'Ip_NONE'(NextHopIp),
   'Route_node'(Route, Node),
   'Route_network'(Route, Network),
   'Route_network'(ContributingRoute, ContributingNetwork),
   'Network_address'(ContributingNetwork, ContributingAddress),
   'MinContributingRouteAddress'(Route, ContributingAddress).

'BestGlobalGeneratedRoute'(Route),
   'BestGlobalGeneratedRoute_nextHopIp'(Route, NextHopIp)
:-
   'MinAdminContributingRoute'(Route, ContributingRoute),
   \+ 'SetGeneratedRouteDiscard'(Node, Network),
   'Route_node'(Route, Node),
   'Route_network'(Route, Network),
   'Route_network'(ContributingRoute, ContributingNetwork),
   'Route_nextHopIp'(ContributingRoute, NextHopIp),
   'Network_address'(ContributingNetwork, ContributingAddress),
   'MinContributingRouteAddress'(Route, ContributingAddress).

'BestPerProtocolRoute'(Route) :-
   'BestGlobalGeneratedRoute'(Route).

'GlobalGeneratedRoute'(Route),
   'Route'(Route),
   'GeneratedRoute_constructor'(Node, Network, Type, Route),
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   'SetGeneratedRoute'(Node, Network, Admin),
   Type = 'GeneratedRouteType_GLOBAL',
   Protocol = 'aggregate',
   (
      'SetGeneratedRouteMetric'(Node, Network, Cost) ;
      (
         \+ 'SetGeneratedRouteMetric'(Node, Network, _),
         Cost = 0
      )
   ).

'GeneratedRoutePolicy'(Route, Policy) :-
   'GeneratedRoute'(Route),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'SetGeneratedRoutePolicy'(Node, Network, Policy).

'MinAdminContributingRoute'(Route, ContributingRoute) :-
   'ActiveGeneratedRouteContributor'(Route, ContributingRoute),
   'GlobalGeneratedRoute'(Route),
   'Route_admin'(ContributingRoute, Admin),
   'MinContributingRouteAdmin'(Route, Admin).

'MinBestGlobalGeneratedRoute_nextHopIpInt'(Route, MinNextHopIpInt) :-
   agg(MinNextHopIpInt = min(NextHopIp),
      'BestGlobalGeneratedRoute_nextHopIp'(Route, NextHopIp)).

'MinContributingRouteAddress'(Route, MinAddress ):-
   agg(MinAddress = min(Address),(
      'MinAdminContributingRoute'(Route, ContributingRoute),
      'Route_network'(ContributingRoute, Network),
      'Network_address'(Network, Address))).

'MinContributingRouteAdmin'(Route, MinAdmin ):-
   agg(MinAdmin = min(Admin),(
      'ActiveGeneratedRouteContributor'(Route, ContributingRoute),
      'GlobalGeneratedRoute'(Route),
      'Route_admin'(ContributingRoute, Admin))).

need_PolicyMapMatchRoute(Map, Route) :-
   'SetGeneratedRoutePolicy'(Node, _, Map),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node).

'Route_nextHopIp'(Route, NextHopIp) :-
   'MinBestGlobalGeneratedRoute_nextHopIpInt'(Route, NextHopIp).

'SetGeneratedRoute'(Node, Network, Admin) :-
   'SetGeneratedRoute_flat'(Node, Network_start, Network_end, Prefix_length, Admin),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'SetGeneratedRouteDiscard'(Node, Network) :-
   'SetGeneratedRouteDiscard_flat'(Node, Network_start, Network_end, Prefix_length),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'SetGeneratedRouteMetric'(Node, Network, Metric) :-
   'SetGeneratedRouteMetric_flat'(Node, Network_start, Network_end, Prefix_length, Metric),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'SetGeneratedRoutePolicy'(Node, Network, Map) :-
   'SetGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'Ip'(Ip) :-
   'SetIpInt'(Node, Interface, Ip, Prefix_length).
'HasIp'(Node, Ip) :-
   'IpReadyInt'(Node, _, Ip, _).

'HasNetwork'(Node, Network) :-
   'IpReadyInt'(Node, _, Ip, Prefix_length),
   'NetworkOf'(Ip, Prefix_length, Network).

'IpCount'(Ip, Cnt ):-
   agg(Cnt = count,
      'HasIp'(_, Ip)).

'Network'(Network),
   'Network_address'(Network, Network_start),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network),
   'Network_index'(Network, Network_start, Network_end, Prefix_length),
   'Network_end'(Network, Network_end),
   'Network_prefix_length'(Network, Prefix_length)
:-
   'IpReadyInt'(_, _, Network_start, _),
   Network_end = Network_start,
   Prefix_length = 32.

'IpReadyInt'(Node, Interface, Ip, Prefix_length) :-
   'SetIpInt'(Node, Interface, Ip, Prefix_length),
   'SetActiveInt'(Node, Interface).

'Ip'(X) :-
   'Ip_NONE'(X).

'Ip'(X) :-
   'Ip_ZERO'(X).

'Ip'(NetworkIp) :-
   'SetNetwork'(NetworkIp, Network_start, Network_end, Prefix_length).

'Ip_NONE'(-1).
'Ip_ZERO'(0).

'Network'(Network),
'Network_address'(Network, Network_start),
'Network_constructor'(Network_start, Network_end, Prefix_length, Network),
'Network_index'(Network, Network_start, Network_end, Prefix_length),
'Network_end'(Network, Network_end),
'Network_prefix_length'(Network, Prefix_length)
:-
   'SetNetwork'(_, Network_start, Network_end, Prefix_length).

'NetworkOf'(Ip, Prefix_length, Network ):-
   'Ip'(Ip),
   Network_start =< Ip,
   Ip =< Network_end,
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'IpAccessListDeny'(List, Line, Flow) :-
   'SetIpAccessListLine_deny'(List, Line),
   'IpAccessListFirstMatch'(List, Flow, Line).
'IpAccessListExists'(List) :-
   'SetIpAccessListLine_permit'(List, _).
'IpAccessListExists'(List) :-
   'SetIpAccessListLine_deny'(List, _).

'IpAccessListDeny'(List, -1, Flow) :-
   \+ 'IpAccessListMatch'(List, _, Flow),
   'IpAccessListExists'(List),
   'Flow'(Flow).

'IpAccessListPermit'(List, Line, Flow) :-
   'SetIpAccessListLine_permit'(List, Line),
   'IpAccessListFirstMatch'(List, Flow, Line).

'IpAccessListFirstMatch'(List, Flow, FirstMatchLine ):-
   agg(FirstMatchLine = min(Line),(
      'IpAccessListMatch'(List, Line, Flow))).

'IpAccessListLine'(List, Line) :-
   'SetIpAccessListLine_deny'(List, Line) ;
   'SetIpAccessListLine_permit'(List, Line).

'IpAccessListMatch'(List, Line, Flow) :-
   'IpAccessListMatchDscp'(List, Line, Flow),
   'IpAccessListMatchDstIp'(List, Line, Flow),
   'IpAccessListMatchDstPort'(List, Line, Flow),
   'IpAccessListMatchEcn'(List, Line, Flow),
   'IpAccessListMatchIcmpCode'(List, Line, Flow),
   'IpAccessListMatchIcmpType'(List, Line, Flow),
   'IpAccessListMatchProtocol'(List, Line, Flow),
   'IpAccessListMatchSrcIp'(List, Line, Flow),
   'IpAccessListMatchSrcOrDstIp'(List, Line, Flow),
   'IpAccessListMatchSrcOrDstPort'(List, Line, Flow),
   'IpAccessListMatchSrcPort'(List, Line, Flow),
   'IpAccessListMatchTcpFlags'(List, Line, Flow).

'IpAccessListMatchDscp'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_dscp'(Flow, Dscp),
   (
      \+ 'SetIpAccessListLine_dscp'(List, Line, _) ;
      'SetIpAccessListLine_dscp'(List, Line, Dscp)
   ).

'IpAccessListMatchDstIp'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_dstIp'(Flow, DstIp),
   (
      \+ 'SetIpAccessListLine_dstIpRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_dstIpRange'(List, Line, DstIp_start, DstIp_end),
         DstIp_start =< DstIp,
         DstIp =< DstIp_end
      )
   ).

'IpAccessListMatchDstPort'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_dstPort'(Flow, DstPort),
   (
      \+ 'SetIpAccessListLine_dstPortRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_dstPortRange'(List, Line, DstPort_start, DstPort_end),
         DstPort_start =< DstPort,
         DstPort =< DstPort_end
      )
   ).

'IpAccessListMatchEcn'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_ecn'(Flow, Ecn),
   (
      \+ 'SetIpAccessListLine_ecn'(List, Line, _) ;
      'SetIpAccessListLine_ecn'(List, Line, Ecn)
   ).

'IpAccessListMatchIcmpCode'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_icmpCode'(Flow, IcmpCode),
   (
      \+ 'SetIpAccessListLine_icmpCode'(List, Line, _) ;
      'SetIpAccessListLine_icmpCode'(List, Line, IcmpCode)
   ).

'IpAccessListMatchIcmpType'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_icmpType'(Flow, IcmpType),
   (
      \+ 'SetIpAccessListLine_icmpType'(List, Line, _) ;
      'SetIpAccessListLine_icmpType'(List, Line, IcmpType)
   ).

'IpAccessListMatchProtocol'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_ipProtocol'(Flow, Protocol),
   (
      \+ 'SetIpAccessListLine_protocol'(List, Line, _) ;
      'SetIpAccessListLine_protocol'(List, Line, Protocol)
   ).

'IpAccessListMatchSrcIp'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_srcIp'(Flow, SrcIp),
   (
      \+ 'SetIpAccessListLine_srcIpRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_srcIpRange'(List, Line, SrcIp_start, SrcIp_end),
         SrcIp_start =< SrcIp,
         SrcIp =< SrcIp_end
      )
   ).

'IpAccessListMatchSrcOrDstIp'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_srcIp'(Flow, SrcIp),
   'Flow_dstIp'(Flow, DstIp),
   (
      \+ 'SetIpAccessListLine_srcOrDstIpRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_srcOrDstIpRange'(List, Line, SrcOrDstIp_start, SrcOrDstIp_end),
         (
            (
               SrcOrDstIp_start =< SrcIp,
               SrcIp =< SrcOrDstIp_end
            ) ;
            (
               SrcOrDstIp_start =< DstIp,
               DstIp =< SrcOrDstIp_end
            )
         )
      )
   ).

'IpAccessListMatchSrcOrDstPort'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_srcPort'(Flow, SrcPort),
   'Flow_dstPort'(Flow, DstPort),
   (
      \+ 'SetIpAccessListLine_srcOrDstPortRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_srcOrDstPortRange'(List, Line, SrcOrDstPort_start, SrcOrDstPort_end),
         (
            (
               SrcOrDstPort_start =< SrcPort,
               SrcPort =< SrcOrDstPort_end
            ) ;
            (
               SrcOrDstPort_start =< DstPort,
               DstPort =< SrcOrDstPort_end
            )
         )
      )
   ).

'IpAccessListMatchSrcPort'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_srcPort'(Flow, SrcPort),
   (
      \+ 'SetIpAccessListLine_srcPortRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_srcPortRange'(List, Line, SrcPort_start, SrcPort_end),
         SrcPort_start =< SrcPort,
         SrcPort =< SrcPort_end
      )
   ).

'IpAccessListMatchTcpFlags'(List, Line, Flow) :-
   'Flow'(flow),
   (
      \+ 'SetIpAccessListLine_tcpFlags'(List, Line, _) ;
      (
         'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
         'IpAccessListMatchTcpFlagsCWR'(List, Line, Alternative, Flow),
         'IpAccessListMatchTcpFlagsECE'(List, Line, Alternative, Flow),
         'IpAccessListMatchTcpFlagsURG'(List, Line, Alternative, Flow),
         'IpAccessListMatchTcpFlagsACK'(List, Line, Alternative, Flow),
         'IpAccessListMatchTcpFlagsPSH'(List, Line, Alternative, Flow),
         'IpAccessListMatchTcpFlagsRST'(List, Line, Alternative, Flow),
         'IpAccessListMatchTcpFlagsSYN'(List, Line, Alternative, Flow),
         'IpAccessListMatchTcpFlagsFIN'(List, Line, Alternative, Flow)
      )
   ).

'IpAccessListMatchTcpFlagsCWR'(List, Line, Alternative, Flow) :-
   'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
   'Flow_tcpFlagsCWR'(Flow, Bit),
   (
      \+ 'SetIpAccessListLine_tcpFlagsCWR'(List, Line, Alternative, _) ;
      'SetIpAccessListLine_tcpFlagsCWR'(List, Line, Alternative, Bit)
   ).

'IpAccessListMatchTcpFlagsECE'(List, Line, Alternative, Flow) :-
   'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
   'Flow_tcpFlagsECE'(Flow, Bit),
   (
      \+ 'SetIpAccessListLine_tcpFlagsECE'(List, Line, Alternative, _) ;
      'SetIpAccessListLine_tcpFlagsECE'(List, Line, Alternative, Bit)
   ).

'IpAccessListMatchTcpFlagsURG'(List, Line, Alternative, Flow) :-
   'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
   'Flow_tcpFlagsURG'(Flow, Bit),
   (
      \+ 'SetIpAccessListLine_tcpFlagsURG'(List, Line, Alternative, _) ;
      'SetIpAccessListLine_tcpFlagsURG'(List, Line, Alternative, Bit)
   ).

'IpAccessListMatchTcpFlagsACK'(List, Line, Alternative, Flow) :-
   'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
   'Flow_tcpFlagsACK'(Flow, Bit),
   (
      \+ 'SetIpAccessListLine_tcpFlagsACK'(List, Line, Alternative, _) ;
      'SetIpAccessListLine_tcpFlagsACK'(List, Line, Alternative, Bit)
   ).

'IpAccessListMatchTcpFlagsPSH'(List, Line, Alternative, Flow) :-
   'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
   'Flow_tcpFlagsPSH'(Flow, Bit),
   (
      \+ 'SetIpAccessListLine_tcpFlagsPSH'(List, Line, Alternative, _) ;
      'SetIpAccessListLine_tcpFlagsPSH'(List, Line, Alternative, Bit)
   ).

'IpAccessListMatchTcpFlagsRST'(List, Line, Alternative, Flow) :-
   'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
   'Flow_tcpFlagsRST'(Flow, Bit),
   (
      \+ 'SetIpAccessListLine_tcpFlagsRST'(List, Line, Alternative, _) ;
      'SetIpAccessListLine_tcpFlagsRST'(List, Line, Alternative, Bit)
   ).

'IpAccessListMatchTcpFlagsSYN'(List, Line, Alternative, Flow) :-
   'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
   'Flow_tcpFlagsSYN'(Flow, Bit),
   (
      \+ 'SetIpAccessListLine_tcpFlagsSYN'(List, Line, Alternative, _) ;
      'SetIpAccessListLine_tcpFlagsSYN'(List, Line, Alternative, Bit)
   ).

'IpAccessListMatchTcpFlagsFIN'(List, Line, Alternative, Flow) :-
   'SetIpAccessListLine_tcpFlags'(List, Line, Alternative),
   'Flow_tcpFlagsFIN'(Flow, Bit),
   (
      \+ 'SetIpAccessListLine_tcpFlagsFIN'(List, Line, Alternative, _) ;
      'SetIpAccessListLine_tcpFlagsFIN'(List, Line, Alternative, Bit)
   ).

'GeneratedRoutePolicy'(Route, Policy) :-
   'IsisGeneratedRoute'(Route),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'SetIsisGeneratedRoutePolicy'(Node, Network, Policy).

'IsisGeneratedRoute'(Route),
   'Route'(Route),
   'GeneratedRoute_constructor'(Node, Network, Type, Route),
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   'SetIsisGeneratedRoute'(Node, Network),
   Type = 'GeneratedRouteType_ISIS',
   Protocol = 'aggregate'.

need_PolicyMapMatchRoute(Map, Route) :-
   'SetIsisGeneratedRoutePolicy'(Node, _, Map),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node).

'SetIsisGeneratedRoute'(Node, Network) :-
   'SetIsisGeneratedRoute_flat'(Node, Network_start, Network_end, Prefix_length),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'SetIsisGeneratedRoutePolicy'(Node, Network, Map) :-
   'SetIsisGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).
% (Base case - import routes exported into L1 by L1 neighbors
'IsisL1Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'IsisL1Neighbors'(Node, NodeIntCost, NextHop, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   Node \== NextHop,
   'IsisExport'(NextHop, Network, ExportCost, Protocol),
   Cost = ExportCost + NodeIntCost,
   Protocol = 'isisL1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
'BestIsisL1Route'(Route),
   'IsisL1Network'(Node, Network)
:-
   'IsisL1Route'(Route),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'MinIsisL1RouteCost'(Node, Network, Cost).

'IsisL1EnabledInterface'(Node, Interface) :-
   'SetIsisL1ActiveInterface'(Node, Interface) ;
   'SetIsisL1PassiveInterface'(Node, Interface).
   
'IsisL1Neighbors'(Node1, Cost1, Node2, Int2) :-
   'SetIsisL1Node'(Node1),
   'SetIsisL1Node'(Node2),
   'SetIsisArea'(Node1, Area),
   'SetIsisArea'(Node2, Area),
   'LanAdjacent'(Node1, Int1, Node2, Int2),
   'SetIsisInterfaceCost'(Node1, Int1, Cost1),
   'SetIsisL1ActiveInterface'(Node1, Int1),
   'SetIsisL1ActiveInterface'(Node2, Int2).

% (Base case)
'IsisL1Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'IsisL1Neighbors'(Node, NodeIntCost, NextHop, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'ConnectedRoute'(NextHop, Network, NextHopConnectedInt),
   'IsisL1EnabledInterface'(NextHop, NextHopConnectedInt),
   'SetIsisInterfaceCost'(NextHop, NextHopConnectedInt, NextHopIntCost),
   Cost = NodeIntCost + NextHopIntCost,
   Protocol = 'isisL1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
% (Recursive case)
'IsisL1Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'IsisL1Neighbors'(Node, NodeIntCost, NextHop, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'IsisL1Neighbors'(NextHop, _, SecondHop, _),
   'BestIsisL1Route'(SubRoute),
   'Route_cost'(SubRoute, SubCost),
   'Route_network'(SubRoute, Network),
   'Route_nextHopIp'(SubRoute, SecondHopIp),
   'Route_node'(SubRoute, NextHop),
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Cost = SubCost + NodeIntCost,
   Protocol = 'isisL1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).

'MinIsisL1RouteCost'(Node, Network, MinCost ):-
   agg(MinCost = min(Cost),(
      'IsisL1Route'(Route),
      'Route_cost'(Route, Cost),
      'Route_network'(Route, Network),
      'Route_node'(Route, Node))).

% (Base case - import routes exported into L2 by L2 neighbors
'IsisL2Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'IsisL2Neighbors'(Node, NodeIntCost, NextHop, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   Node \== NextHop,
   'IsisExport'(NextHop, Network, ExportCost, Protocol),
   Cost = ExportCost + NodeIntCost,
   Protocol = 'isisL2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).

'BestIsisL2Route'(Route) :-
   'IsisL2Route'(Route),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'MinIsisL2RouteCost'(Node, Network, Cost),
   \+ 'IsisL1Network'(Node, Network).

'IsisL2EnabledInterface'(Node, Interface) :-
   'SetIsisL2ActiveInterface'(Node, Interface) ;
   'SetIsisL2PassiveInterface'(Node, Interface).
   
'IsisL2Neighbors'(Node1, Cost1, Node2, Int2) :-
   'SetIsisL2Node'(Node1),
   'SetIsisL2Node'(Node2),
   'SetIsisArea'(Node1, _),
   'SetIsisArea'(Node2, _),
   'LanAdjacent'(Node1, Int1, Node2, Int2),
   'SetIsisInterfaceCost'(Node1, Int1, Cost1),
   'SetIsisL2ActiveInterface'(Node1, Int1),
   'SetIsisL2ActiveInterface'(Node2, Int2).

% (Base case)
'IsisL2Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'IsisL2Neighbors'(Node, NodeIntCost, NextHop, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'ConnectedRoute'(NextHop, Network, NextHopConnectedInt),
   'IsisL2EnabledInterface'(NextHop, NextHopConnectedInt),
   'SetIsisInterfaceCost'(NextHop, NextHopConnectedInt, NextHopIntCost),
   Cost = NodeIntCost + NextHopIntCost,
   Protocol = 'isisL2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
% (Recursive (Forward L2 Routes)
'IsisL2Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'IsisL2Neighbors'(Node, NodeIntCost, NextHop, NextHopInt),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'IsisL2Neighbors'(NextHop, _, SecondHop, _),
   'BestIsisL2Route'(SubRoute),
   'Route_cost'(SubRoute, SubCost),
   'Route_network'(SubRoute, Network),
   'Route_nextHopIp'(SubRoute, SecondHopIp),
   'Route_node'(SubRoute, NextHop),
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Cost = SubCost + NodeIntCost,
   Protocol = 'isisL2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).

'MinIsisL2RouteCost'(Node, Network, MinCost ):-
   agg(MinCost = min(Cost),(
      'IsisL2Route'(Route),
      'Route_cost'(Route, Cost),
      'Route_network'(Route, Network),
      'Route_node'(Route, Node))).
% (Route redistribution)
'IsisExport'(Node, Network, NewCost, Protocol) :-
   (
      'InstalledRoute'(Route) ;
      (
         'IsisGeneratedRoute'(Route),
         'ActiveGeneratedRoute'(Route)
      )
   ),
   \+ 'NonIsisExportableRoute'(Route),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'SetIsisOutboundPolicyMap'(Node, Map),
   (
      'SetPolicyMapIsisExternalRouteType'(Map, Protocol) ;
      (
         \+ 'SetPolicyMapIsisExternalRouteType'(Map, _),
         'SetPolicyMapClauseSetProtocol'(Map, Clause, Protocol)
      )
   ),
   'PolicyMapPermitRoute'(Map, Clause, Route),
   (
      'SetPolicyMapClauseSetMetric'(Map, Clause, NewCost) ;
      (
         \+ 'SetPolicyMapClauseSetMetric'(Map, Clause, _),
         'Route_cost'(Route, NewCost)
      )
   ),
   (
      \+ 'ConnectedRoute'(Node, Network, _) ;
      (
         'ConnectedRoute'(Node, Network, Interface),
         \+ 'SetIsisInterfaceCost'(Node, Interface, _)
      )
   ).

need_PolicyMapMatchRoute(Map, Route) :-
   'SetIsisOutboundPolicyMap'(Node, Map),
   'Route_node'(Route, Node),
   (
      'InstalledRoute'(Route) ;
      (
         'IsisGeneratedRoute'(Route),
         'ActiveGeneratedRoute'(Route)
      )
   ).
'BestPerProtocolRoute'(Route) :-
   'BestIsisL1Route'(Route) ;
   'BestIsisL2Route'(Route).
'LanAdjacent'(Node1, Int1, Node2, Int2) :-
   'SamePhysicalSegment'(Node1, Int1, Node2, Int2).

/*
% adjacent access vlan interfaces
'LanAdjacent'(Node1, Int1, Node2, Int2) :-
   'SamePhysicalSegment'(Node1, Int1, Switch, SInt1),
   'SwitchportAccess'(Switch, SInt1, Vlan),
   'SwitchportAccess'(Switch, SInt2, Vlan),
   'LanAdjacent'(Node2, Int2, Switch, SInt2).
% access vlans are trunked natively
'LanAdjacent'(Node1, Int1, Node2, Int2) :-
   'SamePhysicalSegment'(Node1, Int1, Switch1, SInt11),
   'SwitchportAccess'(Switch1, SInt11, Vlan),
   'SwitchportTrunkAllows'(Switch1, SInt12, Vlan),
   'SwitchportTrunkNative'(Switch1, SInt12, Vlan),
   'SamePhysicalSegment'(Switch1, SInt12, Switch2, SInt21),
   'SwitchportTrunkNative'(Switch2, SInt21, Vlan),
   'SwitchportTrunkAllows'(Switch2, SInt21, Vlan),
   'SwitchportAccess'(Switch2, SInt22, Vlan),
   'LanAdjacent'(Node2, Int2, Switch2, SInt22).
% access vlans are separate from each native vlan in the trunk
'LanAdjacent'(Node1, Int1, Node2, Int2) :-
   'SamePhysicalSegment'(Node1, Int1, Switch1, SInt11),
   'SwitchportAccess'(Switch1, SInt11, Vlan),
   'SwitchportTrunkAllows'(Switch1, SInt12, Vlan),
   'SwitchportTrunkNative'(Switch1, SInt12, Vlan1),
   Vlan \== Vlan1,
   'SetSwitchportTrunkEncapsulation'(Switch1, SInt12, Encap),
   'SamePhysicalSegment'(Switch1, SInt12, Switch2, SInt21),
   'SetSwitchportTrunkEncapsulation'(Switch1, SInt12, Encap),
   Vlan \== Vlan2,
   'SwitchportTrunkNative'(Switch2, SInt21, Vlan2),
   'SwitchportTrunkAllows'(Switch2, SInt21, Vlan),
   'SwitchportAccess'(Switch2, SInt22, Vlan),
   'LanAdjacent'(Node2, Int2, Switch2, SInt22).
% vlan adjacencies
'LanAdjacent'(Node1, Int1, Node2, Int2) :-
   'VlanAdjacent'(Node1, Int1, Node2, Int2).

'SwitchportAccess'(Switch, Interface, Vlan) :-
   'SetSwitchportAccess'(Switch, Interface, Vlan),
   'SetActiveInt'(Switch, Interface).

'SwitchportTrunkAllows'(Switch, Interface, Vlan) :-
   'SetSwitchportTrunkAllows'(Switch, Interface, VlanStart, VlanEnd),
   'UsedVlan'(Vlan),
   'VlanNumber'[Vlan] >= 'VlanNumber'[VlanStart],
   'VlanNumber'[Vlan] =< 'VlanNumber'[VlanEnd].

'SwitchportTrunkNative'(Switch, Interface, Vlan) :-
   'SetSwitchportTrunkNative'(Switch, Interface, Vlan),
   'SetActiveInt'(Switch, Interface).

'UsedVlan'(Vlan) :-
   'SetVlanInterface'(_, _, Vlan).

'VlanAdjacent'(Node1, VlanInt1, Node2, VlanInt2) :-
   'SetVlanInterface'(Node1, VlanInt1, Vlan),
   'SetVlanInterface'(Node2, VlanInt2, Vlan),
   'SwitchportTrunkAllows'(Node1, Int1, Vlan),
   'SwitchportTrunkAllows'(Node2, Int2, Vlan),
   'LanAdjacent'(Node1, Int1, Node2, Int2).
'VlanAdjacent'(Node1, VlanInt1, Node2, VlanInt2) :-
   Node1 \== Node2,
   Node1 \== Node3,
   Node2 \== Node3,
   'VlanAdjacent'(Node1, VlanInt1, Node3, _),
   'VlanAdjacent'(Node3, _, Node2, VlanInt2).

'VlanNumber'(x, n ) :-
   'Vlan_number'(x:n).
*/
'Ip'(Ip) :-
   'SetOspfRouterId'(Node, Ip).
'BestOspfE1Route'(Route),
   'OspfE1Network'(Node, Network)
:-
   'OspfE1Route'(Route),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'MinOspfE1RouteCost'(Node, Network, Cost),
   \+ 'OspfNetwork'(Node, Network),
   \+ 'OspfIANetwork'(Node, Network).

'MinOspfE1RouteCost'(Node, Network, MinCost ):-
   agg(MinCost = min(Cost),(
      'OspfE1Route'(Route),
      'Route_cost'(Route, Cost),
      'Route_network'(Route, Network),
      'Route_node'(Route, Node))).

% (Base case) Import ospfE1 routes exported by ospf neighbors
'OspfE1Route'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'OspfE1Route_constructor'(Advertiser, Node, Network, NextHopIp, Route),
   'OspfRoute_advertiser'(Route, Advertiser),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   Cost = ExportCost + CostToAdvertiser,
   \+ 'ConnectedRoute'(Node, Network, _),
   'OspfNeighbors'(Node, CostToAdvertiser, NextHop, NextHopInt, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   Node \== Advertiser,
   Node \== NextHop,
   Advertiser = NextHop,
   'OspfExport'(Advertiser, Network, ExportCost, Protocol),
   Protocol = 'ospfE1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
% (Recursive case) Propagate ospfE1 over ospf
'OspfE1Route'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'OspfE1Route_constructor'(Advertiser, Node, Network, NextHopIp, Route),
   'OspfRoute_advertiser'(Route, Advertiser),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, NodeIntCost, NextHop, NextHopInt, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'OspfNeighbors'(NextHop, _, SecondHop, _, Area),
   'BestOspfE1Route'(SubRoute),
   'OspfRoute_advertiser'(SubRoute, Advertiser),
   'Route_cost'(SubRoute, SubCost),
   'Route_network'(SubRoute, Network),
   'Route_nextHopIp'(SubRoute, SecondHopIp),
   'Route_node'(SubRoute, NextHop),
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Node \== Advertiser,
   Cost = SubCost + NodeIntCost,
   Protocol = 'ospfE1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
'BestOspfE2Route'(Route) :-
   'OspfE2Route'(Route),
   'OspfRoute_costToAdvertiser'(Route, CostToAdvertiser),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'MinOspfE2RouteCostToAdvertiser'(Node, Network, CostToAdvertiser),
   \+ 'OspfE1Network'(Node, Network),
   \+ 'OspfNetwork'(Node, Network),
   \+ 'OspfIANetwork'(Node, Network).

'MinOspfE2RouteCostToAdvertiser'(Node, Network, MinCostToAdvertiser ):-
   agg(MinCostToAdvertiser = min(CostToAdvertiser),(
      'OspfE2Route'(Route),
      'OspfRoute_costToAdvertiser'(Route, CostToAdvertiser),
      'Route_network'(Route, Network),
      'Route_node'(Route, Node))).   

% (Base case) Import ospfE2 routes exported by ospf neighbors
'OspfE2Route'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'OspfE2Route_constructor'(Advertiser, CostToAdvertiser, Node, Network, NextHopIp, Route),
   'OspfRoute_advertiser'(Route, Advertiser),
   'OspfRoute_advertiserIp'(Route, AdvertiserIp),
   'OspfRoute_costToAdvertiser'(Route, CostToAdvertiser),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'OspfNeighbors'(Node, CostToAdvertiser, NextHop, NextHopInt, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   Node \== Advertiser,
   Node \== NextHop,
   Advertiser = NextHop,
   'SetOspfRouterId'(Advertiser, AdvertiserIp),
   'OspfExport'(Advertiser, Network, Cost, Protocol),
   Protocol = 'ospfE2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
% (Recursive case) Propagate ospfE2 over ospf
'OspfE2Route'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'OspfE2Route_constructor'(Advertiser, CostToAdvertiser, Node, Network, NextHopIp, Route),
   'OspfRoute_advertiser'(Route, Advertiser),
   'OspfRoute_advertiserIp'(Route, AdvertiserIp),
   'OspfRoute_costToAdvertiser'(Route, CostToAdvertiser),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, NodeIntCost, NextHop, NextHopInt, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'OspfNeighbors'(NextHop, _, SecondHop, _, Area),
   'BestOspfE2Route'(SubRoute),
   'Route_cost'(SubRoute, Cost),
   'Route_network'(SubRoute, Network),
   'Route_nextHopIp'(SubRoute, SecondHopIp),
   'Route_node'(SubRoute, NextHop),
   'OspfRoute_advertiser'(SubRoute, Advertiser),
   'OspfRoute_advertiserIp'(SubRoute, AdvertiserIp),
   'OspfRoute_costToAdvertiser'(SubRoute, SubCost),
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Node \== Advertiser,
   CostToAdvertiser = SubCost + NodeIntCost,
   Protocol = 'ospfE2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
need_PolicyMapMatchRoute(Map, Route) :-
   'SetOspfOutboundPolicyMap'(Node, Map),
   'Route_node'(Route, Node),
   (
      'InstalledRoute'(Route) ;
      (
         'OspfGeneratedRoute'(Route),
         'ActiveGeneratedRoute'(Route)
      )
   ).

'OspfExport'(Node, Network, NewCost, Protocol) :-
   (
      'InstalledRoute'(Route) ;
      (
         'OspfGeneratedRoute'(Route),
         'ActiveGeneratedRoute'(Route)
      )
   ),
   \+ 'NonOspfExportableRoute'(Route),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'SetOspfOutboundPolicyMap'(Node, Map),
   'SetPolicyMapOspfExternalRouteType'(Map, Protocol),
   'PolicyMapPermitRoute'(Map, Clause, Route),
   'SetPolicyMapClauseSetMetric'(Map, Clause, NewCost),
   (
      \+ 'ConnectedRoute'(Node, Network, _) ;
      (
         'ConnectedRoute'(Node, Network, Interface),
         \+ 'SetOspfInterface'(Node, Interface, _)
      )
   ).

'OspfOutboundPolicyClauseSetMetric'(Map, Clause, Metric) :-
   'SetOspfOutboundPolicyMap'(_, Map),
   'SetPolicyMapClauseSetMetric'(Map, Clause, Metric).
'GeneratedRoutePolicy'(Route, Policy) :-
   'OspfGeneratedRoute'(Route),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'SetOspfGeneratedRoutePolicy'(Node, Network, Policy).

need_PolicyMapMatchRoute(Map, Route) :-
   'SetOspfGeneratedRoutePolicy'(Node, _, Map),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node).

'OspfGeneratedRoute'(Route),
   'Route'(Route),
   'GeneratedRoute_constructor'(Node, Network, Type, Route),
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   'SetOspfGeneratedRoute'(Node, Network),
   Type = 'GeneratedRouteType_OSPF',
   Protocol = 'aggregate'.

'SetOspfGeneratedRoute'(Node, Network) :-
   'SetOspfGeneratedRoute_flat'(Node, Network_start, Network_end, Prefix_length),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'SetOspfGeneratedRoutePolicy'(Node, Network, Map) :-
   'SetOspfGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).
'BestOspfIARoute'(Route),
   'OspfIANetwork'(Node, Network)
:-
   'OspfIARoute'(Route),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'MinOspfIARouteCost'(Node, Network, Cost),
   \+ 'OspfNetwork'(Node, Network).

'MinOspfIARouteCost'(Node, Network, MinCost ):-
   agg(MinCost = min(Cost),(
      'OspfIARoute'(Route),
      'Route_cost'(Route, Cost),
      'Route_network'(Route, Network),
      'Route_node'(Route, Node))).

% distribute connected routes from another area into backbone area
'OspfIARoute'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, NodeIntCost, NextHop, NextHopInt, 0),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'ConnectedRoute'(NextHop, Network, NextHopConnectedInt),
   'SetOspfInterface'(NextHop, NextHopConnectedInt, Area),
   Area \== 0,
   'SetOspfInterfaceCost'(NextHop, NextHopConnectedInt, NextHopIntCost),
   Cost = NodeIntCost + NextHopIntCost,
   Protocol = 'ospfIA',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
% propagate ospf ia routes through backbone area
'OspfIARoute'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, NodeIntCost, NextHop, NextHopInt, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'OspfNeighbors'(NextHop, _, SecondHop, _, Area),
   'BestOspfIARoute'(SubRoute),
   'Route_cost'(SubRoute, SubCost),
   'Route_network'(SubRoute, Network),
   'Route_nextHopIp'(SubRoute, SecondHopIp),
   'Route_node'(SubRoute, NextHop),
   'HasIp'(SecondHop, SecondHopIp),
   Area = 0,
   Node \== SecondHop,
   Cost = SubCost + NodeIntCost,
   Protocol = 'ospfIA',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).

%TODO: OSPF IA Routes propagated from OSPF routes (Not just connected routes) In another area
'BestOspfRoute'(Route),
   'OspfNetwork'(Node, Network)
:-
   'OspfRoute'(Route),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'MinOspfRouteCost'(Node, Network, Cost).

'MinOspfRouteCost'(Node, Network, MinCost ):-
   agg(MinCost = min(Cost),(
      'OspfRoute'(Route),
      'Route_cost'(Route, Cost),
      'Route_network'(Route, Network),
      'Route_node'(Route, Node))).

% (Base case) Connected route on ospf-enabled interface
'OspfRoute'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, NodeIntCost, NextHop, NextHopInt, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'ConnectedRoute'(NextHop, Network, NextHopConnectedInt),
   'SetOspfInterface'(NextHop, NextHopConnectedInt, Area),
   'SetOspfInterfaceCost'(NextHop, NextHopConnectedInt, NextHopIntCost),
   Cost = NodeIntCost + NextHopIntCost,
   Protocol = 'ospf',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
% (Recursive case) Propagate ospf over ospf
'OspfRoute'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, NodeIntCost, NextHop, NextHopInt, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'OspfNeighbors'(NextHop, _, SecondHop, _, Area),
   'BestOspfRoute'(SubRoute),
   'Route_cost'(SubRoute, SubCost),
   'Route_network'(SubRoute, Network),
   'Route_nextHopIp'(SubRoute, SecondHopIp),
   'Route_node'(SubRoute, NextHop),
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Cost = SubCost + NodeIntCost,
   Protocol = 'ospf',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin).
'BestPerProtocolRoute'(Route) :-
   'BestOspfRoute'(Route) ;
   'BestOspfE1Route'(Route) ;
   'BestOspfE2Route'(Route) ;
   'BestOspfIARoute'(Route).

'OspfNeighbors'(Node1, Cost1, Node2, Int2, Area) :-
   'OspfNode'(Node1, Int1, Cost1, Network, Area),
   'OspfNode'(Node2, Int2, Cost2, Network, Area),
   'LanAdjacent'(Node1, Int1, Node2, Int2).

'OspfNode'(Node, Interface, Cost, Network, Area) :-
   'IpReadyInt'(Node, Interface, Ip, Prefix_length),
   'SetOspfInterfaceCost'(Node, Interface, Cost),
   'SetOspfInterface'(Node, Interface, Area),
   'NetworkOf'(Ip, Prefix_length, Network).
'PolicyMap'(Map) :-
   'SetPolicyMapClauseAddCommunity'(Map, Clause, Community).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseDeleteCommunity'(Map, Clause, Community).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseDeny'(Map, Clause).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseMatchAcl'(Map, Clause, Acl).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseMatchAsPath'(Map, Clause, AsPath).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseMatchColor'(Map, Clause, Color).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseMatchCommunityList'(Map, Clause, List).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseMatchInterface'(Map, Clause, Node, Interface).

'PolicyMap'(Map),
'Ip'(NeighborIp)
:-
   'SetPolicyMapClauseMatchNeighbor'(Map, Clause, NeighborIp).

'PolicyMap'(Map),
'PolicyMap'(Policy)
:-
   'SetPolicyMapClauseMatchPolicy'(Map, Clause, Policy).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseMatchProtocol'(Map, Clause, Protocol).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseMatchRouteFilter'(Map, Clause, Filter).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseMatchTag'(Map, Clause, Tag).

'PolicyMap'(Map) :-
   'SetPolicyMapClausePermit'(Map, Clause).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseSetCommunity'(Map, Clause, Community).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseSetCommunityNone'(Map, Clause).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseSetLocalPreference'(Map, Clause, LocalPref).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseSetMetric'(Map, Clause, Metric).

'PolicyMap'(Map),
'Ip'(NextHopIp)
:-
   'SetPolicyMapClauseSetNextHopIp'(Map, Clause, NextHopIp).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseSetOriginType'(Map, Clause, OriginType).

'PolicyMap'(Map) :-
   'SetPolicyMapClauseSetProtocol'(Map, Clause, Protocol).

'PolicyMap'(Map) :-
   'SetPolicyMapIsisExternalRouteType'(Map, Protocol).

'PolicyMap'(Map) :-
   'SetPolicyMapOspfExternalRouteType'(Map, Protocol).
need_AsPathMatchAdvert(AsPath, Advert) :-
   need_PolicyMapMatchAdvert(Map, Advert),
   'SetPolicyMapClauseMatchAsPath'(Map, _, AsPath).

need_PolicyMapMatchAdvert(Map, Advert) :-
   need_PolicyMapMatchAdvert(ReferringMap, Advert),
   'SetPolicyMapClauseMatchPolicy'(ReferringMap, _, Map).

need_PolicyMapMatchAdvert(Map, Advert) :-
   need_PolicyMapMatchAdvert(ReferringMap, Advert),
   'SetPolicyMapClauseMatchPolicyConjunction'(ReferringMap, _, Map).

need_PolicyMapMatchRoute(Map, Route) :-
   need_PolicyMapMatchRoute(ReferringMap, Route),
   'SetPolicyMapClauseMatchPolicy'(ReferringMap, _, Map).

need_PolicyMapMatchRoute(Map, Route) :-
   need_PolicyMapMatchRoute(ReferringMap, Route),
   'SetPolicyMapClauseMatchPolicyConjunction'(ReferringMap, _, Map).

% policy maps for advertisements
need_RouteFilterMatchNetwork(List, Network) :-
   need_PolicyMapMatchAdvert(Map, Advert),
   'BgpAdvertisement_network'(Advert, Network),
   'SetPolicyMapClauseMatchRouteFilter'(Map, _, List).
% policy maps for routes
need_RouteFilterMatchNetwork(List, Network) :-
   need_PolicyMapMatchRoute(Map, Route),
   'Route_network'(Route, Network),
   'SetPolicyMapClauseMatchRouteFilter'(Map, _, List).

'PolicyMapClauseMatchAdvert'(Map, Clause, Advert) :-
   need_PolicyMapMatchAdvert(Map, Advert),
   'PolicyMapHasClause'(Map, Clause),
   (
      \+ 'SetPolicyMapClauseMatchAsPath'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchAsPath'(Map, Clause, AsPath),
         'AsPathPermitAdvert'(AsPath, Advert)
      )
   ),
   (
      \+ 'SetPolicyMapClauseMatchRouteFilter'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchRouteFilter'(Map, Clause, Filter),
         'BgpAdvertisement_network'(Advert, DstIpBlock),
         'RouteFilterPermitNetwork'(Filter, DstIpBlock) 
      )
   ),
   (
      \+ 'SetPolicyMapClauseMatchNeighbor'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchNeighbor'(Map, Clause, NeighborIp),
         (
            'BgpAdvertisement_srcIp'(Advert, NeighborIp );
            'BgpAdvertisement_dstIp'(Advert, NeighborIp)
         )
      )
   ),
   (
      \+ 'SetPolicyMapClauseMatchPolicy'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchPolicy'(Map, Clause, Policy),
         'PolicyMapPermitAdvert'(Policy, _, Advert)
      )
   ),
   (
      \+ 'SetPolicyMapClauseMatchPolicyConjunction'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchPolicyConjunction'(Map, Clause, Policy),
         \+ 'PolicyMapConjunctionDenyAdvert'(Policy, Advert)
      )
   ),
   (
      \+ 'SetPolicyMapClauseMatchCommunityList'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchCommunityList'(Map, Clause, CommunityList),
         'AdvertisementCommunity'(Advert, Community),
         'CommunityListPermit'(CommunityList, _, Community) 
      )
   ).

'PolicyMapConjunctionDenyAdvert'(Policy, Advert) :-
   need_PolicyMapMatchAdvert(Map, Advert),
   'SetPolicyMapClauseMatchPolicyConjunction'(Map, _, Policy),
   'PolicyMapDenyAdvert'(Policy, Advert).

'PolicyMapConjunctionDenyRoute'(Policy, Route) :-
   need_PolicyMapMatchRoute(Map, Route),
   'SetPolicyMapClauseMatchPolicyConjunction'(Map, _, Policy),
   'PolicyMapDenyRoute'(Policy, Route).

'PolicyMapClauseMatchFlow'(Map, Clause, Flow) :-
   need_PolicyMapMatchFlow(Map, Flow),
   'PolicyMapHasClause'(Map, Clause),
   (
      \+ 'SetPolicyMapClauseMatchAcl'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchAcl'(Map, Clause, Acl),
         'IpAccessListPermit'(Acl, _, Flow)
      )
   ).

'PolicyMapClauseMatchRoute'(Map, Clause, Route) :-
   %TODO: Complete matching here
   need_PolicyMapMatchRoute(Map, Route),
   'PolicyMapHasClause'(Map, Clause),
   % protocol
   (
      \+ 'SetPolicyMapClauseMatchProtocol'(Map, Clause, _) ;
      (
         'Route_protocol'(Route, Protocol),
         'SetPolicyMapClauseMatchProtocol'(Map, Clause, Protocol)
      )
   ),
   % interface
   (
      \+ 'SetPolicyMapClauseMatchInterface'(Map, Clause, _, _) ;
      (
         'Route_network'(Route, Network),
         'ConnectedRoute'(Node, Network, Interface),
         'SetPolicyMapClauseMatchInterface'(Map, Clause, Node, Interface)
      )
   ),
   % policy
   (
      \+ 'SetPolicyMapClauseMatchPolicy'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchPolicy'(Map, Clause, Policy),
         'PolicyMapPermitRoute'(Policy, _, Route)
      )
   ),
   % policy conjunction
   (
      \+ 'SetPolicyMapClauseMatchPolicyConjunction'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchPolicyConjunction'(Map, Clause, Policy),
         \+ 'PolicyMapConjunctionDenyRoute'(Policy, Route)
      )
   ),
   % RouteFilter
   (
      \+ 'SetPolicyMapClauseMatchRouteFilter'(Map, Clause, _);
      (
         'SetPolicyMapClauseMatchRouteFilter'(Map, Clause, Filter),
         'Route_network'(Route, Network),
         'RouteFilterPermitNetwork'(Filter, Network)
      )
   ),
   % tag
   (
      \+ 'SetPolicyMapClauseMatchTag'(Map, Clause, _);
      (
         'SetPolicyMapClauseMatchTag'(Map, Clause, Tag),
         'Route_tag'(Route, Tag)
      )
   ).

'PolicyMapClauseTransformAdvert'(Map, Clause, PrevAdvert, NextHopIp, LocalPref, OriginType, Med, SrcProtocol)
:-
   'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, PrevNextHopIp ),
   'BgpAdvertisement_localPref'(PrevAdvert, PrevLocalPref), 
   'BgpAdvertisement_originType'(PrevAdvert, PrevOriginType),
   'BgpAdvertisement_med'(PrevAdvert, PrevMed),
   'BgpAdvertisement_srcProtocol'(PrevAdvert, PrevSrcProtocol),
   %TODO: Complete untransformed cases
   (
      'SetPolicyMapClauseSetNextHopIp'(Map, Clause, NextHopIp);
      (
         \+ 'SetPolicyMapClauseSetNextHopIp'(Map, Clause, _),
         PrevNextHopIp = NextHopIp
      )
   ),
   (
      'SetPolicyMapClauseSetLocalPreference'(Map, Clause, LocalPref);
      (
         \+ 'SetPolicyMapClauseSetLocalPreference'(Map, Clause, _),
         PrevLocalPref = LocalPref
      )
   ),
   (
      'SetPolicyMapClauseSetOriginType'(Map, Clause, OriginType);
      (
         \+ 'SetPolicyMapClauseSetOriginType'(Map, Clause, _),
         PrevOriginType = OriginType
      )
   ),
   (
      'SetPolicyMapClauseSetMetric'(Map, Clause, Med);
      (
         \+ 'SetPolicyMapClauseSetMetric'(Map, Clause, _),
         PrevMed = Med
      )
   ),
   PrevSrcProtocol = SrcProtocol.

'PolicyMapDenyAdvert'(Map, Advert) :-
   'PolicyMapFirstMatchAdvert'(Map, Advert, Clause),
   \+ 'SetPolicyMapClausePermit'(Map, Clause).
'PolicyMapDenyAdvert'(Map, Advert) :-
   need_PolicyMapMatchAdvert(Map, Advert),
   \+ 'PolicyMapClauseMatchAdvert'(Map, _, Advert).

'PolicyMapDenyFlow'(Map, Flow) :-
   'PolicyMapFirstMatchFlow'(Map, Flow, Clause),
   \+ 'SetPolicyMapClausePermit'(Map, Clause).
'PolicyMapDenyFlow'(Map, Flow) :-
   need_PolicyMapMatchFlow(Map, Flow),
   \+ 'PolicyMapClauseMatchFlow'(Map, _, Flow).

'PolicyMapDenyRoute'(Map, Route) :-
   'PolicyMapFirstMatchRoute'(Map, Route, Clause),
   \+ 'SetPolicyMapClausePermit'(Map, Clause).
'PolicyMapDenyRoute'(Map, Route) :-
   need_PolicyMapMatchRoute(Map, Route),
   \+ 'PolicyMapClauseMatchRoute'(Map, _, Route).

'PolicyMapFirstMatchAdvert'(Map, Advert, FirstClause)
:-
   agg(FirstClause = min(Clause),(
      'PolicyMapClauseMatchAdvert'(Map, Clause, Advert))).

'PolicyMapFirstMatchFlow'(Map, Flow, FirstClause)
:-
   agg(FirstClause = min(Clause),(
      'PolicyMapClauseMatchFlow'(Map, Clause, Flow))).

'PolicyMapFirstMatchRoute'(Map, Route, FirstClause ):-
   agg(FirstClause = min(Clause),(
      'PolicyMapClauseMatchRoute'(Map, Clause, Route))).

'PolicyMapHasClause'(Map, Clause) :-
   'SetPolicyMapClauseDeny'(Map, Clause);
   'SetPolicyMapClausePermit'(Map, Clause).

'PolicyMapPermitAdvert'(Map, Clause, Advert) :-
   'PolicyMapFirstMatchAdvert'(Map, Advert, Clause),
   'SetPolicyMapClausePermit'(Map, Clause).

'PolicyMapPermitFlow'(Map, Clause, Flow) :-
   'PolicyMapFirstMatchFlow'(Map, Flow, Clause),
   'SetPolicyMapClausePermit'(Map, Clause).

'PolicyMapPermitRoute'(Map, Clause, Route) :-
   'PolicyMapFirstMatchRoute'(Map, Route, Clause),
   'SetPolicyMapClausePermit'(Map, Clause).

'Ip'(Ip) :-
   'SetBgpAdvertisementIp'(Ip).

'Network'(Network),
'Network_address'(Network, Network_start),
'Network_constructor'(Network_start, Network_end, Prefix_length, Network),
'Network_index'(Network, Network_start, Network_end, Prefix_length),
'Network_end'(Network, Network_end),
'Network_prefix_length'(Network, Prefix_length)
:-
   'SetBgpAdvertisementIp'(Ip),
   Network_start = Ip,
   Network_end = Ip,
   Prefix_length = 32.

'SetBgpAdvertisementIp'(NextHopIp),
'SetBgpAdvertisementIp'(SrcIp),
'SetBgpAdvertisementIp'(DstIp),
'SetBgpAdvertisementIp'(OriginatorIp)
:-
   'SetBgpAdvertisement_flat'(PcIndex, Type, Network_start, Network_end, Prefix_length, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, OriginatorIp).

'Ip'(Ip1),
'Ip'(Ip2)
:-
   'SetIbgpNeighbors'(Node1, Ip1, Node2, Ip2).

'Ip'(NextHopIp) :-
   'SetPrecomputedRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, Admin, Cost, Protocol, Tag).

'AdvertisementClusterId'(Advert, ClusterId) :-
   'SetBgpAdvertisementClusterId'(PcIndex, ClusterId),
   'PrecomputedAdvertisement_index'(Advert, PcIndex).

'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, Network, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, Advert),
'BgpAdvertisement_dstIp'(Advert, DstIp),
'BgpAdvertisement_dstNode'(Advert, DstNode),
'BgpAdvertisement_localPref'(Advert, LocalPref),
'BgpAdvertisement_med'(Advert, Med),
'BgpAdvertisement_network'(Advert, Network),
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp),
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp),
'BgpAdvertisement_originType'(Advert, OriginType),
'BgpAdvertisement_srcIp'(Advert, SrcIp),
'BgpAdvertisement_srcNode'(Advert, SrcNode),
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol),
'BgpAdvertisement_type'(Advert, Type),
'PrecomputedAdvertisement_index'(Advert, PcIndex)
:-
   'SetBgpAdvertisement_flat'(PcIndex, Type, Network_start, Network_end, Prefix_length, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, OriginatorIp),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

'AdvertisementCommunity'(Advert, Community) :-
   'SetBgpAdvertisementCommunity'(PcIndex, Community),
   'PrecomputedAdvertisement_index'(Advert, PcIndex).

'AdvertisementPath'(Advert, Index, As) :-
   'SetBgpAdvertisementPath'(PcIndex, Index, As),
   'PrecomputedAdvertisement_index'(Advert, PcIndex).

'AdvertisementPathSize'(Advert, Size ):-
   'SetBgpAdvertisementPathSize'(PcIndex, Size),
   'PrecomputedAdvertisement_index'(Advert, PcIndex).

'BestPerProtocolRoute'(Route) :-
   'PrecomputedRoute'(Route).

'IbgpNeighbors'(Node1, Ip1, Node2, Ip2) :-
   'SetIbgpNeighbors'(Node1, Ip1, Node2, Ip2).

'SetPrecomputedRoute'(Node, Network, NextHopIp, Admin, Cost, Protocol, Tag) :-
   'SetPrecomputedRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, Admin, Cost, Protocol, Tag),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

% Precomputed route with next hop ip
'PrecomputedRoute'(Route),
   'Route'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol),
   'Route_tag'(Route, Tag)
:-
   'SetPrecomputedRoute'(Node, Network, NextHopIp, Admin, Cost, Protocol, Tag).
'Ip'(NextHopIp) :-
   'SetStaticIntRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, NextHopInt, Admin, Tag).

'Ip'(NextHopIp) :-
   'SetStaticRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, Admin, Tag).
%'BestConnectedRoute'(Node, Network, NextHop, NextHopIp, Admin, Cost, Protocol) :-
'BestConnectedRoute'(Route),
   'Route'(Route),
   'InterfaceRoute_constructor'(Node, Network, NextHopInt, Protocol, Route),
   'InterfaceRoute'(Route),
   'InterfaceRoute_nextHopInt'(Route, NextHopInt),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol)
:-
   'ConnectedRoute'(Node, Network, NextHopInt),
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin), 
   Cost = 0,
   Protocol = 'connected'.

'BestPerProtocolRoute'(Route) :-
   'BestConnectedRoute'(Route) ;
   'BestStaticRoute'(Route).

'BestStaticRoute'(Route) :-
   'StaticIntRoute'(Route) ;
   'StaticRoute'(Route).

'ConnectedRoute'(Node, Network, Interface) :-
   'IpReadyInt'(Node, Interface, Ip, Prefix_length),
   'NetworkOf'(Ip, Prefix_length, Network).

'InstalledRoute'(Route) :-
   'MinCostRoute'(Route).

'LongestPrefixNetworkMatch'(Node, Ip, MatchNet) :-
   'LongestPrefixNetworkMatchPrefixLength'(Node, Ip, MaxLength),
   'NetworkMatch'(Node, Ip, MatchNet, MaxLength).

'LongestPrefixNetworkMatchPrefixLength'(Node, Ip, MaxLength ):-
   agg(MaxLength = max(MatchLength),(
      'NetworkMatch'(Node, Ip, _, MatchLength))).

'MinAdmin'(Node, Network, MinAdmin ):-
   agg(MinAdmin = min(Admin),(
      'BestPerProtocolRoute'(Route),
      'Route_admin'(Route, Admin),
      'Route_network'(Route, Network),
      'Route_node'(Route, Node))).

'MinAdminRoute'(Route) :-
   'MinAdmin'(Node, Network, MinAdmin),
   'BestPerProtocolRoute'(Route),
   'Route_admin'(Route, MinAdmin),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node).

'MinCost'(Node, Network, MinCost ):-
   agg(MinCost = min(Cost),(
      'MinAdminRoute'(Route),
      'Route_cost'(Route, Cost),
      'Route_network'(Route, Network),
      'Route_node'(Route, Node))).

'MinCostRoute'(Route) :-
   'MinAdminRoute'(Route),
   'MinCost'(Node, Network, MinCost),
   'Route_cost'(Route, MinCost),
   'Route_network'(Route, Network),
   'Route_node'(Route, Node).

'NetworkMatch'(Node, Ip, MatchNet, MatchLength) :-
   'Ip'(Ip),
   'Network_address'(MatchNet, MatchNet_start),
   'Network_prefix_length'(MatchNet, MatchLength),
   'Network_end'(MatchNet, MatchNet_end),
   'InstalledRoute'(Route),
   'Route_network'(Route, MatchNet),
   'Route_node'(Route, Node),
   MatchNet_start =< Ip,
   Ip =< MatchNet_end.

'RouteDetails_admin'(Route, Admin ):-
   'Route_admin'(Route, Admin).
'RouteDetails_admin'(Route, Admin ):-
   'Route'(Route),
   \+ ('Route_admin'(Route, _)),
   Admin = -1.

'RouteDetails_cost'(Route, Cost ):-
   'Route_cost'(Route, Cost).
'RouteDetails_cost'(Route, Cost ):-
   'Route'(Route),
   \+ ('Route_cost'(Route, _)),
   Cost = -1.

'RouteDetails_nextHop'(Route, NextHop ):-
   'Route'(Route),
   'RouteDetails_nextHopIp'(Route, NextHopIp),
   (
      (
         'IpCount'(NextHopIp, IpCountOne),
         'HasIp'(NextHop, NextHopIp),
         IpCountOne = 1
      ) ;
      (
         'IpCount'(NextHopIp, IpCount),
         IpCount > 1,
         NextHop = '(ambiguous)'
      ) ;
      (
         NextHop = '(none)',
         \+ 'HasIp'(_, NextHopIp)
      )
   ).
'RouteDetails_nextHop'(Route, NextHop ):-
   'Route'(Route),
   'Ip_NONE'(Ip),
   'RouteDetails_nextHopIp'(Route, Ip),
   NextHop = '(none)'.

'RouteDetails_nextHopInt'(Route, NextHopInt ):-
   'InterfaceRoute_nextHopInt'(Route, NextHopInt).
'RouteDetails_nextHopInt'(Route, NextHopInt ):-
   'Route'(Route),
   \+ ('InterfaceRoute_nextHopInt'(Route, _)),
   NextHopInt = 'dynamic'.

'RouteDetails_nextHopIp'(Route, NextHopIp ):-
   'Route_nextHopIp'(Route, NextHopIp).
'RouteDetails_nextHopIp'(Route, NextHopIp ):-
   'Route'(Route),
   \+ ('Route_nextHopIp'(Route, _)),
   'Ip_NONE'(NextHopIp).

'RouteDetails_tag'(Route, Tag ):-
   'Route_tag'(Route, Tag).
'RouteDetails_tag'(Route, Tag ):-
   'Route'(Route),
   \+ ('Route_tag'(Route, _)),
   Tag = -1.
'RouteFilterDenyNetwork'(List, Network) :-
   'RouteFilterFirstMatch'(List, Network, Line),
   \+ 'SetRouteFilterPermitLine'(List, Line).
'RouteFilterDenyNetwork'(List, Network) :-
   need_RouteFilterMatchNetwork(List, Network),
   \+ 'RouteFilterMatch'(List, Network, _).

'RouteFilterFirstMatch'(List, Network, MatchLine ):-
   agg(MatchLine = min(Line),( 'RouteFilterMatch'(List, Network, Line))).

'RouteFilterMatch'(List, Network, Line) :-
   need_RouteFilterMatchNetwork(List, Network),
   'SetRouteFilterLine'(List, Line, Line_network_start, Line_network_end, Min_prefix, Max_prefix),
   'Network_constructor'(Network_start, _, Prefix_length, Network),
   Network_start >= Line_network_start,
   Network_start =< Line_network_end,
   Prefix_length >= Min_prefix,
   Prefix_length =< Max_prefix.

'RouteFilterPermitNetwork'(List, Network) :-
   'RouteFilterFirstMatch'(List, Network, Line),
   'SetRouteFilterPermitLine'(List, Line).
'SetStaticIntRoute'(Node, Network, NextHopIp, NextHopInt, Admin, Tag) :-
   'SetStaticIntRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, NextHopInt, Admin, Tag),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

% static route with next hop interface
'StaticIntRoute'(Route),
   'Route'(Route),
   'InterfaceRoute_constructor'(Node, Network, NextHopInt, Protocol, Route),
   'InterfaceRoute'(Route),
   'InterfaceRoute_nextHopInt'(Route, NextHopInt),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol),
   'Route_tag'(Route, Tag)
:-
   'SetStaticIntRoute'(Node, Network, ConfiguredNextHopIp, NextHopInt, Admin, Tag),
   (
      (
         'Ip_ZERO'(ConfiguredNextHopIp),
         'Ip_NONE'(NextHopIp)
      ) ;
      (
        'Ip_ZERO'(IpZ),
         ConfiguredNextHopIp \== IpZ,
         NextHopIp = ConfiguredNextHopIp
      )
   ),
   Cost = 0,
   Protocol = 'static'.
'SetStaticRoute'(Node, Network, NextHopIp, Admin, Tag) :-
   'SetStaticRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, Admin, Tag),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network).

% static route with next hop ip
'StaticRoute'(Route),
   'Route'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route),
   'Route_admin'(Route, Admin),
   'Route_cost'(Route, Cost),
   'Route_network'(Route, Network),
   'Route_nextHopIp'(Route, NextHopIp),
   'Route_node'(Route, Node),
   'Route_protocol'(Route, Protocol),
   'Route_tag'(Route, Tag)
:-
   'LongestPrefixNetworkMatch'(Node, NextHopIp, MatchNet),
   'Network_address'(MatchNet, MatchNet_start),
   'Network_end'(MatchNet, MatchNet_end),
   'Network_address'(Network, Network_start),
   'Network_end'(Network, Network_end),
   'SetStaticRoute'(Node, Network, NextHopIp, Admin, Tag),
   Cost = 0,
   Protocol = 'static',
   (
      MatchNet_start > Network_start ;
      MatchNet_end < Network_end
   ).

'Ip'(SrcIp),
'Ip'(DstIp)
:-
   'SetFlowOriginate'(Node, SrcIp, DstIp, SrcPort, DstPort, Protocol, Dscp, Ecn, IcmpType, IcmpCode, TcpFlagsCWR, TcpFlagsECE, TcpFlagsURG, TcpFlagsACK, TcpFlagsPSH, TcpFlagsRST, TcpFlagsSYN, TcpFlagsFIN, Tag).

/*owner accept*/
'FlowAccepted'(Flow, Node) :-
   'FlowReachPostIn'(Flow, Node),
   'Flow_dstIp'(Flow, DstIp),
   'HasIp'(Node, DstIp).
/*flow sink accept*/
'FlowAccepted'(Flow, Node) :-
   'FlowReachPostOutInterface'(Flow, Node, Interface),
   'SetFlowSinkInterface'(Node, Interface).

'FlowAllowedIn'(Flow, Node, Interface, Filter) :-
   'FlowReachPostInInterface'(Flow, Node, Interface),
   'SetInterfaceFilterIn'(Node, Interface, Filter).

'FlowAllowedOut'(Flow, Node, Interface, Filter) :-
   'FlowReachPostOutInterface'(Flow, Node, Interface),
   'SetInterfaceFilterOut'(Node, Interface, Filter).

'FlowDeniedIn'(Flow, Node, Interface, Filter, Line) :-
   'FlowDeniedInInterfaceAcl'(Flow, Node, Interface, Filter, Line) ;
   'FlowDeniedInCrossZoneFilter'(Flow, Node, Interface, Filter, Line) ;
   'FlowDeniedInInboundFilter'(Flow, Node, Interface, Filter, Line) ;
   'FlowDeniedInToHostFilter'(Flow, Node, Interface, Filter, Line).

/* denied in by missing cross-zone policy with default-deny*/
'FlowDeniedInCrossZoneFilter'(Flow, Node, SrcInt, 'DefaultCrossZoneDeny', 0) :-
   'FlowReachPostIncomingInterfaceAcl'(Flow, Node, SrcInt),
   'FlowInboundInterface'(Flow, Node, InboundInt),
   \+ 'SetDefaultCrossZoneAccept'(Node), /*default deny*/
   (
      \+ 'SetInterfaceZone'(Node, InboundInt, _) ;  /*no policy due to missing inbound zone*/
      'FlowNonInboundNullSrcZone'(Flow, Node) ; /*no policy due to missing src zone*/
      ( /* no cross-zone filter*/
         'SetInterfaceZone'(Node, InboundInt, InboundZone),
         'FlowNonInboundSrcZone'(Flow, Node, SrcZone),
         \+ 'SetCrossZoneFilter'(Node, SrcZone, InboundZone, _)
      )
   ).
/* denied in by cross-zone policy*/
'FlowDeniedInCrossZoneFilter'(Flow, Node, SrcInt, CrossZoneFilter, Line) :-
   'FlowReachPostIncomingInterfaceAcl'(Flow, Node, SrcInt),
   'FlowInboundInterface'(Flow, Node, InboundInt),
   'SetInterfaceZone'(Node, InboundInt, InboundZone),
   'FlowNonInboundSrcZone'(Flow, Node, SrcZone),
   'SetCrossZoneFilter'(Node, SrcZone, InboundZone, CrossZoneFilter),
   'IpAccessListDeny'(CrossZoneFilter, Line, Flow).

/*denied in by to-host filter*/
'FlowDeniedInToHostFilter'(Flow, Node, SrcInt, ToHostFilter, Line) :-
   'FlowReachPostInboundFilter'(Flow, Node, SrcInt),
   'FlowInboundInterface'(Flow, Node, InboundInt),
   'SetInterfaceZone'(Node, InboundInt, InboundZone),
   'SetZoneToHostFilter'(Node, InboundZone, ToHostFilter),
   'IpAccessListDeny'(ToHostFilter, Line, Flow).

/*denied in by missing inbound filter default-deny*/
'FlowDeniedInInboundFilter'(Flow, Node, SrcInt, 'DefaultCrossZoneDeny', 0) :-
   'FlowReachPostInboundCrossZoneAcl'(Flow, Node, SrcInt),
   'FlowInboundInterface'(Flow, Node, InboundInt),
   \+ 'SetDefaultInboundAccept'(Node), /*default deny*/
   (
      \+ 'SetInterfaceZone'(Node, InboundInt, _) ;  /*no policy due to missing inbound zone*/
      ( /* no cross-zone filter*/
         'SetInterfaceZone'(Node, InboundInt, InboundZone),
         \+ 'SetInboundInterfaceFilter'(Node, InboundInt, _)
      )
   ).
/*denied in by inbound filter*/
'FlowDeniedInInboundFilter'(Flow, Node, SrcInt, InboundFilter, Line) :-
   'FlowReachPostInboundCrossZoneAcl'(Flow, Node, SrcInt),
   'FlowInboundInterface'(Flow, Node, InboundInt),
   'SetInterfaceZone'(Node, InboundInt, InboundZone),
   'SetInboundInterfaceFilter'(Node, InboundInt, InboundFilter),
   'IpAccessListDeny'(InboundFilter, Line, Flow).

/*denied in by interface incoming acl*/
'FlowDeniedInInterfaceAcl'(Flow, Node, Interface, Filter, Line) :-
   'FlowReachPreInInterface'(Flow, Node, Interface),
   'SetInterfaceFilterIn'(Node, Interface, Filter),
   'IpAccessListDeny'(Filter, Line, Flow).


'FlowReachPostInboundCrossZoneAcl'(Flow, Node, SrcInt) :-
   'FlowInboundInterface'(Flow, Node, InboundInt),
   'FlowReachPostIncomingInterfaceAcl'(Flow, Node, SrcInt),
   (
      ( /*default accept with no policy*/
         'SetDefaultCrossZoneAccept'(Node),
         ( 
           'FlowNonInboundNullSrcZone'(Flow, Node) ; /*no policy because null src zone*/
           SrcInt == InboundInt ; /* no policy because inbound interface and src interface are same*/
           \+ 'SetInterfaceZone'(Node, InboundInt, _) ; /*no policy because no inbound zone*/
           ( /*no policy for this pair of zones*/
              'SetInterfaceZone'(Node, InboundInt, InboundZone),
              \+ 'SetCrossZoneFilter'(Node, SrcZone, InboundZone, _)
           )
         )
      ) ;
      ( /*policy exists and permits*/
         'SetInterfaceZone'(Node, InboundInt, InboundZone),
         'SetCrossZoneFilter'(Node, SrcZone, InboundZone, CrossZoneFilter),
         \+ 'IpAccessListDeny'(CrossZoneFilter, Line, Flow)
      )
   ).

'FlowReachPostHostInFilter'(Flow, Node, SrcInt) :-
   'FlowReachPostInboundFilter'(Flow, Node, SrcInt),
   'FlowInboundInterface'(Flow, Node, InboundInt),
   (
      \+ 'SetInterfaceZone'(Node, InboundInt, _) ; /*no policy because no inbound zone*/
      (
         'SetInterfaceZone'(Node, InboundInt, InboundZone),
         (
            \+ 'SetZoneToHostFilter'(Node, InboundInt, _) ; /* no policy because while there is inbound zone, interface has no toHost filter*/
            (
               'SetZoneToHostFilter'(Node, InboundInt, ToHostFilter),
               \+ 'IpAccessListDeny'(ToHostFilter, _, Flow)
            )
         )
      )
   ).

'FlowReachPostInboundFilter'(Flow, Node, SrcInt) :-
   'FlowReachPostInboundCrossZoneAcl'(Flow, Node, SrcInt),
   'FlowInboundInterface'(Flow, Node, InboundInt),
   (
      ( /*default accept with no policy*/
         'SetDefaultInboundAccept'(Node),
         (
            \+ 'SetInterfaceZone'(Node, InboundInt, _) ; /*no policy because no inbound zone*/
            ( /* no policy because while there is inbound zone, interface has no inbound filter*/
               'SetInterfaceZone'(Node, InboundInt, _),
               \+ 'SetInboundInterfaceFilter'(Node, InboundInt, _)
            )
         )
      ) ;
      (
         'SetInterfaceZone'(Node, InboundInt, InboundZone),
         'SetInboundInterfaceFilter'(Node, InboundInt, InboundFilter),
         \+ 'IpAccessListDeny'(InboundFilter, _, Flow)
      )
   ).

'FlowReachPostIncomingInterfaceAcl'(Flow, Node, Interface) :-
   'FlowReachPreInInterface'(Flow, Node, Interface),
   (
      \+ 'SetInterfaceFilterIn'(Node, Interface, _) ;
      (
         'SetInterfaceFilterIn'(Node, Interface, Filter),
         \+ 'IpAccessListDeny'(Filter, Line, Flow)
      )
   ).

'FlowDeniedOut'(Flow, Node, Interface, Filter, Line) :-
   'FlowDeniedOutInterfaceAcl'(Flow, Node, Interface, Filter, Line) ;
   'FlowDeniedOutCrossZone'(Flow, Node, Interface, Filter, Line) ;
   'FlowDeniedOutHostOut'(Flow, Node, Interface, Filter, Line).

/*unoriginal with no src zone denied out by cross-zone default-deny*/
'FlowDeniedOutCrossZone'(Flow, Node, Interface, 'DefaultCrossZoneDeny', 0) :-
   'FlowReachPostOutgoingInterfaceAcl'(Flow, Node, Interface),
   'FlowNonInboundNullSrcZone'(Flow, Node),
   \+ 'SetDefaultCrossZoneAccept'(Node).
/*unoriginal with src zone but no dst zone denied out by cross-zone default-deny*/
'FlowDeniedOutCrossZone'(Flow, Node, Interface, 'DefaultCrossZoneDeny', 0) :-
   'FlowReachPostOutgoingInterfaceAcl'(Flow, Node, Interface),
   'FlowNonInboundSrcZone'(Flow, Node, _),
   \+ 'SetDefaultCrossZoneAccept'(Node),
   \+ 'SetInterfaceZone'(Node, Interface, _).
/*unoriginal with src zone and dst zone but no cross-zone filter denied out by cross-zone default-deny*/
'FlowDeniedOutCrossZone'(Flow, Node, Interface, 'DefaultCrossZoneDeny', 0) :-
   'FlowReachPostOutgoingInterfaceAcl'(Flow, Node, Interface),
   'FlowNonInboundSrcZone'(Flow, Node, SrcZone),
   \+ 'SetDefaultCrossZoneAccept'(Node),
   'SetInterfaceZone'(Node, Interface, DstZone),
   \+ 'SetCrossZoneFilter'(Node, SrcZone, DstZone, _).
/*unoriginal with src zone and dst zone and cross-zone filter denied out by cross-zone filter*/
'FlowDeniedOutCrossZone'(Flow, Node, Interface, CrossZoneFilter, Line) :-
   'FlowReachPostOutgoingInterfaceAcl'(Flow, Node, Interface),
   'FlowNonInboundSrcZone'(Flow, Node, SrcZone),
   'SetInterfaceZone'(Node, Interface, DstZone),
   'SetCrossZoneFilter'(Node, SrcZone, DstZone, CrossZoneFilter),
   'IpAccessListDeny'(CrossZoneFilter, Line, Flow).

/*original denied by host-out filter*/
'FlowDeniedOutHostOut'(Flow, Node, Interface, FromHostFilter, Line) :-
   'FlowReachPreOutInterface'(Flow, Node, Interface),
   'FlowReachPostOutgoingInterfaceAcl'(Flow, Node, Interface),
   'Flow_node'(Flow, Node),
   'SetInterfaceZone'(Node, Interface, DstZone),
   'SetZoneFromHostFilter'(Node, DstZone, FromHostFilter),
   'IpAccessListDeny'(FromHostFilter, Line, Flow).

/*denied out by interface acl*/
'FlowDeniedOutInterfaceAcl'(Flow, Node, Interface, Filter, Line) :-
   'FlowReachPreOutInterface'(Flow, Node, Interface),
   'SetInterfaceFilterOut'(Node, Interface, Filter),
   'IpAccessListDeny'(Filter, Line, Flow).

'FlowReachPostOutboundCrossZoneAcl'(Flow, Node, Interface) :-
   'FlowReachPostOutgoingInterfaceAcl'(Flow, Node, Interface),
   (
      ( /*unoriginal, no policy, default is to accept*/
         'SetDefaultCrossZoneAccept'(Node),
         (
            'FlowReachNonInboundNullSrcZone'(Flow, Node) ; /*no policy because no src zone*/
            \+ 'SetInterfaceZone'(Node, Interface, _) ; /*no policy because no dst zone*/
            ( /*there is a destination zone and a source zone but no cross-zone filter*/
               'SetInterfaceZone'(Node, Interface, DstZone),
               'FlowNonInboundSrcZone'(Flow, Node, SrcZone),
               \+ 'SetCrossZoneFilter'(Node, SrcZone, DstZone, _)
            )
         )
      ) ;
      ( /*the packet is original*/
         'Flow_node'(Flow, Node),
         (
            \+ 'SetInterfaceZone'(Node, Interface, _) ; /*no policy because no dst zone*/
            ( /*there is a destination zone but no from-host filter*/
               'SetInterfaceZone'(Node, Interface, DstZone),
               \+ 'SetZoneFromHostFilter'(Node, DstZone, _)
            ) ;
            ( /*from-host filter permits*/
               'SetInterfaceZone'(Node, Interface, DstZone),
               'SetZoneFromHostFilter'(Node, DstZone, FromHostFilter),
               \+ 'IpAccessListDeny'(FromHostFilter, _, Flow)
            )
         )
      ) ;
      ( /*unoriginal, cross-zone filter permits*/
         'FlowNonInboundSrcZone'(Flow, Node, SrcZone),
         'SetInterfaceZone'(Node, Interface, DstZone),
         'SetCrossZoneFilter'(Node, SrcZone, DstZone, CrossZoneFilter),
         \+ 'IpAccessListDeny'(CrossZoneFilter, _, Flow)
      )
   ).

'FlowReachPostOutgoingInterfaceAcl'(Flow, Node, Interface) :-
   'FlowReachPreOutInterface'(Flow, Node, Interface),
   (
      \+ 'SetInterfaceFilterOut'(Node, Interface, _) ;
      (
         'SetInterfaceFilterOut'(Node, Interface, Filter),
         \+ 'IpAccessListDeny'(Filter, _, Flow)
      )
   ).

'FlowDropped'(Flow, Node) :-
   'FlowDeniedIn'(Flow, Node, _, _, _).   
'FlowDropped'(Flow, Node) :-
   'FlowDeniedOut'(Flow, Node, _, _, _).   
'FlowDropped'(Flow, Node) :-
   'FlowNeighborUnreachable'(Flow, Node, _).
'FlowDropped'(Flow, Node) :-
   'FlowNoRoute'(Flow, Node).
'FlowDropped'(Flow, Node) :-
   'FlowNullRouted'(Flow, Node).

'FlowInboundInterface'(Flow, Node, InboundInt) :-
   'FlowReachPostIncomingInterfaceAcl'(Flow, Node, _),
   'FlowMatchInterface'(Flow, Node, InboundInt).

'FlowNeighborUnreachable'(Flow, Node, NeighborIp) :-
   'Flow_dstIp'(Flow, DstIp),
   'FlowReachPreOut'(Flow, Node),
   'FibNeighborUnreachable'(Node, DstIp, NeighborIp).

'FlowMultipathInconsistent'(Flow) :-
   'FlowLost'(Flow),
   'FlowAccepted'(Flow, _).

'FlowLoop'(Flow, Node, OutInt) :-
   'FlowReach'(Flow, Node, OutInt, Node, _).

'FlowLost'(Flow) :-
   'FlowDropped'(Flow, _) ;
   'FlowLoop'(Flow, _, _).

'FlowMatchInterface'(Flow, Node, Int) :-
   'Flow_dstIp'(Flow, DstIp),
   'IpReadyInt'(Node, Int, DstIp, _).

'FlowMatchRoute'(Flow, Route) :-
   'Flow_dstIp'(Flow, DstIp),
   'FlowReachPostIn'(Flow, Node),
   'LongestPrefixNetworkMatch'(Node, DstIp, Network),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node),
   'Route_network'(Route, Network).

'FlowNoRoute'(Flow, Node) :-
   'FlowReachPreOut'(Flow, Node),
   'Flow_dstIp'(Flow, DstIp),
   \+ 'FibRoute'(Node, DstIp).

'FlowNonInboundNullSrcZone'(Flow, Node) :-
   'FlowNonInboundSrcInterface'(Flow, Node, SrcInt),
   \+ 'SetInterfaceZone'(Node, SrcInt, _).

'FlowNonInboundSrcInterface'(Flow, Node, SrcInt) :-
   \+ 'FlowMatchInterface'(Flow, Node, SrcInt),
   'FlowReachPostIncomingInterfaceAcl'(Flow, Node, SrcInt).

'FlowNonInboundSrcZone'(Flow, Node, SrcZone) :-
   'FlowNonInboundSrcInterface'(Flow, Node, SrcInt),
   'SetInterfaceZone'(Node, SrcInt, SrcZone).

'FlowNullRouted'(Flow, Node) :-
   'Flow_dstIp'(Flow, DstIp),
   'FlowReachPreOut'(Flow, Node),
   'FibDrop'(Node, DstIp),
   \+ 'FibNeighborUnreachable'(Node, DstIp, _).

'FlowOriginate'(Node, SrcIp, DstIp, SrcPort, DstPort, Protocol, Dscp, Ecn, IcmpType, IcmpCode, CWR, ECE, URG, ACK, PSH, RST, SYN, FIN, Tag, Flow),
'Flow'(Flow),
'Flow_dscp'(Flow, Dscp),
'Flow_dstIp'(Flow, DstIp),
'Flow_dstPort'(Flow, DstPort),
'Flow_ecn'(Flow, Ecn),
'Flow_icmpCode'(Flow, IcmpCode),
'Flow_icmpType'(Flow, IcmpType),
'Flow_ipProtocol'(Flow, Protocol),
'Flow_node'(Flow, Node),
'Flow_srcIp'(Flow, SrcIp),
'Flow_srcPort'(Flow, SrcPort),
'Flow_tcpFlagsCWR'(Flow, CWR),
'Flow_tcpFlagsECE'(Flow, ECE),
'Flow_tcpFlagsURG'(Flow, URG),
'Flow_tcpFlagsACK'(Flow, ACK),
'Flow_tcpFlagsPSH'(Flow, PSH),
'Flow_tcpFlagsRST'(Flow, RST),
'Flow_tcpFlagsSYN'(Flow, SYN),
'Flow_tcpFlagsFIN'(Flow, FIN),
'Flow_tag'(Flow, Tag)
:-
   'SetFlowOriginate'(Node, SrcIp, DstIp, SrcPort, DstPort, Protocol, Dscp, Ecn, IcmpType, IcmpCode, CWR, ECE, URG, ACK, PSH, RST, SYN, FIN, Tag).

'FlowOriginate'(Node, SrcIp, DstIp, SrcPort, DstPort, Protocol, Dscp, Ecn, IcmpType, IcmpCode, CWR, ECE, URG, ACK, PSH, RST, SYN, FIN, Tag, Flow),
'Flow'(Flow),
'Flow_dscp'(Flow, Dscp),
'Flow_dstIp'(Flow, DstIp),
'Flow_dstPort'(Flow, DstPort),
'Flow_ecn'(Flow, Ecn),
'Flow_icmpCode'(Flow, IcmpCode),
'Flow_icmpType'(Flow, IcmpType),
'Flow_ipProtocol'(Flow, Protocol),
'Flow_node'(Flow, Node),
'Flow_srcIp'(Flow, SrcIp),
'Flow_srcPort'(Flow, SrcPort),
'Flow_tcpFlagsCWR'(Flow, CWR),
'Flow_tcpFlagsECE'(Flow, ECE),
'Flow_tcpFlagsURG'(Flow, URG),
'Flow_tcpFlagsACK'(Flow, ACK),
'Flow_tcpFlagsPSH'(Flow, PSH),
'Flow_tcpFlagsRST'(Flow, RST),
'Flow_tcpFlagsSYN'(Flow, SYN),
'Flow_tcpFlagsFIN'(Flow, FIN),
'Flow_tag'(Flow, Tag)
:-
   'DuplicateRoleFlows'(_),
   'SetFlowOriginate'(AcceptNode, SrcIp, DstIp, SrcPort, DstPort, Protocol, Dscp, Ecn, IcmpType, IcmpCode, CWR, ECE, URG, ACK, PSH, RST, SYN, FIN, Tag),
   'SetNodeRole'(AcceptNode, Role),
   'SetNodeRole'(Node, Role).

'FlowReach'(Flow, SrcNode, SrcOutInt, DstNode, DstInInt) :-
   'FlowReachStep'(Flow, SrcNode, SrcOutInt, DstNode, DstInInt).
'FlowReach'(Flow, SrcNode, SrcOutInt, DstNode, DstInInt) :-
   'FlowReach'(Flow, SrcNode, SrcOutInt, MidNode, _),
   'FlowReach'(Flow, MidNode, _, DstNode, DstInInt). 

'FlowPolicyDenied'(Flow, Node, Policy) :-
   'FlowReachPolicyRoute'(Flow, Node, Policy),
   'PolicyMapDenyFlow'(Policy, Flow).

'FlowReachPolicyRoute'(Flow, Node, Policy) :-
   'FlowReachPreOut'(Flow, Node),
   'FlowReachPostInInterface'(Flow, Node, Interface),
   'SetInterfaceRoutingPolicy'(Node, Interface, Policy).

'FlowReachPostIn'(Flow, Node) :-
   'Flow_node'(Flow, Node).   
'FlowReachPostIn'(Flow, Node) :-
   'FlowReachPostHostInFilter'(Flow, Node, _).
'FlowReachPostIn'(Flow, Node) :-
   'FlowReachPostIncomingInterfaceAcl'(Flow, Node, _),
   'Flow_dstIp'(Flow, DstIp),
   \+ 'HasIp'(Node, DstIp).

'FlowReachPostInInterface'(Flow, Node, Interface) :-
   'FlowReachPreInInterface'(Flow, Node, Interface),
   \+ 'FlowDenyIn'(Flow, Node, Interface, _, _).

'FlowReachPostOutInterface'(Flow, Node, Interface) :-
   'FlowReachPostOutboundCrossZoneAcl'(Flow, Node, Interface).

'FlowReachPreInInterface'(Flow, Node, Interface) :-
   'FlowReachPostOutInterface'(Flow, PrevNode, PrevInterface),
   'FlowReachPreOutEdge'(Flow, PrevNode, _, PrevInterface, Node, Interface),
   Interface \== 'flow_sink_termination'.

'FlowReachPreOut'(Flow, Node) :-
   'FlowReachPostIn'(Flow, Node),
   'Flow_dstIp'(Flow, DstIp),
   \+ 'HasIp'(Node, DstIp).

'FlowReachPreOutInterface'(Flow, Node, Interface) :-
   'FlowReachPreOutEdge'(Flow, Node, _, Interface, _, _).

'FlowReachPreOutEdge'(Flow, Node, ReceivedInt, OutInt, NextHop, NextHopInt) :-
   'FlowReachPreOutEdgeOrigin'(Flow, Node, ReceivedInt, OutInt, NextHop, NextHopInt) ;
   'FlowReachPreOutEdgeStandard'(Flow, Node, ReceivedInt, OutInt, NextHop, NextHopInt) ;
   'FlowReachPreOutEdgePolicyRoute'(Flow, Node, ReceivedInt, OutInt, NextHop, NextHopInt).

'FlowReachPreOutEdgeOrigin'(Flow, Node, ReceivedInt, OutInt, NextHop, NextHopInt) :-
   'FlowReachPreOut'(Flow, Node),
   ReceivedInt = 'null_interface',
   'Flow_dstIp'(Flow, DstIp),
   'FibForward'(Node, DstIp, OutInt, NextHop, NextHopInt),
   'Flow_node'(Flow, Node).

'FlowReachPreOutEdgePolicyRoute'(Flow, Node, ReceivedInt, Interface, NextHop, NextHopInt) :-
   'FlowReachPostInInterface'(Flow, Node, ReceivedInt),
   'SetInterfaceRoutingPolicy'(Node, ReceivedInt, Policy),
   'FlowReachPolicyRoute'(Flow, Node, Policy),
   'PolicyMapPermitFlow'(Policy, Clause, Flow),
   'SetPolicyMapClauseSetNextHopIp'(Policy, Clause, NextHopIp),
   'FibForwardPolicyRouteNextHopIp'(Node, NextHopIp, Interface, NextHop, NextHopInt).

'FlowReachPreOutEdgeStandard'(Flow, Node, ReceivedInt, OutInt, NextHop, NextHopInt) :-
   'FlowReachPreOut'(Flow, Node),
   'Flow_dstIp'(Flow, DstIp),
   'FibForward'(Node, DstIp, OutInt, NextHop, NextHopInt),
   'FlowReachPostInInterface'(Flow, Node, ReceivedInt),
   (
      \+ 'SetInterfaceRoutingPolicy'(Node, ReceivedInt, _) ;
      (
         'SetInterfaceRoutingPolicy'(Node, ReceivedInt, Policy),
         \+ 'PolicyMapPermitFlow'(Policy, _, Flow)
      )
   ).

'FlowReachStep'(Flow, SrcNode, SrcOutInt, DstNode, DstInInt) :-
   'FlowReachPostOutInterface'(Flow, SrcNode, SrcOutInt),
   'FlowReachPostInInterface'(Flow, DstNode, DstInInt),
   'LanAdjacent'(SrcNode, SrcOutInt, DstNode, DstInInt).

'FlowUnknown'(Flow) :-
   'Flow'(Flow),
   \+ 'FlowAccepted'(Flow, _),
   \+ 'FlowLost'(Flow).

need_PolicyMapMatchFlow(Policy, Flow) :-
   'Flow'(Flow),
   'PolicyMap'(Policy).

'FlowPathAcceptedEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   'FlowPathIntermediateEdge'(Flow, I, _, Node1, Int1, Node2, Int2, OldHistory),
   (
      (
         'FlowAccepted'(Flow, Node2),
         'Flow_dstIp'(Flow, DstIp),
         'HasIp'(Node2, DstIp)
      ) ;
      (
         'FlowAccepted'(Flow, Node1),
         Int2 = 'flow_sink_termination'
      )
   ),
   History = [OldHistory, ':accepted'].
'FlowPathAcceptedEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   \+ 'FlowPathIntermediateEdge'(Flow, _, _, _, _, _, _, _),
   Node2 = '(none)',
   I = 0,
   Int1 = 'null_interface',
   Int2 = 'null_interface',
   'FlowAccepted'(Flow, Node1),
   History = ['[\'', Node1, '\':accepted]'].

'FlowPathDeniedInEdge'(Flow, I, ReceivedInt, Node1, Int1, Node2, Int2, History, Filter, Line) :-
   (
      (
         I = J + 1,
         'FlowPathIntermediateEdge'(Flow, J, _, _, _, Node1, ReceivedInt, OldHistory),
         History = [OldHistory, ';', CurrentHistory]
      ) ;
      (
         'Flow_node'(Flow, Node1),
         I = 0,
         ReceivedInt = 'null_interface',
         History = CurrentHistory
      )
   ),
   (
      (
         \+ 'SetInterfaceFilterOut'(Node1, Int1, _),
         AllowedOutStr = ''
      ) ;
      (
         'FlowAllowedOut'(Flow, Node1, Int1, FilterOut),
         AllowedOutStr = [':allowedOut:\'', FilterOut, '\'']
      )
   ),
   'FlowReachPreOutEdge'(Flow, Node1, ReceivedInt, Int1, Node2, Int2),
   'FlowDeniedIn'(Flow, Node2, Int2, Filter, Line),
   CurrentHistory = ['[\'', Node1, '\':\'', Int1, '\'', AllowedOutStr,
		     '-> \'', Node2, '\':\'', Int2, '\':deniedIn:\'',
		     Filter, '\':', Line, ']'].

'FlowPathDeniedOutEdge'(Flow, I, ReceivedInt, Node1, Int1, Node2, Int2, History, Filter, Line) :-
   (
      (
         I = J + 1,
         'FlowPathIntermediateEdge'(Flow, J, _, _, _, Node1, ReceivedInt, OldHistory),
         History = [OldHistory, ';', CurrentHistory]
      ) ;
      (
         'Flow_node'(Flow, Node1),
         I = 0,
         ReceivedInt = 'null_interface',
         History = CurrentHistory
      )
   ),
   'FlowReachPreOutEdge'(Flow, Node1, ReceivedInt, Int1, Node2, Int2),
   'FlowDeniedOut'(Flow, Node1, Int1, Filter, Line),
   CurrentHistory = ['[\'', Node1, '\':\'', Int1, '\':deniedOut:\'',
		     Filter, '\':', Line, '-> \'', Node2, '\':\'',
		     Int2, '\']'].

'FlowPathHistory'(Flow, History) :-
   'FlowPathAcceptedEdge'(Flow, _, _, _, _, _, History) ;
   'FlowPathDeniedInEdge'(Flow, _, _, _, _, _, _, History, _, _) ;
   'FlowPathDeniedOutEdge'(Flow, _, _, _, _, _, _, History, _, _) ;
   'FlowPathNeighborUnreachableEdge'(Flow, _, _, _, _, _, History) ;
   'FlowPathNoRouteEdge'(Flow, _, _, _, _, _, History) ;
   'FlowPathNullRoutedEdge'(Flow, _, _, _, _, _, History).

'FlowPathIntermediateEdge'(Flow, I, ReceivedInt, Node1, Int1, Node2, Int2, History) :-
   (
      (
         I = 0,
         'FlowReachPreOutEdgeOrigin'(Flow, Node1, ReceivedInt, Int1, Node2, Int2),
         History = CurrentHistory
      ) ;
      (
         I = J + 1,
         \+ 'FlowLoop'(Flow, _, _),
         'FlowPathIntermediateEdge'(Flow, J, _, _, _, Node1, ReceivedInt, OldHistory),
         'FlowReachPreOutEdge'(Flow, Node1, ReceivedInt, Int1, Node2, Int2),
         History = [OldHistory, ';', CurrentHistory]
      )
   ),
   (
      (
         \+ 'SetInterfaceFilterIn'(Node2, Int2, _),
         AllowedInStr = ''
      ) ;
      (
         'FlowAllowedIn'(Flow, Node2, Int2, FilterIn),
         AllowedInStr = [':allowedIn:\'', FilterIn, '\'']
      )
   ),
   (
      (
         \+ 'SetInterfaceFilterOut'(Node1, Int1, _),
         AllowedOutStr = ''
      ) ;
      (
         'FlowAllowedOut'(Flow, Node1, Int1, FilterOut),
         AllowedOutStr = [':allowedOut:\'', FilterOut, '\'']
      )
   ),
   'FlowReachPostOutInterface'(Flow, Node1, Int1),
   (
      'FlowReachPostInInterface'(Flow, Node2, Int2) ;
      (
         'SetFlowSinkInterface'(Node1, Int1),
         Node2 = '(none)',
         Int2 = 'flow_sink_termination'
      )
   ),
   CurrentHistory =['[\'', Node1, '\':\'', Int1, '\'', AllowedOutStr,
	    '-> \'', Node2, '\':\'', Int2, '\'', AllowedInStr,
		    ']'].

'FlowPathNeighborUnreachableEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   'FlowPathIntermediateEdge'(Flow, I, _, Node1, Int1, Node2, Int2, OldHistory),
   'FlowNeighborUnreachable'(Flow, Node2, _),
   History = [OldHistory, ':neighborUnreachable'].
'FlowPathNeighborUnreachableEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   \+ 'FlowPathIntermediateEdge'(Flow, _, _, _, _, Node1, _, _),
   Node2 = '(none)',
   I = 0,
   Int1 = 'null_interface',
   Int2 = 'null_interface',
   'FlowNeighborUnreachable'(Flow, Node1, _),
   History = ['[\'', Node1, '\':neighborUnreachable]'].

'FlowPathNoRouteEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   'FlowPathIntermediateEdge'(Flow, I, _, Node1, Int1, Node2, Int2, OldHistory),
   'FlowNoRoute'(Flow, Node2),
   History = [OldHistory, ':noRoute'].
'FlowPathNoRouteEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   \+ 'FlowPathIntermediateEdge'(Flow, _, _, _, _, Node1, _, _),
   Node2 = '(none)',
   I = 0,
   Int1 = 'null_interface',
   Int2 = 'null_interface',
   'FlowNoRoute'(Flow, Node1),
   History = ['[\'', Node1, '\':noRoute]'].

'FlowPathNullRoutedEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   'FlowPathIntermediateEdge'(Flow, I, _, Node1, Int1, Node2, Int2, OldHistory),
   'FlowNullRouted'(Flow, Node2),
   History = [OldHistory, ':nullRouted'].
'FlowPathNullRoutedEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   \+ 'FlowPathIntermediateEdge'(Flow, _, _, _, _, Node1, _, _),
   Node2 = '(none)',
   I = 0,
   Int1 = 'null_interface',
   Int2 = 'null_interface',
   'FlowNullRouted'(Flow, Node1),
   History = ['[\'', Node1, '\':nullRouted]'].

'FlowRoleAccepted'(Flow, Role) :-
   'FlowAccepted'(Flow, Node),
   'SetNodeRole'(Node, Role).

'FlowRoleInconsistent'(TransmittingRole, AcceptedFlow, MissingFlow, ReceivingRole) :-
   'FlowSameHeader'(AcceptedFlow, MissingFlow),
   'Flow_node'(AcceptedFlow, Node1),
   'Flow_node'(MissingFlow, Node2),
   'SetNodeRole'(Node1, TransmittingRole),
   'SetNodeRole'(Node2, TransmittingRole),
   'FlowRoleAccepted'(AcceptedFlow, ReceivingRole),
   \+ 'FlowRoleAccepted'(MissingFlow, ReceivingRole).

'FlowRoleTransitInconsistent'(SrcRole, TransitNode, NonTransitNode, TransitRole, Flow) :-
   'SetNodeRole'(TransitNode, TransitRole),
   'SetNodeRole'(NonTransitNode, TransitRole),
   'FlowRoleTransitNode'(Flow, SrcRole, TransitNode),
   SrcRole \== TransitRole,
   \+ 'FlowSameHeaderRoleTransitNode'(Flow, _, NonTransitNode).

'FlowRoleTransitNode'(Flow, SrcRole, TransitNode) :-
   'Flow_node'(Flow, SrcNode),
   'SetNodeRole'(SrcNode, SrcRole),
   'SetNodeRole'(TransitNode, TransitRole),
   SrcRole \== TransitRole,
   'FlowReachPostOutInterface'(Flow, TransitNode, _).

'FlowSameHeaderRoleTransitNode'(Flow, SimilarFlow, TransitNode) :-
   Flow = SimilarFlow,
   'FlowRoleTransitNode'(Flow, _, TransitNode).
'FlowSameHeaderRoleTransitNode'(Flow, SimilarFlow, TransitNode) :-
   'FlowRoleTransitNode'(Flow, SrcRole, TransitNode),
   'Flow_node'(SimilarFlow, SrcNode),
   'SetNodeRole'(SrcNode, SrcRole),
   'SetNodeRole'(TransitNode, TransitRole),
   SrcRole \== TransitRole,
   'FlowSameHeader'(Flow, SimilarFlow).

'FlowSameHeader'(Flow1, Flow2) :-
   'FlowOriginate'(Node1, SrcIp, DstIp, SrcPort, DstPort, Protocol, Dscp, Ecn, IcmpType, IcmpCode, CWR, ECE, URG, ACK, PSH, RST, SYN, FIN,Tag, Flow1),
   'FlowOriginate'(Node2, SrcIp, DstIp, SrcPort, DstPort, Protocol, Dscp, Ecn, IcmpType, IcmpCode, CWR, ECE, URG, ACK, PSH, RST, SYN, FIN,Tag, Flow2),
   Node1 \== Node2.
