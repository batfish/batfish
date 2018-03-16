package org.batfish.datamodel.questions;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;

public class ReachabilitySettings {

  public static class Builder {

    private NodesSpecifier _finalNodes;

    private HeaderSpace _headerSpace;

    private NodesSpecifier _ingressNodes;

    private int _maxChunkSize;

    private NodesSpecifier _nonTransitNodes;

    private NodesSpecifier _notFinalNodes;

    private NodesSpecifier _notIngressNodes;

    private NodesSpecifier _transitNodes;

    public ReachabilitySettings build() {
      return new ReachabilitySettings(this);
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

    public NodesSpecifier getTransitNodes() {
      return _transitNodes;
    }

    public void setFinalNodes(NodesSpecifier finalNodes) {
      _finalNodes = finalNodes;
    }

    public void setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
    }

    public void setIngressNodes(NodesSpecifier ingressNodes) {
      _ingressNodes = ingressNodes;
    }

    public void setMaxChunkSize(int maxChunkSize) {
      _maxChunkSize = maxChunkSize;
    }

    public void setNonTransitNodes(NodesSpecifier nonTransitNodes) {
      _nonTransitNodes = nonTransitNodes;
    }

    public void setNotFinalNodeRegex(NodesSpecifier notFinalNodes) {
      _notFinalNodes = notFinalNodes;
    }

    public void setNotIngressNodeRegex(NodesSpecifier notIngressNodes) {
      _notIngressNodes = notIngressNodes;
    }

    public void setTransitNodes(NodesSpecifier transitNodes) {
      _transitNodes = transitNodes;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final NodesSpecifier _finalNodes;

  private final HeaderSpace _headerSpace;

  private final NodesSpecifier _ingressNodes;

  private final int _maxChunkSize;

  private final NodesSpecifier _nonTransitNodes;

  private NodesSpecifier _notFinalNodes;

  private NodesSpecifier _notIngressNodes;

  private final NodesSpecifier _transitNodes;

  private ReachabilitySettings(Builder builder) {
    _finalNodes = builder._finalNodes;
    _notFinalNodes = builder._notFinalNodes;
    _headerSpace = builder._headerSpace;
    _ingressNodes = builder._ingressNodes;
    _notIngressNodes = builder._notIngressNodes;
    _maxChunkSize = builder._maxChunkSize;
    _transitNodes = builder._transitNodes;
    _nonTransitNodes = builder._nonTransitNodes;
  }

  public Set<String> computeActiveFinalNodes(Map<String, Configuration> configurations)
      throws InvalidReachabilitySettingsException {
    Set<String> matchingFinalNodes = _finalNodes.getMatchingNodes(configurations);
    Set<String> matchingNotFinalNodes = _notFinalNodes.getMatchingNodes(configurations);
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

  public Set<String> computeActiveIngressNodes(Map<String, Configuration> configurations)
      throws InvalidReachabilitySettingsException {
    Set<String> matchingIngressNodes = _ingressNodes.getMatchingNodes(configurations);
    Set<String> matchingNotIngressNodes = _notIngressNodes.getMatchingNodes(configurations);
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

  public Set<String> computeActiveNonTransitNodes(Map<String, Configuration> configurations) {
    return _nonTransitNodes.getMatchingNodes(configurations);
  }

  public Set<String> computeActiveTransitNodes(Map<String, Configuration> configurations) {
    return _transitNodes.getMatchingNodes(configurations);
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

  public NodesSpecifier getTransitNodes() {
    return _transitNodes;
  }

  public void validateTransitNodes(Map<String, Configuration> configurations)
      throws InvalidReachabilitySettingsException {
    Set<String> commonNodes =
        Sets.intersection(
            computeActiveTransitNodes(configurations),
            computeActiveNonTransitNodes(configurations));
    if (!commonNodes.isEmpty()) {
      throw new InvalidReachabilitySettingsException(
          "The following nodes illegally match both transitNodes and nonTransitNodes: "
              + commonNodes);
    }
  }
}
