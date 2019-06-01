package org.batfish.question.ospfprocess;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.OspfPropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question that returns a table with all OSPF processes configurations */
@ParametersAreNonnullByDefault
public final class OspfProcessConfigurationQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nullable private String _nodes;
  @Nonnull private NodeSpecifier _nodeSpecifier;
  @Nonnull private OspfPropertySpecifier _properties;

  @JsonCreator
  private static OspfProcessConfigurationQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_PROPERTIES) @Nullable OspfPropertySpecifier propertySpec) {
    return new OspfProcessConfigurationQuestion(
        nodes,
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        firstNonNull(propertySpec, OspfPropertySpecifier.ALL));
  }

  public OspfProcessConfigurationQuestion(
      NodeSpecifier nodeSpecifier, OspfPropertySpecifier propertySpec) {
    this(null, nodeSpecifier, propertySpec);
  }

  private OspfProcessConfigurationQuestion(
      @Nullable String nodes, NodeSpecifier nodeSpecifier, OspfPropertySpecifier propertySpec) {
    _nodes = nodes;
    _nodeSpecifier = nodeSpecifier;
    _properties = firstNonNull(propertySpec, OspfPropertySpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "ospfProcessConfiguration";
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodesSpecifier() {
    return _nodeSpecifier;
  }

  @Nonnull
  @JsonProperty(PROP_PROPERTIES)
  public OspfPropertySpecifier getProperties() {
    return _properties;
  }
}
