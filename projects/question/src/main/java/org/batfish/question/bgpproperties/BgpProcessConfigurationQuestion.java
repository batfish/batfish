package org.batfish.question.bgpproperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A question that returns properties of BGP routing processes. {@link #_nodes} and {@link
 * #_properties} determine which nodes and properties are included. The default is to include
 * everything.
 */
@ParametersAreNonnullByDefault
public class BgpProcessConfigurationQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nullable private String _nodes;
  @Nonnull private NodeSpecifier _nodeSpecifier;
  @Nullable private String _properties;
  @Nonnull private BgpProcessPropertySpecifier _propertySpecifier;

  @JsonCreator
  private static BgpProcessConfigurationQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_PROPERTIES) String properties) {
    return new BgpProcessConfigurationQuestion(nodes, properties);
  }

  public BgpProcessConfigurationQuestion(@Nullable String nodes, @Nullable String properties) {
    this(
        nodes,
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        properties,
        BgpProcessPropertySpecifier.create(properties));
  }

  public BgpProcessConfigurationQuestion(
      NodeSpecifier nodeSpecifier, BgpProcessPropertySpecifier properties) {
    this(null, nodeSpecifier, null, properties);
  }

  private BgpProcessConfigurationQuestion(
      @Nullable String nodes,
      NodeSpecifier nodeSpecifier,
      @Nullable String properties,
      BgpProcessPropertySpecifier propertySpecifier) {
    _nodes = nodes;
    _nodeSpecifier = nodeSpecifier;
    _properties = properties;
    _propertySpecifier = propertySpecifier;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "bgpProcessConfiguration";
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
  public BgpProcessPropertySpecifier getPropertySpecifier() {
    return _propertySpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof BgpProcessConfigurationQuestion)) {
      return false;
    }
    BgpProcessConfigurationQuestion that = (BgpProcessConfigurationQuestion) o;
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
