package org.batfish.question;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.SortedSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.DifferenceLocationSpecifier;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.InterfaceSpecifierInterfaceLocationSpecifier;
import org.batfish.specifier.IntersectionLocationSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierInterfaceLocationSpecifier;
import org.batfish.specifier.NodeSpecifiers;

public final class ReachabilitySettings {

  public static final class Builder {

    private SortedSet<FlowDisposition> _actions;

    private NodeSpecifier _finalNodes;

    private HeaderSpace _headerSpace;

    private InterfaceSpecifier _ingressInterfaces;

    private NodeSpecifier _ingressNodes;

    private int _maxChunkSize;

    private NodeSpecifier _nonTransitNodes;

    private NodeSpecifier _notFinalNodes;

    private NodeSpecifier _notIngressNodes;

    private Boolean _srcNatted;

    private NodeSpecifier _transitNodes;

    private boolean _specialize = true;

    public ReachabilitySettings build() {
      return new ReachabilitySettings(this);
    }

    public SortedSet<FlowDisposition> getActions() {
      return _actions;
    }

    public NodeSpecifier getFinalNodes() {
      return _finalNodes;
    }

    public HeaderSpace getHeaderSpace() {
      return _headerSpace;
    }

    public NodeSpecifier getIngressNodes() {
      return _ingressNodes;
    }

    public int getMaxChunkSize() {
      return _maxChunkSize;
    }

    public NodeSpecifier getNonTransitNodes() {
      return _nonTransitNodes;
    }

    public NodeSpecifier getNotFinalNodes() {
      return _notFinalNodes;
    }

    public NodeSpecifier getNotIngressNodes() {
      return _notIngressNodes;
    }

    public boolean getSpecialize() {
      return _specialize;
    }

    public Boolean getSrcNatted() {
      return _srcNatted;
    }

    public NodeSpecifier getTransitNodes() {
      return _transitNodes;
    }

    public Builder setActions(Iterable<FlowDisposition> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setFinalNodes(NodeSpecifier finalNodes) {
      _finalNodes = finalNodes;
      return this;
    }

    public Builder setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
      return this;
    }

    public Builder setIngressNodes(NodeSpecifier ingressNodes) {
      _ingressNodes = ingressNodes;
      return this;
    }

    public Builder setMaxChunkSize(int maxChunkSize) {
      _maxChunkSize = maxChunkSize;
      return this;
    }

    public Builder setNonTransitNodes(NodeSpecifier nonTransitNodes) {
      _nonTransitNodes = nonTransitNodes;
      return this;
    }

    public Builder setNotFinalNodeRegex(NodeSpecifier notFinalNodes) {
      _notFinalNodes = notFinalNodes;
      return this;
    }

    public Builder setNotIngressNodeRegex(NodeSpecifier notIngressNodes) {
      _notIngressNodes = notIngressNodes;
      return this;
    }

    public Builder setSrcNatted(Boolean srcNatted) {
      _srcNatted = srcNatted;
      return this;
    }

    public Builder setSpecialize(boolean specialize) {
      _specialize = specialize;
      return this;
    }

    public Builder setTransitNodes(NodeSpecifier transitNodes) {
      _transitNodes = transitNodes;
      return this;
    }

    public Builder setIngressInterfaces(InterfaceSpecifier ingressInterfaces) {
      _ingressInterfaces = ingressInterfaces;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final SortedSet<FlowDisposition> _actions;

  private final NodeSpecifier _finalNodes;

  private final HeaderSpace _headerSpace;

  private final InterfaceSpecifier _ingressInterfaces;

  private final NodeSpecifier _ingressNodes;

  private final int _maxChunkSize;

  private final NodeSpecifier _nonTransitNodes;

  private NodeSpecifier _notFinalNodes;

  private NodeSpecifier _notIngressNodes;

  private final Boolean _srcNatted;

  private final boolean _specialize;

  private final NodeSpecifier _transitNodes;

  private ReachabilitySettings(Builder builder) {
    _finalNodes = builder._finalNodes;
    _notFinalNodes = builder._notFinalNodes;
    _headerSpace = builder._headerSpace;
    _ingressInterfaces = builder._ingressInterfaces;
    _ingressNodes = builder._ingressNodes;
    _notIngressNodes = builder._notIngressNodes;
    _maxChunkSize = builder._maxChunkSize;
    _transitNodes = builder._transitNodes;
    _nonTransitNodes = builder._nonTransitNodes;
    _specialize = builder._specialize;
    _actions = builder._actions;
    _srcNatted = builder._srcNatted;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ReachabilitySettings)) {
      return false;
    }

    ReachabilitySettings other = (ReachabilitySettings) obj;
    return _actions.equals(other._actions)
        && _finalNodes.equals(other._finalNodes)
        && _headerSpace.equals(other._headerSpace)
        && _ingressNodes.equals(other._ingressNodes)
        && _maxChunkSize == other._maxChunkSize
        && _nonTransitNodes.equals(other._nonTransitNodes)
        && Objects.equals(_srcNatted, other._srcNatted)
        && _transitNodes.equals(other._transitNodes)
        && _specialize == other._specialize;
  }

  public SortedSet<FlowDisposition> getActions() {
    return _actions;
  }

  public NodeSpecifier getFinalNodes() {
    return _finalNodes;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public InterfaceSpecifier getIngressInterfaces() {
    return _ingressInterfaces;
  }

  public NodeSpecifier getIngressNodes() {
    return _ingressNodes;
  }

  public int getMaxChunkSize() {
    return _maxChunkSize;
  }

  public NodeSpecifier getNonTransitNodes() {
    return _nonTransitNodes;
  }

  public NodeSpecifier getNotFinalNodes() {
    return _notFinalNodes;
  }

  public NodeSpecifier getNotIngressNodes() {
    return _notIngressNodes;
  }

  public Boolean getSrcNatted() {
    return _srcNatted;
  }

  public NodeSpecifier getTransitNodes() {
    return _transitNodes;
  }

  public boolean getSpecialize() {
    return _specialize;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _actions,
        _finalNodes,
        _headerSpace,
        _ingressNodes,
        _maxChunkSize,
        _nonTransitNodes,
        _srcNatted,
        _transitNodes,
        _specialize);
  }

  private static IpSpaceSpecifier toIpSpaceSpecifier(IpSpace include, IpSpace exclude) {
    return new ConstantIpSpaceSpecifier(
        firstNonNull(AclIpSpace.difference(include, exclude), UniverseIpSpace.INSTANCE));
  }

  /**
   * Convert to a {@link ReachabilityParameters} object. Mostly this means converting user input
   * into {@link LocationSpecifier} and {@link IpSpaceSpecifier} objects. At some point, this class
   * can be removed and replaced entirely with {@link ReachabilityParameters}.
   */
  public ReachabilityParameters toReachabilityParameters() {
    // compute the source LocationSpecifier
    LocationSpecifier ingressInterfaces =
        _ingressInterfaces == null
            ? null
            : new InterfaceSpecifierInterfaceLocationSpecifier(_ingressInterfaces);
    LocationSpecifier ingressNodes =
        _ingressNodes == null ? null : new NodeSpecifierInterfaceLocationSpecifier(_ingressNodes);
    LocationSpecifier notIngressNodes =
        _notIngressNodes == null
            ? null
            : new NodeSpecifierInterfaceLocationSpecifier(_notIngressNodes);

    // combine ingressNodes and notIngressNodes into ingressNodes
    if (ingressNodes != null && notIngressNodes != null) {
      ingressNodes = new DifferenceLocationSpecifier(ingressNodes, notIngressNodes);
    } else if (ingressNodes == null && notIngressNodes != null) {
      ingressNodes =
          new DifferenceLocationSpecifier(AllInterfacesLocationSpecifier.INSTANCE, notIngressNodes);
    }

    // combine ingressInterfaces and ingressNodes into sourceLocations
    LocationSpecifier sourceLocations = AllInterfacesLocationSpecifier.INSTANCE;
    if (ingressInterfaces != null && ingressNodes != null) {
      sourceLocations = new IntersectionLocationSpecifier(ingressInterfaces, ingressNodes);
    } else if (ingressInterfaces != null) {
      sourceLocations = ingressInterfaces;
    } else if (ingressNodes != null) {
      sourceLocations = ingressNodes;
    }

    // compute the source and destination IpSpaceSpecifier
    IpSpaceSpecifier sourceIpSpaceSpecifier =
        toIpSpaceSpecifier(_headerSpace.getSrcIps(), _headerSpace.getNotSrcIps());
    IpSpaceSpecifier destinationIpSpaceSpecifier =
        toIpSpaceSpecifier(_headerSpace.getDstIps(), _headerSpace.getNotDstIps());

    // remove src and dst IPs from headerspace since they're specified via IpSpaceSpecifiers
    IpSpace nullIpSpace = null;
    HeaderSpace headerSpace =
        _headerSpace
            .toBuilder()
            .setDstIps(nullIpSpace)
            .setNotDstIps(nullIpSpace)
            .setNotSrcIps(nullIpSpace)
            .setSrcIps(nullIpSpace)
            .build();

    return ReachabilityParameters.builder()
        .setActions(_actions)
        .setDestinationIpSpaceSpecifier(destinationIpSpaceSpecifier)
        .setFinalNodesSpecifier(
            firstNonNull(
                NodeSpecifiers.difference(_finalNodes, _notFinalNodes),
                AllNodesNodeSpecifier.INSTANCE))
        .setForbiddenTransitNodesSpecifier(
            firstNonNull(_nonTransitNodes, NoNodesNodeSpecifier.INSTANCE))
        .setHeaderSpace(headerSpace)
        .setMaxChunkSize(_maxChunkSize)
        .setRequiredTransitNodesSpecifier(
            firstNonNull(_transitNodes, NoNodesNodeSpecifier.INSTANCE))
        .setSourceIpSpaceSpecifier(sourceIpSpaceSpecifier)
        .setSourceLocationSpecifier(sourceLocations)
        .setSrcNatted(SrcNattedConstraint.fromBoolean(_srcNatted))
        .setSpecialize(_specialize)
        .build();
  }
}
