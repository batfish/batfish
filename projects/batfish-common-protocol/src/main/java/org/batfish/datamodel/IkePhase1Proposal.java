package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.batfish.common.util.ComparableStructure;

/** Represents the IKE proposal used for IKE phase 1 negotiation */
public class IkePhase1Proposal extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private static final String PROP_AUTHENTICATION_METHOD = "authenticationMethod";

  private static final String PROP_ENCRYPTION_ALGORITHM = "encryptionAlgorithm";

  private static final String PROP_DIFFIE_HELLMAN_GROUP = "diffieHellmanGroup";

  private static final String PROP_HASHING_ALGORITHM = "hashingAlgorithm";

  private static final String PROP_LIFETIME_SECONDS = "lifetimeSeconds";

  private IkeAuthenticationMethod _authenticationMethod;

  private DiffieHellmanGroup _diffieHellmanGroup;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private IkeHashingAlgorithm _hashingAlgorithm;

  private Integer _lifetimeSeconds;

  @JsonCreator
  public IkePhase1Proposal(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }

  @JsonPropertyDescription("Authentication method to use for this IKE phase 1 proposal")
  @JsonProperty(PROP_AUTHENTICATION_METHOD)
  public IkeAuthenticationMethod getAuthenticationMethod() {
    return _authenticationMethod;
  }

  @JsonPropertyDescription(
      "Diffie-Hellman group to use for key exchange for this IKE phase 1 proposal")
  @JsonProperty(PROP_DIFFIE_HELLMAN_GROUP)
  public DiffieHellmanGroup getDiffieHellmanGroup() {
    return _diffieHellmanGroup;
  }

  @JsonPropertyDescription("Encryption algorithm to use for this IKE phase 1 proposal")
  @JsonProperty(PROP_ENCRYPTION_ALGORITHM)
  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return _encryptionAlgorithm;
  }

  @JsonPropertyDescription("Hashing algorithm to be used for this IKE phase 1 proposal")
  @JsonProperty(PROP_HASHING_ALGORITHM)
  public IkeHashingAlgorithm getHashingAlgorithm() {
    return _hashingAlgorithm;
  }

  @JsonPropertyDescription("Lifetime in seconds to use for this IKE phase 1 proposal")
  @JsonProperty(PROP_LIFETIME_SECONDS)
  public Integer getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  @JsonProperty(PROP_AUTHENTICATION_METHOD)
  public void setAuthenticationMethod(IkeAuthenticationMethod authenticationMethod) {
    _authenticationMethod = authenticationMethod;
  }

  @JsonProperty(PROP_DIFFIE_HELLMAN_GROUP)
  public void setDiffieHellmanGroup(DiffieHellmanGroup diffieHellmanGroup) {
    _diffieHellmanGroup = diffieHellmanGroup;
  }

  @JsonProperty(PROP_ENCRYPTION_ALGORITHM)
  public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
  }

  @JsonProperty(PROP_HASHING_ALGORITHM)
  public void setHashingAlgorithm(IkeHashingAlgorithm hashingAlgorithm) {
    _hashingAlgorithm = hashingAlgorithm;
  }

  @JsonProperty(PROP_LIFETIME_SECONDS)
  public void setLifetimeSeconds(Integer lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }
}
