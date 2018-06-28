package org.batfish.representation.juniper;

import org.batfish.common.BatfishException;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;

public class IkeProposal extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  public static class Builder {

    private IkeHashingAlgorithm _authenticationAlgorithm;

    private IkeAuthenticationMethod _authenticationMethod;

    private DiffieHellmanGroup _diffieHellmanGroup;

    private EncryptionAlgorithm _encryptionAlgorithm;

    private Integer _lifetimeSeconds;

    private String _name;

    public IkeProposal build() {
      if (_name == null) {
        throw new BatfishException("Cannot create an IKE proposal with no name");
      }
      IkeProposal ikeProposal = new IkeProposal(_name);
      ikeProposal.setAuthenticationMethod(_authenticationMethod);
      ikeProposal.setDiffieHellmanGroup(_diffieHellmanGroup);
      ikeProposal.setEncryptionAlgorithm(_encryptionAlgorithm);
      ikeProposal.setAuthenticationAlgorithm(_authenticationAlgorithm);
      ikeProposal.setLifetimeSeconds(_lifetimeSeconds);
      return ikeProposal;
    }

    public Builder setAuthenticationAlgorithm(IkeHashingAlgorithm authenticationAlgorithm) {
      _authenticationAlgorithm = authenticationAlgorithm;
      return this;
    }

    public Builder setAuthenticationMethod(IkeAuthenticationMethod authenticationMethod) {
      _authenticationMethod = authenticationMethod;
      return this;
    }

    public Builder setDiffieHellmanGroup(DiffieHellmanGroup diffieHellmanGroup) {
      _diffieHellmanGroup = diffieHellmanGroup;
      return this;
    }

    public Builder setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
      _encryptionAlgorithm = encryptionAlgorithm;
      return this;
    }

    public Builder setLifetimeSeconds(Integer lifetimeSeconds) {
      _lifetimeSeconds = lifetimeSeconds;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }
  }

  private IkeHashingAlgorithm _authenticationAlgorithm;

  private IkeAuthenticationMethod _authenticationMethod;

  private DiffieHellmanGroup _diffieHellmanGroup;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private Integer _lifetimeSeconds;

  public IkeProposal(String name) {
    super(name);
  }

  public static Builder builder() {
    return new Builder();
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

  public void setAuthenticationMethod(IkeAuthenticationMethod authenticationMethod) {
    _authenticationMethod = authenticationMethod;
  }

  public void setDiffieHellmanGroup(DiffieHellmanGroup diffieHellmanGroup) {
    _diffieHellmanGroup = diffieHellmanGroup;
  }

  public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
  }

  public void setAuthenticationAlgorithm(IkeHashingAlgorithm hashingAlgorithm) {
    _authenticationAlgorithm = hashingAlgorithm;
  }

  public void setLifetimeSeconds(Integer lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }
}
