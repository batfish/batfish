package org.batfish.vendor.arista.representation;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocols;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.vendor.arista.representation.Conversions.nameOfSourceNatIpSpaceFromAcl;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/** Interface-level configuration of {@code ip nat source static}. */
@ParametersAreNonnullByDefault
public final class AristaStaticSourceNat implements Serializable {

  private final @Nonnull Ip _originalIp;
  private final @Nullable Integer _originalPort;
  private final @Nonnull Ip _translatedIp;
  private final @Nullable Integer _translatedPort;
  private final @Nullable String _extendedAclName;
  private final @Nonnull NatProtocol _protocol;

  public AristaStaticSourceNat(
      Ip originalIp,
      @Nullable Integer originalPort,
      Ip translatedIp,
      @Nullable Integer translatedPort,
      @Nullable String extendedAclName,
      NatProtocol protocol) {
    checkArgument(
        (originalPort == null) == (translatedPort == null),
        "Must have translated ports for both original and translated traffic.");
    _originalIp = originalIp;
    _originalPort = originalPort;
    _translatedIp = translatedIp;
    _translatedPort = translatedPort;
    _extendedAclName = extendedAclName;
    _protocol = protocol;
  }

  public @Nullable String getExtendedAclName() {
    return _extendedAclName;
  }

  public @Nonnull Ip getOriginalIp() {
    return _originalIp;
  }

  public @Nullable Integer getOriginalPort() {
    return _originalPort;
  }

  public @Nonnull NatProtocol getProtocol() {
    return _protocol;
  }

  public @Nonnull Ip getTranslatedIp() {
    return _translatedIp;
  }

  public @Nullable Integer getTranslatedPort() {
    return _translatedPort;
  }

  private Transformation toTransformation(@Nullable Transformation orElse, boolean forward) {
    assert (_originalPort == null) == (_translatedPort == null); // invariant
    ImmutableList.Builder<AclLineMatchExpr> conditions = ImmutableList.builder();
    conditions.add(forward ? matchSrc(_originalIp) : matchDst(_translatedIp));
    if (_originalPort != null) {
      conditions.add(forward ? matchSrcPort(_originalPort) : matchDstPort(_translatedPort));
    }
    switch (_protocol) {
      case TCP:
        conditions.add(matchIpProtocol(IpProtocol.TCP));
        break;
      case UDP:
        conditions.add(matchIpProtocol(IpProtocol.UDP));
        break;
      default:
        if (_originalPort != null) {
          // Matching and translating port, treat "ANY" as TCP or UDP.
          conditions.add(matchIpProtocols(IpProtocol.TCP, IpProtocol.UDP));
        }
        break;
    }
    if (_extendedAclName != null) {
      IpSpace space = new IpSpaceReference(nameOfSourceNatIpSpaceFromAcl(_extendedAclName));
      conditions.add(forward ? matchDst(space) : matchSrc(space));
    }
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    steps.add(
        forward ? assignSourceIp(_translatedIp, _translatedIp) : assignDestinationIp(_originalIp));
    if (_originalPort != null) {
      steps.add(forward ? assignSourcePort(_translatedPort) : assignDestinationPort(_originalPort));
    }
    return when(and(conditions.build())).apply(steps.build()).setOrElse(orElse).build();
  }

  /** Translate from original source IP when sending. */
  public Transformation toOutgoingTransformation(@Nullable Transformation orElse) {
    return toTransformation(orElse, true);
  }

  /** Translate back to original source IP when receiving. */
  public Transformation toIncomingTransformation(@Nullable Transformation orElse) {
    return toTransformation(orElse, false);
  }
}
