package org.batfish.bdp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.batfish.common.BatfishException;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfExternalRoute;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.OspfExternalType2Route;
import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class VirtualRouter extends ComparableStructure<String> {

   private static final int DEFAULT_CISCO_VLAN_OSPF_COST = 1;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   BgpRib _baseEbgpRib;

   BgpRib _baseIbgpRib;

   BgpRib _bgpRib;

   final Configuration _c;

   ConnectedRib _connectedRib;

   BgpRib _ebgpRib;

   BgpRib _ebgpStagingRib;

   Fib _fib;

   Rib _generatedRib;

   BgpRib _ibgpRib;

   BgpRib _ibgpStagingRib;

   Rib _independentRib;

   Rib _mainRib;

   private final Map<String, Node> _nodes;

   OspfExternalType1Rib _ospfExternalType1Rib;

   OspfExternalType1Rib _ospfExternalType1StagingRib;

   OspfExternalType2Rib _ospfExternalType2Rib;

   OspfExternalType2Rib _ospfExternalType2StagingRib;

   OspfInterAreaRib _ospfInterAreaRib;

   OspfInterAreaRib _ospfInterAreaStagingRib;

   OspfIntraAreaRib _ospfIntraAreaRib;

   OspfIntraAreaRib _ospfIntraAreaStagingRib;

   OspfRib _ospfRib;

   BgpRib _prevBgpRib;

   BgpRib _prevEbgpRib;

   BgpRib _prevIbgpRib;

   Rib _prevMainRib;

   OspfExternalType1Rib _prevOspfExternalType1Rib;

   OspfExternalType2Rib _prevOspfExternalType2Rib;

   StaticRib _staticInterfaceRib;

   StaticRib _staticRib;

   final Vrf _vrf;

   public VirtualRouter(String name, Configuration c, Map<String, Node> nodes) {
      super(name);
      _bgpRib = new BgpRib();
      _c = c;
      _connectedRib = new ConnectedRib();
      _ebgpRib = new BgpRib();
      _ebgpStagingRib = new BgpRib();
      _ibgpRib = new BgpRib();
      _ibgpStagingRib = new BgpRib();
      _independentRib = new Rib();
      _mainRib = new Rib();
      _nodes = nodes;
      _ospfExternalType1Rib = new OspfExternalType1Rib();
      _ospfExternalType2Rib = new OspfExternalType2Rib();
      _ospfExternalType1StagingRib = new OspfExternalType1Rib();
      _ospfExternalType2StagingRib = new OspfExternalType2Rib();
      _ospfInterAreaRib = new OspfInterAreaRib();
      _ospfInterAreaStagingRib = new OspfInterAreaRib();
      _ospfIntraAreaRib = new OspfIntraAreaRib();
      _ospfIntraAreaStagingRib = new OspfIntraAreaRib();
      _ospfRib = new OspfRib();
      _staticRib = new StaticRib();
      _staticInterfaceRib = new StaticRib();
      _vrf = c.getVrfs().get(name);
   }

   public boolean activateGeneratedRoutes() {
      boolean changed = false;
      for (GeneratedRoute gr : _vrf.getGeneratedRoutes()) {
         boolean active = true;
         String generationPolicyName = gr.getGenerationPolicy();
         GeneratedRoute.Builder grb = new GeneratedRoute.Builder();
         grb.setNetwork(gr.getNetwork());
         grb.setAdmin(gr.getAdministrativeCost());
         grb.setMetric(gr.getMetric() != null ? gr.getMetric() : 0);
         grb.setAsPath(new AsPath());
         grb.setAttributePolicy(gr.getAttributePolicy());
         grb.setGenerationPolicy(gr.getGenerationPolicy());
         boolean discard = gr.getDiscard();
         grb.setDiscard(discard);
         if (discard) {
            grb.setNextHopInterface(Interface.NULL_INTERFACE_NAME);
         }
         if (generationPolicyName != null) {
            RoutingPolicy generationPolicy = _c.getRoutingPolicies()
                  .get(generationPolicyName);
            if (generationPolicy != null) {
               active = false;
               for (AbstractRoute contributingRoute : _prevMainRib
                     .getRoutes()) {
                  if (generationPolicy.process(contributingRoute, null, grb,
                        null, _key)) {
                     if (!discard) {
                        grb.setNextHopIp(contributingRoute.getNextHopIp());
                     }
                     active = true;
                     break;
                  }
               }
            }
         }
         if (active) {
            GeneratedRoute newGr = grb.build();
            if (_generatedRib.mergeRoute(newGr)) {
               changed = true;
            }
         }
      }
      return changed;
   }

   public boolean activateStaticRoutes() {
      boolean changed = false;
      for (StaticRoute sr : _staticRib.getRoutes()) {
         Set<AbstractRoute> matchingRoutes = _prevMainRib
               .longestPrefixMatch(sr.getNextHopIp());
         Prefix prefix = sr.getNetwork();
         for (AbstractRoute matchingRoute : matchingRoutes) {
            Prefix matchingRoutePrefix = matchingRoute.getNetwork();
            // check to make sure matching route's prefix does not totally
            // contain this static route's prefix
            if (matchingRoutePrefix.getAddress().asLong() > prefix.getAddress()
                  .asLong()
                  || matchingRoutePrefix.getEndAddress().asLong() < prefix
                        .getEndAddress().asLong()) {
               if (_mainRib.mergeRoute(sr)) {
                  changed = true;
               }
               break;
            }
         }
      }
      return changed;
   }

   public void computeFib() {
      _fib = new Fib(_mainRib);
   }

   public <U extends AbstractRoute, T extends U> void importRib(
         AbstractRib<U> importingRib, AbstractRib<T> exportingRib) {
      for (T route : exportingRib.getRoutes()) {
         importingRib.mergeRoute(route);
      }
   }

   public void initBaseBgpRibs(AdvertisementSet externalAdverts) {
      _bgpRib = new BgpRib();
      _baseEbgpRib = new BgpRib();
      _baseIbgpRib = new BgpRib();

      if (_vrf.getBgpProcess() != null) {
         int ebgpAdmin = RoutingProtocol.BGP
               .getDefaultAdministrativeCost(_c.getConfigurationFormat());
         int ibgpAdmin = RoutingProtocol.IBGP
               .getDefaultAdministrativeCost(_c.getConfigurationFormat());

         for (BgpAdvertisement advert : externalAdverts) {
            if (advert.getDstNode().equals(_c.getHostname())) {
               Ip srcIp = advert.getSrcIp();
               // TODO: support passive bgp connections
               Prefix srcPrefix = new Prefix(srcIp, Prefix.MAX_PREFIX_LENGTH);
               BgpNeighbor neighbor = _vrf.getBgpProcess().getNeighbors()
                     .get(srcPrefix);
               if (neighbor != null) {
                  BgpAdvertisementType type = advert.getType();
                  BgpRoute.Builder outgoingRouteBuilder = new BgpRoute.Builder();
                  boolean ebgp;
                  if (type == BgpAdvertisementType.EBGP_SENT) {
                     ebgp = true;
                  }
                  else if (type == BgpAdvertisementType.IBGP_SENT) {
                     ebgp = false;
                  }
                  else {
                     throw new BatfishException(
                           "Missing or invalid bgp advertisement type");
                  }
                  BgpRib targetRib = ebgp ? _baseEbgpRib : _baseIbgpRib;
                  int localPreference;
                  if (ebgp) {
                     localPreference = BgpRoute.DEFAULT_LOCAL_PREFERENCE;
                  }
                  else {
                     localPreference = advert.getLocalPreference();
                  }
                  RoutingProtocol targetProtocol = ebgp ? RoutingProtocol.BGP
                        : RoutingProtocol.IBGP;
                  outgoingRouteBuilder.setAsPath(advert.getAsPath());
                  outgoingRouteBuilder.setCommunities(advert.getCommunities());
                  outgoingRouteBuilder.setLocalPreference(localPreference);
                  outgoingRouteBuilder.setMetric(advert.getMed());
                  outgoingRouteBuilder.setNetwork(advert.getNetwork());
                  outgoingRouteBuilder.setNextHopIp(advert.getNextHopIp());
                  outgoingRouteBuilder
                        .setOriginatorIp(advert.getOriginatorIp());
                  outgoingRouteBuilder.setOriginType(advert.getOriginType());
                  outgoingRouteBuilder.setProtocol(targetProtocol);
                  // TODO:
                  // outgoingRouteBuilder.setReceivedFromRouteReflectorClient(...);
                  outgoingRouteBuilder.setSrcProtocol(advert.getSrcProtocol());
                  BgpRoute transformedOutgoingRoute = outgoingRouteBuilder
                        .build();
                  BgpRoute.Builder transformedIncomingRouteBuilder = new BgpRoute.Builder();

                  // Incoming originatorIp
                  transformedIncomingRouteBuilder.setOriginatorIp(
                        transformedOutgoingRoute.getOriginatorIp());

                  // Incoming clusterList
                  transformedIncomingRouteBuilder.getClusterList()
                        .addAll(transformedOutgoingRoute.getClusterList());

                  // Incoming receivedFromRouteReflectorClient
                  transformedIncomingRouteBuilder
                        .setReceivedFromRouteReflectorClient(
                              transformedOutgoingRoute
                                    .getReceivedFromRouteReflectorClient());

                  // Incoming asPath
                  transformedIncomingRouteBuilder.getAsPath()
                        .addAll(transformedOutgoingRoute.getAsPath());

                  // Incoming communities
                  transformedIncomingRouteBuilder.getCommunities()
                        .addAll(transformedOutgoingRoute.getCommunities());

                  // Incoming protocol
                  transformedIncomingRouteBuilder.setProtocol(targetProtocol);

                  // Incoming network
                  transformedIncomingRouteBuilder
                        .setNetwork(transformedOutgoingRoute.getNetwork());

                  // Incoming nextHopIp
                  transformedIncomingRouteBuilder
                        .setNextHopIp(transformedOutgoingRoute.getNextHopIp());

                  // Incoming localPreference
                  transformedIncomingRouteBuilder.setLocalPreference(
                        transformedOutgoingRoute.getLocalPreference());

                  // Incoming admin
                  int admin = ebgp ? ebgpAdmin : ibgpAdmin;
                  transformedIncomingRouteBuilder.setAdmin(admin);

                  // Incoming metric
                  transformedIncomingRouteBuilder
                        .setMetric(transformedOutgoingRoute.getMetric());

                  // Incoming srcProtocol
                  transformedIncomingRouteBuilder
                        .setSrcProtocol(targetProtocol);
                  String importPolicyName = neighbor.getImportPolicy();
                  // TODO: ensure there is always an import policy

                  if (ebgp
                        && transformedOutgoingRoute.getAsPath()
                              .containsAs(neighbor.getLocalAs())
                        && !neighbor.getAllowLocalAsIn()) {
                     // skip routes containing peer's AS unless
                     // disable-peer-as-check (getAllowRemoteAsOut) is set
                     continue;
                  }

                  /*
                   * CREATE INCOMING ROUTE
                   */
                  boolean allowed = true;
                  if (importPolicyName != null) {
                     RoutingPolicy importPolicy = _c.getRoutingPolicies()
                           .get(importPolicyName);
                     if (importPolicy != null) {
                        allowed = importPolicy.process(transformedOutgoingRoute,
                              null, transformedIncomingRouteBuilder,
                              advert.getSrcIp(), _key);
                     }
                  }
                  if (allowed) {
                     BgpRoute transformedIncomingRoute = transformedIncomingRouteBuilder
                           .build();
                     targetRib.mergeRoute(transformedIncomingRoute);
                  }
               }
            }
         }
      }

      _ebgpRib = new BgpRib();
      importRib(_ebgpRib, _baseEbgpRib);
      _ibgpRib = new BgpRib();
      importRib(_ibgpRib, _baseIbgpRib);
   }

   public void initBaseOspfRoutes() {
      if (_vrf.getOspfProcess() != null) {
         // init intra-area routes from connected routes
         _vrf.getOspfProcess().getAreas().forEach((areaNum, area) -> {
            for (Interface iface : area.getInterfaces()) {
               if (iface.getActive()) {
                  Set<Prefix> allNetworkPrefixes = iface.getAllPrefixes()
                        .stream().map(prefix -> prefix.getNetworkPrefix())
                        .collect(Collectors.toSet());
                  int cost = iface.getOspfCost();
                  for (Prefix prefix : allNetworkPrefixes) {
                     OspfIntraAreaRoute route = new OspfIntraAreaRoute(prefix,
                           null,
                           RoutingProtocol.OSPF.getDefaultAdministrativeCost(
                                 _c.getConfigurationFormat()),
                           cost, areaNum);
                     _ospfIntraAreaRib.addRoute(route);
                  }
               }
            }
         });
      }
   }

   public void initBgpAggregateRoutes() {
      // first import aggregates
      switch (_c.getConfigurationFormat()) {
      case JUNIPER:
      case JUNIPER_SWITCH:
         return;
      // $CASES-OMITTED$
      default:
         break;
      }
      for (AbstractRoute grAbstract : _generatedRib.getRoutes()) {
         GeneratedRoute gr = (GeneratedRoute) grAbstract;
         BgpRoute.Builder b = new BgpRoute.Builder();
         b.setAdmin(gr.getAdministrativeCost());
         b.setAsPath(gr.getAsPath());
         b.setMetric(gr.getMetric());
         b.setSrcProtocol(RoutingProtocol.AGGREGATE);
         b.setProtocol(RoutingProtocol.AGGREGATE);
         b.setNetwork(gr.getNetwork());
         b.setLocalPreference(BgpRoute.DEFAULT_LOCAL_PREFERENCE);
         BgpRoute br = b.build();
         br.setNonRouting(true);
         _bgpRib.mergeRoute(br);
      }

   }

   public void initConnectedRib() {
      for (Interface i : _vrf.getInterfaces().values()) {
         if (i.getActive()) {
            for (Prefix prefix : i.getAllPrefixes()) {
               ConnectedRoute cr = new ConnectedRoute(prefix.getNetworkPrefix(),
                     i.getName());
               _connectedRib.addRoute(cr);
            }
         }
      }
   }

   public void initEbgpTopology(BdpDataPlane dp) {
      if (_vrf.getBgpProcess() != null) {
         for (BgpNeighbor neighbor : _vrf.getBgpProcess().getNeighbors()
               .values()) {
            if (!neighbor.getRemoteAs().equals(neighbor.getLocalAs())) {
               BgpNeighbor remoteNeighbor = neighbor.getRemoteBgpNeighbor();
               if (remoteNeighbor != null) {
                  if (dp.getIpOwners().get(neighbor.getLocalIp())
                        .contains(_c.getHostname())
                        && dp.getIpOwners().get(remoteNeighbor.getLocalIp())
                              .contains(
                                    remoteNeighbor.getOwner().getHostname())) {
                     // pretty confident we have a bgp connection here
                     // for now, just leave it alone
                  }
                  else {
                     neighbor.setRemoteBgpNeighbor(null);
                     remoteNeighbor.setRemoteBgpNeighbor(null);
                  }
               }
            }
         }
      }
   }

   public void initIbgpTopology(BdpDataPlane dp) {
      // TODO: implement
   }

   public void initOspfExports() {
      if (_vrf.getOspfProcess() != null) {
         // init ospf exports
         String exportPolicyName = _vrf.getOspfProcess().getExportPolicy();
         if (exportPolicyName != null) {
            RoutingPolicy exportPolicy = _c.getRoutingPolicies()
                  .get(exportPolicyName);
            if (exportPolicy != null) {
               for (AbstractRoute potentialExport : _prevMainRib.getRoutes()) {
                  OspfExternalRoute.Builder outputRouteBuilder = new OspfExternalRoute.Builder();
                  if (exportPolicy.process(potentialExport, null,
                        outputRouteBuilder, null, _key)) {
                     outputRouteBuilder.setAdmin(outputRouteBuilder
                           .getOspfMetricType().toRoutingProtocol()
                           .getDefaultAdministrativeCost(
                                 _c.getConfigurationFormat()));
                     outputRouteBuilder
                           .setNetwork(potentialExport.getNetwork());
                     outputRouteBuilder.setCostToAdvertiser(0);
                     OspfExternalRoute outputRoute = outputRouteBuilder.build();
                     // shouldn't be null
                     if (outputRoute.getOspfMetricType() == OspfMetricType.E1) {
                        _ospfExternalType1Rib
                              .addRoute((OspfExternalType1Route) outputRoute);
                     }
                     else {
                        _ospfExternalType2Rib
                              .addRoute((OspfExternalType2Route) outputRoute);
                     }
                  }
               }
            }
         }
      }
   }

   public void initOspfInterfaceCosts() {
      if (_vrf.getOspfProcess() != null) {
         _vrf.getInterfaces().forEach((interfaceName, i) -> {
            if (i.getActive()) {
               Integer ospfCost = i.getOspfCost();
               if (ospfCost == null) {
                  if (interfaceName.startsWith("Vlan")) {
                     // TODO: fix for non-cisco
                     ospfCost = DEFAULT_CISCO_VLAN_OSPF_COST;
                  }
                  else {
                     if (i.getBandwidth() != null) {
                        ospfCost = Math.max((int) (_vrf.getOspfProcess()
                              .getReferenceBandwidth() / i.getBandwidth()), 1);
                     }
                     else {
                        throw new BatfishException(
                              "Expected non-null interface bandwidth for \""
                                    + _c.getHostname() + "\":\"" + interfaceName
                                    + "\"");
                     }
                  }
               }
               i.setOspfCost(ospfCost);
            }
         });
      }
   }

   public void initStaticRib() {
      for (StaticRoute sr : _vrf.getStaticRoutes()) {
         String nextHopInt = sr.getNextHopInterface();
         if (nextHopInt != null
               && !Interface.NULL_INTERFACE_NAME.equals(nextHopInt)
               && !_vrf.getInterfaces().get(nextHopInt).getActive()) {
            continue;
         }
         // interface route
         if (sr.getNextHopIp() == null) {
            _staticInterfaceRib.addRoute(sr);
         }
         else {
            _staticRib.addRoute(sr);
         }
      }
   }

   public boolean propagateBgpRoutes(Map<String, Node> nodes,
         Topology topology) {
      boolean changed = false;
      if (_vrf.getBgpProcess() != null) {
         int ebgpAdmin = RoutingProtocol.BGP
               .getDefaultAdministrativeCost(_c.getConfigurationFormat());
         int ibgpAdmin = RoutingProtocol.IBGP
               .getDefaultAdministrativeCost(_c.getConfigurationFormat());

         for (BgpNeighbor neighbor : _vrf.getBgpProcess().getNeighbors()
               .values()) {
            BgpNeighbor remoteBgpNeighbor = neighbor.getRemoteBgpNeighbor();
            if (remoteBgpNeighbor != null) {
               int localAs = neighbor.getLocalAs();
               int remoteAs = neighbor.getRemoteAs();
               Configuration remoteConfig = remoteBgpNeighbor.getOwner();
               String remoteHostname = remoteConfig.getHostname();
               String remoteVrfName = remoteBgpNeighbor.getVrf();
               Vrf remoteVrf = remoteConfig.getVrfs().get(remoteVrfName);
               VirtualRouter remoteVirtualRouter = _nodes
                     .get(remoteHostname)._virtualRouters.get(remoteVrfName);
               RoutingPolicy remoteExportPolicy = remoteConfig
                     .getRoutingPolicies()
                     .get(remoteBgpNeighbor.getExportPolicy());
               boolean ebgp = localAs != remoteAs;
               BgpRib targetRib = ebgp ? _ebgpStagingRib : _ibgpStagingRib;
               RoutingProtocol targetProtocol = ebgp ? RoutingProtocol.BGP
                     : RoutingProtocol.IBGP;
               List<AbstractRoute> remoteCandidateRoutes = new ArrayList<>();
               remoteCandidateRoutes
                     .addAll(remoteVirtualRouter._prevMainRib.getRoutes());

               // bgp advertise-external
               if (!ebgp && remoteBgpNeighbor.getAdvertiseExternal()) {
                  remoteCandidateRoutes
                        .addAll(remoteVirtualRouter._prevEbgpRib.getRoutes());
               }

               // bgp advertise-inactive
               if (remoteBgpNeighbor.getAdvertiseInactive()) {
                  remoteCandidateRoutes
                        .addAll(remoteVirtualRouter._prevBgpRib.getRoutes());
               }

               for (AbstractRoute remoteRoute : remoteCandidateRoutes) {
                  BgpRoute.Builder transformedOutgoingRouteBuilder = new BgpRoute.Builder();

                  RoutingProtocol remoteRouteProtocol = remoteRoute
                        .getProtocol();
                  boolean remoteRouteIsBgp = remoteRouteProtocol == RoutingProtocol.IBGP
                        || remoteRouteProtocol == RoutingProtocol.BGP;

                  // originatorIp, clusterList, receivedFromRouteReflectorClient
                  if (remoteRouteIsBgp) {
                     BgpRoute bgpRemoteRoute = (BgpRoute) remoteRoute;
                     if (ebgp
                           && bgpRemoteRoute.getAsPath()
                                 .containsAs(remoteBgpNeighbor.getRemoteAs())
                           && !remoteBgpNeighbor.getAllowRemoteAsOut()) {
                        // skip routes containing peer's AS unless
                        // disable-peer-as-check (getAllowRemoteAsOut) is set
                        continue;
                     }
                     /*
                      * route reflection: reflect everything received from
                      * clients to clients and non-clients. reflect everything
                      * received from non-clients to clients. Do not reflect to
                      * originator
                      */

                     Ip remoteOriginatorIp = bgpRemoteRoute.getOriginatorIp();
                     // don't accept routes whose originator ip is my BGP id
                     if (remoteOriginatorIp != null && _vrf.getBgpProcess()
                           .getRouterId().equals(remoteOriginatorIp)) {
                        continue;
                     }
                     if (remoteRouteProtocol.equals(RoutingProtocol.IBGP)
                           && !ebgp) {
                        Ip originatorIp;
                        if (remoteOriginatorIp != null) {
                           originatorIp = remoteOriginatorIp;
                        }
                        else {
                           originatorIp = remoteVrf.getBgpProcess()
                                 .getRouterId();
                        }
                        transformedOutgoingRouteBuilder
                              .setOriginatorIp(originatorIp);
                        boolean remoteRouteReceivedFromRouteReflectorClient = bgpRemoteRoute
                              .getReceivedFromRouteReflectorClient();
                        boolean sendingToRouteReflectorClient = remoteBgpNeighbor
                              .getRouteReflectorClient();
                        boolean newRouteReceivedFromRouteReflectorClient = neighbor
                              .getRouteReflectorClient();
                        transformedOutgoingRouteBuilder
                              .setReceivedFromRouteReflectorClient(
                                    newRouteReceivedFromRouteReflectorClient);
                        transformedOutgoingRouteBuilder.getClusterList()
                              .addAll(bgpRemoteRoute.getClusterList());
                        if (!remoteRouteReceivedFromRouteReflectorClient
                              && !sendingToRouteReflectorClient) {
                           continue;
                        }
                        if (sendingToRouteReflectorClient) {
                           // sender adds its local cluster id to clusterlist of
                           // new route
                           transformedOutgoingRouteBuilder.getClusterList()
                                 .add(remoteBgpNeighbor.getClusterId());
                        }
                        if (transformedOutgoingRouteBuilder.getClusterList()
                              .contains(neighbor.getClusterId())) {
                           // receiver will reject new route if it contains its
                           // local cluster id
                           continue;
                        }
                     }
                  }

                  // Outgoing asPath
                  // Outgoing communities
                  if (remoteRouteIsBgp) {
                     BgpRoute bgpRemoteRoute = (BgpRoute) remoteRoute;
                     transformedOutgoingRouteBuilder.getAsPath()
                           .addAll(bgpRemoteRoute.getAsPath());
                     if (remoteBgpNeighbor.getSendCommunity()) {
                        transformedOutgoingRouteBuilder.getCommunities()
                              .addAll(bgpRemoteRoute.getCommunities());
                     }
                  }
                  if (ebgp) {
                     AsSet newAsPathElement = new AsSet();
                     newAsPathElement.add(remoteAs);
                     transformedOutgoingRouteBuilder.getAsPath().add(0,
                           newAsPathElement);
                  }

                  // Outgoing protocol
                  transformedOutgoingRouteBuilder.setProtocol(targetProtocol);
                  transformedOutgoingRouteBuilder
                        .setNetwork(remoteRoute.getNetwork());

                  // Outgoing metric
                  if (remoteRouteIsBgp) {
                     transformedOutgoingRouteBuilder
                           .setMetric(remoteRoute.getMetric());
                  }

                  // Outgoing nextHopIp
                  // Outgoing localPreference
                  Ip nextHopIp;
                  int localPreference;
                  if (ebgp || !remoteRouteIsBgp) {
                     nextHopIp = remoteBgpNeighbor.getLocalIp();
                     localPreference = BgpRoute.DEFAULT_LOCAL_PREFERENCE;
                  }
                  else {
                     nextHopIp = remoteRoute.getNextHopIp();
                     BgpRoute remoteIbgpRoute = (BgpRoute) remoteRoute;
                     localPreference = remoteIbgpRoute.getLocalPreference();
                  }
                  if (nextHopIp == null) {
                     // should only happen for ibgp
                     String nextHopInterface = remoteRoute
                           .getNextHopInterface();
                     Prefix nextHopPrefix = remoteVrf.getInterfaces()
                           .get(nextHopInterface).getPrefix();
                     if (nextHopPrefix == null) {
                        throw new BatfishException(
                              "remote route's nextHopInterface has no address");
                     }
                     nextHopIp = nextHopPrefix.getAddress();
                  }
                  transformedOutgoingRouteBuilder.setNextHopIp(nextHopIp);
                  transformedOutgoingRouteBuilder
                        .setLocalPreference(localPreference);

                  // Outgoing srcProtocol
                  transformedOutgoingRouteBuilder
                        .setSrcProtocol(remoteRoute.getProtocol());

                  /*
                   * CREATE OUTGOING ROUTE
                   */
                  Ip remoteLocalIp = remoteBgpNeighbor.getLocalIp();
                  if (remoteExportPolicy.process(remoteRoute, null,
                        transformedOutgoingRouteBuilder, remoteLocalIp,
                        remoteVrfName)) {
                     BgpRoute transformedOutgoingRoute = transformedOutgoingRouteBuilder
                           .build();
                     BgpRoute.Builder transformedIncomingRouteBuilder = new BgpRoute.Builder();

                     // Incoming originatorIp
                     transformedIncomingRouteBuilder.setOriginatorIp(
                           transformedOutgoingRoute.getOriginatorIp());

                     // Incoming clusterList
                     transformedIncomingRouteBuilder.getClusterList()
                           .addAll(transformedOutgoingRoute.getClusterList());

                     // Incoming receivedFromRouteReflectorClient
                     transformedIncomingRouteBuilder
                           .setReceivedFromRouteReflectorClient(
                                 transformedOutgoingRoute
                                       .getReceivedFromRouteReflectorClient());

                     // Incoming asPath
                     transformedIncomingRouteBuilder.getAsPath()
                           .addAll(transformedOutgoingRoute.getAsPath());

                     // Incoming communities
                     transformedIncomingRouteBuilder.getCommunities()
                           .addAll(transformedOutgoingRoute.getCommunities());

                     // Incoming protocol
                     transformedIncomingRouteBuilder
                           .setProtocol(targetProtocol);

                     // Incoming network
                     transformedIncomingRouteBuilder
                           .setNetwork(remoteRoute.getNetwork());

                     // Incoming nextHopIp
                     transformedIncomingRouteBuilder.setNextHopIp(nextHopIp);

                     // Incoming localPreference
                     transformedIncomingRouteBuilder.setLocalPreference(
                           transformedOutgoingRoute.getLocalPreference());

                     // Incoming admin
                     int admin = ebgp ? ebgpAdmin : ibgpAdmin;
                     transformedIncomingRouteBuilder.setAdmin(admin);

                     // Incoming metric
                     transformedIncomingRouteBuilder
                           .setMetric(transformedOutgoingRoute.getMetric());

                     // Incoming srcProtocol
                     transformedIncomingRouteBuilder
                           .setSrcProtocol(targetProtocol);
                     String importPolicyName = neighbor.getImportPolicy();
                     // TODO: ensure there is always an import policy

                     if (ebgp
                           && transformedOutgoingRoute.getAsPath()
                                 .containsAs(neighbor.getLocalAs())
                           && !neighbor.getAllowLocalAsIn()) {
                        // skip routes containing peer's AS unless
                        // disable-peer-as-check (getAllowRemoteAsOut) is set
                        continue;
                     }

                     /*
                      * CREATE INCOMING ROUTE
                      */
                     boolean allowed = true;
                     if (importPolicyName != null) {
                        RoutingPolicy importPolicy = _c.getRoutingPolicies()
                              .get(importPolicyName);
                        if (importPolicy != null) {
                           allowed = importPolicy.process(
                                 transformedOutgoingRoute, null,
                                 transformedIncomingRouteBuilder,
                                 remoteBgpNeighbor.getLocalIp(), _key);
                        }
                     }
                     if (allowed) {
                        BgpRoute transformedIncomingRoute = transformedIncomingRouteBuilder
                              .build();
                        if (targetRib.mergeRoute(transformedIncomingRoute)) {
                           changed = true;
                        }
                     }
                  }
               }
            }
         }
      }
      return changed;
   }

   public boolean propagateOspfExternalRoutes(Map<String, Node> nodes,
         Topology topology) {
      boolean changed = false;
      String node = _c.getHostname();
      if (_vrf.getOspfProcess() != null) {
         int admin = RoutingProtocol.OSPF
               .getDefaultAdministrativeCost(_c.getConfigurationFormat());
         EdgeSet edges = topology.getNodeEdges().get(node);
         if (edges == null) {
            // there are no edges, so OSPF won't produce anything
            return false;
         }
         for (Edge edge : edges) {
            if (!edge.getNode1().equals(node)) {
               continue;
            }
            String connectingInterfaceName = edge.getInt1();
            Interface connectingInterface = _vrf.getInterfaces()
                  .get(connectingInterfaceName);
            String neighborName = edge.getNode2();
            Node neighbor = nodes.get(neighborName);
            String neighborInterfaceName = edge.getInt2();
            OspfArea area = _vrf.getInterfaces().get(connectingInterfaceName)
                  .getOspfArea();
            Configuration nc = neighbor._c;
            Interface neighborInterface = nc.getInterfaces()
                  .get(neighborInterfaceName);
            String neighborVrfName = neighborInterface.getVrfName();
            VirtualRouter neighborVirtualRouter = _nodes
                  .get(neighborName)._virtualRouters.get(neighborVrfName);

            OspfArea neighborArea = neighborInterface.getOspfArea();
            if (connectingInterface.getOspfEnabled()
                  && !connectingInterface.getOspfPassive()
                  && neighborInterface.getOspfEnabled()
                  && !neighborInterface.getOspfPassive() && area != null
                  && neighborArea != null) {
               /*
                * We have an ospf neighbor relationship on this edge. So we
                * should add all ospf external type 1(2) routes from this
                * neighbor into our ospf external type 1(2) staging rib. For
                * type 1, the cost of the route increases each time. For type 2,
                * the cost remains constant, but we must keep track of cost to
                * advertiser as a tie-breaker.
                */
               int connectingInterfaceCost = connectingInterface.getOspfCost();
               for (OspfExternalType1Route neighborRoute : neighborVirtualRouter._prevOspfExternalType1Rib
                     .getRoutes()) {
                  int newMetric = neighborRoute.getMetric()
                        + connectingInterfaceCost;
                  OspfExternalType1Route newRoute = new OspfExternalType1Route(
                        neighborRoute.getNetwork(),
                        neighborInterface.getPrefix().getAddress(), admin,
                        newMetric);
                  if (_ospfExternalType1StagingRib.mergeRoute(newRoute)) {
                     changed = true;
                  }
               }
               for (OspfExternalType2Route neighborRoute : neighborVirtualRouter._prevOspfExternalType2Rib
                     .getRoutes()) {
                  int newCostToAdvertiser = neighborRoute.getCostToAdvertiser()
                        + connectingInterfaceCost;
                  OspfExternalType2Route newRoute = new OspfExternalType2Route(
                        neighborRoute.getNetwork(),
                        neighborInterface.getPrefix().getAddress(), admin,
                        neighborRoute.getMetric(), newCostToAdvertiser);
                  if (_ospfExternalType2StagingRib.mergeRoute(newRoute)) {
                     changed = true;
                  }
               }
            }
         }
      }
      return changed;
   }

   public boolean propagateOspfInternalRoutes(Map<String, Node> nodes,
         Topology topology) {
      boolean changed = false;
      String node = _c.getHostname();
      if (_vrf.getOspfProcess() != null) {
         int admin = RoutingProtocol.OSPF
               .getDefaultAdministrativeCost(_c.getConfigurationFormat());
         EdgeSet edges = topology.getNodeEdges().get(node);
         if (edges == null) {
            // there are no edges, so OSPF won't produce anything
            return false;
         }
         for (Edge edge : edges) {
            if (!edge.getNode1().equals(node)) {
               continue;
            }
            String connectingInterfaceName = edge.getInt1();
            Interface connectingInterface = _vrf.getInterfaces()
                  .get(connectingInterfaceName);
            String neighborName = edge.getNode2();
            Node neighbor = nodes.get(neighborName);
            String neighborInterfaceName = edge.getInt2();
            OspfArea area = _vrf.getInterfaces().get(connectingInterfaceName)
                  .getOspfArea();
            Configuration nc = neighbor._c;
            Interface neighborInterface = nc.getInterfaces()
                  .get(neighborInterfaceName);
            String neighborVrfName = neighborInterface.getVrfName();
            VirtualRouter neighborVirtualRouter = _nodes
                  .get(neighborName)._virtualRouters.get(neighborVrfName);
            OspfArea neighborArea = neighborInterface.getOspfArea();
            if (connectingInterface.getOspfEnabled()
                  && !connectingInterface.getOspfPassive()
                  && neighborInterface.getOspfEnabled()
                  && !neighborInterface.getOspfPassive() && area != null
                  && neighborArea != null) {

               if (area.getName().equals(neighborArea.getName())) {
                  /*
                   * We have an ospf intra-area neighbor relationship on this
                   * edge. So we should add all ospf routes from this neighbor
                   * into our ospf intra-area staging rib, adding the cost of
                   * the connecting interface, and using the neighborInterface's
                   * address as the next hop ip
                   */
                  int connectingInterfaceCost = connectingInterface
                        .getOspfCost();
                  long areaNum = area.getName();
                  for (OspfIntraAreaRoute neighborRoute : neighborVirtualRouter._ospfIntraAreaRib
                        .getRoutes()) {
                     int newCost = neighborRoute.getMetric()
                           + connectingInterfaceCost;
                     OspfIntraAreaRoute newRoute = new OspfIntraAreaRoute(
                           neighborRoute.getNetwork(),
                           neighborInterface.getPrefix().getAddress(), admin,
                           newCost, areaNum);
                     if (_ospfIntraAreaStagingRib.mergeRoute(newRoute)) {
                        changed = true;
                     }
                  }
               }
               else {
                  /*
                   * We have an ospf inter-area neighbor relationship on this
                   * edge. So we should add all ospf routes from this neighbor
                   * into our ospf inter-area staging rib, adding the cost of
                   * the connecting interface, and using the neighborInterface's
                   * address as the next hop ip
                   *
                   * TODO: implement correct limitations on what gets passed
                   * between areas
                   *
                   * TODO: implement, period
                   */
               }
            }
         }
      }
      return changed;
   }

   public void unstageBgpRoutes() {
      for (BgpRoute route : _ebgpStagingRib.getRoutes()) {
         _ebgpRib.mergeRoute(route);
      }
      for (BgpRoute route : _ibgpStagingRib.getRoutes()) {
         _ibgpRib.mergeRoute(route);
      }
   }

   public void unstageOspfExternalRoutes() {
      for (OspfExternalType1Route route : _ospfExternalType1StagingRib
            .getRoutes()) {
         _ospfExternalType1Rib.mergeRoute(route);
      }
      for (OspfExternalType2Route route : _ospfExternalType2StagingRib
            .getRoutes()) {
         _ospfExternalType2Rib.mergeRoute(route);
      }
   }

   public void unstageOspfInternalRoutes() {
      for (OspfIntraAreaRoute route : _ospfIntraAreaStagingRib.getRoutes()) {
         _ospfIntraAreaRib.mergeRoute(route);
      }
      for (OspfInterAreaRoute route : _ospfInterAreaStagingRib.getRoutes()) {
         _ospfInterAreaRib.mergeRoute(route);
      }
   }

}
