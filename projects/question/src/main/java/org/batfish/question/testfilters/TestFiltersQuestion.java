package org.batfish.question.testfilters;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class TestFiltersQuestion extends Question {
  private static final String PROP_FILTERS = "filters";
  private static final String PROP_HEADERS = "headers";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_START_LOCATION = "startLocation";

  @Nullable private final String _filters;
  @Nonnull private final PacketHeaderConstraints _headers;
  @Nullable private final String _nodes;
  @Nullable private final String _startLocation;

  @JsonCreator
  public TestFiltersQuestion(
      @JsonProperty(PROP_NODES) String nodes,
      @JsonProperty(PROP_FILTERS) String filters,
      @JsonProperty(PROP_HEADERS) PacketHeaderConstraints headers,
      @JsonProperty(PROP_START_LOCATION) String startLocation) {
    _nodes = nodes;
    _filters = filters;
    _headers = firstNonNull(headers, PacketHeaderConstraints.unconstrained());
    _startLocation = startLocation;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Nullable
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
  @JsonIgnore
  public NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
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

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
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
