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
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ReachFilterParameters;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;
import org.batfish.specifier.FlexibleLocationSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.FlexibleUniverseIpSpaceSpecifierFactory;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/** A question to determine which flows match a particular ACL action. */
public final class ReachFilterQuestion extends Question {

  private static final String DEFAULT_DST_IP_SPECIFIER_FACTORY =
      FlexibleUniverseIpSpaceSpecifierFactory.NAME;

  private static final String DEFAULT_SRC_IP_SPECIFIER_FACTORY =
      FlexibleUniverseIpSpaceSpecifierFactory.NAME;

  private static final String FILTER_SPECIFIER_FACTORY = FlexibleFilterSpecifierFactory.NAME;

  private static final String PROP_FILTERS = "filters";

  private static final String PROP_COMPLEMENT_HEADERSPACE = "complementHeaderSpace";

  private static final String PROP_DESTINATION_IP_SPACE_SPECIFIER_FACTORY =
      "destinationIpSpaceSpecifierFactory";

  private static final String PROP_DESTINATION_IP_SPACE_SPECIFIER_INPUT = "dst";

  private static final String PROP_DST_PORTS = "dstPorts";

  private static final String PROP_DST_PROTOCOLS = "dstProtocols";

  private static final String PROP_NODE_SPECIFIER_FACTORY = "nodeSpecifierFactory";

  private static final String PROP_NODES = "nodes";

  private static final String PROP_IP_PROTOCOLS = "ipProtocols";

  private static final String PROP_SRC_PORTS = "srcPorts";

  private static final String PROP_START_LOCATION = "start";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY =
      "sourceIpSpaceSpecifierFactory";

  private static final String PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT = "src";

  // TODO: this should probably be "action" for consistency
  private static final String PROP_QUERY = "query";

  public enum Type {
    PERMIT,
    DENY,
    MATCH_LINE
  }

  private final boolean _complementHeaderSpace;

  // Invariant: null unless _type == MATCH_LINE
  @Nullable private Integer _lineNumber;

  @Nullable private final String _filters;

  @Nonnull private final String _nodeSpecifierFactory;

  @Nullable private final String _nodes;

  @Nonnull private final String _destinationIpSpaceSpecifierFactory;

  @Nullable private final String _destinationIpSpaceSpecifierInput;

  @Nonnull private final SortedSet<SubRange> _dstPorts;

  @Nonnull private final SortedSet<Protocol> _dstProtocols;

  @Nonnull private final SortedSet<IpProtocol> _ipProtocols;

  @Nullable private final String _start;

  @Nonnull private final String _sourceIpSpaceSpecifierFactory;

  @Nullable private final String _sourceIpSpaceSpecifierInput;

  @Nonnull private final SortedSet<SubRange> _srcPorts;

  @Nonnull private Type _type = PERMIT;

  @JsonCreator
  private ReachFilterQuestion(
      @JsonProperty(PROP_COMPLEMENT_HEADERSPACE) boolean complementHeaderSpace,
      @JsonProperty(PROP_FILTERS) @Nullable String filters,
      @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_FACTORY) @Nullable
          String destinationIpSpaceSpecifierFactory,
      @JsonProperty(PROP_DESTINATION_IP_SPACE_SPECIFIER_INPUT) @Nullable
          String destinationIpSpaceSpecifierInput,
      @JsonProperty(PROP_DST_PORTS) @Nullable SortedSet<SubRange> dstPorts,
      @JsonProperty(PROP_DST_PROTOCOLS) @Nullable SortedSet<Protocol> dstProtocols,
      @JsonProperty(PROP_IP_PROTOCOLS) @Nullable SortedSet<IpProtocol> ipProtocols,
      @JsonProperty(PROP_NODE_SPECIFIER_FACTORY) @Nullable String nodeSpecifierFactory,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_START_LOCATION) @Nullable String start,
      @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY) @Nullable
          String sourceIpSpaceSpecifierFactory,
      @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT) @Nullable
          String sourceIpSpaceSpecifierInput,
      @JsonProperty(PROP_SRC_PORTS) @Nullable SortedSet<SubRange> srcPorts,
      @JsonProperty(PROP_QUERY) @Nullable String type) {
    _complementHeaderSpace = complementHeaderSpace;
    _filters = filters;
    _nodeSpecifierFactory = firstNonNull(nodeSpecifierFactory, FlexibleNodeSpecifierFactory.NAME);
    _nodes = nodes;
    _destinationIpSpaceSpecifierFactory =
        firstNonNull(destinationIpSpaceSpecifierFactory, DEFAULT_DST_IP_SPECIFIER_FACTORY);
    _destinationIpSpaceSpecifierInput = destinationIpSpaceSpecifierInput;
    _dstPorts = firstNonNull(dstPorts, ImmutableSortedSet.of());
    _dstProtocols = firstNonNull(dstProtocols, ImmutableSortedSet.of());
    _ipProtocols = firstNonNull(ipProtocols, ImmutableSortedSet.of());
    _start = start;
    _sourceIpSpaceSpecifierFactory =
        firstNonNull(sourceIpSpaceSpecifierFactory, DEFAULT_SRC_IP_SPECIFIER_FACTORY);
    _sourceIpSpaceSpecifierInput = sourceIpSpaceSpecifierInput;
    _srcPorts = firstNonNull(srcPorts, ImmutableSortedSet.of());
    setQuery(firstNonNull(type, "permit"));
  }

  ReachFilterQuestion() {
    this(false, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  @JsonProperty(PROP_COMPLEMENT_HEADERSPACE)
  public boolean getComplementHeaderSpace() {
    return _complementHeaderSpace;
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
    return FilterSpecifierFactory.load(FILTER_SPECIFIER_FACTORY).buildFilterSpecifier(_filters);
  }

  @Nullable
  @JsonProperty(PROP_FILTERS)
  private String getFilters() {
    return _filters;
  }

  @JsonProperty(PROP_NODE_SPECIFIER_FACTORY)
  @Nonnull
  public String getNodeSpecifierFactory() {
    return _nodeSpecifierFactory;
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodes() {
    return _nodes;
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

  @Nonnull
  @JsonProperty(PROP_DST_PORTS)
  public SortedSet<SubRange> getDstPorts() {
    return _dstPorts;
  }

  @Nonnull
  @JsonProperty(PROP_DST_PROTOCOLS)
  public SortedSet<Protocol> getDstProtocols() {
    return _dstProtocols;
  }

  @Nonnull
  @JsonProperty(PROP_IP_PROTOCOLS)
  public SortedSet<IpProtocol> getIpProtocols() {
    return _ipProtocols;
  }

  @Nonnull
  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_FACTORY)
  public String getSourceIpSpaceSpecifierFactory() {
    return _sourceIpSpaceSpecifierFactory;
  }

  @JsonProperty(PROP_SOURCE_IP_SPACE_SPECIFIER_INPUT)
  @Nullable
  public String getSourceIpSpaceSpecifierInput() {
    return _sourceIpSpaceSpecifierInput;
  }

  @Nonnull
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
  private LocationSpecifier getStartLocationSpecifier() {
    return new FlexibleLocationSpecifierFactory().buildLocationSpecifier(_start);
  }

  @Nonnull
  private IpSpaceSpecifier getSourceSpecifier() {
    return IpSpaceSpecifierFactory.load(_destinationIpSpaceSpecifierFactory)
        .buildIpSpaceSpecifier(_sourceIpSpaceSpecifierInput);
  }

  @Nonnull
  NodeSpecifier getNodesSpecifier() {
    return NodeSpecifierFactory.load(_nodeSpecifierFactory).buildNodeSpecifier(_nodes);
  }

  @VisibleForTesting
  @Nonnull
  HeaderSpace getHeaderSpace() {
    return HeaderSpace.builder()
        .setDstPorts(_dstPorts)
        .setDstProtocols(_dstProtocols)
        .setIpProtocols(_ipProtocols)
        .setSrcPorts(_srcPorts)
        .setNegate(_complementHeaderSpace)
        .build();
  }

  @JsonProperty(PROP_QUERY)
  private void setQuery(String query) {
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
        .setStartLocationSpecifier(getStartLocationSpecifier())
        .setSourceIpSpaceSpecifier(getSourceSpecifier())
        .setDestinationIpSpaceSpecifier(getDestinationSpecifier())
        .build();
  }

  static Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  static final class Builder {
    private boolean _complementHeaderSpace;
    private String _filterSpecifierInput;
    private String _nodeSpecifierFactory;
    private String _nodeSpecifierInput;
    private String _destinationIpSpaceSpecifierFactory;
    private String _destinationIpSpaceSpecifierInput;
    private SortedSet<SubRange> _dstPorts;
    private SortedSet<Protocol> _dstProtocols;
    private SortedSet<IpProtocol> _ipProtocols;
    private String _start;
    private String _sourceIpSpaceSpecifierFactory;
    private String _sourceIpSpaceSpecifierInput;
    private SortedSet<SubRange> _srcPorts;
    private String _type;

    private Builder() {}

    public Builder setComplementHeaderSpace(boolean complementHeaderSpace) {
      _complementHeaderSpace = complementHeaderSpace;
      return this;
    }

    public Builder setFilterSpecifierInput(String filterSpecifierInput) {
      this._filterSpecifierInput = filterSpecifierInput;
      return this;
    }

    public Builder setNodeSpecifierFactory(String nodesSpecifierFactory) {
      this._nodeSpecifierFactory = nodesSpecifierFactory;
      return this;
    }

    public Builder setNodeSpecifierInput(String nodesSpecifierInput) {
      this._nodeSpecifierInput = nodesSpecifierInput;
      return this;
    }

    public Builder setDestinationIpSpaceSpecifierFactory(
        String destinationIpSpaceSpecifierFactory) {
      this._destinationIpSpaceSpecifierFactory = destinationIpSpaceSpecifierFactory;
      return this;
    }

    public Builder setDestinationIpSpaceSpecifierInput(String destinationIpSpaceSpecifierInput) {
      this._destinationIpSpaceSpecifierInput = destinationIpSpaceSpecifierInput;
      return this;
    }

    public Builder setDstPorts(SortedSet<SubRange> dstPorts) {
      this._dstPorts = dstPorts;
      return this;
    }

    public Builder setIpProtocols(Iterable<IpProtocol> ipProtocols) {
      _ipProtocols = ImmutableSortedSet.copyOf(ipProtocols);
      return this;
    }

    public Builder setSrcPorts(SortedSet<SubRange> srcPorts) {
      this._srcPorts = srcPorts;
      return this;
    }

    public Builder setDstProtocols(SortedSet<Protocol> dstProtocols) {
      this._dstProtocols = dstProtocols;
      return this;
    }

    public Builder setStart(String start) {
      _start = start;
      return this;
    }

    public Builder setSourceIpSpaceSpecifierFactory(String sourceIpSpaceSpecifierFactory) {
      this._sourceIpSpaceSpecifierFactory = sourceIpSpaceSpecifierFactory;
      return this;
    }

    public Builder setSourceIpSpaceSpecifierInput(String sourceIpSpaceSpecifierInput) {
      this._sourceIpSpaceSpecifierInput = sourceIpSpaceSpecifierInput;
      return this;
    }

    public Builder setQuery(String type) {
      this._type = type;
      return this;
    }

    public ReachFilterQuestion build() {
      return new ReachFilterQuestion(
          _complementHeaderSpace,
          _filterSpecifierInput,
          _destinationIpSpaceSpecifierFactory,
          _destinationIpSpaceSpecifierInput,
          _dstPorts,
          _dstProtocols,
          _ipProtocols,
          _nodeSpecifierFactory,
          _nodeSpecifierInput,
          _start,
          _sourceIpSpaceSpecifierFactory,
          _sourceIpSpaceSpecifierInput,
          _srcPorts,
          _type);
    }
  }
}
