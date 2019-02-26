package org.batfish.question.bgpproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
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
public class BgpProcessConfigurationQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nonnull private NodeSpecifier _nodes;
  @Nonnull private BgpProcessPropertySpecifier _properties;

  public BgpProcessConfigurationQuestion(
      @JsonProperty(PROP_NODES) String nodes,
      @JsonProperty(PROP_PROPERTIES) BgpProcessPropertySpecifier propertySpec) {
    _nodes = SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE);
    _properties = firstNonNull(propertySpec, BgpProcessPropertySpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "bgpProcessConfiguration";
  }

  @JsonProperty(PROP_NODES)
  public NodeSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_PROPERTIES)
  public BgpProcessPropertySpecifier getProperties() {
    return _properties;
  }
}
