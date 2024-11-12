package org.batfish.representation.arista;

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

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/** Interface-level configuration of {@code ip nat destination static}. */
@ParametersAreNonnullByDefault
public final class AristaDestinationStaticNat implements Serializable {
  private final @Nonnull Ip _originalIp;
  private final @Nullable Integer _originalPort;
  private final @Nonnull Ip _translatedIp;
  private final @Nullable Integer _translatedPort;
  private final @Nullable String _aclName;
  private final @Nonnull NatProtocol _protocol;

  public AristaDestinationStaticNat(
      Ip originalIp,
      @Nullable Integer originalPort,
      Ip translatedIp,
      @Nullable Integer translatedPort,
      @Nullable String aclName,
      NatProtocol protocol) {
    checkArgument(
        (originalPort == null) == (translatedPort == null),
        "Must have translated ports for both original and translated traffic.");
    _originalIp = originalIp;
    _originalPort = originalPort;
    _translatedIp = translatedIp;
    _translatedPort = translatedPort;
    _aclName = aclName;
    _protocol = protocol;
  }

  public @Nullable String getAclName() {
    return _aclName;
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

  /**
   * Returns the {@link Transformation} representing the match and assignments from this destination
   * NAT rule.
   *
   * <p>Destination NAT is applied upon receiving a packet, matching the original protocol/IP/port
   * and translating it as specified. This corresponds to {@code forward} being {@literal true}.
   */
  private Transformation toTransformation(@Nullable Transformation orElse, boolean forward) {
    assert (_originalPort == null) == (_translatedPort == null); // invariant
    boolean usesPorts = _originalPort != null;
    ImmutableList.Builder<AclLineMatchExpr> conditions = ImmutableList.builder();
    conditions.add(forward ? matchDst(_originalIp) : matchSrc(_translatedIp));
    if (usesPorts) {
      conditions.add(forward ? matchDstPort(_originalPort) : matchSrcPort(_translatedPort));
    }
    switch (_protocol) {
      case TCP:
        conditions.add(matchIpProtocol(IpProtocol.TCP));
        break;
      case UDP:
        conditions.add(matchIpProtocol(IpProtocol.UDP));
        break;
      default:
        if (usesPorts) {
          // ANY means anything with ports, so TCP or UDP.
          conditions.add(matchIpProtocols(IpProtocol.TCP, IpProtocol.UDP));
        }
        break;
    }
    // TODO: ACL support.
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    steps.add(forward ? assignDestinationIp(_translatedIp) : assignSourceIp(_originalIp));
    if (usesPorts) {
      steps.add(forward ? assignDestinationPort(_translatedPort) : assignSourcePort(_originalPort));
    }
    return when(and(conditions.build())).apply(steps.build()).setOrElse(orElse).build();
  }

  /** Translate back to original packet when sending out the receiving interface. */
  public Transformation toOutgoingTransformation(@Nullable Transformation orElse) {
    return toTransformation(orElse, false);
  }

  /** Translate into translated packet when receiving a packet from this interface. */
  public Transformation toIncomingTransformation(@Nullable Transformation orElse) {
    return toTransformation(orElse, true);
  }
}
