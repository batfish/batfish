package org.batfish.question.bgpproperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A question that returns properties of BGP routing processes. {@link #_nodes} determines which
 * nodes are included. The default is to include everything.
 */
@ParametersAreNonnullByDefault
public class BgpPeerConfigurationQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nullable private final String _nodes;
  @Nullable private final String _properties;

  @Nonnull private BgpPeerPropertySpecifier _propertySpecifier;

  @JsonCreator
  private static BgpPeerConfigurationQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_PROPERTIES) @Nullable String properties) {
    return new BgpPeerConfigurationQuestion(nodes, properties);
  }

  public BgpPeerConfigurationQuestion(@Nullable String nodes, @Nullable String properties) {
    this(nodes, properties, BgpPeerPropertySpecifier.create(properties));
  }

  public BgpPeerConfigurationQuestion(
      @Nullable String nodes, BgpPeerPropertySpecifier propertySpecifier) {
    this(nodes, null, propertySpecifier);
  }

  private BgpPeerConfigurationQuestion(
      @Nullable String nodes,
      @Nullable String properties,
      BgpPeerPropertySpecifier propertySpecifier) {
    _nodes = nodes;
    _properties = properties;
    _propertySpecifier = propertySpecifier;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "bgpPeerConfiguration";
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_PROPERTIES)
  @Nullable
  public String getProperties() {
    return _properties;
  }

  @JsonIgnore
  @Nonnull
  public BgpPeerPropertySpecifier getPropertySpecifier() {
    return _propertySpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof BgpPeerConfigurationQuestion)) {
      return false;
    }
    BgpPeerConfigurationQuestion that = (BgpPeerConfigurationQuestion) o;
    return Objects.equals(_nodes, that._nodes)
        && Objects.equals(_properties, that._properties)
        && Objects.equals(_propertySpecifier, that._propertySpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodes, _properties, _propertySpecifier);
  }
}
