package org.batfish.question.specifiers;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ReachabilityParameters;
import org.batfish.specifier.AllInterfaceLinksLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantWildcardSetIpSpaceSpecifierFactory;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationSpecifierFactory;
import org.batfish.specifier.NameRegexInterfaceLinkLocationSpecifierFactory;
import org.batfish.specifier.NameRegexNodeSpecifierFactory;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/**
 * A version of reachability question that supports {@link LocationSpecifier location} and {@link
 * IpSpaceSpecifier ipSpace} specifiers.
 */
public final class SpecifiersReachabilityQuestion extends Question {
  private static final String PROP_ACTIONS = "actions";

  private static final String PROP_FINAL_NODES_SPECIFIER_FACTORY = "finalNodesSpecifierFactory";

  private static final String PROP_FINAL_NODES_SPECIFIER_INPUT = "finalNodesSpecifierInput";

  private static final String PROP_FORBIDDEN_TRANSIT_NODES_NODE_SPECIFIER_INPUT =
      "forbiddenTransitNodesNodeSpecifierFactory";

  private static final String PROP_FORBIDDEN_TRANSIT_NODES_NODE_SPECIFIER_FACTORY =
      "forbiddenTransitNodesNodeSpecifierInput";

  private static final String PROP_HEADERSPACE = "headerSpace";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY =
      "sourceIpSpaceSpecifierFactory";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT = "sourceIpSpaceSpecifierInput";

  private static final String PROP_SOURCE_LOCATION_SPECIFIER_FACTORY =
      "sourceLocationSpecifierFactory";

  private static final String PROP_SOURCE_LOCATION_SPECIFIER_INPUT = "sourceLocationSpecifierInput";

  private static final String PROP_REQUIRED_TRANSIT_NODES_SPECIFIER_FACTORY =
      "requiredTransitNodesNodeSpecifierFactory";

  private static final String PROP_REQUIRED_TRANSIT_NODES_SPECIFIER_INPUT =
      "requiredTransitNodesNodeSpecifierInput";

  private static NodeSpecifier getNodeSpecifier(String factory, String input) {
    if (factory == null && input == null) {
      return NoNodesNodeSpecifier.INSTANCE;
    }
    if (factory == null) {
      return new NameRegexNodeSpecifierFactory().buildNodeSpecifier(input);
    }
    return NodeSpecifierFactory.load(factory).buildNodeSpecifier(input);
  }

  private SortedSet<ForwardingAction> _actions;

  private String _finalNodesNodeSpecifierFactory;

  private String _finalNodesNodeSpecifierInput;

  private String _forbiddenTransitNodesNodeSpecifierFactory;

  private String _forbiddenTransitNodesNodeSpecifierInput;

  private HeaderSpace _headerSpace;

  private String _sourceIpSpaceSpecifierFactory;

  private String _sourceIpSpaceSpecifierInput;

  private String _sourceLocationSpecifierFactory;

  private String _sourceLocationSpecifierInput;

  private String _requiredTransitNodesNodeSpecifierFactory;

  private String _requiredTransitNodesNodeSpecifierInput;

  public SpecifiersReachabilityQuestion() {}

  @JsonProperty(PROP_ACTIONS)
  public SortedSet<ForwardingAction> getActions() {
    return firstNonNull(_actions, ImmutableSortedSet.of(ForwardingAction.ACCEPT));
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  NodeSpecifier getFinalNodesSpecifier() {
    if (_finalNodesNodeSpecifierFactory == null && _finalNodesNodeSpecifierInput == null) {
      return AllNodesNodeSpecifier.INSTANCE;
    }
    if (_finalNodesNodeSpecifierFactory == null) {
      return new NameRegexNodeSpecifierFactory().buildNodeSpecifier(_finalNodesNodeSpecifierInput);
    }
    return NodeSpecifierFactory.load(_finalNodesNodeSpecifierFactory)
        .buildNodeSpecifier(_finalNodesNodeSpecifierInput);
  }

  @JsonProperty(PROP_FINAL_NODES_SPECIFIER_FACTORY)
  public String getFinalNodesSpecifierFactory() {
    return _finalNodesNodeSpecifierFactory;
  }

  @JsonProperty(PROP_FINAL_NODES_SPECIFIER_INPUT)
  public String getFinalNodesSpecifierInput() {
    return _finalNodesNodeSpecifierInput;
  }

  NodeSpecifier getForbiddenTransitNodesSpecifier() {
    return getNodeSpecifier(
        _forbiddenTransitNodesNodeSpecifierFactory, _forbiddenTransitNodesNodeSpecifierInput);
  }

  @JsonProperty(PROP_HEADERSPACE)
  private HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  @Override
  public String getName() {
    return "specifiersreachability";
  }

  ReachabilityParameters getReachabilityParameters() {
    return ReachabilityParameters.builder()
        .setActions(getActions())
        .setFinalNodesSpecifier(getFinalNodesSpecifier())
        .setForbiddenTransitNodesSpecifier(getForbiddenTransitNodesSpecifier())
        .setHeaderSpace(_headerSpace != null ? _headerSpace : HeaderSpace.builder().build())
        .setRequiredTransitNodesSpecifier(getRequiredTransitNodesSpecifier())
        .setSourceSpecifier(getSourceLocationSpecifier())
        .setSourceIpSpaceSpecifier(getSourceIpSpaceSpecifier())
        .setSpecialize(true)
        .build();
  }

  NodeSpecifier getRequiredTransitNodesSpecifier() {
    return getNodeSpecifier(
        _requiredTransitNodesNodeSpecifierFactory, _requiredTransitNodesNodeSpecifierInput);
  }

  IpSpaceSpecifier getSourceIpSpaceSpecifier() {
    if (_sourceIpSpaceSpecifierFactory == null && _sourceIpSpaceSpecifierInput == null) {
      return InferFromLocationIpSpaceSpecifier.INSTANCE;
    }
    if (_sourceIpSpaceSpecifierFactory == null) {
      return new ConstantWildcardSetIpSpaceSpecifierFactory()
          .buildIpSpaceSpecifier(_sourceIpSpaceSpecifierInput);
    }
    return IpSpaceSpecifierFactory.load(_sourceIpSpaceSpecifierFactory)
        .buildIpSpaceSpecifier(_sourceIpSpaceSpecifierInput);
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY)
  public String getSourceIpSpaceSpecifierFactory() {
    return _sourceIpSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT)
  public String getSourceIpSpaceSpecifierInput() {
    return _sourceIpSpaceSpecifierInput;
  }

  @VisibleForTesting
  LocationSpecifier getSourceLocationSpecifier() {
    if (_sourceLocationSpecifierFactory == null && _sourceLocationSpecifierInput == null) {
      return AllInterfaceLinksLocationSpecifier.INSTANCE;
    }
    if (_sourceLocationSpecifierFactory == null) {
      return new NameRegexInterfaceLinkLocationSpecifierFactory()
          .buildLocationSpecifier(_sourceLocationSpecifierInput);
    }
    return LocationSpecifierFactory.load(_sourceLocationSpecifierFactory)
        .buildLocationSpecifier(_sourceLocationSpecifierInput);
  }

  @JsonProperty(PROP_SOURCE_LOCATION_SPECIFIER_FACTORY)
  public String getSourceLocationSpecifierFactory() {
    return _sourceLocationSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_LOCATION_SPECIFIER_INPUT)
  public String getSourceLocationSpecifierInput() {
    return _sourceLocationSpecifierInput;
  }

  @JsonProperty(PROP_REQUIRED_TRANSIT_NODES_SPECIFIER_FACTORY)
  public String getTransitNodesSpecifierFactory() {
    return _requiredTransitNodesNodeSpecifierFactory;
  }

  @JsonProperty(PROP_REQUIRED_TRANSIT_NODES_SPECIFIER_INPUT)
  public String getTransitNodesSpecifierInput() {
    return _requiredTransitNodesNodeSpecifierInput;
  }

  @JsonProperty(PROP_ACTIONS)
  public void setActions(SortedSet<ForwardingAction> actionSet) {
    _actions = ImmutableSortedSet.copyOf(actionSet);
  }

  @JsonProperty(PROP_FINAL_NODES_SPECIFIER_FACTORY)
  public void setFinalNodesSpecifierFactory(String finalNodesNodeSpecifierFactory) {
    _finalNodesNodeSpecifierFactory = finalNodesNodeSpecifierFactory;
  }

  @JsonProperty(PROP_FINAL_NODES_SPECIFIER_INPUT)
  public void setFinalNodesSpecifierInput(String finalNodesNodeSpecifierInput) {
    _finalNodesNodeSpecifierInput = finalNodesNodeSpecifierInput;
  }

  @JsonProperty(PROP_HEADERSPACE)
  public void setHeaderSpace(HeaderSpace headerSpace) {
    _headerSpace = headerSpace;
  }

  @JsonProperty(PROP_FORBIDDEN_TRANSIT_NODES_NODE_SPECIFIER_FACTORY)
  public void setForbiddenTransitNodesNodeSpecifierFactory(
      String forbiddenTransitNodesNodeSpecifierFactory) {
    _forbiddenTransitNodesNodeSpecifierFactory = forbiddenTransitNodesNodeSpecifierFactory;
  }

  @JsonProperty(PROP_FORBIDDEN_TRANSIT_NODES_NODE_SPECIFIER_INPUT)
  public void setForbiddenTransitNodesNodeSpecifierInput(
      String forbiddenTransitNodesNodeSpecifierInput) {
    _forbiddenTransitNodesNodeSpecifierInput = forbiddenTransitNodesNodeSpecifierInput;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY)
  public void setSourceIpSpaceSpecifierFactory(String ipSpaceSpecifierFactory) {
    _sourceIpSpaceSpecifierFactory = ipSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT)
  public void setSourceIpSpaceSpecifierInput(String ipSpaceSpecifierInput) {
    _sourceIpSpaceSpecifierInput = ipSpaceSpecifierInput;
  }

  @JsonProperty(PROP_SOURCE_LOCATION_SPECIFIER_FACTORY)
  public void setSourceLocationSpecifierFactory(String locationSpecifierFactory) {
    _sourceLocationSpecifierFactory = locationSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_LOCATION_SPECIFIER_INPUT)
  public void setSourceLocationSpecifierInput(String locationSpecifierInput) {
    _sourceLocationSpecifierInput = locationSpecifierInput;
  }

  @JsonProperty(PROP_REQUIRED_TRANSIT_NODES_SPECIFIER_FACTORY)
  public void setRequiredTransitNodesNodeSpecifierFactory(
      String requiredTransitNodesNodeSpecifierFactory) {
    _requiredTransitNodesNodeSpecifierFactory = requiredTransitNodesNodeSpecifierFactory;
  }

  @JsonProperty(PROP_REQUIRED_TRANSIT_NODES_SPECIFIER_INPUT)
  public void setRequiredTransitNodesNodeSpecifierInput(String transitNodesNodeSpecifierInput) {
    _requiredTransitNodesNodeSpecifierInput = transitNodesNodeSpecifierInput;
  }
}
