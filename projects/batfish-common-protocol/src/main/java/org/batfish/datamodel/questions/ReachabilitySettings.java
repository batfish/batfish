package org.batfish.datamodel.questions;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;

public class ReachabilitySettings {

  private final Set<String> _ingressNodes;

  public AnswerElement getInvalidSettingsAnswer() {
    return _invalidSettingsAnswer;
  }

  public Set<String> getIngressNodes() {
    return _ingressNodes;
  }

  public Set<String> getFinalNodes() {
    return _finalNodes;
  }

  public Set<String> getTransitNodes() {
    return _transitNodes;
  }

  public Set<String> getNotTransitNodes() {
    return _notTransitNodes;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public int getMaxChunkSize() {
    return _maxChunkSize;
  }

  private final Set<String> _finalNodes;

  private final Set<String> _transitNodes;

  private final Set<String> _notTransitNodes;

  private AnswerElement _invalidSettingsAnswer;

  private final HeaderSpace _headerSpace;

  private final int _maxChunkSize;

  public static Builder builder() {
    return new Builder();
  }

  private ReachabilitySettings(Builder builder, Map<String, Configuration> configurations) {
    _headerSpace = builder._headerSpace;
    _maxChunkSize = builder._maxChunkSize;
    _finalNodes = computeFinalNodes(builder._finalNodes, builder._notFinalNodes, configurations);
    _ingressNodes =
        computeIngressNodes(builder._ingressNodes, builder._notIngressNodes, configurations);
    _transitNodes = builder._transitNodes.getMatchingNodes(configurations);
    _notTransitNodes = builder._notTransitNodes.getMatchingNodes(configurations);
    validateTransitNodes();
  }

  private void validateTransitNodes() {
    Set<String> commonNodes = Sets.intersection(_transitNodes, _notTransitNodes);
    if (!commonNodes.isEmpty()) {
      _invalidSettingsAnswer =
          new StringAnswerElement(
              "The following nodes illegally match both transitNodes and notTransitNodes: "
                  + commonNodes);
    }
  }

  private Set<String> computeFinalNodes(
      NodesSpecifier finalNodes,
      NodesSpecifier notFinalNodes,
      Map<String, Configuration> configurations) {
    Set<String> matchingFinalNodes = finalNodes.getMatchingNodes(configurations);
    Set<String> matchingNotFinalNodes = notFinalNodes.getMatchingNodes(configurations);
    Set<String> activeFinalNodes = Sets.difference(matchingFinalNodes, matchingNotFinalNodes);
    if (activeFinalNodes.isEmpty()) {
      _invalidSettingsAnswer =
          new StringAnswerElement(
              "NOTHING TO DO: No nodes both match finalNodeRegex: '"
                  + finalNodes
                  + "' and fail to match notFinalNodeRegex: '"
                  + notFinalNodes
                  + "'");
    }
    return activeFinalNodes;
  }

  private Set<String> computeIngressNodes(
      NodesSpecifier ingressNodes,
      NodesSpecifier notIngressNodes,
      Map<String, Configuration> configurations) {
    Set<String> matchingIngressNodes = ingressNodes.getMatchingNodes(configurations);
    Set<String> matchingNotIngressNodes = notIngressNodes.getMatchingNodes(configurations);
    Set<String> activeIngressNodes = Sets.difference(matchingIngressNodes, matchingNotIngressNodes);
    if (activeIngressNodes.isEmpty()) {
      _invalidSettingsAnswer =
          new StringAnswerElement(
              "NOTHING TO DO: No nodes both match ingressNodeRegex: '"
                  + ingressNodes
                  + "' and fail to match notIngressNodeRegex: '"
                  + notIngressNodes
                  + "'");
    }
    return activeIngressNodes;
  }

  public static class Builder {

    public ReachabilitySettings build(Map<String, Configuration> configurations) {
      return new ReachabilitySettings(this, configurations);
    }

    private NodesSpecifier _finalNodes;

    private HeaderSpace _headerSpace;

    private NodesSpecifier _ingressNodes;

    private int _maxChunkSize;

    private NodesSpecifier _notFinalNodes;

    private NodesSpecifier _notIngressNodes;

    private NodesSpecifier _notTransitNodes;

    private NodesSpecifier _transitNodes;

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

    public NodesSpecifier getNotFinalNodes() {
      return _notFinalNodes;
    }

    public NodesSpecifier getNotIngressNodes() {
      return _notIngressNodes;
    }

    public NodesSpecifier getNotTransitNodes() {
      return _notTransitNodes;
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

    public void setNotFinalNodeRegex(NodesSpecifier notFinalNodes) {
      _notFinalNodes = notFinalNodes;
    }

    public void setNotIngressNodeRegex(NodesSpecifier notIngressNodes) {
      _notIngressNodes = notIngressNodes;
    }

    public void setNotTransitNodes(NodesSpecifier notTransitNodes) {
      _notTransitNodes = notTransitNodes;
    }

    public void setTransitNodes(NodesSpecifier transitNodes) {
      _transitNodes = transitNodes;
    }
  }
}
