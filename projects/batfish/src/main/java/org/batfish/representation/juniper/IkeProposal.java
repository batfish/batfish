package org.batfish.representation.juniper;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;

public class IkeProposal extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  // https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/security-edit-lifetime-seconds-ike.html
  private static final Integer DEFAULT_LIFETIME_SECONDS = 28800;

  private IkeHashingAlgorithm _authenticationAlgorithm;

  private IkeAuthenticationMethod _authenticationMethod;

  private DiffieHellmanGroup _diffieHellmanGroup;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private Integer _lifetimeSeconds;

  public IkeProposal(String name) {
    super(name);
    _lifetimeSeconds = DEFAULT_LIFETIME_SECONDS;
  }

  public IkeAuthenticationMethod getAuthenticationMethod() {
    return _authenticationMethod;
  }

  public DiffieHellmanGroup getDiffieHellmanGroup() {
    return _diffieHellmanGroup;
  }

  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return _encryptionAlgorithm;
  }

  public IkeHashingAlgorithm getAuthenticationAlgorithm() {
    return _authenticationAlgorithm;
  }

  public Integer getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public IkeProposal setAuthenticationMethod(IkeAuthenticationMethod authenticationMethod) {
    _authenticationMethod = authenticationMethod;
    return this;
  }

  public IkeProposal setDiffieHellmanGroup(DiffieHellmanGroup diffieHellmanGroup) {
    _diffieHellmanGroup = diffieHellmanGroup;
    return this;
  }

  public IkeProposal setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
    return this;
  }

  public IkeProposal setAuthenticationAlgorithm(IkeHashingAlgorithm hashingAlgorithm) {
    _authenticationAlgorithm = hashingAlgorithm;
    return this;
  }

  public IkeProposal setLifetimeSeconds(Integer lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
    return this;
  }
}
