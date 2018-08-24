package org.batfish.question.nodeproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns properties of nodes in a tabular format. {@link
 * NodePropertiesQuestion#_nodes} determines which nodes are included, and {@link
 * NodePropertiesQuestion#_properties} determines which properties are included.
 */
public class NodePropertiesQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nonnull private NodesSpecifier _nodes;

  @Nonnull private NodePropertySpecifier _properties;

  public NodePropertiesQuestion(
      @JsonProperty(PROP_NODES) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_PROPERTIES) NodePropertySpecifier propertySpec) {
    _nodes = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _properties = firstNonNull(propertySpec, NodePropertySpecifier.ALL);
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
  public NodesSpecifier getNodes() {
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
