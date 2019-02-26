package org.batfish.question.testfilters;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllFiltersFilterSpecifier;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * Computes the fate of the flow at a filter. The set of filters to consider are controlled by
 * 'nodes' and 'filters' fields. By default, all filters on all nodes are considered.
 */
@ParametersAreNonnullByDefault
public class TestFiltersQuestion extends Question {

  private static final String PROP_FILTERS = "filters";
  private static final String PROP_HEADERS = "headers";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_START_LOCATION = "startLocation";

  @Nullable private final String _filters;
  @Nonnull private final PacketHeaderConstraints _headers;
  @Nonnull private final NodeSpecifier _nodes;
  @Nullable private final String _startLocation;

  @JsonCreator
  private static TestFiltersQuestion create(
      @JsonProperty(PROP_NODES) String nodes,
      @JsonProperty(PROP_FILTERS) String filters,
      @JsonProperty(PROP_HEADERS) PacketHeaderConstraints headers,
      @JsonProperty(PROP_START_LOCATION) String startLocation) {
    return new TestFiltersQuestion(
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        filters,
        firstNonNull(headers, PacketHeaderConstraints.unconstrained()),
        startLocation);
  }

  @JsonCreator
  public TestFiltersQuestion(
      NodeSpecifier nodes,
      @Nullable String filters,
      PacketHeaderConstraints headers,
      @Nullable String startLocation) {
    _nodes = nodes;
    _filters = filters;
    _headers = firstNonNull(headers, PacketHeaderConstraints.unconstrained());
    _startLocation = startLocation;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_FILTERS)
  private String getFilters() {
    return _filters;
  }

  @Nonnull
  @JsonIgnore
  public FilterSpecifier getFilterSpecifier() {
    return SpecifierFactories.getFilterSpecifierOrDefault(
        _filters, AllFiltersFilterSpecifier.INSTANCE);
  }

  @Nonnull
  @JsonProperty(PROP_HEADERS)
  public PacketHeaderConstraints getHeaders() {
    return _headers;
  }

  @Override
  public String getName() {
    return "testFilters";
  }

  @JsonProperty(PROP_NODES)
  public NodeSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_START_LOCATION)
  public String getStartLocation() {
    return _startLocation;
  }

  @Nonnull
  @JsonIgnore
  public LocationSpecifier getStartLocationSpecifier() {
    return SpecifierFactories.getLocationSpecifierOrDefault(
        _startLocation, AllInterfacesLocationSpecifier.INSTANCE);
  }
}
