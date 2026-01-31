package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;

public class IsakmpPolicy implements Serializable {

  private IkeAuthenticationMethod _authenticationMethod;

  private DiffieHellmanGroup _diffieHellmanGroup;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private IkeHashingAlgorithm _hashAlgorithm;

  private Integer _lifetimeSeconds;

  private final Integer _name;

  public IsakmpPolicy(Integer name) {
    _name = name;
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

  public Integer getName() {
    return _name;
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
