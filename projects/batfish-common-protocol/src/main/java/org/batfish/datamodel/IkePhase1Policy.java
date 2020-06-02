package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;

/** Represents the IKE policy used for IKE phase 1 negotiation */
public class IkePhase1Policy extends ComparableStructure<String> {

  public static final String PREFIX_RSA_PUB = "RSA_PUB";
  public static final String PREFIX_ISAKMP_KEY = "ISAKMP_KEY";

  private static final String PROP_IKE_PHASE1_PROPOSALS = "ikePhase1Proposals";
  private static final String PROP_IKE_PHASE1_KEY = "ikePhase1Key";
  private static final String PROP_REMOTE_IDENTITY = "remoteIdentity";
  private static final String PROP_SELF_IDENTITY = "selfIdentity";
  private static final String PROP_LOCAL_INTERFACE = "localInterface";

  private @Nonnull List<String> _ikePhase1Proposals;
  private @Nullable IkePhase1Key _ikePhase1Key;
  private @Nullable IpSpace _remoteIdentity;
  private @Nullable Ip _selfIdentity;
  private @Nonnull String _localInterface;

  @JsonCreator
  public IkePhase1Policy(@JsonProperty(PROP_NAME) String name) {
    super(name);
    _ikePhase1Proposals = ImmutableList.of();
    _localInterface = UNSET_LOCAL_INTERFACE;
  }

  /** IKE phase 1 proposals to be used with this IKE phase 1 policy. */
  @JsonProperty(PROP_IKE_PHASE1_PROPOSALS)
  public @Nonnull List<String> getIkePhase1Proposals() {
    return _ikePhase1Proposals;
  }

  /** Key to be used with this IKE phase 1 policy. */
  @JsonProperty(PROP_IKE_PHASE1_KEY)
  public @Nullable IkePhase1Key getIkePhase1Key() {
    return _ikePhase1Key;
  }

  /** Identity of the remote peer that can match with this IKE phase 1 policy. */
  @JsonProperty(PROP_REMOTE_IDENTITY)
  public @Nullable IpSpace getRemoteIdentity() {
    return _remoteIdentity;
  }

  /** Self identity to be used with a remote peer while using this IKE phase 1 policy. */
  @JsonProperty(PROP_SELF_IDENTITY)
  public @Nullable Ip getSelfIdentity() {
    return _selfIdentity;
  }

  /** Local interface on which this IKE phase 1 policy can be used. */
  @JsonProperty(PROP_LOCAL_INTERFACE)
  public @Nonnull String getLocalInterface() {
    return _localInterface;
  }

  @JsonProperty(PROP_IKE_PHASE1_PROPOSALS)
  public void setIkePhase1Proposals(@Nullable List<String> ikePhase1Proposals) {
    _ikePhase1Proposals =
        ikePhase1Proposals == null ? ImmutableList.of() : ImmutableList.copyOf(ikePhase1Proposals);
  }

  @JsonProperty(PROP_IKE_PHASE1_KEY)
  public void setIkePhase1Key(@Nullable IkePhase1Key ikePhase1Key) {
    _ikePhase1Key = ikePhase1Key;
  }

  @JsonProperty(PROP_REMOTE_IDENTITY)
  public void setRemoteIdentity(@Nullable IpSpace remoteIdentity) {
    _remoteIdentity = remoteIdentity;
  }

  @JsonProperty(PROP_SELF_IDENTITY)
  public void setSelfIdentity(@Nullable Ip selfIdentity) {
    _selfIdentity = selfIdentity;
  }

  @JsonProperty(PROP_LOCAL_INTERFACE)
  public void setLocalInterface(@Nullable String localInterface) {
    _localInterface = firstNonNull(localInterface, UNSET_LOCAL_INTERFACE);
  }
}
