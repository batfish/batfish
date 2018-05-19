package org.batfish.question.nodeproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns properties of nodes in a tabular format. {@link
 * NodePropertiesQuestion#_nodeRegex} determines which nodes are included, and {@link
 * NodePropertiesQuestion#_properties} determines which properties are included.
 */
public class NodePropertiesQuestion extends Question {

  private static final String PROP_NODE_REGEX = "nodeRegex";
  private static final String PROP_PROPERTIES = "properties";

  @Nonnull private NodesSpecifier _nodeRegex;

  @Nonnull private List<NodePropertySpecifier> _properties;

  public NodePropertiesQuestion(
      @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_PROPERTIES) List<NodePropertySpecifier> properties) {
    _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _properties =
        firstNonNull(
            properties,
            NodePropertySpecifier.JAVA_MAP
                .keySet()
                .stream()
                .map(p -> new NodePropertySpecifier(p))
                .collect(Collectors.toList()));
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "nodeproperties";
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
  }

  @JsonProperty(PROP_PROPERTIES)
  public List<NodePropertySpecifier> getProperties() {
    return _properties;
  }
}
