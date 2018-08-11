package org.batfish.question.interfaceproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns properties of interfaces in a tabular format. {@link #_nodeRegex}, {@link
 * #_interfaceRegex}, and {@link #_propertySpec} determine which nodes, interfaces, and properties
 * are included. The default is to include everything.
 */
public class InterfacePropertiesQuestion extends Question {

  static final boolean DEFAULT_EXCLUDE_SHUT_INTERFACES = false;

  private static final String PROP_EXCLUDE_SHUT_INTERFACES = "excludeShutInterfaces";
  private static final String PROP_INTERFACE_REGEX = "interfaceRegex";
  private static final String PROP_NODE_REGEX = "nodeRegex";
  private static final String PROP_PROPERTY_SPEC = "propertySpec";

  @Nonnull private InterfacesSpecifier _interfaceRegex;
  @Nonnull private NodesSpecifier _nodeRegex;
  private boolean _onlyActive;
  @Nonnull private InterfacePropertySpecifier _propertySpec;

  public InterfacePropertiesQuestion(
      @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES) Boolean excludeShutInterfaces,
      @JsonProperty(PROP_INTERFACE_REGEX) InterfacesSpecifier interfaceRegex,
      @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_PROPERTY_SPEC) InterfacePropertySpecifier propertySpec) {
    _onlyActive = firstNonNull(excludeShutInterfaces, DEFAULT_EXCLUDE_SHUT_INTERFACES);
    _interfaceRegex = firstNonNull(interfaceRegex, InterfacesSpecifier.ALL);
    _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _propertySpec = firstNonNull(propertySpec, InterfacePropertySpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "interfaceproperties";
  }

  @JsonProperty(PROP_EXCLUDE_SHUT_INTERFACES)
  public boolean getOnlyActive() {
    return _onlyActive;
  }

  @JsonProperty(PROP_INTERFACE_REGEX)
  public InterfacesSpecifier getInterfaceRegex() {
    return _interfaceRegex;
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
  }

  @JsonProperty(PROP_PROPERTY_SPEC)
  public InterfacePropertySpecifier getPropertySpec() {
    return _propertySpec;
  }
}
