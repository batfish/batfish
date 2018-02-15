package org.batfish.z3;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.state.StateParameter.Type;

public class TestSynthesizerInput implements SynthesizerInput {

  public static class Builder {

    private Map<String, Map<String, Map<Integer, LineAction>>> _aclActions;

    private Map<String, Map<String, Map<Integer, BooleanExpr>>> _aclConditions;

    private Set<Edge> _enabledEdges;

    private Set<NodeInterfacePair> _enabledFlowSinks;

    private Map<String, Map<String, Interface>> _enabledInterfaces;

    private Map<String, Configuration> _enabledNodes;

    private Map<String, Map<String, Vrf>> _enabledVrfs;

    private Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>
        _fibConditions;

    private Map<String, Map<String, SortedSet<FibRow>>> _fibs;

    private Map<String, Set<Ip>> _ipsByHostname;

    private boolean _simplify;

    private Map<String, Set<Interface>> _topologyInterfaces;

    private Set<Type> _vectorizedParameters;

    private Builder() {
      _aclActions = ImmutableMap.of();
      _aclConditions = ImmutableMap.of();
      _enabledEdges = ImmutableSet.of();
      _enabledFlowSinks = ImmutableSet.of();
      _enabledInterfaces = ImmutableMap.of();
      _enabledNodes = ImmutableMap.of();
      _enabledVrfs = ImmutableMap.of();
      _fibConditions = ImmutableMap.of();
      _fibs = ImmutableMap.of();
      _ipsByHostname = ImmutableMap.of();
      _topologyInterfaces = ImmutableMap.of();
      _vectorizedParameters = ImmutableSet.of();
    }

    public TestSynthesizerInput build() {
      return new TestSynthesizerInput(
          _aclActions,
          _aclConditions,
          _enabledEdges,
          _enabledFlowSinks,
          _enabledInterfaces,
          _enabledNodes,
          _enabledVrfs,
          _fibConditions,
          _fibs,
          _ipsByHostname,
          _simplify,
          _topologyInterfaces,
          _vectorizedParameters);
    }

    public Builder setAclActions(Map<String, Map<String, Map<Integer, LineAction>>> aclActions) {
      _aclActions = aclActions;
      return this;
    }

    public Builder setAclConditions(
        Map<String, Map<String, Map<Integer, BooleanExpr>>> aclConditions) {
      _aclConditions = aclConditions;
      return this;
    }

    public Builder setEnabledEdges(Set<Edge> enabledEdges) {
      _enabledEdges = enabledEdges;
      return this;
    }

    public Builder setEnabledFlowSinks(Set<NodeInterfacePair> enabledFlowSinks) {
      _enabledFlowSinks = enabledFlowSinks;
      return this;
    }

    public Builder setEnabledInterfaces(Map<String, Map<String, Interface>> enabledInterfaces) {
      _enabledInterfaces = enabledInterfaces;
      return this;
    }

    public Builder setEnabledNodes(Map<String, Configuration> enabledNodes) {
      _enabledNodes = enabledNodes;
      return this;
    }

    public Builder setEnabledVrfs(Map<String, Map<String, Vrf>> enabledVrfs) {
      _enabledVrfs = enabledVrfs;
      return this;
    }

    public Builder setFibConditions(
        Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>> fibConditions) {
      _fibConditions = fibConditions;
      return this;
    }

    public Builder setFibs(Map<String, Map<String, SortedSet<FibRow>>> fibs) {
      _fibs = fibs;
      return this;
    }

    public Builder setIpsByHostname(Map<String, Set<Ip>> ipsByHostname) {
      _ipsByHostname = ipsByHostname;
      return this;
    }

    public Builder setSimplify(boolean simplify) {
      _simplify = simplify;
      return this;
    }

    public Builder setTopologyInterfaces(Map<String, Set<Interface>> topologyInterfaces) {
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

  private final Map<String, Map<String, Map<Integer, LineAction>>> _aclActions;

  private final Map<String, Map<String, Map<Integer, BooleanExpr>>> _aclConditions;

  private final Set<Edge> _enabledEdges;

  private final Set<NodeInterfacePair> _enabledFlowSinks;

  private final Map<String, Map<String, Interface>> _enabledInterfaces;

  private final Map<String, Configuration> _enabledNodes;

  private final Map<String, Map<String, Vrf>> _enabledVrfs;

  private final Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>
      _fibConditions;

  private final Map<String, Map<String, SortedSet<FibRow>>> _fibs;

  private final Map<String, Set<Ip>> _ipsByHostname;

  private final boolean _simplify;

  private final Map<String, Set<Interface>> _topologyInterfaces;

  private final Set<Type> _vectorizedParameters;

  private TestSynthesizerInput(
      Map<String, Map<String, Map<Integer, LineAction>>> aclActions,
      Map<String, Map<String, Map<Integer, BooleanExpr>>> aclConditions,
      Set<Edge> enabledEdges,
      Set<NodeInterfacePair> enabledFlowSinks,
      Map<String, Map<String, Interface>> enabledInterfaces,
      Map<String, Configuration> enabledNodes,
      Map<String, Map<String, Vrf>> enabledVrfs,
      Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>> fibConditions,
      Map<String, Map<String, SortedSet<FibRow>>> fibs,
      Map<String, Set<Ip>> ipsByHostname,
      boolean simplify,
      Map<String, Set<Interface>> topologyInterfaces,
      Set<Type> vectorizedParameters) {
    _aclActions = aclActions;
    _aclConditions = aclConditions;
    _enabledEdges = enabledEdges;
    _enabledFlowSinks = enabledFlowSinks;
    _enabledInterfaces = enabledInterfaces;
    _enabledNodes = enabledNodes;
    _enabledVrfs = enabledVrfs;
    _fibConditions = fibConditions;
    _fibs = fibs;
    _ipsByHostname = ipsByHostname;
    _simplify = simplify;
    _topologyInterfaces = topologyInterfaces;
    _vectorizedParameters = vectorizedParameters;
  }

  @Override
  public Map<String, Map<String, Map<Integer, LineAction>>> getAclActions() {
    return _aclActions;
  }

  @Override
  public Map<String, Map<String, Map<Integer, BooleanExpr>>> getAclConditions() {
    return _aclConditions;
  }

  @Override
  public Set<Edge> getEnabledEdges() {
    return _enabledEdges;
  }

  @Override
  public Set<NodeInterfacePair> getEnabledFlowSinks() {
    return _enabledFlowSinks;
  }

  @Override
  public Map<String, Map<String, Interface>> getEnabledInterfaces() {
    return _enabledInterfaces;
  }

  @Override
  public Map<String, Configuration> getEnabledNodes() {
    return _enabledNodes;
  }

  @Override
  public Map<String, Map<String, Vrf>> getEnabledVrfs() {
    return _enabledVrfs;
  }

  @Override
  public Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>
      getFibConditions() {
    return _fibConditions;
  }

  @Override
  public Map<String, Map<String, SortedSet<FibRow>>> getFibs() {
    return _fibs;
  }

  @Override
  public Map<String, Set<Ip>> getIpsByHostname() {
    return _ipsByHostname;
  }

  @Override
  public boolean getSimplify() {
    return _simplify;
  }

  @Override
  public Map<String, Set<Interface>> getTopologyInterfaces() {
    return _topologyInterfaces;
  }

  @Override
  public Set<Type> getVectorizedParameters() {
    return _vectorizedParameters;
  }
}
