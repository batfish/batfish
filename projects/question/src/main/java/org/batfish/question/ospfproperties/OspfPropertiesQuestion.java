package org.batfish.question.ospfproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.OspfPropertySpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns properties of OSPF routing processes. {@link #_nodes} and {@link
 * #_properties} determine which nodes and properties are included. The default is to include
 * everything.
 */
public class OspfPropertiesQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROPERTIES = "properties";

  @Nonnull private NodesSpecifier _nodes;
  @Nonnull private OspfPropertySpecifier _properties;

  public OspfPropertiesQuestion(
      @JsonProperty(PROP_NODES) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_PROPERTIES) OspfPropertySpecifier propertySpec) {
    _nodes = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _properties = firstNonNull(propertySpec, OspfPropertySpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "ospfProperties";
  }

  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_PROPERTIES)
  public OspfPropertySpecifier getProperties() {
    return _properties;
  }
}
