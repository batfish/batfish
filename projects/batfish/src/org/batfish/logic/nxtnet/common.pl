'AsPathDenyAdvert'(AsPath, Advert) :-
   'AsPathFirstMatchAdvert'(AsPath, Advert, Line) /*fn*/,
   'SetAsPathLineDeny'(AsPath, Line).
'AsPathDenyAdvert'(AsPath, Advert) :-
   need_AsPathMatchAdvert(AsPath, Advert),
   \+ 'AsPathLineMatchAdvert'(AsPath, _, Advert).

'AsPathFirstMatchAdvert'(AsPath, Advert, MatchLine ) /*fn*/:-
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
   'AdvertisementPathSize'(Advert, Size) /*fn*/,
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
   'AdvertisementPathSize'(Advert, Size) /*fn*/,
   Index1 = Size - 1.

'AsPathLineMatchEmpty'(AsPath, Line, Advert) :-
   need_AsPathMatchAdvert(AsPath, Advert),
   'SetAsPathLineMatchEmpty'(AsPath, Line),
   'AdvertisementPathSize'(Advert, Size) /*fn*/,
   Size = 0.

'AsPathPermitAdvert'(AsPath, Advert) :-
   'AsPathFirstMatchAdvert'(AsPath, Advert, Line) /*fn*/,
   'SetAsPathLinePermit'(AsPath, Line).
'AdministrativeDistance'('arista', 'bgp', 200) /*fn*/.
'AdministrativeDistance'('arista', 'ibgp', 200) /*fn*/.
'AdministrativeDistance'('arista', 'connected', 0) /*fn*/.
'AdministrativeDistance'('arista', 'ospf', 110) /*fn*/.
'AdministrativeDistance'('arista', 'ospfIA', 110) /*fn*/.
'AdministrativeDistance'('arista', 'ospfE1', 110) /*fn*/.
'AdministrativeDistance'('arista', 'ospfE2', 110) /*fn*/.

'AdministrativeDistance'('cisco', 'bgp', 20) /*fn*/.
'AdministrativeDistance'('cisco', 'ibgp', 200) /*fn*/.
'AdministrativeDistance'('cisco', 'connected', 0) /*fn*/.
'AdministrativeDistance'('cisco', 'isisL1', 115) /*fn*/.
'AdministrativeDistance'('cisco', 'isisL2', 115) /*fn*/.
'AdministrativeDistance'('cisco', 'isisEL1', 115) /*fn*/.
'AdministrativeDistance'('cisco', 'isisEL2', 115) /*fn*/.
'AdministrativeDistance'('cisco', 'ospf', 110) /*fn*/.
'AdministrativeDistance'('cisco', 'ospfIA', 110) /*fn*/.
'AdministrativeDistance'('cisco', 'ospfE1', 110) /*fn*/.
'AdministrativeDistance'('cisco', 'ospfE2', 110) /*fn*/.

'AdministrativeDistance'('juniper', 'bgp', 170) /*fn*/.
'AdministrativeDistance'('juniper', 'ibgp', 170) /*fn*/.
'AdministrativeDistance'('juniper', 'connected', 0) /*fn*/.
'AdministrativeDistance'('juniper', 'isisL1', 15) /*fn*/.
'AdministrativeDistance'('juniper', 'isisL2', 18) /*fn*/.
'AdministrativeDistance'('juniper', 'isisEL1', 160) /*fn*/.
'AdministrativeDistance'('juniper', 'isisEL2', 165) /*fn*/.
'AdministrativeDistance'('juniper', 'ospf', 10) /*fn*/.
'AdministrativeDistance'('juniper', 'ospfIA', 10) /*fn*/.
'AdministrativeDistance'('juniper', 'ospfE1', 150) /*fn*/.
'AdministrativeDistance'('juniper', 'ospfE2', 150) /*fn*/.
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
'BgpAdvertisement_constructor'(Type, DstIpBlock, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, Advert) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, Med) /*fn*/,
'BgpAdvertisement_network'(Advert, DstIpBlock) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, OriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize) /*fn*/
:-
   Type = 'bgp',
   'Ip_NONE'(OriginatorIp),
   (
      'BgpAdvertisement_type'(PrevAdvert, 'bgp_ti' ) /*fn*/;
      'BgpAdvertisement_type'(PrevAdvert, 'ibgp_ti') /*fn*/
   ),
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock) /*fn*/,
   'BgpAdvertisement_nextHopIp'(PrevAdvert, OrigNextHopIp) /*fn*/,
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol) /*fn*/,
   'BgpAdvertisement_dstNode'(PrevAdvert, SrcNode) /*fn*/,
   'AdvertisementPathSize'(PrevAdvert, PrevPathSize) /*fn*/,
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
'BgpAdvertisement_constructor'(Type, Network, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, Advert) /*fn*/,
'AdvertisementPathSize'(Advert, PathSize) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, Med) /*fn*/,
'BgpAdvertisement_network'(Advert, Network) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, OriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
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
   'Route_network'(Route, Network) /*fn*/,
   'Route_protocol'(Route, SrcProtocol) /*fn*/,
   'Route_node'(Route, SrcNode) /*fn*/,
   (
      (
         'ActiveGeneratedRoute'(Route),
         'BgpGeneratedRoute'(Route)
      ) ;
      (
         'ActiveGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute_neighborIp'(Route, DstIp) /*fn*/
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
         'BgpNeighborGeneratedRoute_neighborIp'(Route, NeighborIp) /*fn*/
      )
   ),
   'Route_node'(Route, Node) /*fn*/,
   'BgpOriginationPolicy'(Node, NeighborIp, Map),
   'BgpNeighbors'(Node, _, _, NeighborIp).
% incoming transformation
'AdvertisementCommunity'(Advert, Community) :-
   PriorType = 'bgp_to',
   'BgpNeighborSendCommunity'(DstNode, SrcIp),
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode) /*fn*/,
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp) /*fn*/,
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
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'BestBgpRouteNetwork'(Node, Network),
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/,
   'BgpAdvertisementRoute'(Advert, Route) /*fn*/
:-
   Type = 'bgp_ti',
   Protocol = 'bgp',
   'BgpAdvertisement_type'(Advert, Type) /*fn*/,
   'BgpAdvertisement_network'(Advert, Network) /*fn*/,
   'BgpAdvertisement_nextHopIp'(Advert, NextHopIp) /*fn*/,
   'BgpAdvertisement_dstNode'(Advert, Node) /*fn*/,
   'BgpAdvertisement_med'(Advert, Cost) /*fn*/,
   'BestBgpAdvertisement'(Advert),
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.

% ebgp transformed incoming
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, TNextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, TOriginType, Advert) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, TLocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, TMed) /*fn*/,
'BgpAdvertisement_network'(Advert, DstIpBlock) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, TNextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, TOriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, TSrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize) /*fn*/
:-
   Type = 'bgp_ti',
   PriorType = 'bgp_to',
   \+ 'HasIp'(DstNode, OriginatorIp),
   'LocalAs'(DstNode, SrcIp, ReceiverAs),
   \+ 'AdvertisementPath'(PrevAdvert, _, ReceiverAs),
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp) /*fn*/,
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode) /*fn*/,
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref) /*fn*/,
   'BgpAdvertisement_med'(PrevAdvert, Med) /*fn*/,
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock) /*fn*/,
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp) /*fn*/,
   'BgpAdvertisement_originatorIp'(PrevAdvert, OriginatorIp) /*fn*/,
   'BgpAdvertisement_originType'(PrevAdvert, OriginType) /*fn*/,
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp) /*fn*/,
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode) /*fn*/,
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol) /*fn*/,
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'AdvertisementPathSize'(PrevAdvert, PathSize) /*fn*/,
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
   'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
   'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
   'BgpAdvertisement_type'(Advert, Type) /*fn*/,
   'BgpImportPolicy'(DstNode, SrcIp, Map).
% append as to path for bgp route advertisement
'AdvertisementPath'(Advert, Index, As) :-
   'BgpAdvertisement_type'(Advert, Type) /*fn*/,
   'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
   'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
   Type = 'bgp_to',
   'ParentAdvertisement'(PrevAdvert, Advert),
   'AdvertisementPathSize'(PrevAdvert, PrevPathSize) /*fn*/,
   Index = PrevPathSize,
   'RemoteAs'(DstNode, SrcIp, As).

% outgoing transformation
'AdvertisementCommunity'(Advert, Community) :-
   PriorType = 'bgp',
   'BgpNeighborSendCommunity'(SrcNode, DstIp),
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode) /*fn*/,
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp) /*fn*/,
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
'BgpAdvertisement_constructor'(Type, DstIpBlock, TNextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, TOriginType, Advert) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, TLocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, TMed) /*fn*/,
'BgpAdvertisement_network'(Advert, DstIpBlock) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, TNextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, TOriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, TSrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize) /*fn*/
:-
   Type = 'bgp_to',
   PriorType = 'bgp',
   PathSize = PrevPathSize + 1,
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp) /*fn*/,
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode) /*fn*/,
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref) /*fn*/,
   'BgpAdvertisement_med'(PrevAdvert, Med) /*fn*/,
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock) /*fn*/,
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp) /*fn*/,
   'BgpAdvertisement_originatorIp'(PrevAdvert, OriginatorIp) /*fn*/,
   'BgpAdvertisement_originType'(PrevAdvert, OriginType) /*fn*/,
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp) /*fn*/,
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode) /*fn*/,
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol) /*fn*/,
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'AdvertisementPathSize'(PrevAdvert, PrevPathSize) /*fn*/,
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
   'GeneratedRoute_constructor'(Node, Network, Type, Route) /*fn*/,
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   'SetBgpGeneratedRoute'(Node, Network),
   Type = 'GeneratedRouteType_BGP',
   Protocol = 'aggregate'.

'BgpNeighborGeneratedRoute'(Route),
   'Route'(Route),
   'BgpNeighborGeneratedRoute_constructor'(Node, Network, NeighborIp, Route) /*fn*/,
   'BgpNeighborGeneratedRoute_neighborIp'(Route, NeighborIp) /*fn*/,
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   'BgpNeighborIpGeneratedRoute'(Node, NeighborIp, Network),
   Type = 'GeneratedRouteType_BGP_NEIGHBOR',
   Protocol = 'aggregate'.

'BgpNeighborIpGeneratedRoute'(Node, NeighborIp, Network) :-
   'SetBgpNeighborGeneratedRoute'(Node, NeighborNetwork, Network),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'BgpNeighborGeneratedRoutePolicy'(Node, NeighborIp, Network, Map) :-
   'SetBgpNeighborGeneratedRoutePolicy'(Node, NeighborNetwork, Network, Map),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'GeneratedRoutePolicy'(Route, Policy) :-
   'BgpGeneratedRoute'(Route),
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'SetBgpGeneratedRoutePolicy'(Node, Network, Policy).
'GeneratedRoutePolicy'(Route, Policy) :-
   'BgpNeighborGeneratedRoute'(Route),
   'BgpNeighborGeneratedRoute_neighborIp'(Route, NeighborIp) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'BgpNeighborGeneratedRoutePolicy'(Node, NeighborIp, Network, Policy).

need_PolicyMapMatchRoute(Map, Route) :-
   'InstalledRoute'(Route),
   'Route_node'(Route, Node) /*fn*/,
   (
      'SetBgpGeneratedRoutePolicy'(Node, _, Map) ;
      'BgpNeighborGeneratedRoutePolicy'(Node, _, _, Map)
   ).

'SetBgpGeneratedRoute'(Node, Network) :-
   'SetBgpGeneratedRoute_flat'(Node, Network_start, Network_end, Prefix_length),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'SetBgpGeneratedRoutePolicy'(Node, Network, Map) :-
   'SetBgpGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'SetBgpNeighborGeneratedRoute'(Node, NeighborNetwork, Network) :-
   'SetBgpNeighborGeneratedRoute_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Network_start, Network_end, Prefix_length),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/,
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'SetBgpNeighborGeneratedRoutePolicy'(Node, NeighborNetwork, Network, Map) :-
   'SetBgpNeighborGeneratedRoutePolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/,
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.
% ibgp advertisement from bgp
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, Advert) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, Med) /*fn*/,
'BgpAdvertisement_network'(Advert, DstIpBlock) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, OriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize) /*fn*/
:-
   Type = 'ibgp',
   PriorType = 'bgp_ti',
   'Ip_NONE'(OriginatorIp),
   'BgpAdvertisement_dstNode'(PrevAdvert, SrcNode) /*fn*/,
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref) /*fn*/,
   'BgpAdvertisement_med'(PrevAdvert, Med) /*fn*/,
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock) /*fn*/,
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp) /*fn*/,
   'BgpAdvertisement_originType'(PrevAdvert, OriginType) /*fn*/,
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol) /*fn*/,
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'AdvertisementPathSize'(PrevAdvert, PathSize) /*fn*/,
   'BestBgpAdvertisement'(PrevAdvert),
   'InstalledBgpAdvertisement'(PrevAdvert),
   'IbgpNeighbors'(SrcNode, SrcIp, DstNode, DstIp).
% advertise an internally received route
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, Network, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, Advert) /*fn*/,
'AdvertisementPathSize'(Advert, PathSize) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, Med) /*fn*/,
'BgpAdvertisement_network'(Advert, Network) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, OriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
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
   'Route_network'(Route, Network) /*fn*/,
   'Route_protocol'(Route, SrcProtocol) /*fn*/,
   'Route_node'(Route, SrcNode) /*fn*/,
   (
      (
         'ActiveGeneratedRoute'(Route),
         'BgpGeneratedRoute'(Route)
      ) ;
      (
         'ActiveGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute'(Route),
         'BgpNeighborGeneratedRoute_neighborIp'(Route, DstIp) /*fn*/
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
         'BgpNeighborGeneratedRoute_neighborIp'(Route, NeighborIp) /*fn*/
      )
   ),
   'Route_node'(Route, Node) /*fn*/,
   'BgpOriginationPolicy'(Node, NeighborIp, Map),
   'IbgpNeighbors'(Node, _, _, NeighborIp).
% incoming transformation
'AdvertisementCommunity'(Advert, Community) :-
   PriorType = 'ibgp_to',
   'BgpNeighborSendCommunity'(DstNode, SrcIp),
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode) /*fn*/,
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp) /*fn*/,
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
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/,
   'BgpAdvertisementRoute'(Advert, Route) /*fn*/
:-
   Type = 'ibgp_ti',
   Protocol = 'ibgp',
   'BgpAdvertisement_type'(Advert, Type) /*fn*/,
   'BgpAdvertisement_network'(Advert, Network) /*fn*/,
   'BgpAdvertisement_nextHopIp'(Advert, NextHopIp) /*fn*/,
   'BgpAdvertisement_dstNode'(Advert, Node) /*fn*/,
   'BgpAdvertisement_med'(Advert, Cost) /*fn*/,
   'BestBgpAdvertisement'(Advert),
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/,
   \+ 'BestBgpRouteNetwork'(Node, Network).

% ibgp transformed incoming
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, TNextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, TOriginType, Advert) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, TLocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, TMed) /*fn*/,
'BgpAdvertisement_network'(Advert, DstIpBlock) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, TNextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, TOriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, TSrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize) /*fn*/
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
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp) /*fn*/,
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode) /*fn*/,
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref) /*fn*/,
   'BgpAdvertisement_med'(PrevAdvert, Med) /*fn*/,
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock) /*fn*/,
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp) /*fn*/,
   'BgpAdvertisement_originatorIp'(PrevAdvert, OriginatorIp) /*fn*/,
   'BgpAdvertisement_originType'(PrevAdvert, OriginType) /*fn*/,
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp) /*fn*/,
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode) /*fn*/,
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol) /*fn*/,
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'AdvertisementPathSize'(PrevAdvert, PathSize) /*fn*/,
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
   'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
   'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
   'BgpAdvertisement_type'(Advert, Type) /*fn*/,
   'BgpImportPolicy'(DstNode, SrcIp, Map).
'IbgpNeighborTo'(Node, Neighbor, NeighborIp) :-
   'NetworkOf'(NeighborIp, Prefix_length, NeighborNetwork) /*fn*/,
   'InstalledRoute'(Route),
   'Route_network'(Route, NeighborNetwork) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
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
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode) /*fn*/,
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp) /*fn*/,
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
'BgpAdvertisement_constructor'(Type, DstIpBlock, TNextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, TOriginType, Advert) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, TLocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, TMed) /*fn*/,
'BgpAdvertisement_network'(Advert, DstIpBlock) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, TNextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, TOriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, TSrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize) /*fn*/
:-
   Type = 'ibgp_to',
   PriorType = 'ibgp',
   'BgpAdvertisement_dstIp'(PrevAdvert, DstIp) /*fn*/,
   'BgpAdvertisement_dstNode'(PrevAdvert, DstNode) /*fn*/,
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref) /*fn*/,
   'BgpAdvertisement_med'(PrevAdvert, Med) /*fn*/,
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock) /*fn*/,
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp) /*fn*/,
   'BgpAdvertisement_originatorIp'(PrevAdvert, OriginatorIp) /*fn*/,
   'BgpAdvertisement_originType'(PrevAdvert, OriginType) /*fn*/,
   'BgpAdvertisement_srcIp'(PrevAdvert, SrcIp) /*fn*/,
   'BgpAdvertisement_srcNode'(PrevAdvert, SrcNode) /*fn*/,
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol) /*fn*/,
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'AdvertisementPathSize'(PrevAdvert, PathSize) /*fn*/,
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
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'SetBgpOriginationPolicy'(Node, NeighborNetwork, Map) :-
   'SetBgpOriginationPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.
'BgpImportPolicy'(Node, NeighborIp, Map) :-
   'SetBgpImportPolicy'(Node, NeighborNetwork, Map),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'SetBgpImportPolicy'(Node, NeighborNetwork, Map) :-
   'SetBgpImportPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.
'BgpExportPolicy'(Node, NeighborIp, Map) :-
   'SetBgpExportPolicy'(Node, NeighborNetwork, Map),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'SetBgpExportPolicy'(Node, NeighborNetwork, Map) :-
   'SetBgpExportPolicy_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Map),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.
'AdvertisementClusterId'(Advert, ClusterId) :-
   (
      PriorType = 'ibgp' ;
      PriorType = 'ibgp_ti' ;
      PriorType = 'ibgp_to'
   ),
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'ParentAdvertisement'(PrevAdvert, Advert),
   'AdvertisementClusterId'(PrevAdvert, ClusterId).
'AdvertisementClusterId'(Advert, ClusterId) :-
   Type = 'ibgp',
   'BgpAdvertisement_type'(Advert, Type) /*fn*/,
   'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
   'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
   'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
   'Ip_NONE'(IpNone),
   OriginatorIp \== IpNone,
   'RouteReflectorClient'(SrcNode, DstIp, ClusterId).

% ibgp route reflection
'BgpAdvertisement'(Advert),
'BgpAdvertisement_constructor'(Type, DstIpBlock, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, Advert) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, Med) /*fn*/,
'BgpAdvertisement_network'(Advert, DstIpBlock) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, OriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
'ParentAdvertisement'(PrevAdvert, Advert),
'AdvertisementPathSize'(Advert, PathSize) /*fn*/
:-
   Type = 'ibgp',
   PriorType = 'ibgp_ti',
   'BgpAdvertisement_dstNode'(PrevAdvert, SrcNode) /*fn*/,
   'BgpAdvertisement_localPref'(PrevAdvert, LocalPref) /*fn*/,
   'BgpAdvertisement_med'(PrevAdvert, Med) /*fn*/,
   'BgpAdvertisement_network'(PrevAdvert, DstIpBlock) /*fn*/,
   'BgpAdvertisement_nextHopIp'(PrevAdvert, NextHopIp) /*fn*/,
   'BgpAdvertisement_originatorIp'(PrevAdvert, PrevOriginatorIp) /*fn*/,
   'BgpAdvertisement_originType'(PrevAdvert, OriginType) /*fn*/,
   'BgpAdvertisement_srcIp'(PrevAdvert, SenderIp) /*fn*/,
   'BgpAdvertisement_srcProtocol'(PrevAdvert, SrcProtocol) /*fn*/,
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'AdvertisementPathSize'(PrevAdvert, PathSize) /*fn*/,
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
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'SetRouteReflectorClient'(Node, NeighborNetwork, ClusterId) :-
   'SetRouteReflectorClient_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, ClusterId),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.
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
   'BgpAdvertisement_type'(PrevAdvert, PriorType) /*fn*/,
   'ParentAdvertisement'(PrevAdvert, Advert).

'BestBgpAdvertisement'(Advert) :-
   'MinAsPathLengthBgpAdvertisement'(Advert).

'BestPerProtocolRoute'(Route) :-
   'BestBgpRoute'(Route) ;
   'BestIbgpRoute'(Route).

'BgpDefaultLocalPref'(Node, NeighborIp, LocalPref) :-
   'SetBgpDefaultLocalPref'(Node, NeighborNetwork, LocalPref),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'BgpNeighborDefaultMetric'(Node, NeighborIp, Metric) :-
   'SetBgpNeighborDefaultMetric'(Node, NeighborNetwork, Metric),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'BgpNeighborIp'(Node, NeighborIp) :-
   'SetBgpNeighborNetwork'(Node, NeighborNetwork),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'BgpNeighbors'(Node1, Ip1, Node2, Ip2) :-
   'IpReadyInt'(Node1, Int1, Ip1, _),
   'IpReadyInt'(Node2, Int2, Ip2, _),
   'LanAdjacent'(Node1, Int1, Node2, Int2),
   'BgpNeighborIp'(Node1, Ip2),
   'BgpNeighborIp'(Node2, Ip1).

'BgpNeighborSendCommunity'(Node, NeighborIp) :-
   'SetBgpNeighborSendCommunity'(Node, NeighborNetwork),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'InstalledBgpAdvertisement'(Advert) :-
   'InstalledBgpAdvertisementRoute'(Advert, _) /*fn*/.

'InstalledBgpAdvertisementRoute'(Advert, Route ) /*fn*/:-
   'BgpAdvertisementRoute'(Advert, Route) /*fn*/,
   'InstalledRoute'(Route).

'LocalAs'(Node, NeighborIp, LocalAs) :-
   'SetLocalAs'(Node, NeighborNetwork, LocalAs),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'MaxLocalPref'(Node, Network, MaxLocalPref ) /*fn*/:-
   agg(MaxLocalPref = max(LocalPref),(
      'ReceivedBgpAdvertisement'(Advert),
      'BgpAdvertisement_dstNode'(Advert, Node) /*fn*/,
      'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
      'BgpAdvertisement_network'(Advert, Network) /*fn*/)).

'MaxLocalPrefBgpAdvertisement'(Advert) :-
   'ReceivedBgpAdvertisement'(Advert),
   \+ 'OriginatedBgpNetwork'(Node, Network),
   'BgpAdvertisement_dstNode'(Advert, Node) /*fn*/,
   'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
   'BgpAdvertisement_network'(Advert, Network) /*fn*/,
   'MaxLocalPref'(Node, Network, LocalPref) /*fn*/.

'MinAsPathLengthBgpAdvertisement'(Advert) :-
   'MaxLocalPrefBgpAdvertisement'(Advert),
   'BgpAdvertisement_dstNode'(Advert, Node) /*fn*/,
   'BgpAdvertisement_network'(Advert, Network) /*fn*/,
   'AdvertisementPathSize'(Advert, BestAsPathSize) /*fn*/,
   'MinAsPathSize'(Node, Network, BestAsPathSize) /*fn*/.

'MinAsPathSize'(Node, Network, MinSize ) /*fn*/:-
   agg(MinSize = min(Size),(
      'MaxLocalPrefBgpAdvertisement'(Advert),
      'BgpAdvertisement_dstNode'(Advert, Node) /*fn*/,
      'BgpAdvertisement_network'(Advert, Network) /*fn*/,
      'AdvertisementPathSize'(Advert, Size) /*fn*/)).

need_PolicyMapMatchAdvert(Map, Advert)
:-
   (
      Type = 'bgp' ;
      Type = 'ibgp'
   ),
   'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
   'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
   'BgpAdvertisement_type'(Advert, Type) /*fn*/,
   'BgpExportPolicy'(SrcNode, DstIp, Map).

'OriginalBgpAdvertisement'(Advert) :-
   'OriginalBgpAdvertisementRoute'(Advert, _).

'ReceivedBgpAdvertisement'(Advert) :-
   'BgpAdvertisement_type'(Advert, Type) /*fn*/,
   (
      Type = 'bgp_ti' ;
      Type = 'ibgp_ti'
   ).

'RemoteAs'(Node, NeighborIp, RemoteAs) :-
   'SetRemoteAs'(Node, NeighborNetwork, RemoteAs),
   'NetworkOf'(NeighborIp, _, NeighborNetwork) /*fn*/.

'SetBgpDefaultLocalPref'(Node, NeighborNetwork, LocalPref) :-
   'SetBgpDefaultLocalPref_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, LocalPref),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.

'SetBgpNeighborDefaultMetric'(Node, NeighborNetwork, Metric) :-
   'SetBgpNeighborDefaultMetric_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, Metric),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.

'SetBgpNeighborNetwork'(Node, NeighborNetwork) :-
   'SetBgpNeighborNetwork_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.

'SetBgpNeighborSendCommunity'(Node, NeighborNetwork) :-
   'SetBgpNeighborSendCommunity_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.

'SetLocalAs'(Node, NeighborNetwork, LocalAs) :-
   'SetLocalAs_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, LocalAs),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.

'SetRemoteAs'(Node, NeighborNetwork, RemoteAs) :-
   'SetRemoteAs_flat'(Node, NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, RemoteAs),
   'Network_constructor'(NeighborNetwork_start, NeighborNetwork_end, NeighborNetwork_prefix_length, NeighborNetwork) /*fn*/.
'BgpAdvertisement_details'(Type, DstIpBlock, NextHopIp, SrcIp, DstIp, SrcProtocol, SrcNode, DstNode, LocalPref, Med, OriginatorIp, OriginType)
:-
   'BgpAdvertisement_constructor'(Type, DstIpBlock, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, Advert) /*fn*/,
   'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
   'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
   'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
   'BgpAdvertisement_med'(Advert, Med) /*fn*/,
   'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/.
'CommunityListFirstMatch'(List, Community, FirstLine ) /*fn*/:-
   agg(FirstLine = min(Line),( 'CommunityListMatch'(List, Line, Community))).

'CommunityListMatch'(List, Line, Community) :-
   'SetCommunityListLine'(List, Line, Community).
   %'Pneed_CommunityListMatch'(List, Line, Community). %TODO: Implement this

'CommunityListPermit'(List, Line, Community) :-
   'CommunityListFirstMatch'(List, Community, Line) /*fn*/,
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
   'Route_network'(Route, MatchNet) /*fn*/,
   'Network_prefix_length'(MatchNet, Prefix_length) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
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
   'Route_network'(Route, MatchNet) /*fn*/,
   'Network_prefix_length'(MatchNet, Prefix_length) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_nextHopIp'(Route, RouteNextHopIp) /*fn*/,
   'Ip_NONE'(RouteNextHopIp),
   'InterfaceRoute_nextHopInt'(Route, Interface) /*fn*/,
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
   'Route_network'(Route, MatchNet) /*fn*/,
   'Network_prefix_length'(MatchNet, Prefix_length) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   NextHop = '(none)',
   'Ip_NONE'(NextHopIp),
   'InterfaceRoute_nextHopInt'(Route, Interface) /*fn*/,
   Interface = 'null_interface',
   NextHopInt = 'null_interface'.
% static non-null interface+NextHopIp route -- send to all adjacent neighbors who would route
%    the declared nextHopIp to an interface other than the one on which the packet was received
'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, NextHopIp) :-
   \+ 'HasIp'(Node, Ip),
   'LongestPrefixNetworkMatch'(Node, Ip, MatchNet),
   'InstalledRoute'(Route),
   'StaticIntRoute'(Route),
   'Route_network'(Route, MatchNet) /*fn*/,
   'Network_prefix_length'(MatchNet, Prefix_length) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_nextHopIp'(Route, RouteNextHopIp) /*fn*/,
   'Ip_NONE'(IpNone),
   NextHopIp \== IpNone,
   'InterfaceRoute_nextHopInt'(Route, Interface) /*fn*/,
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
   'Route_network'(Route, MatchNet) /*fn*/,
   'Network_prefix_length'(MatchNet, Prefix_length) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   /*(
      NextHopInt = 'flow_sink_termination' ;
      (
         NextHopIp \== 'Ip_ZERO',

      )
   ),*/
   'Route_nextHopIp'(Route, RouteNextHopIp) /*fn*/,
   'Fib'(Node, RouteNextHopIp, _, Interface, NextHop, NextHopInt, NextHopIp).

'FibNetwork'(Node, Network, Interface, NextHop, NextHopInt) :-
   'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, _),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Network_address'(Network, Network_address) /*fn*/,
   'Ip_address'(Ip:Network_address),
   'NetworkOf'(Ip, Prefix_length, Network) /*fn*/.
'FibNetwork'(Node, Network, Interface, NextHop, NextHopInt) :-
   'Fib'(Node, Ip, Prefix_length, Interface, NextHop, NextHopInt, _),
   'FibNeighborIp'(Node, Ip),
   'Network_address'(Network, Network_address) /*fn*/,
   'Ip_address'(Ip:Network_address),
   'NetworkOf'(Ip, Prefix_length, Network) /*fn*/.

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
   'Route_network'(ContributingRoute, ContributingNetwork) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(ContributingRoute, Node) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Network_address'(ContributingNetwork, ContributingAddress) /*fn*/,
   'Network_address'(Network, StartAddress) /*fn*/,
   'Network_end'(Network, EndAddress) /*fn*/,
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
   'InterfaceRoute_nextHopInt'(Route, NextHopInt) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/
:-
   'MinAdminContributingRoute'(Route, ContributingRoute),
   'SetGeneratedRouteDiscard'(Node, Network),
   NextHopInt = 'null_interface',
   'Ip_NONE'(NextHopIp),
   'Route_node'(Route, Node) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_network'(ContributingRoute, ContributingNetwork) /*fn*/,
   'Network_address'(ContributingNetwork, ContributingAddress) /*fn*/,
   'MinContributingRouteAddress'(Route, ContributingAddress) /*fn*/.

'BestGlobalGeneratedRoute'(Route),
   'BestGlobalGeneratedRoute_nextHopIp'(Route, NextHopIp)
:-
   'MinAdminContributingRoute'(Route, ContributingRoute),
   \+ 'SetGeneratedRouteDiscard'(Node, Network),
   'Route_node'(Route, Node) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_network'(ContributingRoute, ContributingNetwork) /*fn*/,
   'Route_nextHopIp'(ContributingRoute, NextHopIp) /*fn*/,
   'Network_address'(ContributingNetwork, ContributingAddress) /*fn*/,
   'MinContributingRouteAddress'(Route, ContributingAddress) /*fn*/.

'BestPerProtocolRoute'(Route) :-
   'BestGlobalGeneratedRoute'(Route).

'GlobalGeneratedRoute'(Route),
   'Route'(Route),
   'GeneratedRoute_constructor'(Node, Network, Type, Route) /*fn*/,
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
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
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'SetGeneratedRoutePolicy'(Node, Network, Policy).

'MinAdminContributingRoute'(Route, ContributingRoute) :-
   'ActiveGeneratedRouteContributor'(Route, ContributingRoute),
   'GlobalGeneratedRoute'(Route),
   'Route_admin'(ContributingRoute, Admin) /*fn*/,
   'MinContributingRouteAdmin'(Route, Admin) /*fn*/.

'MinBestGlobalGeneratedRoute_nextHopIpInt'(Route, MinNextHopIpInt ) /*fn*/:-
   agg(MinNextHopIpInt = min(NextHopIpInt),(
      'BestGlobalGeneratedRoute_nextHopIp'(Route, NextHopIp),
      'Ip_address'(NextHopIp:NextHopIpInt))).

'MinContributingRouteAddress'(Route, MinAddress ) /*fn*/:-
   agg(MinAddress = min(Address),(
      'MinAdminContributingRoute'(Route, ContributingRoute),
      'Route_network'(ContributingRoute, Network) /*fn*/,
      'Network_address'(Network, Address) /*fn*/)).

'MinContributingRouteAdmin'(Route, MinAdmin ) /*fn*/:-
   agg(MinAdmin = min(Admin),(
      'ActiveGeneratedRouteContributor'(Route, ContributingRoute),
      'GlobalGeneratedRoute'(Route),
      'Route_admin'(ContributingRoute, Admin) /*fn*/)).

need_PolicyMapMatchRoute(Map, Route) :-
   'SetGeneratedRoutePolicy'(Node, _, Map),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node) /*fn*/.

'Route_nextHopIp'(Route, NextHopIp ) /*fn*/:-
   'MinBestGlobalGeneratedRoute_nextHopIpInt'(Route, NextHopIpInt) /*fn*/,
   'Ip_address'(NextHopIp:NextHopIpInt).

'SetGeneratedRoute'(Node, Network, Admin) :-
   'SetGeneratedRoute_flat'(Node, Network_start, Network_end, Prefix_length, Admin),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'SetGeneratedRouteDiscard'(Node, Network) :-
   'SetGeneratedRouteDiscard_flat'(Node, Network_start, Network_end, Prefix_length),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'SetGeneratedRouteMetric'(Node, Network, Metric) :-
   'SetGeneratedRouteMetric_flat'(Node, Network_start, Network_end, Prefix_length, Metric),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'SetGeneratedRoutePolicy'(Node, Network, Map) :-
   'SetGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.
'Ip'(Ip) :-
   'SetIpInt'(Node, Interface, Ip, Prefix_length).
'HasIp'(Node, Ip) :-
   'IpReadyInt'(Node, _, Ip, _).

'HasNetwork'(Node, Network) :-
   'IpReadyInt'(Node, _, Ip, Prefix_length),
   'NetworkOf'(Ip, Prefix_length, Network) /*fn*/.

'IpCount'(Ip, Cnt ) /*fn*/:-
   agg(Cnt = count,
      'HasIp'(_, Ip)).

'Network'(Network),
   'Network_address'(Network, Network_start) /*fn*/,
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/,
   'Network_index'(Network, Network_start, Network_end, Prefix_length),
   'Network_end'(Network, Network_end) /*fn*/,
   'Network_prefix_length'(Network, Prefix_length) /*fn*/
:-
   'IpReadyInt'(_, _, Ip, _),
   'Ip_address'(Ip:Network_start),
   Network_end = Network_start,
   Prefix_length = 32.

'IpReadyInt'(Node, Interface, Ip, Prefix_length) :-
   'SetIpInt'(Node, Interface, Ip, Prefix_length),
   'SetActiveInt'(Node, Interface).
'Ip_address'(N:N) :-
   'Ip'(N).

'Ip'(X) :-
   'Ip_NONE'(X).

'Ip'(X) :-
   'Ip_ZERO'(X).

'Ip'(NetworkIp) :-
   'SetNetwork'(NetworkIp, Network_start, Network_end, Prefix_length).
'Ip_NONE'(-1).
'Ip_ZERO'(0).

'Network'(Network),
'Network_address'(Network, Network_start) /*fn*/,
'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/,
'Network_index'(Network, Network_start, Network_end, Prefix_length),
'Network_end'(Network, Network_end) /*fn*/,
'Network_prefix_length'(Network, Prefix_length) /*fn*/
:-
   'SetNetwork'(_, Network_start, Network_end, Prefix_length).

'NetworkOf'(Ip, Prefix_length, Network ) /*fn*/:-
   'Ip_address'(Ip:Ip_int),
   Network_start =< Ip_int,
   Ip_int =< Network_end,
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.
'IpAccessListDeny'(List, Line, Flow) :-
   'SetIpAccessListLine_deny'(List, Line),
   'IpAccessListFirstMatch'(List, Flow, Line) /*fn*/.
'IpAccessListExists'(List) :-
   'SetIpAccessListLine_permit'(List, _).
'IpAccessListExists'(List) :-
   'SetIpAccessListLine_deny'(List, _).

'IpAccessListDeny'(List, -1, Flow) :-
   \+ 'IpAccessListMatch'(List, _, Flow) /*fn*/,
   'IpAccessListExists'(List),
   'Flow'(Flow).

'IpAccessListPermit'(List, Line, Flow) :-
   'SetIpAccessListLine_permit'(List, Line),
   'IpAccessListFirstMatch'(List, Flow, Line) /*fn*/.

'IpAccessListFirstMatch'(List, Flow, FirstMatchLine ) /*fn*/:-
   agg(FirstMatchLine = min(Line),(
      'IpAccessListMatch'(List, Line, Flow))).

'IpAccessListLine'(List, Line) :-
   'SetIpAccessListLine_deny'(List, Line) ;
   'SetIpAccessListLine_permit'(List, Line).

'IpAccessListMatch'(List, Line, Flow) :-
   'IpAccessListMatchDstIp'(List, Line, Flow),
   'IpAccessListMatchDstPort'(List, Line, Flow),
   'IpAccessListMatchProtocol'(List, Line, Flow),
   'IpAccessListMatchSrcIp'(List, Line, Flow),
   'IpAccessListMatchSrcPort'(List, Line, Flow).

'IpAccessListMatchDstIp'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
   (
      \+ 'SetIpAccessListLine_dstIpRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_dstIpRange'(List, Line, DstIp_start, DstIp_end),
         'Ip_address'(DstIp:DstIpNum),
         DstIp_start =< DstIpNum,
         DstIpNum =< DstIp_end
      )
   ).

'IpAccessListMatchDstPort'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_dstPort'(Flow, DstPort) /*fn*/,
   (
      \+ 'SetIpAccessListLine_dstPortRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_dstPortRange'(List, Line, DstPort_start, DstPort_end),
         DstPort_start =< DstPort,
         DstPort =< DstPort_end
      )
   ).

'IpAccessListMatchProtocol'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_ipProtocol'(Flow, Protocol) /*fn*/,
   (
      \+ 'SetIpAccessListLine_protocol'(List, Line, _) ;
      'SetIpAccessListLine_protocol'(List, Line, Protocol)
   ).

'IpAccessListMatchSrcIp'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_srcIp'(Flow, SrcIp) /*fn*/,
   (
      \+ 'SetIpAccessListLine_srcIpRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_srcIpRange'(List, Line, SrcIp_start, SrcIp_end),
         'Ip_address'(SrcIp:SrcIpNum),
         SrcIp_start =< SrcIpNum,
         SrcIpNum =< SrcIp_end
      )
   ).

'IpAccessListMatchSrcPort'(List, Line, Flow) :-
   'IpAccessListLine'(List, Line),
   'Flow_srcPort'(Flow, SrcPort) /*fn*/,
   (
      \+ 'SetIpAccessListLine_srcPortRange'(List, Line, _, _) ;
      (
         'SetIpAccessListLine_srcPortRange'(List, Line, SrcPort_start, SrcPort_end),
         SrcPort_start =< SrcPort,
         SrcPort =< SrcPort_end
      )
   ).
'GeneratedRoutePolicy'(Route, Policy) :-
   'IsisGeneratedRoute'(Route),
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'SetIsisGeneratedRoutePolicy'(Node, Network, Policy).

'IsisGeneratedRoute'(Route),
   'Route'(Route),
   'GeneratedRoute_constructor'(Node, Network, Type, Route) /*fn*/,
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   'SetIsisGeneratedRoute'(Node, Network),
   Type = 'GeneratedRouteType_ISIS',
   Protocol = 'aggregate'.

need_PolicyMapMatchRoute(Map, Route) :-
   'SetIsisGeneratedRoutePolicy'(Node, _, Map),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node) /*fn*/.

'SetIsisGeneratedRoute'(Node, Network) :-
   'SetIsisGeneratedRoute_flat'(Node, Network_start, Network_end, Prefix_length),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'SetIsisGeneratedRoutePolicy'(Node, Network, Map) :-
   'SetIsisGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.
% (Base case - import routes exported into L1 by L1 neighbors
'IsisL1Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'IsisL1Neighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   Node \== NextHop,
   'IsisExport'(NextHop, Network, ExportCost, Protocol),
   Cost = ExportCost + NodeIntCost,
   Protocol = 'isisL1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
'BestIsisL1Route'(Route),
   'IsisL1Network'(Node, Network)
:-
   'IsisL1Route'(Route),
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'MinIsisL1RouteCost'(Node, Network, Cost) /*fn*/.

'IsisL1EnabledInterface'(Node, Interface) :-
   'SetIsisL1ActiveInterface'(Node, Interface) ;
   'SetIsisL1PassiveInterface'(Node, Interface).
   
'IsisL1Neighbors'(Node1, Int1, Cost1, Node2, Int2, Cost2) :-
   'SetIsisL1Node'(Node1),
   'SetIsisL1Node'(Node2),
   'SetIsisArea'(Node1, Area),
   'SetIsisArea'(Node2, Area),
   'LanAdjacent'(Node1, Int1, Node2, Int2),
   'SetIsisInterfaceCost'(Node1, Int1, Cost1),
   'SetIsisInterfaceCost'(Node2, Int2, Cost2),
   'SetIsisL1ActiveInterface'(Node1, Int1),
   'SetIsisL1ActiveInterface'(Node2, Int2).

% (Base case)
'IsisL1Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'IsisL1Neighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'ConnectedRoute'(NextHop, Network, NextHopConnectedInt),
   'IsisL1EnabledInterface'(NextHop, NextHopConnectedInt),
   'SetIsisInterfaceCost'(NextHop, NextHopConnectedInt, NextHopIntCost) /*fn*/,
   Cost = NodeIntCost + NextHopIntCost,
   Protocol = 'isisL1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
% (Recursive case)
'IsisL1Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'IsisL1Neighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'IsisL1Neighbors'(NextHop, _, _, SecondHop, _, _),
   'BestIsisL1Route'(SubRoute),
   'Route_cost'(SubRoute, SubCost) /*fn*/,
   'Route_network'(SubRoute, Network) /*fn*/,
   'Route_nextHopIp'(SubRoute, SecondHopIp) /*fn*/,
   'Route_node'(SubRoute, NextHop) /*fn*/,
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Cost = SubCost + NodeIntCost,
   Protocol = 'isisL1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.

'MinIsisL1RouteCost'(Node, Network, MinCost ) /*fn*/:-
   agg(MinCost = min(Cost),(
      'IsisL1Route'(Route),
      'Route_cost'(Route, Cost) /*fn*/,
      'Route_network'(Route, Network) /*fn*/,
      'Route_node'(Route, Node) /*fn*/)).
% (Base case - import routes exported into L2 by L2 neighbors
'IsisL2Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'IsisL2Neighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   Node \== NextHop,
   'IsisExport'(NextHop, Network, ExportCost, Protocol),
   Cost = ExportCost + NodeIntCost,
   Protocol = 'isisL2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
'BestIsisL2Route'(Route),
   'IsisL2Network'(Node, Network)
:-
   'IsisL2Route'(Route),
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'MinIsisL2RouteCost'(Node, Network, Cost) /*fn*/,
   \+ 'IsisL1Network'(Node, Network).

'IsisL2EnabledInterface'(Node, Interface) :-
   'SetIsisL2ActiveInterface'(Node, Interface) ;
   'SetIsisL2PassiveInterface'(Node, Interface).
   
'IsisL2Neighbors'(Node1, Int1, Cost1, Node2, Int2, Cost2) :-
   'SetIsisL2Node'(Node1),
   'SetIsisL2Node'(Node2),
   'SetIsisArea'(Node1, _),
   'SetIsisArea'(Node2, _),
   'LanAdjacent'(Node1, Int1, Node2, Int2),
   'SetIsisInterfaceCost'(Node1, Int1, Cost1),
   'SetIsisInterfaceCost'(Node2, Int2, Cost2),
   'SetIsisL2ActiveInterface'(Node1, Int1),
   'SetIsisL2ActiveInterface'(Node2, Int2).

% (Base case)
'IsisL2Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'IsisL2Neighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'ConnectedRoute'(NextHop, Network, NextHopConnectedInt),
   'IsisL2EnabledInterface'(NextHop, NextHopConnectedInt),
   'SetIsisInterfaceCost'(NextHop, NextHopConnectedInt, NextHopIntCost) /*fn*/,
   Cost = NodeIntCost + NextHopIntCost,
   Protocol = 'isisL2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
% (Recursive (Forward L2 Routes)
'IsisL2Route'(Route),
   'Route'(Route),
   'NonIsisExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'IsisL2Neighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'IsisL2Neighbors'(NextHop, _, _, SecondHop, _, _),
   'BestIsisL2Route'(SubRoute),
   'Route_cost'(SubRoute, SubCost) /*fn*/,
   'Route_network'(SubRoute, Network) /*fn*/,
   'Route_nextHopIp'(SubRoute, SecondHopIp) /*fn*/,
   'Route_node'(SubRoute, NextHop) /*fn*/,
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Cost = SubCost + NodeIntCost,
   Protocol = 'isisL2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.

'MinIsisL2RouteCost'(Node, Network, MinCost ) /*fn*/:-
   agg(MinCost = min(Cost),(
      'IsisL2Route'(Route),
      'Route_cost'(Route, Cost) /*fn*/,
      'Route_network'(Route, Network) /*fn*/,
      'Route_node'(Route, Node) /*fn*/)).
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
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
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
         'Route_cost'(Route, NewCost) /*fn*/
      )
   ),
   (
      \+ 'ConnectedRoute'(Node, Network, _) ;
      (
         'ConnectedRoute'(Node, Network, Interface),
         \+ 'SetIsisInterfaceCost'(Node, Interface, _) /*fn*/
      )
   ).

need_PolicyMapMatchRoute(Map, Route) :-
   'SetIsisOutboundPolicyMap'(Node, Map),
   'Route_node'(Route, Node) /*fn*/,
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
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'MinOspfE1RouteCost'(Node, Network, Cost) /*fn*/,
   \+ 'OspfNetwork'(Node, Network),
   \+ 'OspfIANetwork'(Node, Network).

'MinOspfE1RouteCost'(Node, Network, MinCost ) /*fn*/:-
   agg(MinCost = min(Cost),(
      'OspfE1Route'(Route),
      'Route_cost'(Route, Cost) /*fn*/,
      'Route_network'(Route, Network) /*fn*/,
      'Route_node'(Route, Node) /*fn*/)).

% (Base case) Import ospfE1 routes exported by ospf neighbors
'OspfE1Route'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'OspfE1Route_constructor'(Advertiser, Node, Network, NextHopIp, Route) /*fn*/,
   'OspfRoute_advertiser'(Route, Advertiser) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   Cost = ExportCost + CostToAdvertiser,
   \+ 'ConnectedRoute'(Node, Network, _),
   'OspfNeighbors'(Node, _, CostToAdvertiser, NextHop, NextHopInt, _, _, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   Node \== Advertiser,
   Node \== NextHop,
   Advertiser = NextHop,
   'OspfExport'(Advertiser, Network, ExportCost, Protocol),
   Protocol = 'ospfE1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
% (Recursive case) Propagate ospfE1 over ospf
'OspfE1Route'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'OspfE1Route_constructor'(Advertiser, Node, Network, NextHopIp, Route) /*fn*/,
   'OspfRoute_advertiser'(Route, Advertiser) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _, _, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'OspfNeighbors'(NextHop, _, _, SecondHop, _, _, _, Area),
   'BestOspfE1Route'(SubRoute),
   'OspfRoute_advertiser'(SubRoute, Advertiser) /*fn*/,
   'Route_cost'(SubRoute, SubCost) /*fn*/,
   'Route_network'(SubRoute, Network) /*fn*/,
   'Route_nextHopIp'(SubRoute, SecondHopIp) /*fn*/,
   'Route_node'(SubRoute, NextHop) /*fn*/,
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Node \== Advertiser,
   Cost = SubCost + NodeIntCost,
   Protocol = 'ospfE1',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
'BestOspfE2Route'(Route) :-
   'OspfE2Route'(Route),
   'OspfRoute_costToAdvertiser'(Route, CostToAdvertiser) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'MinOspfE2RouteCostToAdvertiser'(Node, Network, CostToAdvertiser) /*fn*/,
   \+ 'OspfE1Network'(Node, Network),
   \+ 'OspfNetwork'(Node, Network),
   \+ 'OspfIANetwork'(Node, Network).

'MinOspfE2RouteCostToAdvertiser'(Node, Network, MinCostToAdvertiser ) /*fn*/:-
   agg(MinCostToAdvertiser = min(CostToAdvertiser),(
      'OspfE2Route'(Route),
      'OspfRoute_costToAdvertiser'(Route, CostToAdvertiser) /*fn*/,
      'Route_network'(Route, Network) /*fn*/,
      'Route_node'(Route, Node) /*fn*/)).   

% (Base case) Import ospfE2 routes exported by ospf neighbors
'OspfE2Route'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'OspfE2Route_constructor'(Advertiser, CostToAdvertiser, Node, Network, NextHopIp, Route) /*fn*/,
   'OspfRoute_advertiser'(Route, Advertiser) /*fn*/,
   'OspfRoute_advertiserIp'(Route, AdvertiserIp) /*fn*/,
   'OspfRoute_costToAdvertiser'(Route, CostToAdvertiser) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _),
   'OspfNeighbors'(Node, _, CostToAdvertiser, NextHop, NextHopInt, _, _, _),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   Node \== Advertiser,
   Node \== NextHop,
   Advertiser = NextHop,
   'SetOspfRouterId'(Advertiser, AdvertiserIp),
   'OspfExport'(Advertiser, Network, Cost, Protocol),
   Protocol = 'ospfE2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
% (Recursive case) Propagate ospfE2 over ospf
'OspfE2Route'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'OspfE2Route_constructor'(Advertiser, CostToAdvertiser, Node, Network, NextHopIp, Route) /*fn*/,
   'OspfRoute_advertiser'(Route, Advertiser) /*fn*/,
   'OspfRoute_advertiserIp'(Route, AdvertiserIp) /*fn*/,
   'OspfRoute_costToAdvertiser'(Route, CostToAdvertiser) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _, _, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'OspfNeighbors'(NextHop, _, _, SecondHop, _, _, _, Area),
   'BestOspfE2Route'(SubRoute),
   'Route_cost'(SubRoute, Cost) /*fn*/,
   'Route_network'(SubRoute, Network) /*fn*/,
   'Route_nextHopIp'(SubRoute, SecondHopIp) /*fn*/,
   'Route_node'(SubRoute, NextHop) /*fn*/,
   'OspfRoute_advertiser'(SubRoute, Advertiser) /*fn*/,
   'OspfRoute_advertiserIp'(SubRoute, AdvertiserIp) /*fn*/,
   'OspfRoute_costToAdvertiser'(SubRoute, SubCost) /*fn*/,
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Node \== Advertiser,
   CostToAdvertiser = SubCost + NodeIntCost,
   Protocol = 'ospfE2',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
need_PolicyMapMatchRoute(Map, Route) :-
   'SetOspfOutboundPolicyMap'(Node, Map),
   'Route_node'(Route, Node) /*fn*/,
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
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
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
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'SetOspfGeneratedRoutePolicy'(Node, Network, Policy).

need_PolicyMapMatchRoute(Map, Route) :-
   'SetOspfGeneratedRoutePolicy'(Node, _, Map),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node) /*fn*/.

'OspfGeneratedRoute'(Route),
   'Route'(Route),
   'GeneratedRoute_constructor'(Node, Network, Type, Route) /*fn*/,
   'GeneratedRoute'(Route),
   'GeneratedRoute_type'(Route, Type) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   'SetOspfGeneratedRoute'(Node, Network),
   Type = 'GeneratedRouteType_OSPF',
   Protocol = 'aggregate'.

'SetOspfGeneratedRoute'(Node, Network) :-
   'SetOspfGeneratedRoute_flat'(Node, Network_start, Network_end, Prefix_length),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'SetOspfGeneratedRoutePolicy'(Node, Network, Map) :-
   'SetOspfGeneratedRoutePolicy_flat'(Node, Network_start, Network_end, Prefix_length, Map),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.
'BestOspfIARoute'(Route),
   'OspfIANetwork'(Node, Network)
:-
   'OspfIARoute'(Route),
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'MinOspfIARouteCost'(Node, Network, Cost) /*fn*/,
   \+ 'OspfNetwork'(Node, Network).

'MinOspfIARouteCost'(Node, Network, MinCost ) /*fn*/:-
   agg(MinCost = min(Cost),(
      'OspfIARoute'(Route),
      'Route_cost'(Route, Cost) /*fn*/,
      'Route_network'(Route, Network) /*fn*/,
      'Route_node'(Route, Node) /*fn*/)).

% distribute connected routes from another area into backbone area
'OspfIARoute'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _, _, 0),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'ConnectedRoute'(NextHop, Network, NextHopConnectedInt),
   'SetOspfInterface'(NextHop, NextHopConnectedInt, Area),
   Area \== 0,
   'SetOspfInterfaceCost'(NextHop, NextHopConnectedInt, NextHopIntCost) /*fn*/,
   Cost = NodeIntCost + NextHopIntCost,
   Protocol = 'ospfIA',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
% propagate ospf ia routes through backbone area
'OspfIARoute'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _, _, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'OspfNeighbors'(NextHop, _, _, SecondHop, _, _, _, Area),
   'BestOspfIARoute'(SubRoute),
   'Route_cost'(SubRoute, SubCost) /*fn*/,
   'Route_network'(SubRoute, Network) /*fn*/,
   'Route_nextHopIp'(SubRoute, SecondHopIp) /*fn*/,
   'Route_node'(SubRoute, NextHop) /*fn*/,
   'HasIp'(SecondHop, SecondHopIp),
   Area = 0,
   Node \== SecondHop,
   Cost = SubCost + NodeIntCost,
   Protocol = 'ospfIA',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.

%TODO: OSPF IA Routes propagated from OSPF routes (Not just connected routes) In another area
'BestOspfRoute'(Route),
   'OspfNetwork'(Node, Network)
:-
   'OspfRoute'(Route),
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'MinOspfRouteCost'(Node, Network, Cost) /*fn*/.

'MinOspfRouteCost'(Node, Network, MinCost ) /*fn*/:-
   agg(MinCost = min(Cost),(
      'OspfRoute'(Route),
      'Route_cost'(Route, Cost) /*fn*/,
      'Route_network'(Route, Network) /*fn*/,
      'Route_node'(Route, Node) /*fn*/)).

% (Base case) Connected route on ospf-enabled interface
'OspfRoute'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _, _, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'ConnectedRoute'(NextHop, Network, NextHopConnectedInt),
   'SetOspfInterface'(NextHop, NextHopConnectedInt, Area),
   'SetOspfInterfaceCost'(NextHop, NextHopConnectedInt, NextHopIntCost) /*fn*/,
   Cost = NodeIntCost + NextHopIntCost,
   Protocol = 'ospf',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
% (Recursive case) Propagate ospf over ospf
'OspfRoute'(Route),
   'Route'(Route),
   'NonOspfExportableRoute'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   \+ 'ConnectedRoute'(Node, Network, _), % is this necessary?
   'OspfNeighbors'(Node, _, NodeIntCost, NextHop, NextHopInt, _, _, Area),
   'IpReadyInt'(NextHop, NextHopInt, NextHopIp, _),
   'OspfNeighbors'(NextHop, _, _, SecondHop, _, _, _, Area),
   'BestOspfRoute'(SubRoute),
   'Route_cost'(SubRoute, SubCost) /*fn*/,
   'Route_network'(SubRoute, Network) /*fn*/,
   'Route_nextHopIp'(SubRoute, SecondHopIp) /*fn*/,
   'Route_node'(SubRoute, NextHop) /*fn*/,
   'HasIp'(SecondHop, SecondHopIp),
   Node \== SecondHop,
   Cost = SubCost + NodeIntCost,
   Protocol = 'ospf',
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/.
'BestPerProtocolRoute'(Route) :-
   'BestOspfRoute'(Route) ;
   'BestOspfE1Route'(Route) ;
   'BestOspfE2Route'(Route) ;
   'BestOspfIARoute'(Route).

'OspfNeighbors'(Node1, Int1, Cost1, Node2, Int2, Cost2, Network, Area) :-
   'OspfNode'(Node1, Int1, Cost1, Network, Area),
   'OspfNode'(Node2, Int2, Cost2, Network, Area),
   'LanAdjacent'(Node1, Int1, Node2, Int2).

'OspfNode'(Node, Interface, Cost, Network, Area) :-
   'IpReadyInt'(Node, Interface, Ip, Prefix_length),
   'SetOspfInterfaceCost'(Node, Interface, Cost) /*fn*/,
   'SetOspfInterface'(Node, Interface, Area),
   'NetworkOf'(Ip, Prefix_length, Network) /*fn*/.
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
   'SetPolicyMapClauseMatchInterface'(Map, Clause, Interface).

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

need_PolicyMapMatchRoute(Map, Route) :-
   need_PolicyMapMatchRoute(ReferringMap, Route),
   'SetPolicyMapClauseMatchPolicy'(ReferringMap, _, Map).

% policy maps for advertisements
need_RouteFilterMatchNetwork(List, Network) :-
   need_PolicyMapMatchAdvert(Map, Advert),
   'BgpAdvertisement_network'(Advert, Network) /*fn*/,
   'SetPolicyMapClauseMatchRouteFilter'(Map, _, List).
% policy maps for routes
need_RouteFilterMatchNetwork(List, Network) :-
   need_PolicyMapMatchRoute(Map, Route),
   'Route_network'(Route, Network) /*fn*/,
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
         'BgpAdvertisement_network'(Advert, DstIpBlock) /*fn*/,
         'RouteFilterPermitNetwork'(Filter, DstIpBlock) 
      )
   ),
   (
      \+ 'SetPolicyMapClauseMatchNeighbor'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchNeighbor'(Map, Clause, NeighborIp),
         (
            'BgpAdvertisement_srcIp'(Advert, NeighborIp ) /*fn*/;
            'BgpAdvertisement_dstIp'(Advert, NeighborIp) /*fn*/
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
      \+ 'SetPolicyMapClauseMatchCommunityList'(Map, Clause, _) ;
      (
         'SetPolicyMapClauseMatchCommunityList'(Map, Clause, CommunityList),
         'AdvertisementCommunity'(Advert, Community),
         'CommunityListPermit'(CommunityList, _, Community) 
      )
   ).
   %TODO: Finish definition and replace underscores at top of rule

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
         'Route_protocol'(Route, Protocol) /*fn*/,
         'SetPolicyMapClauseMatchProtocol'(Map, Clause, Protocol)
      )
   ),
   % RouteFilter
   (
      \+ 'SetPolicyMapClauseMatchRouteFilter'(Map, Clause, _);
      (
         'SetPolicyMapClauseMatchRouteFilter'(Map, Clause, Filter),
         'Route_network'(Route, Network) /*fn*/,
         'RouteFilterPermitNetwork'(Filter, Network)
      )
   ),
   % tag
   (
      \+ 'SetPolicyMapClauseMatchTag'(Map, Clause, _);
      (
         'SetPolicyMapClauseMatchTag'(Map, Clause, Tag),
         'Route_tag'(Route, Tag) /*fn*/
      )
   ).

'PolicyMapClauseTransformAdvert'(Map, Clause, PrevAdvert, NextHopIp, LocalPref, OriginType, Med, SrcProtocol)
:-
   'PolicyMapPermitAdvert'(Map, Clause, PrevAdvert),
   'BgpAdvertisement_nextHopIp'(PrevAdvert, PrevNextHopIp ) /*fn*/,
   'BgpAdvertisement_localPref'(PrevAdvert, PrevLocalPref) /*fn*/, 
   'BgpAdvertisement_originType'(PrevAdvert, PrevOriginType) /*fn*/,
   'BgpAdvertisement_med'(PrevAdvert, PrevMed) /*fn*/,
   'BgpAdvertisement_srcProtocol'(PrevAdvert, PrevSrcProtocol) /*fn*/,
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
   'PolicyMapFirstMatchAdvert'(Map, Advert, Clause) /*fn*/,
   \+ 'SetPolicyMapClausePermit'(Map, Clause).
'PolicyMapDenyAdvert'(Map, Advert) :-
   need_PolicyMapMatchAdvert(Map, Advert),
   \+ 'PolicyMapClauseMatchAdvert'(Map, _, Advert).

'PolicyMapDenyFlow'(Map, Flow) :-
   'PolicyMapFirstMatchFlow'(Map, Flow, Clause) /*fn*/,
   \+ 'SetPolicyMapClausePermit'(Map, Clause).
'PolicyMapDenyFlow'(Map, Flow) :-
   need_PolicyMapMatchFlow(Map, Flow),
   \+ 'PolicyMapClauseMatchFlow'(Map, _, Flow).

'PolicyMapDenyRoute'(Map, Route) :-
   'PolicyMapFirstMatchRoute'(Map, Route, Clause) /*fn*/,
   \+ 'SetPolicyMapClausePermit'(Map, Clause).
'PolicyMapDenyRoute'(Map, Route) :-
   need_PolicyMapMatchRoute(Map, Route),
   \+ 'PolicyMapClauseMatchRoute'(Map, _, Route).

'PolicyMapFirstMatchAdvert'(Map, Advert, FirstClause) /*fn*/
:-
   agg(FirstClause = min(Clause),(
      'PolicyMapClauseMatchAdvert'(Map, Clause, Advert))).

'PolicyMapFirstMatchFlow'(Map, Flow, FirstClause) /*fn*/
:-
   agg(FirstClause = min(Clause),(
      'PolicyMapClauseMatchFlow'(Map, Clause, Flow))).

'PolicyMapFirstMatchRoute'(Map, Route, FirstClause ) /*fn*/:-
   agg(FirstClause = min(Clause),(
      'PolicyMapClauseMatchRoute'(Map, Clause, Route))).

'PolicyMapHasClause'(Map, Clause) :-
   'SetPolicyMapClauseDeny'(Map, Clause);
   'SetPolicyMapClausePermit'(Map, Clause).

'PolicyMapPermitAdvert'(Map, Clause, Advert) :-
   'PolicyMapFirstMatchAdvert'(Map, Advert, Clause) /*fn*/,
   'SetPolicyMapClausePermit'(Map, Clause).

'PolicyMapPermitFlow'(Map, Clause, Flow) :-
   'PolicyMapFirstMatchFlow'(Map, Flow, Clause) /*fn*/,
   'SetPolicyMapClausePermit'(Map, Clause).

'PolicyMapPermitRoute'(Map, Clause, Route) :-
   'PolicyMapFirstMatchRoute'(Map, Route, Clause) /*fn*/,
   'SetPolicyMapClausePermit'(Map, Clause).
'Ip'(NextHopIp),
'Ip'(SrcIp),
'Ip'(DstIp),
'Ip'(OriginatorIp)
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
'BgpAdvertisement_constructor'(Type, Network, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, Advert) /*fn*/,
'BgpAdvertisement_dstIp'(Advert, DstIp) /*fn*/,
'BgpAdvertisement_dstNode'(Advert, DstNode) /*fn*/,
'BgpAdvertisement_localPref'(Advert, LocalPref) /*fn*/,
'BgpAdvertisement_med'(Advert, Med) /*fn*/,
'BgpAdvertisement_network'(Advert, Network) /*fn*/,
'BgpAdvertisement_nextHopIp'(Advert, NextHopIp) /*fn*/,
'BgpAdvertisement_originatorIp'(Advert, OriginatorIp) /*fn*/,
'BgpAdvertisement_originType'(Advert, OriginType) /*fn*/,
'BgpAdvertisement_srcIp'(Advert, SrcIp) /*fn*/,
'BgpAdvertisement_srcNode'(Advert, SrcNode) /*fn*/,
'BgpAdvertisement_srcProtocol'(Advert, SrcProtocol) /*fn*/,
'BgpAdvertisement_type'(Advert, Type) /*fn*/,
'PrecomputedAdvertisement_index'(Advert, PcIndex)
:-
   'SetBgpAdvertisement_flat'(PcIndex, Type, Network_start, Network_end, Prefix_length, NextHopIp, SrcNode, SrcIp, DstNode, DstIp, SrcProtocol, OriginType, LocalPref, Med, OriginatorIp),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

'AdvertisementCommunity'(Advert, Community) :-
   'SetBgpAdvertisementCommunity'(PcIndex, Community),
   'PrecomputedAdvertisement_index'(Advert, PcIndex).

'AdvertisementPath'(Advert, Index, As) :-
   'SetBgpAdvertisementPath'(PcIndex, Index, As),
   'PrecomputedAdvertisement_index'(Advert, PcIndex).

'AdvertisementPathSize'(Advert, Size ) /*fn*/:-
   'SetBgpAdvertisementPathSize'(PcIndex, Size),
   'PrecomputedAdvertisement_index'(Advert, PcIndex).

'BestPerProtocolRoute'(Route) :-
   'PrecomputedRoute'(Route).

'IbgpNeighbors'(Node1, Ip1, Node2, Ip2) :-
   'SetIbgpNeighbors'(Node1, Ip1, Node2, Ip2).

'SetPrecomputedRoute'(Node, Network, NextHopIp, Admin, Cost, Protocol, Tag) :-
   'SetPrecomputedRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, Admin, Cost, Protocol, Tag),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

% Precomputed route with next hop ip
'PrecomputedRoute'(Route),
   'Route'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/,
   'Route_tag'(Route, Tag) /*fn*/
:-
   'SetPrecomputedRoute'(Node, Network, NextHopIp, Admin, Cost, Protocol, Tag).
'Ip'(NextHopIp) :-
   'SetStaticIntRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, NextHopInt, Admin, Tag).

'Ip'(NextHopIp) :-
   'SetStaticRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, Admin, Tag).
%'BestConnectedRoute'(Node, Network, NextHop, NextHopIp, Admin, Cost, Protocol) :-
'BestConnectedRoute'(Route),
   'Route'(Route),
   'InterfaceRoute_constructor'(Node, Network, NextHopInt, Protocol, Route) /*fn*/,
   'InterfaceRoute'(Route),
   'InterfaceRoute_nextHopInt'(Route, NextHopInt) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/
:-
   'ConnectedRoute'(Node, Network, NextHopInt),
   'SetNodeVendor'(Node, Vendor),
   'AdministrativeDistance'(Vendor, Protocol, Admin) /*fn*/, 
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
   'NetworkOf'(Ip, Prefix_length, Network) /*fn*/.

'InstalledRoute'(Route) :-
   'MinCostRoute'(Route).

'LongestPrefixNetworkMatch'(Node, Ip, MatchNet) :-
   'LongestPrefixNetworkMatchPrefixLength'(Node, Ip, MaxLength) /*fn*/,
   'NetworkMatch'(Node, Ip, MatchNet, MaxLength).

'LongestPrefixNetworkMatchPrefixLength'(Node, Ip, MaxLength ) /*fn*/:-
   agg(MaxLength = max(MatchLength),(
      'NetworkMatch'(Node, Ip, _, MatchLength))).

'MinAdmin'(Node, Network, MinAdmin ) /*fn*/:-
   agg(MinAdmin = min(Admin),(
      'BestPerProtocolRoute'(Route),
      'Route_admin'(Route, Admin) /*fn*/,
      'Route_network'(Route, Network) /*fn*/,
      'Route_node'(Route, Node) /*fn*/)).

'MinAdminRoute'(Route) :-
   'MinAdmin'(Node, Network, MinAdmin),
   'BestPerProtocolRoute'(Route),
   'Route_admin'(Route, MinAdmin) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/.

'MinCost'(Node, Network, MinCost ) /*fn*/:-
   agg(MinCost = min(Cost),(
      'MinAdminRoute'(Route),
      'Route_cost'(Route, Cost) /*fn*/,
      'Route_network'(Route, Network) /*fn*/,
      'Route_node'(Route, Node) /*fn*/)).

'MinCostRoute'(Route) :-
   'MinAdminRoute'(Route),
   'MinCost'(Node, Network, MinCost),
   'Route_cost'(Route, MinCost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_node'(Route, Node) /*fn*/.

'NetworkMatch'(Node, Ip, MatchNet, MatchLength) :-
   'Ip_address'(Ip:Address),
   'Network_address'(MatchNet, MatchNet_start) /*fn*/,
   'Network_prefix_length'(MatchNet, MatchLength) /*fn*/,
   'Network_end'(MatchNet, MatchNet_end) /*fn*/,
   'InstalledRoute'(Route),
   'Route_network'(Route, MatchNet) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   MatchNet_start =< Address,
   Address =< MatchNet_end.

'RouteDetails_admin'(Route, Admin ) /*fn*/:-
   'Route_admin'(Route, Admin) /*fn*/.
'RouteDetails_admin'(Route, Admin ) /*fn*/:-
   'Route'(Route),
   \+ ('Route_admin'(Route, _) /*fn*/),
   Admin = -1.

'RouteDetails_cost'(Route, Cost ) /*fn*/:-
   'Route_cost'(Route, Cost) /*fn*/.
'RouteDetails_cost'(Route, Cost ) /*fn*/:-
   'Route'(Route),
   \+ ('Route_cost'(Route, _) /*fn*/),
   Cost = -1.

'RouteDetails_nextHop'(Route, NextHop ) /*fn*/:-
   'Route'(Route),
   'RouteDetails_nextHopIp'(Route, NextHopIp) /*fn*/,
   (
      (
         'IpCount'(NextHopIp, IpCountOne) /*fn*/,
         'HasIp'(NextHop, NextHopIp),
         IpCountOne = 1
      ) ;
      (
         'IpCount'(NextHopIp, IpCount) /*fn*/,
         IpCount > 1,
         NextHop = '(ambiguous)'
      ) ;
      (
         NextHop = '(none)',
         \+ 'HasIp'(_, NextHopIp)
      )
   ).
'RouteDetails_nextHop'(Route, NextHop ) /*fn*/:-
   'Route'(Route),
   'Ip_NONE'(Ip),
   'RouteDetails_nextHopIp'(Route, Ip) /*fn*/,
   NextHop = '(none)'.

'RouteDetails_nextHopInt'(Route, NextHopInt ) /*fn*/:-
   'InterfaceRoute_nextHopInt'(Route, NextHopInt) /*fn*/.
'RouteDetails_nextHopInt'(Route, NextHopInt ) /*fn*/:-
   'Route'(Route),
   \+ ('InterfaceRoute_nextHopInt'(Route, _) /*fn*/),
   NextHopInt = 'dynamic'.

'RouteDetails_nextHopIp'(Route, NextHopIp ) /*fn*/:-
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/.
'RouteDetails_nextHopIp'(Route, NextHopIp ) /*fn*/:-
   'Route'(Route),
   \+ ('Route_nextHopIp'(Route, _) /*fn*/),
   'Ip_NONE'(NextHopIp).

'RouteDetails_tag'(Route, Tag ) /*fn*/:-
   'Route_tag'(Route, Tag) /*fn*/.
'RouteDetails_tag'(Route, Tag ) /*fn*/:-
   'Route'(Route),
   \+ ('Route_tag'(Route, _) /*fn*/),
   Tag = -1.
'RouteFilterDenyNetwork'(List, Network) :-
   'RouteFilterFirstMatch'(List, Network, Line) /*fn*/,
   \+ 'SetRouteFilterPermitLine'(List, Line).
'RouteFilterDenyNetwork'(List, Network) :-
   need_RouteFilterMatchNetwork(List, Network),
   \+ 'RouteFilterMatch'(List, Network, _).

'RouteFilterFirstMatch'(List, Network, MatchLine ) /*fn*/:-
   agg(MatchLine = min(Line),( 'RouteFilterMatch'(List, Network, Line))).

'RouteFilterMatch'(List, Network, Line) :-
   need_RouteFilterMatchNetwork(List, Network),
   'SetRouteFilterLine'(List, Line, Line_network_start, Line_network_end, Min_prefix, Max_prefix),
   'Network_constructor'(Network_start, _, Prefix_length, Network) /*fn*/,
   Network_start >= Line_network_start,
   Network_start =< Line_network_end,
   Prefix_length >= Min_prefix,
   Prefix_length =< Max_prefix.

'RouteFilterPermitNetwork'(List, Network) :-
   'RouteFilterFirstMatch'(List, Network, Line) /*fn*/,
   'SetRouteFilterPermitLine'(List, Line).
'SetStaticIntRoute'(Node, Network, NextHopIp, NextHopInt, Admin, Tag) :-
   'SetStaticIntRoute_flat'(Node, Network_start, Network_end, Prefix_length, NextHopIp, NextHopInt, Admin, Tag),
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

% static route with next hop interface
'StaticIntRoute'(Route),
   'Route'(Route),
   'InterfaceRoute_constructor'(Node, Network, NextHopInt, Protocol, Route) /*fn*/,
   'InterfaceRoute'(Route),
   'InterfaceRoute_nextHopInt'(Route, NextHopInt) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/,
   'Route_tag'(Route, Tag) /*fn*/
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
   'Network_constructor'(Network_start, Network_end, Prefix_length, Network) /*fn*/.

% static route with next hop ip
'StaticRoute'(Route),
   'Route'(Route),
   'Route_constructor'(Node, Network, NextHopIp, Protocol, Route) /*fn*/,
   'Route_admin'(Route, Admin) /*fn*/,
   'Route_cost'(Route, Cost) /*fn*/,
   'Route_network'(Route, Network) /*fn*/,
   'Route_nextHopIp'(Route, NextHopIp) /*fn*/,
   'Route_node'(Route, Node) /*fn*/,
   'Route_protocol'(Route, Protocol) /*fn*/,
   'Route_tag'(Route, Tag) /*fn*/
:-
   'LongestPrefixNetworkMatch'(Node, NextHopIp, MatchNet),
   Network \== MatchNet,
   'SetStaticRoute'(Node, Network, NextHopIp, Admin, Tag),
   Cost = 0,
   Protocol = 'static'.
'Ip'(SrcIp),
'Ip'(DstIp)
:-
   'SetFlowOriginate'(Node, SrcIp, DstIp, SrcPort, DstPort, Protocol, Tag).

/*owner accept*/
'FlowAccepted'(Flow, Node) :-
   'FlowReachPostIn'(Flow, Node),
   'FlowInboundInterface'(Flow, Node, _),
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
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
   'Flow_node'(Flow, Node) /*fn*/,
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
               'FlowReachNonInboundSrcZone'(Flow, Node, SrcZone),
               \+ 'SetCrossZoneFilter'(Node, SrcZone, DstZone, _)
            )
         )
      ) ;
      ( /*the packet is original*/
         'Flow_node'(Flow, Node) /*fn*/,
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
         'FlowReachNonInboundSrcZone'(Flow, Node, SrcZone),
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
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
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
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
   'IpReadyInt'(Node, Int, DstIp, _).

'FlowMatchRoute'(Flow, Route) :-
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
   'FlowReachPostIn'(Flow, Node),
   'LongestPrefixNetworkMatch'(Node, DstIp, Network),
   'InstalledRoute'(Route),
   'Route_node'(Route, Node) /*fn*/,
   'Route_network'(Route, Network) /*fn*/.

'FlowNoRoute'(Flow, Node) :-
   'FlowReachPreOut'(Flow, Node),
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
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
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
   'FlowReachPreOut'(Flow, Node),
   'FibDrop'(Node, DstIp),
   \+ 'FibNeighborUnreachable'(Node, DstIp, _).

'FlowOriginate'(Node, SrcIp, DstIp, SrcPort, DstPort, Protocol, Tag, Flow) /*fn*/,
'Flow'(Flow),
'Flow_dstIp'(Flow, DstIp) /*fn*/,
'Flow_dstPort'(Flow, DstPort) /*fn*/,
'Flow_ipProtocol'(Flow, Protocol) /*fn*/,
'Flow_node'(Flow, Node) /*fn*/,
'Flow_srcIp'(Flow, SrcIp) /*fn*/,
'Flow_srcPort'(Flow, SrcPort) /*fn*/,
'Flow_tag'(Flow, Tag) /*fn*/
:-
   'SetFlowOriginate'(Node, SrcIp, DstIp, SrcPort, DstPort, Protocol, Tag).
'FlowOriginate'(Node, SrcIp, DstIp, SrcPort, DstPort, Protocol, Tag, Flow) /*fn*/,
'Flow'(Flow),
'Flow_dstIp'(Flow, DstIp) /*fn*/,
'Flow_dstPort'(Flow, DstPort) /*fn*/,
'Flow_ipProtocol'(Flow, Protocol) /*fn*/,
'Flow_node'(Flow, Node) /*fn*/,
'Flow_srcIp'(Flow, SrcIp) /*fn*/,
'Flow_srcPort'(Flow, SrcPort) /*fn*/,
'Flow_tag'(Flow, Tag) /*fn*/
:-
   'DuplicateRoleFlows'(_),
   'SetFlowOriginate'(AcceptNode, SrcIp, DstIp, SrcPort, DstPort, Protocol, Tag),
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
   'Flow_node'(Flow, Node) /*fn*/.   
'FlowReachPostIn'(Flow, Node) :-
   'FlowReachPostHostInFilter'(Flow, Node, _).
'FlowReachPostIn'(Flow, Node) :-
   'FlowReachPostIncomingInterfaceAcl'(Flow, Node, _),
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
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
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
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
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
   'FibForward'(Node, DstIp, OutInt, NextHop, NextHopInt),
   'Flow_node'(Flow, Node) /*fn*/.

'FlowReachPreOutEdgePolicyRoute'(Flow, Node, ReceivedInt, Interface, NextHop, NextHopInt) :-
   'FlowReachPostInInterface'(Flow, Node, ReceivedInt),
   'SetInterfaceRoutingPolicy'(Node, ReceivedInt, Policy),
   'FlowReachPolicyRoute'(Flow, Node, Policy),
   'PolicyMapPermitFlow'(Policy, Clause, Flow),
   'SetPolicyMapClauseSetNextHopIp'(Policy, Clause, NextHopIp),
   'FibForwardPolicyRouteNextHopIp'(Node, NextHopIp, Interface, NextHop, NextHopInt).

'FlowReachPreOutEdgeStandard'(Flow, Node, ReceivedInt, OutInt, NextHop, NextHopInt) :-
   'FlowReachPreOut'(Flow, Node),
   'Flow_dstIp'(Flow, DstIp) /*fn*/,
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
   'FlowAccepted'(Flow, Node2),
   History = [OldHistory, ':accepted'].
'FlowPathAcceptedEdge'(Flow, I, Node1, Int1, Node2, Int2, History) :-
   \+ 'FlowPathIntermediateEdge'(Flow, _, _, _, _, Node1, _, _),
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
         'Flow_node'(Flow, Node1) /*fn*/,
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
         'Flow_node'(Flow, Node1) /*fn*/,
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
   'FlowReachPostInInterface'(Flow, Node2, Int2),
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
   'Flow_node'(AcceptedFlow, Node1) /*fn*/,
   'Flow_node'(MissingFlow, Node2) /*fn*/,
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
   'Flow_node'(Flow, SrcNode) /*fn*/,
   'SetNodeRole'(SrcNode, SrcRole),
   'SetNodeRole'(TransitNode, TransitRole),
   SrcRole \== TransitRole,
   'FlowReachPostOutInterface'(Flow, TransitNode, _).

'FlowSameHeaderRoleTransitNode'(Flow, SimilarFlow, TransitNode) :-
   Flow = SimilarFlow,
   'FlowRoleTransitNode'(Flow, _, TransitNode).
'FlowSameHeaderRoleTransitNode'(Flow, SimilarFlow, TransitNode) :-
   'FlowRoleTransitNode'(Flow, SrcRole, TransitNode),
   'Flow_node'(SimilarFlow, SrcNode) /*fn*/,
   'SetNodeRole'(SrcNode, SrcRole),
   'SetNodeRole'(TransitNode, TransitRole),
   SrcRole \== TransitRole,
   'FlowSameHeader'(Flow, SimilarFlow).

'FlowSameHeader'(Flow1, Flow2) :-
   'FlowOriginate'(Node1, SrcIp, DstIp, SrcPort, DstPort, Protocol, Tag, Flow1) /*fn*/,
   'FlowOriginate'(Node2, SrcIp, DstIp, SrcPort, DstPort, Protocol, Tag, Flow2) /*fn*/,
   Node1 \== Node2.
function_sig('Network_end', 2).
function_sig('BgpAdvertisement_nextHopIp', 2).
function_sig('BgpNeighborGeneratedRoute_constructor', 4).
function_sig('Flow_tag', 2).
function_sig('AdministrativeDistance', 3).
function_sig('FlowOriginate', 8).
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
function_sig('BgpAdvertisement_constructor', 10).
function_sig('MinContributingRouteAdmin', 2).
function_sig('BgpAdvertisement_srcIp', 2).
function_sig('RouteDetails_nextHop', 2).
