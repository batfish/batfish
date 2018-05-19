package org.batfish.datamodel.questions;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;

public class ReachabilitySettings {

  public static class Builder {

    private SortedSet<ForwardingAction> _actions;

    private NodesSpecifier _finalNodes;

    private HeaderSpace _headerSpace;

    private InterfacesSpecifier _ingressInterfaces;

    private NodesSpecifier _ingressNodes;

    private int _maxChunkSize;

    private NodesSpecifier _nonTransitNodes;

    private NodesSpecifier _notFinalNodes;

    private NodesSpecifier _notIngressNodes;

    private Boolean _srcNatted;

    private NodesSpecifier _transitNodes;

    private boolean _specialize;

    private boolean _useCompression;

    public ReachabilitySettings build() {
      return new ReachabilitySettings(this);
    }

    public SortedSet<ForwardingAction> getActions() {
      return _actions;
    }

    public NodesSpecifier getFinalNodes() {
      return _finalNodes;
    }

    public HeaderSpace getHeaderSpace() {
      return _headerSpace;
    }

    public NodesSpecifier getIngressNodes() {
      return _ingressNodes;
    }

    public int getMaxChunkSize() {
      return _maxChunkSize;
    }

    public NodesSpecifier getNonTransitNodes() {
      return _nonTransitNodes;
    }

    public NodesSpecifier getNotFinalNodes() {
      return _notFinalNodes;
    }

    public NodesSpecifier getNotIngressNodes() {
      return _notIngressNodes;
    }

    public boolean getSpecialize() {
      return _specialize;
    }

    public Boolean getSrcNatted() {
      return _srcNatted;
    }

    public NodesSpecifier getTransitNodes() {
      return _transitNodes;
    }

    public boolean getUseCompression() {
      return _useCompression;
    }

    public Builder setActions(Iterable<ForwardingAction> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setFinalNodes(NodesSpecifier finalNodes) {
      _finalNodes = finalNodes;
      return this;
    }

    public Builder setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
      return this;
    }

    public Builder setIngressNodes(NodesSpecifier ingressNodes) {
      _ingressNodes = ingressNodes;
      return this;
    }

    public Builder setMaxChunkSize(int maxChunkSize) {
      _maxChunkSize = maxChunkSize;
      return this;
    }

    public Builder setNonTransitNodes(NodesSpecifier nonTransitNodes) {
      _nonTransitNodes = nonTransitNodes;
      return this;
    }

    public Builder setNotFinalNodeRegex(NodesSpecifier notFinalNodes) {
      _notFinalNodes = notFinalNodes;
      return this;
    }

    public Builder setNotIngressNodeRegex(NodesSpecifier notIngressNodes) {
      _notIngressNodes = notIngressNodes;
      return this;
    }

    public Builder setSrcNatted(Boolean srcNatted) {
      _srcNatted = srcNatted;
      return this;
    }

    public Builder setSpecialize(boolean specialize) {
      _specialize = specialize;
      return this;
    }

    public Builder setTransitNodes(NodesSpecifier transitNodes) {
      _transitNodes = transitNodes;
      return this;
    }

    public Builder setUseCompression(boolean useCompression) {
      _useCompression = useCompression;
      return this;
    }

    public Builder setIngressInterfaces(InterfacesSpecifier ingressInterfaces) {
      _ingressInterfaces = ingressInterfaces;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final SortedSet<ForwardingAction> _actions;

  private final NodesSpecifier _finalNodes;

  private final HeaderSpace _headerSpace;

  private final InterfacesSpecifier _ingressInterfaces;

  private final NodesSpecifier _ingressNodes;

  private final int _maxChunkSize;

  private final NodesSpecifier _nonTransitNodes;

  private NodesSpecifier _notFinalNodes;

  private NodesSpecifier _notIngressNodes;

  private final Boolean _srcNatted;

  private final boolean _specialize;

  private final NodesSpecifier _transitNodes;

  private final boolean _useCompression;

  private ReachabilitySettings(Builder builder) {
    _finalNodes = builder._finalNodes;
    _notFinalNodes = builder._notFinalNodes;
    _headerSpace = builder._headerSpace;
    _ingressInterfaces = builder._ingressInterfaces;
    _ingressNodes = builder._ingressNodes;
    _notIngressNodes = builder._notIngressNodes;
    _maxChunkSize = builder._maxChunkSize;
    _transitNodes = builder._transitNodes;
    _nonTransitNodes = builder._nonTransitNodes;
    _specialize = builder._specialize;
    _useCompression = builder._useCompression;
    _actions = builder._actions;
    _srcNatted = builder._srcNatted;
  }

  public Set<String> computeActiveFinalNodes(IBatfish batfish)
      throws InvalidReachabilitySettingsException {
    Set<String> matchingFinalNodes = _finalNodes.getMatchingNodes(batfish);
    Set<String> matchingNotFinalNodes = _notFinalNodes.getMatchingNodes(batfish);
    Set<String> activeFinalNodes = Sets.difference(matchingFinalNodes, matchingNotFinalNodes);
    if (activeFinalNodes.isEmpty()) {
      throw new InvalidReachabilitySettingsException(
          "NOTHING TO DO: No nodes both match finalNodeRegex: '"
              + _finalNodes
              + "' and fail to match notFinalNodeRegex: '"
              + _notFinalNodes
              + "'");
    }
    return activeFinalNodes;
  }

  public Set<String> computeActiveIngressNodes(IBatfish batfish)
      throws InvalidReachabilitySettingsException {
    Set<String> matchingIngressNodes = _ingressNodes.getMatchingNodes(batfish);
    Set<String> matchingNotIngressNodes = _notIngressNodes.getMatchingNodes(batfish);
    Set<String> activeIngressNodes = Sets.difference(matchingIngressNodes, matchingNotIngressNodes);
    if (activeIngressNodes.isEmpty()) {
      throw new InvalidReachabilitySettingsException(
          "NOTHING TO DO: No nodes both match ingressNodeRegex: '"
              + _ingressNodes
              + "' and fail to match notIngressNodeRegex: '"
              + _notIngressNodes
              + "'");
    }
    return activeIngressNodes;
  }

  public Set<String> computeActiveNonTransitNodes(IBatfish batfish) {
    return _nonTransitNodes.getMatchingNodes(batfish);
  }

  public Set<String> computeActiveTransitNodes(IBatfish batfish) {
    return _transitNodes.getMatchingNodes(batfish);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ReachabilitySettings)) {
      return false;
    }

    ReachabilitySettings other = (ReachabilitySettings) obj;
    return _actions.equals(other._actions)
        && _finalNodes.equals(other._finalNodes)
        && _headerSpace.equals(other._headerSpace)
        && _ingressNodes.equals(other._ingressNodes)
        && _maxChunkSize == other._maxChunkSize
        && _nonTransitNodes.equals(other._nonTransitNodes)
        && Objects.equals(_srcNatted, other._srcNatted)
        && _transitNodes.equals(other._transitNodes)
        && _specialize == other._specialize
        && _useCompression == other._useCompression;
  }

  public SortedSet<ForwardingAction> getActions() {
    return _actions;
  }

  public NodesSpecifier getFinalNodes() {
    return _finalNodes;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public InterfacesSpecifier getIngressInterfaces() {
    return _ingressInterfaces;
  }

  public NodesSpecifier getIngressNodes() {
    return _ingressNodes;
  }

  public int getMaxChunkSize() {
    return _maxChunkSize;
  }

  public NodesSpecifier getNonTransitNodes() {
    return _nonTransitNodes;
  }

  public NodesSpecifier getNotFinalNodes() {
    return _notFinalNodes;
  }

  public NodesSpecifier getNotIngressNodes() {
    return _notIngressNodes;
  }

  public Boolean getSrcNatted() {
    return _srcNatted;
  }

  public NodesSpecifier getTransitNodes() {
    return _transitNodes;
  }

  public boolean getSpecialize() {
    return _specialize;
  }

  public boolean getUseCompression() {
    return _useCompression;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _actions,
        _finalNodes,
        _headerSpace,
        _ingressNodes,
        _maxChunkSize,
        _nonTransitNodes,
        _srcNatted,
        _transitNodes,
        _specialize,
        _useCompression);
  }

  public void validateTransitNodes(IBatfish batfish) throws InvalidReachabilitySettingsException {
    Set<String> commonNodes =
        Sets.intersection(
            computeActiveTransitNodes(batfish), computeActiveNonTransitNodes(batfish));
    if (!commonNodes.isEmpty()) {
      throw new InvalidReachabilitySettingsException(
          "The following nodes illegally match both transitNodes and nonTransitNodes: "
              + commonNodes);
    }
  }
}
