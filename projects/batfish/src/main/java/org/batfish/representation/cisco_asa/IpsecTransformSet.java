package org.batfish.representation.cisco_asa;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.IpsecProtocol;

public class IpsecTransformSet implements Serializable {

  @Nullable private IpsecAuthenticationAlgorithm _authenticationAlgorithm;

  @Nullable private EncryptionAlgorithm _encryptionAlgorithm;

  @Nonnull private IpsecEncapsulationMode _ipsecEncapsulationMode;

  private final String _name;

  @Nonnull private SortedSet<IpsecProtocol> _protocols;

  public IpsecTransformSet(String name) {
    _ipsecEncapsulationMode = IpsecEncapsulationMode.TUNNEL;
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

  public String getName() {
    return _name;
  }

  public SortedSet<IpsecProtocol> getProtocols() {
    return _protocols;
  }

  public void setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm authenticationAlgorithm) {
    _authenticationAlgorithm = authenticationAlgorithm;
  }

  public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
  }

  public void setIpsecEncapsulationMode(@Nullable IpsecEncapsulationMode ipsecEncapsulationMode) {
    _ipsecEncapsulationMode = firstNonNull(ipsecEncapsulationMode, IpsecEncapsulationMode.TUNNEL);
  }

  public void setProtocols(@Nullable SortedSet<IpsecProtocol> protocols) {
    _protocols = protocols == null ? new TreeSet<>() : protocols;
  }
}
