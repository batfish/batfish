package org.batfish.z3;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.StateParameter.Type;

public class TestSynthesizerInput implements SynthesizerInput {

  public static class Builder {

    private Map<String, Map<String, List<LineAction>>> _aclActions;

    private Map<String, Map<String, List<BooleanExpr>>> _aclConditions;

    private Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>>
        _arpTrueEdge;

    private Set<Edge> _enabledEdges;

    private Map<String, Set<String>> _enabledInterfaces;

    private Map<String, Map<String, Set<String>>> _enabledInterfacesByNodeVrf;

    private Set<String> _enabledNodes;

    private Map<String, Set<String>> _enabledVrfs;

    private Map<String, Map<String, String>> _incomingAcls;

    private Map<String, Set<Ip>> _ipsByHostname;

    private Map<String, Map<String, Map<String, BooleanExpr>>> _neighborUnreachable;

    private Map<String, Map<String, BooleanExpr>> _nullRoutedIps;

    private Map<String, Map<String, String>> _outgoingAcls;

    private Map<String, Map<String, BooleanExpr>> _routableIps;

    private boolean _simplify;

    private Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> _sourceNats;

    private Map<String, Set<String>> _topologyInterfaces;

    private Set<Type> _vectorizedParameters;

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
      _ipsByHostname = ImmutableMap.of();
      _neighborUnreachable = ImmutableMap.of();
      _nullRoutedIps = ImmutableMap.of();
      _outgoingAcls = ImmutableMap.of();
      _routableIps = ImmutableMap.of();
      _sourceNats = ImmutableMap.of();
      _topologyInterfaces = ImmutableMap.of();
      _vectorizedParameters = ImmutableSet.of();
    }

    public TestSynthesizerInput build() {
      return new TestSynthesizerInput(
          _aclActions,
          _aclConditions,
          _arpTrueEdge,
          _enabledEdges,
          _enabledInterfaces,
          _enabledInterfacesByNodeVrf,
          _enabledNodes,
          _enabledVrfs,
          _incomingAcls,
          _ipsByHostname,
          _neighborUnreachable,
          _nullRoutedIps,
          _outgoingAcls,
          _routableIps,
          _simplify,
          _sourceNats,
          _topologyInterfaces,
          _vectorizedParameters);
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

    public Builder setIpsByHostname(Map<String, Set<Ip>> ipsByHostname) {
      _ipsByHostname = ipsByHostname;
      return this;
    }

    public Builder setNeighborUnreachable(
        Map<String, Map<String, Map<String, BooleanExpr>>> neighborUnreachable) {
      _neighborUnreachable = neighborUnreachable;
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

    public Builder setTopologyInterfaces(Map<String, Set<String>> topologyInterfaces) {
      _topologyInterfaces = topologyInterfaces;
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

  private final Map<String, Map<String, List<LineAction>>> _aclActions;

  private final Map<String, Map<String, List<BooleanExpr>>> _aclConditions;

  private final Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>>
      _arpTrueEdge;

  private final Set<Edge> _enabledEdges;

  private final Map<String, Set<String>> _enabledInterfaces;

  private final Map<String, Map<String, Set<String>>> _enabledInterfacesByNodeVrf;

  private final Set<String> _enabledNodes;

  private final Map<String, Set<String>> _enabledVrfs;

  private final Map<String, Map<String, String>> _incomingAcls;

  private final Map<String, Set<Ip>> _ipsByHostname;

  private final Map<String, Map<String, Map<String, BooleanExpr>>> _neighborUnreachable;

  private final Map<String, Map<String, BooleanExpr>> _nullRoutedIps;

  private final Map<String, Map<String, String>> _outgoingAcls;

  private final Map<String, Map<String, BooleanExpr>> _routableIps;

  private final boolean _simplify;

  private final Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> _sourceNats;

  private final Map<String, Set<String>> _topologyInterfaces;

  private final Set<Type> _vectorizedParameters;

  private TestSynthesizerInput(
      Map<String, Map<String, List<LineAction>>> aclActions,
      Map<String, Map<String, List<BooleanExpr>>> aclConditions,
      Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>> arpTrueEdge,
      Set<Edge> enabledEdges,
      Map<String, Set<String>> enabledInterfaces,
      Map<String, Map<String, Set<String>>> enabledInterfacesByNodeVrf,
      Set<String> enabledNodes,
      Map<String, Set<String>> enabledVrfs,
      Map<String, Map<String, String>> incomingAcls,
      Map<String, Set<Ip>> ipsByHostname,
      Map<String, Map<String, Map<String, BooleanExpr>>> neighborUnreachable,
      Map<String, Map<String, BooleanExpr>> nullRoutedIps,
      Map<String, Map<String, String>> outgoingAcls,
      Map<String, Map<String, BooleanExpr>> routableIps,
      boolean simplify,
      Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> sourceNats,
      Map<String, Set<String>> topologyInterfaces,
      Set<Type> vectorizedParameters) {
    _aclActions = aclActions;
    _aclConditions = aclConditions;
    _arpTrueEdge = arpTrueEdge;
    _enabledEdges = enabledEdges;
    _enabledInterfaces = enabledInterfaces;
    _enabledInterfacesByNodeVrf = enabledInterfacesByNodeVrf;
    _enabledNodes = enabledNodes;
    _enabledVrfs = enabledVrfs;
    _incomingAcls = incomingAcls;
    _ipsByHostname = ipsByHostname;
    _neighborUnreachable = neighborUnreachable;
    _nullRoutedIps = nullRoutedIps;
    _outgoingAcls = outgoingAcls;
    _routableIps = routableIps;
    _simplify = simplify;
    _sourceNats = sourceNats;
    _topologyInterfaces = topologyInterfaces;
    _vectorizedParameters = vectorizedParameters;
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
  public Map<String, Set<Ip>> getIpsByHostname() {
    return _ipsByHostname;
  }

  @Override
  public Map<String, Map<String, Map<String, BooleanExpr>>> getNeighborUnreachable() {
    return _neighborUnreachable;
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
  public Map<String, Set<String>> getTraversableInterfaces() {
    return _topologyInterfaces;
  }

  @Override
  public Set<Type> getVectorizedParameters() {
    return _vectorizedParameters;
  }
}
