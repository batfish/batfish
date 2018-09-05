package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.utils.PrefixUtils;
import org.batfish.symbolic.utils.Tuple;

/**
 * Creates an abstraction(s) of the network by splitting the network into a collection of
 * equivalence classes and compressing the representation of each equivalence class. Each
 * equivalence class has the property that all of its stable solutions are bisimilar to the original
 * network. That is, there is a bug in the abstracted network iff there is a bug in the concrete
 * network.
 *
 * <p>How the compression occurs does depend on the property we want to check, since we can only
 * check properties for all concrete nodes that map to the same abstract node. For example, if we
 * want to check reachability between two concrete nodes, then these 2 nodes must remain distinct in
 * the compressed form.
 */

// - iBGP check source ACL?
// - add parent / client RRs?
// - Always assume multipath?

public class DestinationClasses {

  private IBatfish _batfish;

  private Graph _graph;

  private HeaderSpace _headerspace;

  private boolean _useDefaultCase;

  private Map<Set<String>, Tuple<HeaderSpace, Tuple<List<Prefix>, Boolean>>> _headerspaceMap;

  private DestinationClasses(
      IBatfish batfish, Graph graph, @Nullable HeaderSpace h, boolean defaultCase) {
    _batfish = batfish;
    _graph = graph;
    _headerspace = h;
    _headerspaceMap = new HashMap<>();
    _useDefaultCase = defaultCase;
  }

  public static DestinationClasses create(
      IBatfish batfish, Graph graph, @Nullable HeaderSpace h, boolean defaultCase) {
    DestinationClasses abs = new DestinationClasses(batfish, graph, h, defaultCase);
    abs.initDestinationMap();
    return abs;
  }

  /*
   * Initialize a map from sets of nodes that represent possible destinations,
   * to a set of prefixes that represent the collection of destination
   * IP addresses for which those nodes might be physical destinations
   */
  private void initDestinationMap() {
    Map<String, List<Protocol>> protoMap = buildProtocolMap();
    List<Prefix> dstIps = new ArrayList<>();
    List<Prefix> notDstIps = new ArrayList<>();
    extractPrefixesFromHeaderSpace(dstIps, notDstIps);
    PrefixTrieMap pt = new PrefixTrieMap();
    buildPrefixTrie(protoMap, dstIps, notDstIps, pt);
    Map<Set<String>, List<Prefix>> destinationMap = pt.createDestinationMap();
    buildHeaderSpaceEcs(destinationMap);
    if (_useDefaultCase) {
      addCatchAllCase(dstIps, notDstIps, destinationMap);
    }
  }

  /**
   * Adds a catch-all headerspace to the headerspace map. The catch-all case matches dstIps, does
   * not match notDstIps, and doesn't match anything matched by anything in destinationMap.
   *
   * @param dstIps A list of destination IPs that should be in the catch-all headerspace.
   * @param notDstIps A list of destination IPs that should not be in the catch-all headerspace.
   * @param destinationMap Inversion of the prefix trie -- from sets of destinations to prefixes
   */
  private void addCatchAllCase(
      List<Prefix> dstIps, List<Prefix> notDstIps, Map<Set<String>, List<Prefix>> destinationMap) {
    HeaderSpace catchAll = createHeaderSpace(dstIps);

    catchAll.setNotDstIps(
        Stream.concat(
                notDstIps.stream(), destinationMap.values().stream().flatMap(Collection::stream))
            .map(IpWildcard::new)
            .collect(Collectors.toSet()));

    if (_headerspace != null) {
      copyAllButDestinationIp(catchAll, _headerspace);
    }
    if (!catchAll.getNotDstIps().equals(catchAll.getDstIps())) {
      _headerspaceMap.put(new HashSet<>(), new Tuple<>(catchAll, new Tuple<>(null, true)));
    }
  }

  private void buildHeaderSpaceEcs(Map<Set<String>, List<Prefix>> destinationMap) {
    destinationMap.forEach(
        (devices, prefixes) -> {
          HeaderSpace h = createHeaderSpace(prefixes);
          if (_headerspace != null) {
            copyAllButDestinationIp(h, _headerspace);
          }
          _headerspaceMap.put(devices, new Tuple<>(h, new Tuple<>(prefixes, false)));
        });
  }

  private void buildPrefixTrie(
      Map<String, List<Protocol>> protoMap,
      List<Prefix> dstIps,
      List<Prefix> notDstIps,
      PrefixTrieMap pt) {
    // Populate prefix trie
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Configuration conf = entry.getValue();
      for (Protocol proto : protoMap.get(router)) {
        Set<Prefix> destinations = new HashSet<>();
        if (!proto.isStatic()) {
          destinations = Graph.getOriginatedNetworks(conf, proto);
        }
        // Add all destinations to the prefix trie relevant to this slice
        for (Prefix p : destinations) {
          if (PrefixUtils.overlap(p, dstIps) && !PrefixUtils.overlap(p, notDstIps)) {
            Set<Prefix> toAdd = new HashSet<>();
            for (Prefix pfx : dstIps) {
              if (p.equals(pfx)) {
                toAdd.add(p);
              } else if (pfx.containsPrefix(p)) {
                toAdd.add(p);
              } else if (p.containsPrefix(pfx)) {
                toAdd.add(pfx);
              }
            }
            for (Prefix prefix : toAdd) {
              pt.add(prefix, router);
            }
          }
        }
      }
    }
  }

  private void extractPrefixesFromHeaderSpace(List<Prefix> dstIps, List<Prefix> notDstIps) {
    /* TODO this should be updated to handle arbitrary IpSpaces better.
     * Consider whether there is a better encoding of an IpSpace than PrefixTrie.
     */
    if (_headerspace == null || _headerspace.getDstIps() == null) {
      dstIps.add(Prefix.parse("0.0.0.0/0"));
    } else {
      IpSpace dstIpSpace = _headerspace.getDstIps();
      IpSpacePrefixCollector dstPrefixCollector = new IpSpacePrefixCollector();
      dstPrefixCollector.collectPrefixes(dstIpSpace);
      dstIps.addAll(dstPrefixCollector.getPrefixes());
      notDstIps.addAll(dstPrefixCollector.getNotPrefixes());

      IpSpace notDstIpSpace = _headerspace.getNotDstIps();
      IpSpacePrefixCollector notDstPrefixCollector = new IpSpacePrefixCollector();
      notDstPrefixCollector.collectPrefixes(notDstIpSpace);
      if (!notDstPrefixCollector.getNotPrefixes().isEmpty()) {
        throw new BatfishException("Unimplemented: not not destination prefixes");
      }
      notDstIps.addAll(notDstPrefixCollector.getPrefixes());
    }
  }

  private void copyAllButDestinationIp(HeaderSpace h1, HeaderSpace h2) {
    h1.setDscps(h2.getDscps());
    h1.setDstPorts(h2.getDstPorts());
    h1.setNotDstPorts(h2.getNotDstPorts());
    h1.setNotDstProtocols(h2.getNotDstProtocols());
    h1.setDstProtocols(h2.getDstProtocols());
    h1.setSrcPorts(h2.getSrcPorts());
    h1.setSrcIps(h2.getSrcIps());
    h1.setSrcOrDstIps(h2.getSrcOrDstIps());
    h1.setSrcOrDstPorts(h2.getSrcOrDstPorts());
    h1.setNotSrcIps(h2.getNotSrcIps());
    h1.setNotSrcPorts(h2.getNotSrcPorts());
    h1.setEcns(h2.getEcns());
    h1.setNotEcns(h2.getNotEcns());
    h1.setFragmentOffsets(h2.getFragmentOffsets());
    h1.setNotFragmentOffsets(h2.getNotFragmentOffsets());
    h1.setPacketLengths(h2.getPacketLengths());
    h1.setNotPacketLengths(h2.getNotPacketLengths());
    h1.setIcmpCodes(h2.getIcmpCodes());
    h1.setNotIcmpCodes(h2.getNotIcmpCodes());
    h1.setIcmpTypes(h2.getIcmpTypes());
    h1.setNotIcmpTypes(h2.getNotIcmpTypes());
    h1.setStates(h2.getStates());
    h1.setTcpFlags(h2.getTcpFlags());
  }

  /*
   * Initialize a mapping from router to collection of protocol
   */
  private Map<String, List<Protocol>> buildProtocolMap() {
    // Figure out which protocols are running on which devices
    Map<String, List<Protocol>> protocols = new HashMap<>();
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Configuration conf = entry.getValue();
      List<Protocol> protos = new ArrayList<>();
      protocols.put(router, protos);

      if (conf.getDefaultVrf().getOspfProcess() != null) {
        protos.add(Protocol.OSPF);
      }

      if (conf.getDefaultVrf().getBgpProcess() != null) {
        protos.add(Protocol.BGP);
      }

      if (!conf.getDefaultVrf().getStaticRoutes().isEmpty()) {
        protos.add(Protocol.STATIC);
      }

      if (!conf.getAllInterfaces().isEmpty()) {
        protos.add(Protocol.CONNECTED);
      }
    }
    return protocols;
  }

  /*
   * Convert a collection of prefixes over destination IP in to a headerspace
   */
  private HeaderSpace createHeaderSpace(List<Prefix> prefixes) {
    HeaderSpace h = new HeaderSpace();
    h.setDstIps(prefixes.stream().map(IpWildcard::new).collect(Collectors.toSet()));
    return h;
  }

  public IBatfish getBatfish() {
    return _batfish;
  }

  public Graph getGraph() {
    return _graph;
  }

  public HeaderSpace getHeaderspace() {
    return _headerspace;
  }

  public Map<Set<String>, Tuple<HeaderSpace, Tuple<List<Prefix>, Boolean>>> getHeaderspaceMap() {
    return _headerspaceMap;
  }
}
