package org.batfish.z3;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.StateParameter.Type;

public class MockSynthesizerInput implements SynthesizerInput {

  public static class Builder {

    private Map<String, Map<String, List<LineAction>>> _aclActions;

    private Map<String, Map<String, List<BooleanExpr>>> _aclConditions;

    private Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>>
        _arpTrueEdge;

    private boolean _dataPlane;

    private Set<Edge> _enabledEdges;

    private Map<String, Set<String>> _enabledInterfaces;

    private Map<String, Map<String, Set<String>>> _enabledInterfacesByNodeVrf;

    private Set<String> _enabledNodes;

    private Map<String, Set<String>> _enabledVrfs;

    private Map<String, Map<String, String>> _incomingAcls;

    private Map<IngressLocation, BooleanExpr> _srcIpConstraints;

    private Map<String, Set<Ip>> _ipsByHostname;

    private Map<String, Map<String, Set<Ip>>> _ipsByNodeVrf;

    private Map<String, Map<String, IpSpace>> _namedIpSpaces;

    private Map<String, Map<String, Map<String, BooleanExpr>>> _neighborUnreachableOrExitsNetwork;

    private Map<String, List<String>> _nodeInterfaces;

    private Set<String> _nodesWithSrcInterfaceConstraints;

    private Set<String> _nonTransitNodes;

    private Map<String, Map<String, BooleanExpr>> _nullRoutedIps;

    private Map<String, Map<String, String>> _outgoingAcls;

    private Map<String, Map<String, BooleanExpr>> _routableIps;

    private boolean _simplify;

    private Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> _sourceNats;

    private Map<String, Set<String>> _topologyInterfaces;

    private Set<Type> _vectorizedParameters;

    private Field _srcInterfaceField;

    private Map<String, Map<String, IntExpr>> _srcInterfaceFieldValues;

    private Set<String> _transitNodes;

    private Builder() {
      _aclActions = ImmutableMap.of();
      _aclConditions = ImmutableMap.of();
      _arpTrueEdge = ImmutableMap.of();
      _enabledEdges = ImmutableSet.of();
      _enabledInterfaces = ImmutableMap.of();
      _enabledInterfacesByNodeVrf = ImmutableMap.of();
      _enabledNodes = ImmutableSet.of();
      _enabledVrfs = ImmutableMap.of();
      _incomingAcls = ImmutableMap.of();
      _srcIpConstraints = ImmutableMap.of();
      _ipsByHostname = ImmutableMap.of();
      _ipsByNodeVrf = ImmutableMap.of();
      _namedIpSpaces = ImmutableMap.of();
      _neighborUnreachableOrExitsNetwork = ImmutableMap.of();
      _nodeInterfaces = ImmutableMap.of();
      _nodesWithSrcInterfaceConstraints = ImmutableSet.of();
      _nonTransitNodes = ImmutableSortedSet.of();
      _nullRoutedIps = ImmutableMap.of();
      _outgoingAcls = ImmutableMap.of();
      _routableIps = ImmutableMap.of();
      _sourceNats = ImmutableMap.of();
      _srcInterfaceField = null;
      _srcInterfaceFieldValues = ImmutableMap.of();
      _topologyInterfaces = ImmutableMap.of();
      _transitNodes = ImmutableSortedSet.of();
      _vectorizedParameters = ImmutableSet.of();
    }

    public MockSynthesizerInput build() {
      return new MockSynthesizerInput(this);
    }

    public Builder setAclActions(Map<String, Map<String, List<LineAction>>> aclActions) {
      _aclActions = aclActions;
      return this;
    }

    public Builder setAclConditions(Map<String, Map<String, List<BooleanExpr>>> aclConditions) {
      _aclConditions = aclConditions;
      return this;
    }

    public Builder setArpTrueEdge(
        Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>> arpTrueEdge) {
      _arpTrueEdge = arpTrueEdge;
      return this;
    }

    public Builder setEnabledEdges(Set<Edge> enabledEdges) {
      _enabledEdges = enabledEdges;
      return this;
    }

    public Builder setEnabledInterfaces(Map<String, Set<String>> enabledInterfaces) {
      _enabledInterfaces = enabledInterfaces;
      return this;
    }

    public Builder setEnabledInterfacesByNodeVrf(
        Map<String, Map<String, Set<String>>> enabledInterfacesByNodeVrf) {
      _enabledInterfacesByNodeVrf = enabledInterfacesByNodeVrf;
      return this;
    }

    public Builder setEnabledNodes(Set<String> enabledNodes) {
      _enabledNodes = enabledNodes;
      return this;
    }

    public Builder setEnabledVrfs(Map<String, Set<String>> enabledVrfs) {
      _enabledVrfs = enabledVrfs;
      return this;
    }

    public Builder setIncomingAcls(Map<String, Map<String, String>> incomingAcls) {
      _incomingAcls = incomingAcls;
      return this;
    }

    public Builder setSrcIpConstraints(Map<IngressLocation, BooleanExpr> srcIpConstraints) {
      _srcIpConstraints = ImmutableMap.copyOf(srcIpConstraints);
      return this;
    }

    public Builder setIpsByHostname(Map<String, Set<Ip>> ipsByHostname) {
      _ipsByHostname = ipsByHostname;
      return this;
    }

    public Builder setDataPlane(boolean dataPlane) {
      _dataPlane = dataPlane;
      return this;
    }

    public Builder setNamedIpSpaces(Map<String, Map<String, IpSpace>> namedIpSpaces) {
      _namedIpSpaces = namedIpSpaces;
      return this;
    }

    public Builder setNeighborUnreachableOrExitsNetwork(
        Map<String, Map<String, Map<String, BooleanExpr>>> neighborUnreachableOrExitsNetwork) {
      _neighborUnreachableOrExitsNetwork = neighborUnreachableOrExitsNetwork;
      return this;
    }

    public Builder setNodeInterfaces(Map<String, List<String>> nodeInterfaces) {
      _nodeInterfaces = nodeInterfaces;
      return this;
    }

    public Builder setNodesWithSrcInterfaceConstraints(
        Set<String> nodesWithSrcInterfaceConstraints) {
      _nodesWithSrcInterfaceConstraints = nodesWithSrcInterfaceConstraints;
      return this;
    }

    public Builder setNonTransitNodes(Set<String> nonTransitNodes) {
      _nonTransitNodes = nonTransitNodes;
      return this;
    }

    public Builder setNullRoutedIps(Map<String, Map<String, BooleanExpr>> nullRoutedIps) {
      _nullRoutedIps = nullRoutedIps;
      return this;
    }

    public Builder setOutgoingAcls(Map<String, Map<String, String>> outgoingAcls) {
      _outgoingAcls = outgoingAcls;
      return this;
    }

    public Builder setRoutableIps(Map<String, Map<String, BooleanExpr>> routableIps) {
      _routableIps = routableIps;
      return this;
    }

    public Builder setSimplify(boolean simplify) {
      _simplify = simplify;
      return this;
    }

    public Builder setSourceNats(
        Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> sourceNats) {
      _sourceNats = sourceNats;
      return this;
    }

    public Builder setSrcInterfaceField(Field srcInterfaceField) {
      _srcInterfaceField = srcInterfaceField;
      return this;
    }

    public Builder setSrcInterfaceFieldValues(
        Map<String, Map<String, IntExpr>> srcInterfaceFieldValues) {
      _srcInterfaceFieldValues = srcInterfaceFieldValues;
      return this;
    }

    public Builder setTopologyInterfaces(Map<String, Set<String>> topologyInterfaces) {
      _topologyInterfaces = topologyInterfaces;
      return this;
    }

    public Builder setVectorizedParameters(Set<Type> vectorizedParameters) {
      _vectorizedParameters = vectorizedParameters;
      return this;
    }

    public Builder setTransitNodes(Set<String> transitNodes) {
      _transitNodes = transitNodes;
      return this;
    }

    public Builder setIpsByNodeVrf(Map<String, Map<String, Set<Ip>>> ipsByNodeVrf) {
      _ipsByNodeVrf = ipsByNodeVrf;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Map<String, Map<String, List<LineAction>>> _aclActions;

  private final Map<String, Map<String, List<BooleanExpr>>> _aclConditions;

  private final Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>>
      _arpTrueEdge;

  private final boolean _dataPlane;

  private final Set<Edge> _enabledEdges;

  private final Map<String, Set<String>> _enabledInterfaces;

  private final Map<String, Map<String, Set<String>>> _enabledInterfacesByNodeVrf;

  private final Set<String> _enabledNodes;

  private final Map<String, Set<String>> _enabledVrfs;

  private final Map<String, Map<String, String>> _incomingAcls;

  private final Map<IngressLocation, BooleanExpr> _srcIpConstraints;

  private final Map<String, Set<Ip>> _ipsByHostname;

  private final Map<String, Map<String, Set<Ip>>> _ipsByNodeVrf;

  private final Map<String, Map<String, IpSpace>> _namedIpSpaces;

  private final Map<String, Map<String, Map<String, BooleanExpr>>>
      _neighborUnreachableOrExitsNetwork;

  private final Map<String, List<String>> _nodeInterfaces;

  private final Set<String> _nodesWithSrcInterfaceConstraints;

  private final Set<String> _nonTransitNodes;

  private final Map<String, Map<String, BooleanExpr>> _nullRoutedIps;

  private final Map<String, Map<String, String>> _outgoingAcls;

  private final Map<String, Map<String, BooleanExpr>> _routableIps;

  private final boolean _simplify;

  private final Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> _sourceNats;

  private final Field _sourceInterfaceField;

  private final Map<String, Map<String, IntExpr>> _sourceInterfaceFieldValues;

  private final Map<String, Set<String>> _topologyInterfaces;

  private Set<String> _transitNodes;

  private final Set<Type> _vectorizedParameters;

  private MockSynthesizerInput(Builder builder) {
    _aclActions = builder._aclActions;
    _aclConditions = builder._aclConditions;
    _arpTrueEdge = builder._arpTrueEdge;
    _dataPlane = builder._dataPlane;
    _enabledEdges = builder._enabledEdges;
    _enabledInterfaces = builder._enabledInterfaces;
    _enabledInterfacesByNodeVrf = builder._enabledInterfacesByNodeVrf;
    _enabledNodes = builder._enabledNodes;
    _enabledVrfs = builder._enabledVrfs;
    _incomingAcls = builder._incomingAcls;
    _srcIpConstraints = builder._srcIpConstraints;
    _ipsByHostname = builder._ipsByHostname;
    _ipsByNodeVrf = builder._ipsByNodeVrf;
    _neighborUnreachableOrExitsNetwork = builder._neighborUnreachableOrExitsNetwork;
    _nodeInterfaces = builder._nodeInterfaces;
    _nodesWithSrcInterfaceConstraints = builder._nodesWithSrcInterfaceConstraints;
    _nullRoutedIps = builder._nullRoutedIps;
    _nonTransitNodes = builder._nonTransitNodes;
    _outgoingAcls = builder._outgoingAcls;
    _routableIps = builder._routableIps;
    _simplify = builder._simplify;
    _sourceNats = builder._sourceNats;
    _sourceInterfaceField = builder._srcInterfaceField;
    _sourceInterfaceFieldValues = builder._srcInterfaceFieldValues;
    _topologyInterfaces = builder._topologyInterfaces;
    _transitNodes = builder._transitNodes;
    _vectorizedParameters = builder._vectorizedParameters;
    _namedIpSpaces = builder._namedIpSpaces;
  }

  @Override
  public int getNodeInterfaceId(String node, String iface) {
    return getNodeInterfaces().get(node).indexOf(iface) + 1;
  }

  @Override
  public Map<String, Map<String, List<LineAction>>> getAclActions() {
    return _aclActions;
  }

  @Override
  public Map<String, Map<String, List<BooleanExpr>>> getAclConditions() {
    return _aclConditions;
  }

  @Override
  public Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>>
      getArpTrueEdge() {
    return _arpTrueEdge;
  }

  @Override
  public Set<Edge> getEnabledEdges() {
    return _enabledEdges;
  }

  @Override
  public Map<String, Set<String>> getEnabledInterfaces() {
    return _enabledInterfaces;
  }

  @Override
  public Map<String, Map<String, Set<String>>> getEnabledInterfacesByNodeVrf() {
    return _enabledInterfacesByNodeVrf;
  }

  @Override
  public Set<String> getEnabledNodes() {
    return _enabledNodes;
  }

  @Override
  public Map<String, Set<String>> getEnabledVrfs() {
    return _enabledVrfs;
  }

  @Override
  public Map<String, Map<String, String>> getIncomingAcls() {
    return _incomingAcls;
  }

  @Override
  public Map<IngressLocation, BooleanExpr> getSrcIpConstraints() {
    return _srcIpConstraints;
  }

  @Override
  public Map<String, Set<Ip>> getIpsByHostname() {
    return _ipsByHostname;
  }

  @Override
  public Map<String, Map<String, Set<Ip>>> getIpsByNodeVrf() {
    return _ipsByNodeVrf;
  }

  @Override
  public Map<String, Map<String, IpSpace>> getNamedIpSpaces() {
    return _namedIpSpaces;
  }

  @Override
  public Map<String, Map<String, Map<String, BooleanExpr>>> getNeighborUnreachableOrExitsNetwork() {
    return _neighborUnreachableOrExitsNetwork;
  }

  @Override
  public Map<String, List<String>> getNodeInterfaces() {
    return _nodeInterfaces;
  }

  @Override
  public Map<String, Map<String, BooleanExpr>> getNullRoutedIps() {
    return _nullRoutedIps;
  }

  @Override
  public Map<String, Map<String, String>> getOutgoingAcls() {
    return _outgoingAcls;
  }

  @Override
  public Map<String, Map<String, BooleanExpr>> getRoutableIps() {
    return _routableIps;
  }

  @Override
  public boolean getSimplify() {
    return _simplify;
  }

  @Override
  public Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> getSourceNats() {
    return _sourceNats;
  }

  @Override
  public Set<String> getTransitNodes() {
    return _transitNodes;
  }

  @Override
  public Field getSourceInterfaceField() {
    return _sourceInterfaceField;
  }

  @Override
  public Map<String, Map<String, IntExpr>> getSourceInterfaceFieldValues() {
    return _sourceInterfaceFieldValues;
  }

  @Override
  public Set<String> getNodesWithSrcInterfaceConstraints() {
    return _nodesWithSrcInterfaceConstraints;
  }

  @Override
  public Set<String> getNonTransitNodes() {
    return _nonTransitNodes;
  }

  @Override
  public Map<String, Set<String>> getTraversableInterfaces() {
    return _topologyInterfaces;
  }

  @Override
  public Set<Type> getVectorizedParameters() {
    return _vectorizedParameters;
  }

  @Override
  public boolean isDataPlane() {
    return _dataPlane;
  }
}
