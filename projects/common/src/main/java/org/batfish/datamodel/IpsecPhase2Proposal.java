package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IpsecPhase2Proposal implements Serializable {

  private static final String PROP_AUTHENTICATION_ALGORITHM = "authenticationAlgorithm";
  private static final String PROP_ENCRYPTION_ALGORITHM = "encryptionAlgorithm";
  private static final String PROP_IPSEC_ENCAPSULATION_MODE = "ipsecEncapsulationMode";
  private static final String PROP_PROTOCOLS = "protocols";

  private IpsecAuthenticationAlgorithm _authenticationAlgorithm;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private @Nonnull IpsecEncapsulationMode _ipsecEncapsulationMode;

  private @Nonnull SortedSet<IpsecProtocol> _protocols;

  @JsonCreator
  public IpsecPhase2Proposal() {
    _protocols = ImmutableSortedSet.of();
    _ipsecEncapsulationMode = IpsecEncapsulationMode.TUNNEL;
  }

  /** Authentication algorithm to be used with this IPSec Proposal. */
  @JsonProperty(PROP_AUTHENTICATION_ALGORITHM)
  public IpsecAuthenticationAlgorithm getAuthenticationAlgorithm() {
    return _authenticationAlgorithm;
  }

  /** Encryption algorithm to be used with this IPSec Proposal. */
  @JsonProperty(PROP_ENCRYPTION_ALGORITHM)
  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return _encryptionAlgorithm;
  }

  /** IPSec encapsulation mode to be used with this IPSec Proposal. */
  @JsonProperty(PROP_IPSEC_ENCAPSULATION_MODE)
  public IpsecEncapsulationMode getIpsecEncapsulationMode() {
    return _ipsecEncapsulationMode;
  }

  /** IPSec protocols to be used with this IPSec Proposal. */
  @JsonProperty(PROP_PROTOCOLS)
  public SortedSet<IpsecProtocol> getProtocols() {
    return _protocols;
  }

  @JsonProperty(PROP_AUTHENTICATION_ALGORITHM)
  public void setAuthenticationAlgorithm(
      @Nullable IpsecAuthenticationAlgorithm authenticationAlgorithm) {
    _authenticationAlgorithm = authenticationAlgorithm;
  }

  @JsonProperty(PROP_ENCRYPTION_ALGORITHM)
  public void setEncryptionAlgorithm(@Nullable EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
  }

  @JsonProperty(PROP_IPSEC_ENCAPSULATION_MODE)
  public void setIpsecEncapsulationMode(@Nullable IpsecEncapsulationMode ipsecEncapsulationMode) {
    _ipsecEncapsulationMode = firstNonNull(ipsecEncapsulationMode, IpsecEncapsulationMode.TUNNEL);
  }

  @JsonProperty(PROP_PROTOCOLS)
  public void setProtocols(@Nullable SortedSet<IpsecProtocol> protocols) {
    _protocols = protocols == null ? ImmutableSortedSet.of() : ImmutableSortedSet.copyOf(protocols);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpsecPhase2Proposal)) {
      return false;
    }
    IpsecPhase2Proposal other = (IpsecPhase2Proposal) o;
    return Objects.equals(_authenticationAlgorithm, other._authenticationAlgorithm)
        && Objects.equals(_encryptionAlgorithm, other._encryptionAlgorithm)
        && Objects.equals(_ipsecEncapsulationMode, other._ipsecEncapsulationMode)
        && Objects.equals(_protocols, other._protocols);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _authenticationAlgorithm, _encryptionAlgorithm, _ipsecEncapsulationMode, _protocols);
  }
}
