package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.IpsecProtocol;

public class IpsecProposal implements Serializable {

  private IpsecAuthenticationAlgorithm _authenticationAlgorithm;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private IpsecEncapsulationMode _ipsecEncapsulationMode;

  private Integer _lifetimeKilobytes;

  private Integer _lifetimeSeconds;

  private final String _name;

  private SortedSet<IpsecProtocol> _protocols;

  public IpsecProposal(String name) {
    _name = name;
    _protocols = new TreeSet<>();
  }

  public IpsecAuthenticationAlgorithm getAuthenticationAlgorithm() {
    return _authenticationAlgorithm;
  }

  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return _encryptionAlgorithm;
  }

  public IpsecEncapsulationMode getIpsecEncapsulationMode() {
    return _ipsecEncapsulationMode;
  }

  public Integer getLifetimeKilobytes() {
    return _lifetimeKilobytes;
  }

  public Integer getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public String getName() {
    return _name;
  }

  public SortedSet<IpsecProtocol> getProtocols() {
    return _protocols;
  }

  public IpsecProposal setAuthenticationAlgorithm(
      IpsecAuthenticationAlgorithm authenticationAlgorithm) {
    _authenticationAlgorithm = authenticationAlgorithm;
    return this;
  }

  public IpsecProposal setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
    return this;
  }

  public void setIpsecEncapsulationMode(IpsecEncapsulationMode ipsecEncapsulationMode) {
    _ipsecEncapsulationMode = ipsecEncapsulationMode;
  }

  public void setLifetimeKilobytes(int lifetimeKilobytes) {
    _lifetimeKilobytes = lifetimeKilobytes;
  }

  public void setLifetimeSeconds(int lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }

  public IpsecProposal setProtocols(SortedSet<IpsecProtocol> protocols) {
    _protocols = protocols;
    return this;
  }
}
