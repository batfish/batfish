package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.FibRowMatchExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.state.StateParameter.Type;

public final class SynthesizerInputImpl implements SynthesizerInput {

  public static class Builder {
    private Map<String, Configuration> _configurations;

    private DataPlane _dataPlane;

    private Map<String, Set<String>> _disabledAcls;

    private Map<String, Set<String>> _disabledInterfaces;

    private Set<String> _disabledNodes;

    private Map<String, Set<String>> _disabledVrfs;

    private boolean _simplify;

    private Set<Type> _vectorizedParameters;

    private Builder() {
      _disabledAcls = ImmutableMap.of();
      _disabledInterfaces = ImmutableMap.of();
      _disabledNodes = ImmutableSet.of();
      _disabledVrfs = ImmutableMap.of();
      _simplify = false;
      _vectorizedParameters = ImmutableSet.of();
    }

    public SynthesizerInputImpl build() {
      return new SynthesizerInputImpl(
          _configurations,
          _dataPlane,
          _disabledAcls,
          _disabledInterfaces,
          _disabledNodes,
          _disabledVrfs,
          _simplify,
          _vectorizedParameters);
    }

    public Builder setConfigurations(Map<String, Configuration> configurations) {
      _configurations = configurations;
      return this;
    }

    public Builder setDataPlane(DataPlane dataPlane) {
      _dataPlane = dataPlane;
      return this;
    }

    public Builder setDisabledAcls(Map<String, Set<String>> disabledAcls) {
      _disabledAcls = disabledAcls;
      return this;
    }

    public Builder setDisabledInterfaces(Map<String, Set<String>> disabledInterfaces) {
      _disabledInterfaces = disabledInterfaces;
      return this;
    }

    public Builder setDisabledNodes(Set<String> disabledNodes) {
      _disabledNodes = disabledNodes;
      return this;
    }

    public Builder setDisabledVrfs(Map<String, Set<String>> disabledVrfs) {
      _disabledVrfs = disabledVrfs;
      return this;
    }

    public Builder setSimplify(boolean simplify) {
      _simplify = simplify;
      return this;
    }

    public Builder setVectorizedParameters(Set<Type> vectorizedParameters) {
      _vectorizedParameters = vectorizedParameters;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private static boolean isLoopbackInterface(String ifaceName) {
    String lcIfaceName = ifaceName.toLowerCase();
    return lcIfaceName.startsWith("lo");
  }

  private final Map<String, Map<String, Map<Integer, LineAction>>> _aclActions;

  private final Map<String, Map<String, Map<Integer, BooleanExpr>>> _aclConditions;

  private final Map<String, Configuration> _configurations;

  private final Map<String, Set<String>> _disabledAcls;

  private final Map<String, Set<String>> _disabledInterfaces;

  private final Set<String> _disabledNodes;

  private final Map<String, Set<String>> _disabledVrfs;

  private final Set<Edge> _edges;

  private final Map<String, Map<String, IpAccessList>> _enabledAcls;

  private final Set<Edge> _enabledEdges;

  private final Set<NodeInterfacePair> _enabledFlowSinks;

  private final Map<String, Map<String, Interface>> _enabledInterfaces;

  private final Map<String, Configuration> _enabledNodes;

  private final Map<String, Map<String, Vrf>> _enabledVrfs;

  private final Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>
      _fibConditions;

  private final Map<String, Map<String, SortedSet<FibRow>>> _fibs;

  private final Set<NodeInterfacePair> _flowSinks;

  private final Map<String, Set<Ip>> _ipsByHostname;

  private final boolean _simplify;

  private final Map<String, Set<Interface>> _topologyInterfaces;

  private final Set<Type> _vectorizedParameters;

  public SynthesizerInputImpl(
      Map<String, Configuration> configurations,
      DataPlane dataPlane,
      Map<String, Set<String>> disabledAcls,
      Map<String, Set<String>> disabledInterfaces,
      Set<String> disabledNodes,
      Map<String, Set<String>> disabledVrfs,
      boolean simplify,
      Set<Type> vectorizedParameters) {
    if (configurations == null) {
      throw new BatfishException("Must supply configurations");
    }
    _configurations = ImmutableMap.copyOf(configurations);
    _disabledAcls = ImmutableMap.copyOf(disabledAcls);
    _disabledInterfaces = ImmutableMap.copyOf(disabledInterfaces);
    _disabledNodes = ImmutableSet.copyOf(disabledNodes);
    _disabledVrfs = ImmutableMap.copyOf(disabledVrfs);
    _enabledNodes = computeEnabledNodes();
    _enabledVrfs = computeEnabledVrfs();
    _enabledInterfaces = computeEnabledInterfaces();
    _simplify = simplify;
    _vectorizedParameters = vectorizedParameters;
    if (dataPlane != null) {
      _fibs = ImmutableMap.copyOf(dataPlane.getFibs());
      _flowSinks = ImmutableSet.copyOf(dataPlane.getFlowSinks());
      _enabledFlowSinks = computeEnabledFlowSinks();
      _ipsByHostname = computeIpsByHostname();
      _fibConditions = computeFibConditions();
      _edges = ImmutableSet.copyOf(dataPlane.getTopologyEdges());
      _enabledEdges = computeEnabledEdges();
      _topologyInterfaces = computeTopologyInterfaces();
    } else {
      _fibs = null;
      _flowSinks = null;
      _enabledFlowSinks = null;
      _ipsByHostname = null;
      _fibConditions = null;
      _edges = null;
      _enabledEdges = null;
      _topologyInterfaces = null;
    }
    _enabledAcls = computeEnabledAcls();
    _aclActions = computeAclActions();
    _aclConditions = computeAclConditions();
  }

  private Map<String, Map<String, Map<Integer, LineAction>>> computeAclActions() {
    return _enabledAcls
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e ->
                    e.getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey,
                                e2 -> {
                                  IpAccessList acl = e2.getValue();
                                  ImmutableMap.Builder<Integer, LineAction> lineActions =
                                      ImmutableMap.builder();
                                  List<IpAccessListLine> lines = acl.getLines();
                                  for (int i = 0; i < lines.size(); i++) {
                                    lineActions.put(i, lines.get(i).getAction());
                                  }
                                  return lineActions.build();
                                }))));
  }

  private Map<String, Map<String, Map<Integer, BooleanExpr>>> computeAclConditions() {
    return _enabledAcls
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e ->
                    e.getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey,
                                e2 -> {
                                  IpAccessList acl = e2.getValue();
                                  ImmutableMap.Builder<Integer, BooleanExpr> lineConditions =
                                      ImmutableMap.builder();
                                  List<IpAccessListLine> lines = acl.getLines();
                                  for (int i = 0; i < lines.size(); i++) {
                                    lineConditions.put(i, new HeaderSpaceMatchExpr(lines.get(i)));
                                  }
                                  return lineConditions.build();
                                }))));
  }

  private Map<String, Map<String, IpAccessList>> computeEnabledAcls() {
    if (_topologyInterfaces != null) {
      return _topologyInterfaces
          .entrySet()
          .stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey,
                  e ->
                      e.getValue()
                          .stream()
                          .flatMap(
                              i -> {
                                ImmutableList.Builder<Pair<String, IpAccessList>> interfaceAcls =
                                    ImmutableList.builder();
                                IpAccessList aclIn = i.getIncomingFilter();
                                IpAccessList aclOut = i.getOutgoingFilter();
                                if (aclIn != null) {
                                  interfaceAcls.add(new Pair<>(aclIn.getName(), aclIn));
                                }
                                if (aclOut != null) {
                                  interfaceAcls.add(new Pair<>(aclOut.getName(), aclOut));
                                }
                                return interfaceAcls.build().stream();
                              })
                          .collect(ImmutableSet.toImmutableSet())
                          .stream()
                          .collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond))));
    } else {
      return _configurations
          .entrySet()
          .stream()
          .filter(e -> !_disabledNodes.contains(e.getKey()))
          .collect(
              ImmutableMap.toImmutableMap(
                  Entry::getKey,
                  e -> {
                    String hostname = e.getKey();
                    Set<String> disabledAcls = _disabledAcls.get(hostname);
                    return e.getValue()
                        .getIpAccessLists()
                        .entrySet()
                        .stream()
                        .filter(e2 -> disabledAcls == null || !disabledAcls.contains(e2.getKey()))
                        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
                  }));
    }
  }

  private Set<Edge> computeEnabledEdges() {
    return _edges
        .stream()
        .filter(
            e -> {
              Map<String, Interface> enabledInterfaces1 = _enabledInterfaces.get(e.getNode1());
              Map<String, Interface> enabledInterfaces2 = _enabledInterfaces.get(e.getNode2());
              return enabledInterfaces1 != null
                  && enabledInterfaces1.keySet().contains(e.getInt1())
                  && enabledInterfaces2 != null
                  && enabledInterfaces2.keySet().contains(e.getInt2());
            })
        .collect(ImmutableSet.toImmutableSet());
  }

  private Set<NodeInterfacePair> computeEnabledFlowSinks() {
    return _flowSinks
        .stream()
        .filter(
            f -> {
              Map<String, Interface> enabledInterfaces = _enabledInterfaces.get(f.getHostname());
              return enabledInterfaces != null
                  && enabledInterfaces.keySet().contains(f.getInterface());
            })
        .collect(ImmutableSet.toImmutableSet());
  }

  private Map<String, Map<String, Interface>> computeEnabledInterfaces() {
    return _enabledVrfs
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e -> {
                  Set<String> disabledInterfaces = _disabledInterfaces.get(e.getKey());
                  return e.getValue()
                      .entrySet()
                      .stream()
                      .flatMap(e2 -> e2.getValue().getInterfaces().entrySet().stream())
                      .filter(
                          e2 ->
                              disabledInterfaces == null
                                  || !disabledInterfaces.contains(e2.getKey()))
                      .filter(e2 -> e2.getValue().getActive())
                      .filter(e2 -> !e2.getValue().getBlacklisted())
                      .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
                }));
  }

  private Map<String, Configuration> computeEnabledNodes() {
    return _configurations
        .entrySet()
        .stream()
        .filter(e -> !_disabledNodes.contains(e.getKey()))
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  private Map<String, Map<String, Vrf>> computeEnabledVrfs() {
    return _enabledNodes
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e -> {
                  Set<String> disabledVrfs = _disabledVrfs.get(e.getKey());
                  return e.getValue()
                      .getVrfs()
                      .entrySet()
                      .stream()
                      .filter(e2 -> disabledVrfs == null || !disabledVrfs.contains(e2.getKey()))
                      .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
                }));
  }

  private Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>
      computeFibConditions() {
    return _configurations
        .entrySet()
        .stream()
        .filter(e -> !_disabledNodes.contains(e.getKey()))
        .filter(e -> _fibs.containsKey(e.getKey()))
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e -> {
                  String hostname = e.getKey();
                  Configuration c = e.getValue();
                  Set<String> disabledVrfs = _disabledVrfs.get(hostname);
                  return c.getVrfs()
                      .keySet()
                      .stream()
                      .filter(vrfName -> disabledVrfs == null || !disabledVrfs.contains(vrfName))
                      .filter(_fibs.get(hostname)::containsKey)
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Function.identity(),
                              vrfName -> computeFibConditionsByInterface(hostname, vrfName)));
                }));
  }

  private Map<String, Map<NodeInterfacePair, BooleanExpr>> computeFibConditionsByInterface(
      String hostname, String vrfName) {
    Map<String, Map<NodeInterfacePair, ImmutableList.Builder<BooleanExpr>>> conditionsByInterface =
        new HashMap<>();
    SortedSet<FibRow> fibSet = _fibs.get(hostname).get(vrfName);
    List<FibRow> fib = ImmutableList.copyOf(fibSet);
    for (int i = 0; i < fib.size(); i++) {
      FibRow currentRow = fib.get(i);
      String ifaceOutName = currentRow.getInterface();
      NodeInterfacePair receiver;
      if (isLoopbackInterface(ifaceOutName)
          || CommonUtil.isNullInterface(ifaceOutName)
          || ifaceOutName.equals(FibRow.DROP_NO_ROUTE)) {
        receiver = NodeInterfacePair.NONE;
      } else {
        receiver = new NodeInterfacePair(currentRow.getNextHop(), currentRow.getNextHopInterface());
      }

      conditionsByInterface
          .computeIfAbsent(ifaceOutName, n -> new HashMap<>())
          .computeIfAbsent(receiver, r -> ImmutableList.builder())
          .add(FibRowMatchExpr.getFibRowConditions(hostname, vrfName, fib, i, currentRow));
    }
    conditionsByInterface.entrySet().stream();
    return conditionsByInterface
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                conditionsByInterfaceEntry ->
                    conditionsByInterfaceEntry
                        .getValue()
                        .entrySet()
                        .stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                Entry::getKey,
                                conditionsByReceiverEntry ->
                                    new OrExpr(conditionsByReceiverEntry.getValue().build())))));
  }

  private Map<String, Set<Ip>> computeIpsByHostname() {
    Map<Ip, Set<String>> ipOwners = CommonUtil.computeIpOwners(true, _enabledInterfaces);
    Map<String, Set<Ip>> map = new HashMap<>();
    ipOwners.forEach(
        (ip, owners) -> {
          for (String owner : owners) {
            map.computeIfAbsent(owner, o -> new HashSet<>()).add(ip);
          }
        });
    return map.entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(Entry::getKey, e -> ImmutableSet.copyOf(e.getValue())));
  }

  private Map<String, Set<Interface>> computeTopologyInterfaces() {
    Map<String, Set<Interface>> topologyEdges = new HashMap<>();
    _enabledEdges
        .stream()
        .forEach(
            e ->
                topologyEdges
                    .computeIfAbsent(e.getNode1(), n -> new HashSet<>())
                    .add(_configurations.get(e.getNode1()).getInterfaces().get(e.getInt1())));
    _enabledFlowSinks
        .stream()
        .forEach(
            f ->
                topologyEdges
                    .computeIfAbsent(f.getHostname(), n -> new HashSet<>())
                    .add(
                        _configurations
                            .get(f.getHostname())
                            .getInterfaces()
                            .get(f.getInterface())));
    return topologyEdges
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(Entry::getKey, e -> ImmutableSet.copyOf(e.getValue())));
  }

  @Override
  public Map<String, Map<String, Map<Integer, LineAction>>> getAclActions() {
    return _aclActions;
  }

  /**
   * Mapping: hostname -> aclName -> lineNumber -> lineConditions <br>
   * lineConditions is a boolean expression representing the constraints on a header necessary for
   * that line to be matched.
   */
  public Map<String, Map<String, Map<Integer, BooleanExpr>>> getAclConditions() {
    return _aclConditions;
  }

  public Set<Edge> getEnabledEdges() {
    return _enabledEdges;
  }

  public Set<NodeInterfacePair> getEnabledFlowSinks() {
    return _enabledFlowSinks;
  }

  public Map<String, Map<String, Interface>> getEnabledInterfaces() {
    return _enabledInterfaces;
  }

  public Map<String, Configuration> getEnabledNodes() {
    return _enabledNodes;
  }

  public Map<String, Map<String, Vrf>> getEnabledVrfs() {
    return _enabledVrfs;
  }

  public Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>
      getFibConditions() {
    return _fibConditions;
  }

  public Map<String, Map<String, SortedSet<FibRow>>> getFibs() {
    return _fibs;
  }

  public Map<String, Set<Ip>> getIpsByHostname() {
    return _ipsByHostname;
  }

  public boolean getSimplify() {
    return _simplify;
  }

  public Map<String, Set<Interface>> getTopologyInterfaces() {
    return _topologyInterfaces;
  }

  public Set<Type> getVectorizedParameters() {
    return _vectorizedParameters;
  }
}
