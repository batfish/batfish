package org.batfish.question;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.SortedSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.DifferenceLocationSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationSpecifiers;
import org.batfish.specifier.NodeSpecifiers;
import org.batfish.specifier.UnionLocationSpecifier;

public final class ReachabilitySettings {

  public static final class Builder {

    private SortedSet<ForwardingAction> _actions;

    private NodesSpecifier _finalNodes;

    private HeaderSpace _headerSpace;

    private InterfacesSpecifier _ingressInterfaces;

    private NodesSpecifier _ingressNodes;

    private int _maxChunkSize;

    private NodesSpecifier _nonTransitNodes;

    private NodesSpecifier _notFinalNodes;

    private NodesSpecifier _notIngressNodes;

    private Boolean _srcNatted;

    private NodesSpecifier _transitNodes;

    private boolean _specialize = true;

    private boolean _useCompression;

    public ReachabilitySettings build() {
      return new ReachabilitySettings(this);
    }

    public SortedSet<ForwardingAction> getActions() {
      return _actions;
    }

    public NodesSpecifier getFinalNodes() {
      return _finalNodes;
    }

    public HeaderSpace getHeaderSpace() {
      return _headerSpace;
    }

    public NodesSpecifier getIngressNodes() {
      return _ingressNodes;
    }

    public int getMaxChunkSize() {
      return _maxChunkSize;
    }

    public NodesSpecifier getNonTransitNodes() {
      return _nonTransitNodes;
    }

    public NodesSpecifier getNotFinalNodes() {
      return _notFinalNodes;
    }

    public NodesSpecifier getNotIngressNodes() {
      return _notIngressNodes;
    }

    public boolean getSpecialize() {
      return _specialize;
    }

    public Boolean getSrcNatted() {
      return _srcNatted;
    }

    public NodesSpecifier getTransitNodes() {
      return _transitNodes;
    }

    public boolean getUseCompression() {
      return _useCompression;
    }

    public Builder setActions(Iterable<ForwardingAction> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setFinalNodes(NodesSpecifier finalNodes) {
      _finalNodes = finalNodes;
      return this;
    }

    public Builder setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
      return this;
    }

    public Builder setIngressNodes(NodesSpecifier ingressNodes) {
      _ingressNodes = ingressNodes;
      return this;
    }

    public Builder setMaxChunkSize(int maxChunkSize) {
      _maxChunkSize = maxChunkSize;
      return this;
    }

    public Builder setNonTransitNodes(NodesSpecifier nonTransitNodes) {
      _nonTransitNodes = nonTransitNodes;
      return this;
    }

    public Builder setNotFinalNodeRegex(NodesSpecifier notFinalNodes) {
      _notFinalNodes = notFinalNodes;
      return this;
    }

    public Builder setNotIngressNodeRegex(NodesSpecifier notIngressNodes) {
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

    public Builder setTransitNodes(NodesSpecifier transitNodes) {
      _transitNodes = transitNodes;
      return this;
    }

    public Builder setUseCompression(boolean useCompression) {
      _useCompression = useCompression;
      return this;
    }

    public Builder setIngressInterfaces(InterfacesSpecifier ingressInterfaces) {
      _ingressInterfaces = ingressInterfaces;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final SortedSet<ForwardingAction> _actions;

  private final NodesSpecifier _finalNodes;

  private final HeaderSpace _headerSpace;

  private final InterfacesSpecifier _ingressInterfaces;

  private final NodesSpecifier _ingressNodes;

  private final int _maxChunkSize;

  private final NodesSpecifier _nonTransitNodes;

  private NodesSpecifier _notFinalNodes;

  private NodesSpecifier _notIngressNodes;

  private final Boolean _srcNatted;

  private final boolean _specialize;

  private final NodesSpecifier _transitNodes;

  private final boolean _useCompression;

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
    _useCompression = builder._useCompression;
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
        && _specialize == other._specialize
        && _useCompression == other._useCompression;
  }

  public SortedSet<ForwardingAction> getActions() {
    return _actions;
  }

  public NodesSpecifier getFinalNodes() {
    return _finalNodes;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public InterfacesSpecifier getIngressInterfaces() {
    return _ingressInterfaces;
  }

  public NodesSpecifier getIngressNodes() {
    return _ingressNodes;
  }

  public int getMaxChunkSize() {
    return _maxChunkSize;
  }

  public NodesSpecifier getNonTransitNodes() {
    return _nonTransitNodes;
  }

  public NodesSpecifier getNotFinalNodes() {
    return _notFinalNodes;
  }

  public NodesSpecifier getNotIngressNodes() {
    return _notIngressNodes;
  }

  public Boolean getSrcNatted() {
    return _srcNatted;
  }

  public NodesSpecifier getTransitNodes() {
    return _transitNodes;
  }

  public boolean getSpecialize() {
    return _specialize;
  }

  public boolean getUseCompression() {
    return _useCompression;
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
        _specialize,
        _useCompression);
  }

  /**
   * Convert to a {@link ReachabilityParameters} object. Mostly this means converting user input
   * into {@link LocationSpecifier} and {@link IpSpaceSpecifier} objects. At some point, this class
   * can be removed and replaced entirely with {@link ReachabilityParameters}.
   */
  public ReachabilityParameters toReachabilityParameters() {
    // compute the source LocationSpecifier
    LocationSpecifier ingressInterfaces =
        _ingressInterfaces == null ? null : LocationSpecifiers.from(_ingressInterfaces);
    LocationSpecifier ingressNodes =
        _ingressNodes == null ? null : LocationSpecifiers.from(_ingressNodes);
    LocationSpecifier notIngressNodes =
        _notIngressNodes == null ? null : LocationSpecifiers.from(_notIngressNodes);

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
      sourceLocations = new UnionLocationSpecifier(ingressInterfaces, ingressNodes);
    } else if (ingressInterfaces != null) {
      sourceLocations = ingressInterfaces;
    } else if (ingressNodes != null) {
      sourceLocations = ingressNodes;
    }

    // compute the source IpSpaceSpecifier
    IpSpaceSpecifier sourceIpSpace = new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE);
    IpSpace srcIps = _headerSpace.getSrcIps();
    IpSpace notSrcIps = _headerSpace.getNotSrcIps();
    if (srcIps != null && notSrcIps != null) {
      sourceIpSpace = new ConstantIpSpaceSpecifier(AclIpSpace.difference(srcIps, notSrcIps));
    } else if (srcIps != null) {
      sourceIpSpace = new ConstantIpSpaceSpecifier(srcIps);
    } else if (notSrcIps != null) {
      sourceIpSpace =
          new ConstantIpSpaceSpecifier(AclIpSpace.difference(UniverseIpSpace.INSTANCE, notSrcIps));
    }

    // remove src IPs from headerspace since they're specified via an IpSpaceSpecifier
    IpSpace nullIpSpace = null;
    HeaderSpace headerSpace =
        _headerSpace.toBuilder().setNotSrcIps(nullIpSpace).setSrcIps(nullIpSpace).build();

    return ReachabilityParameters.builder()
        .setActions(_actions)
        .setFinalNodesSpecifier(
            NodeSpecifiers.difference(
                NodeSpecifiers.from(_finalNodes), NodeSpecifiers.from(_notFinalNodes)))
        .setForbiddenTransitNodesSpecifier(NodeSpecifiers.from(_nonTransitNodes))
        .setHeaderSpace(headerSpace)
        .setMaxChunkSize(_maxChunkSize)
        .setRequiredTransitNodesSpecifier(NodeSpecifiers.from(_transitNodes))
        .setSourceIpSpaceSpecifier(sourceIpSpace)
        .setSourceLocationSpecifier(sourceLocations)
        .setSrcNatted(SrcNattedConstraint.fromBoolean(_srcNatted))
        .setSpecialize(_specialize)
        .setUseCompression(_useCompression)
        .build();
  }
}
