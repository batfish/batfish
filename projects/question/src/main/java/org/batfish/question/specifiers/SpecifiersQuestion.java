package org.batfish.question.specifiers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;
import org.batfish.specifier.FlexibleInterfaceSpecifierFactory;
import org.batfish.specifier.FlexibleLocationSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifierFactory;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.InterfaceSpecifierFactory;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationSpecifierFactory;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/**
 * Allows users to see how different specifiers ({@link LocationSpecifier}, {@link
 * IpSpaceSpecifier}, {@link NodeSpecifier}, {@link FilterSpecifier}, and {@link
 * InterfaceSpecifier}) are resolved.
 */
public final class SpecifiersQuestion extends Question {

  public enum QueryType {
    FILTER,
    INTERFACE,
    IP_SPACE,
    LOCATION,
    NODE
  }

  private static final String PROP_FILTER_SPECIFIER_FACTORY = "filterSpecifierFactory";
  private static final String PROP_INTERFACE_SPECIFIER_FACTORY = "interfaceSpecifierFactory";
  private static final String PROP_IP_SPACE_SPECIFIER_FACTORY = "ipSpaceSpecifierFactory";
  private static final String PROP_LOCATION_SPECIFIER_FACTORY = "locationSpecifierFactory";
  private static final String PROP_NODE_SPECIFIER_FACTORY = "nodeSpecifierFactory";

  private static final String PROP_FILTER_SPECIFIER_INPUT = "filterSpecifierInput";
  private static final String PROP_INTERFACE_SPECIFIER_INPUT = "interfaceSpecifierInput";
  private static final String PROP_IP_SPACE_SPECIFIER_INPUT = "ipSpaceSpecifierInput";
  private static final String PROP_LOCATION_SPECIFIER_INPUT = "locationSpecifierInput";
  private static final String PROP_NODE_SPECIFIER_INPUT = "nodeSpecifierInput";

  private static final String PROP_QUERY_TYPE = "queryType";

  private String _filterSpecifierFactory = FlexibleFilterSpecifierFactory.NAME;
  private String _interfaceSpecifierFactory = FlexibleInterfaceSpecifierFactory.NAME;
  private String _ipSpaceSpecifierFactory =
      InferFromLocationIpSpaceSpecifierFactory
          .NAME; // reasonable default (perhaps we need a unique entry factory)?
  private String _locationSpecifierFactory = FlexibleLocationSpecifierFactory.NAME;
  private String _nodeSpecifierFactory = FlexibleNodeSpecifierFactory.NAME;

  @Nullable private String _filterSpecifierInput;
  @Nullable private String _interfaceSpecifierInput;
  @Nullable private String _ipSpaceSpecifierInput;
  @Nullable private String _locationSpecifierInput;
  @Nullable private String _nodeSpecifierInput;

  @Nonnull private QueryType _queryType;

  public SpecifiersQuestion(@JsonProperty(PROP_QUERY_TYPE) QueryType queryType) {
    checkArgument(queryType != null, "'queryType must be specified");
    _queryType = queryType;
  }

  public FilterSpecifier getFilterSpecifier() {
    checkNotNull(_filterSpecifierFactory, PROP_FILTER_SPECIFIER_FACTORY + " is null");
    return FilterSpecifierFactory.load(_filterSpecifierFactory)
        .buildFilterSpecifier(_filterSpecifierInput);
  }

  public InterfaceSpecifier getInterfaceSpecifier() {
    checkNotNull(_interfaceSpecifierFactory, PROP_INTERFACE_SPECIFIER_FACTORY + " is null");
    return InterfaceSpecifierFactory.load(_interfaceSpecifierFactory)
        .buildInterfaceSpecifier(_interfaceSpecifierInput);
  }

  public IpSpaceSpecifier getIpSpaceSpecifier() {
    checkNotNull(_ipSpaceSpecifierFactory, PROP_IP_SPACE_SPECIFIER_FACTORY + " is null");
    return IpSpaceSpecifierFactory.load(_ipSpaceSpecifierFactory)
        .buildIpSpaceSpecifier(_ipSpaceSpecifierInput);
  }

  public LocationSpecifier getLocationSpecifier() {
    checkNotNull(_locationSpecifierFactory, PROP_LOCATION_SPECIFIER_FACTORY + " is null");
    return LocationSpecifierFactory.load(_locationSpecifierFactory)
        .buildLocationSpecifier(_locationSpecifierInput);
  }

  public NodeSpecifier getNodeSpecifier() {
    checkNotNull(_nodeSpecifierFactory, PROP_NODE_SPECIFIER_FACTORY + " is null");
    return NodeSpecifierFactory.load(_nodeSpecifierFactory).buildNodeSpecifier(_nodeSpecifierInput);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "specifiers";
  }

  @JsonProperty(PROP_FILTER_SPECIFIER_FACTORY)
  public String getFilterSpecifierFactory() {
    return _filterSpecifierFactory;
  }

  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  public String getFilterSpecifierInput() {
    return _filterSpecifierInput;
  }

  @JsonProperty(PROP_INTERFACE_SPECIFIER_FACTORY)
  public String getInterfaceSpecifierFactory() {
    return _interfaceSpecifierFactory;
  }

  @JsonProperty(PROP_INTERFACE_SPECIFIER_INPUT)
  public String getInterfaceSpecifierInput() {
    return _interfaceSpecifierInput;
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_FACTORY)
  public String getIpSpaceSpecifierFactory() {
    return _ipSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_INPUT)
  public String getIpSpaceSpecifierInput() {
    return _ipSpaceSpecifierInput;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_FACTORY)
  public String getLocationSpecifierFactory() {
    return _locationSpecifierFactory;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_INPUT)
  public String getLocationSpecifierInput() {
    return _locationSpecifierInput;
  }

  @JsonProperty(PROP_NODE_SPECIFIER_FACTORY)
  public String getNodeSpecifierFactory() {
    return _nodeSpecifierFactory;
  }

  @JsonProperty(PROP_NODE_SPECIFIER_INPUT)
  public String getNodeSpecifierInput() {
    return _nodeSpecifierInput;
  }

  @JsonProperty(PROP_QUERY_TYPE)
  public QueryType getQueryType() {
    return _queryType;
  }

  @JsonProperty(PROP_FILTER_SPECIFIER_FACTORY)
  public void setFilterSpecifierFactory(String filterSpecifierFactory) {
    _filterSpecifierFactory = filterSpecifierFactory;
  }

  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  public void setFilterSpecifierInput(String filterSpecifierInput) {
    _filterSpecifierInput = filterSpecifierInput;
  }

  @JsonProperty(PROP_INTERFACE_SPECIFIER_FACTORY)
  public void setInterfaceSpecifierFactory(String interfaceSpecifierFactory) {
    _interfaceSpecifierFactory = interfaceSpecifierFactory;
  }

  @JsonProperty(PROP_INTERFACE_SPECIFIER_INPUT)
  public void setInterfaceSpecifierInput(String interfaceSpecifierInput) {
    _interfaceSpecifierInput = interfaceSpecifierInput;
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_FACTORY)
  public void setIpSpaceSpecifierFactory(String ipSpaceSpecifierFactory) {
    _ipSpaceSpecifierFactory = ipSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_IP_SPACE_SPECIFIER_INPUT)
  public void setIpSpaceSpecifierInput(String ipSpaceSpecifierInput) {
    _ipSpaceSpecifierInput = ipSpaceSpecifierInput;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_FACTORY)
  public void setLocationSpecifierFactory(String locationSpecifierFactory) {
    _locationSpecifierFactory = locationSpecifierFactory;
  }

  @JsonProperty(PROP_LOCATION_SPECIFIER_INPUT)
  public void setLocationSpecifierInput(String locationSpecifierInput) {
    _locationSpecifierInput = locationSpecifierInput;
  }

  @JsonProperty(PROP_NODE_SPECIFIER_FACTORY)
  public void setNodeSpecifierFactory(String nodeSpecifierFactory) {
    _nodeSpecifierFactory = nodeSpecifierFactory;
  }

  @JsonProperty(PROP_NODE_SPECIFIER_INPUT)
  public void setNodeSpecifierInput(String nodeSpecifierInput) {
    _nodeSpecifierInput = nodeSpecifierInput;
  }
}
