package org.batfish.question.nodeproperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A question that returns properties of nodes in a tabular format. {@link
 * NodePropertiesQuestion#_nodes} determines which nodes are included, and {@link
 * NodePropertiesQuestion#_propertySpecifier} determines which properties are included.
 */
@ParametersAreNonnullByDefault
public class NodePropertiesQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nullable private String _nodes;

  @Nonnull private NodeSpecifier _nodeSpecifier;

  @Nullable private String _properties;

  @Nonnull private NodePropertySpecifier _propertySpecifier;

  @JsonCreator
  private static NodePropertiesQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_PROPERTIES) String properties) {
    return new NodePropertiesQuestion(nodes, properties);
  }

  public NodePropertiesQuestion(@Nullable String nodes, @Nullable String properties) {
    this(
        nodes,
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        properties,
        NodePropertySpecifier.create(properties));
  }

  public NodePropertiesQuestion(NodeSpecifier nodeSpecifier, NodePropertySpecifier propertySpec) {
    this(null, nodeSpecifier, null, propertySpec);
  }

  private NodePropertiesQuestion(
      @Nullable String nodes,
      NodeSpecifier nodeSpecifier,
      @Nullable String properties,
      NodePropertySpecifier propertySpec) {
    _nodes = nodes;
    _nodeSpecifier = nodeSpecifier;
    _properties = properties;
    _propertySpecifier = propertySpec;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "nodeproperties";
  }

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @Nonnull
  @JsonIgnore
  public NodeSpecifier getNodeSpecifier() {
    return _nodeSpecifier;
  }

  @Nullable
  @JsonProperty(PROP_PROPERTIES)
  public String getProperties() {
    return _properties;
  }

  @Nonnull
  @JsonIgnore
  public NodePropertySpecifier getPropertySpecifier() {
    return _propertySpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof NodePropertiesQuestion)) {
      return false;
    }
    NodePropertiesQuestion that = (NodePropertiesQuestion) o;
    return Objects.equals(_nodes, that._nodes)
        && Objects.equals(_nodeSpecifier, that._nodeSpecifier)
        && Objects.equals(_properties, that._properties)
        && Objects.equals(_propertySpecifier, that._propertySpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodes, _nodeSpecifier, _properties, _propertySpecifier);
  }
}
