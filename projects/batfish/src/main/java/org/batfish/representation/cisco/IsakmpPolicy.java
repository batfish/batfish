package org.batfish.representation.cisco;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;

public class IsakmpPolicy extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private IkeAuthenticationMethod _authenticationMethod;

  private DiffieHellmanGroup _diffieHellmanGroup;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private IkeHashingAlgorithm _hashAlgorithm;

  private Integer _lifetimeSeconds;

  public IsakmpPolicy(String name) {
    super(name);
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

  public IkeHashingAlgorithm getHashAlgorithm() {
    return _hashAlgorithm;
  }

  public Integer getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public void setAuthenticationMethod(IkeAuthenticationMethod authenticationMethod) {
    _authenticationMethod = authenticationMethod;
  }

  public void setDiffieHellmanGroup(DiffieHellmanGroup diffieHellmanGroup) {
    _diffieHellmanGroup = diffieHellmanGroup;
  }

  public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
  }

  public void setHashAlgorithm(IkeHashingAlgorithm hashingAlgorithm) {
    _hashAlgorithm = hashingAlgorithm;
  }

  public void setLifetimeSeconds(Integer lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }
}
