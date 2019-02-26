package org.batfish.question.nodeproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A question that returns properties of nodes in a tabular format. {@link
 * NodePropertiesQuestion#_nodes} determines which nodes are included, and {@link
 * NodePropertiesQuestion#_properties} determines which properties are included.
 */
@ParametersAreNonnullByDefault
public class NodePropertiesQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nonnull private NodeSpecifier _nodes;

  @Nonnull private NodePropertySpecifier _properties;

  @JsonCreator
  private static NodePropertiesQuestion create(
      @JsonProperty(PROP_NODES) String nodes,
      @JsonProperty(PROP_PROPERTIES) NodePropertySpecifier propertySpec) {
    return new NodePropertiesQuestion(
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        firstNonNull(propertySpec, NodePropertySpecifier.ALL));
  }

  public NodePropertiesQuestion(NodeSpecifier nodes, NodePropertySpecifier propertySpec) {
    _nodes = nodes;
    _properties = propertySpec;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "nodeproperties";
  }

  @JsonProperty(PROP_NODES)
  public NodeSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_PROPERTIES)
  public NodePropertySpecifier getProperties() {
    return _properties;
  }

  @Deprecated // backwards compatibility for older questions
  @JsonProperty("properties")
  void setProperties(List<String> properties) {
    _properties = new NodePropertySpecifier(StringUtils.join(properties, "|"));
  }
}
