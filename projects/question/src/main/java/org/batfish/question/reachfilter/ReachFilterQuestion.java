package org.batfish.question.reachfilter;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.DENY;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.MATCH_LINE;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.PERMIT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ReachFilterParameters;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;
import org.batfish.specifier.FlexibleUniverseIpSpaceSpecifierFactory;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;

/** A question to determine which flows match a particular ACL action. */
public final class ReachFilterQuestion extends Question {

  private static final String DEFAULT_DST_IP_SPECIFIER_FACTORY =
      FlexibleUniverseIpSpaceSpecifierFactory.NAME;

  private static final String DEFAULT_SRC_IP_SPECIFIER_FACTORY =
      FlexibleUniverseIpSpaceSpecifierFactory.NAME;

  private static final String FILTER_SPECIFIER_FACTORY = FlexibleFilterSpecifierFactory.NAME;

  private static final String PROP_FILTER_SPECIFIER_INPUT = "filterRegex";

  private static final String PROP_NODES_SPECIFIER_NAME = "nodeRegex";

  // TODO: this should probably be "action" for consistency
  private static final String PROP_QUERY = "query";

  private static final String PROP_DESTINATION_IP_SPACE_SPECIFIER_FACTORY =
      "destinationIpSpaceSpecifierFactory";

  private static final String PROP_DESTINATION_IP_SPACE_SPECIFIER_INPUT = "dst";

  private static final String PROP_DST_PORTS = "dstPorts";

  private static final String PROP_DST_PROTOCOLS = "dstProtocols";

  private static final String PROP_SRC_PORTS = "srcPorts";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY =
      "sourceIpSpaceSpecifierFactory";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT = "src";

  public enum Type {
    PERMIT,
    DENY,
    MATCH_LINE
  }

  // Invariant: null unless _type == MATCH_LINE
  @Nullable private Integer _lineNumber;

  @Nullable private String _filterSpecifierInput;

  @Nonnull private NodesSpecifier _nodesSpecifier;

  @Nonnull private final String _destinationIpSpaceSpecifierFactory;

  @Nullable private final String _destinationIpSpaceSpecifierInput;

  @Nonnull private final SortedSet<SubRange> _dstPorts;

  @Nonnull private final SortedSet<SubRange> _srcPorts;

  @Nonnull private final SortedSet<Protocol> _dstProtocols;

  @Nonnull private final String _sourceIpSpaceSpecifierFactory;

  @Nullable private final String _sourceIpSpaceSpecifierInput;

  @Nonnull private Type _type = PERMIT;

  @JsonCreator
  private ReachFilterQuestion(
      @JsonProperty(PROP_FILTER_SPECIFIER_INPUT) @Nullable String filterSpecifierInput,
      @JsonProperty(PROP_NODES_SPECIFIER_NAME) @Nullable NodesSpecifier nodesSpecifier,
      @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_FACTORY) @Nullable
          String destinationIpSpaceSpecifierFactory,
      @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_INPUT) @Nullable
          String destinationIpSpaceSpecifierInput,
      @JsonProperty(PROP_DST_PORTS) @Nullable SortedSet<SubRange> dstPorts,
      @JsonProperty(PROP_SRC_PORTS) @Nullable SortedSet<SubRange> srcPorts,
      @JsonProperty(PROP_DST_PROTOCOLS) @Nullable SortedSet<Protocol> dstProtocols,
      @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY) @Nullable
          String sourceIpSpaceSpecifierFactory,
      @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT) @Nullable
          String sourceIpSpaceSpecifierInput,
      @JsonProperty(PROP_QUERY) @Nullable String type) {
    _filterSpecifierInput = filterSpecifierInput;
    _nodesSpecifier = firstNonNull(nodesSpecifier, NodesSpecifier.ALL);
    _destinationIpSpaceSpecifierFactory =
        firstNonNull(destinationIpSpaceSpecifierFactory, DEFAULT_DST_IP_SPECIFIER_FACTORY);
    _destinationIpSpaceSpecifierInput = destinationIpSpaceSpecifierInput;
    _dstPorts = firstNonNull(dstPorts, ImmutableSortedSet.of());
    _srcPorts = firstNonNull(srcPorts, ImmutableSortedSet.of());
    _dstProtocols = firstNonNull(dstProtocols, ImmutableSortedSet.of());
    _sourceIpSpaceSpecifierFactory =
        firstNonNull(sourceIpSpaceSpecifierFactory, DEFAULT_SRC_IP_SPECIFIER_FACTORY);
    _sourceIpSpaceSpecifierInput = sourceIpSpaceSpecifierInput;
    setQuery(firstNonNull(type, "permit"));
  }

  ReachFilterQuestion() {
    this(null, null, null, null, null, null, null, null, null, null);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "reachfilter";
  }

  @Nonnull
  @JsonIgnore
  public FilterSpecifier getFilterSpecifier() {
    return FilterSpecifierFactory.load(FILTER_SPECIFIER_FACTORY)
        .buildFilterSpecifier(_filterSpecifierInput);
  }

  @Nullable
  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  private String getFilterSpecifierInput() {
    return _filterSpecifierInput;
  }

  @Nonnull
  @JsonProperty(PROP_NODES_SPECIFIER_NAME)
  public NodesSpecifier getNodesSpecifier() {
    return _nodesSpecifier;
  }

  @JsonIgnore
  @Nullable
  public Integer getLineNumber() {
    return _lineNumber;
  }

  @JsonIgnore
  public Type getType() {
    return _type;
  }

  @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_FACTORY)
  public String getDestinationIpSpaceSpecifierFactory() {
    return _destinationIpSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_INPUT)
  @Nullable
  public String getDestinationIpSpaceSpecifierInput() {
    return _destinationIpSpaceSpecifierInput;
  }

  @JsonProperty(PROP_DST_PORTS)
  public SortedSet<SubRange> getDstPorts() {
    return _dstPorts;
  }

  @JsonProperty(PROP_DST_PROTOCOLS)
  public SortedSet<Protocol> getDstProtocols() {
    return _dstProtocols;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY)
  public String getSourceIpSpaceSpecifierFactory() {
    return _sourceIpSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT)
  @Nullable
  public String getSourceIpSpaceSpecifierInput() {
    return _sourceIpSpaceSpecifierInput;
  }

  @JsonProperty(PROP_SRC_PORTS)
  public SortedSet<SubRange> getSrcPorts() {
    return _srcPorts;
  }

  @Nonnull
  private IpSpaceSpecifier getDestinationSpecifier() {
    return IpSpaceSpecifierFactory.load(_destinationIpSpaceSpecifierFactory)
        .buildIpSpaceSpecifier(_destinationIpSpaceSpecifierInput);
  }

  @Nonnull
  private IpSpaceSpecifier getSourceSpecifier() {
    return IpSpaceSpecifierFactory.load(_destinationIpSpaceSpecifierFactory)
        .buildIpSpaceSpecifier(_sourceIpSpaceSpecifierInput);
  }

  @VisibleForTesting
  @Nonnull
  HeaderSpace getHeaderSpace() {
    return HeaderSpace.builder()
        .setDstPorts(_dstPorts)
        .setDstProtocols(_dstProtocols)
        .setSrcPorts(_srcPorts)
        // src and dst protocols can't be different for ACLs
        .setSrcProtocols(_dstProtocols)
        .build();
  }

  void setFilterSpecifierInput(@Nullable String filterSpecifierInput) {
    _filterSpecifierInput = filterSpecifierInput;
  }

  void setNodesSpecifier(@Nonnull NodesSpecifier nodesSpecifier) {
    _nodesSpecifier = nodesSpecifier;
  }

  @JsonProperty(PROP_QUERY)
  public void setQuery(String query) {
    if (query.equals("permit")) {
      _type = PERMIT;
      _lineNumber = null;
    } else if (query.equals("deny")) {
      _type = DENY;
      _lineNumber = null;
    } else if (query.startsWith("matchLine")) {
      _type = MATCH_LINE;
      _lineNumber = Integer.parseInt(query.substring("matchLine".length()).trim());
    } else {
      throw new BatfishException("Unrecognized query: " + query);
    }
  }

  @Nonnull
  @VisibleForTesting
  ReachFilterParameters toReachFilterParameters() {
    return ReachFilterParameters.builder()
        .setHeaderSpace(getHeaderSpace())
        .setSourceIpSpaceSpecifier(getSourceSpecifier())
        .setDestinationIpSpaceSpecifier(getDestinationSpecifier())
        .build();
  }
}
