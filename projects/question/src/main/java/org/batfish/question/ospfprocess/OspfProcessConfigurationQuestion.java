package org.batfish.question.ospfprocess;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.OspfProcessPropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question that returns a table with all OSPF processes configurations */
@ParametersAreNonnullByDefault
public final class OspfProcessConfigurationQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nullable private final String _nodes;

  @Nonnull private final NodeSpecifier _nodeSpecifier;

  @Nullable private final String _properties;

  @Nonnull private final OspfProcessPropertySpecifier _propertySpecifier;

  @JsonCreator
  private static OspfProcessConfigurationQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_PROPERTIES) @Nullable String properties) {
    return new OspfProcessConfigurationQuestion(nodes, properties);
  }

  public OspfProcessConfigurationQuestion(@Nullable String nodes, @Nullable String properties) {
    this(
        nodes,
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        properties,
        OspfProcessPropertySpecifier.create(properties));
  }

  public OspfProcessConfigurationQuestion(
      NodeSpecifier nodeSpecifier, OspfProcessPropertySpecifier propertySpec) {
    this(null, nodeSpecifier, null, propertySpec);
  }

  private OspfProcessConfigurationQuestion(
      @Nullable String nodes,
      NodeSpecifier nodeSpecifier,
      @Nullable String properties,
      OspfProcessPropertySpecifier propertySpecifier) {
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

  @Nullable
  @JsonProperty(PROP_PROPERTIES)
  public String getProperties() {
    return _properties;
  }

  @Nonnull
  @JsonIgnore
  public OspfProcessPropertySpecifier getPropertySpecifier() {
    return _propertySpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof OspfProcessConfigurationQuestion)) {
      return false;
    }
    OspfProcessConfigurationQuestion that = (OspfProcessConfigurationQuestion) o;
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
