package org.batfish.question.namedstructures;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.batfish.datamodel.questions.NamedStructureSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns properties of nodes in a tabular format. {@link
 * NamedStructuresQuestion#_nodeRegex} determines which nodes are included, and {@link
 * NamedStructuresQuestion#_propertySpec} determines which properties are included.
 */
public class NamedStructuresQuestion extends Question {

  private static final String PROP_NODE_REGEX = "nodeRegex";
  private static final String PROP_PROPERTY_SPEC = "propertySpec";

  private final NodesSpecifier _nodeRegex;

  @Nonnull private NamedStructureSpecifier _propertySpec;

  public NamedStructuresQuestion(
      @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_PROPERTY_SPEC) NamedStructureSpecifier propertySpec) {
    _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _propertySpec = firstNonNull(propertySpec, NamedStructureSpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "namedstructures";
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
  }

  @JsonProperty(PROP_PROPERTY_SPEC)
  public NamedStructureSpecifier getPropertySpec() {
    return _propertySpec;
  }

  @Deprecated // backwards compatibility for older questions
  @JsonProperty("properties")
  void setProperties(List<String> properties) {
    _propertySpec = new NamedStructureSpecifier(StringUtils.join(properties, "|"));
  }
}
