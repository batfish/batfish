package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;

/** Represents an IPsec transform set configuration */
public class FtdIpsecTransformSet implements Serializable {

  private final String _name;
  private EncryptionAlgorithm _espEncryption;
  private IpsecAuthenticationAlgorithm _espAuthentication;
  private IpsecAuthenticationAlgorithm _ahAuthentication;
  private IpsecEncapsulationMode _mode;

  public FtdIpsecTransformSet(String name) {
    _name = name;
    _mode = IpsecEncapsulationMode.TUNNEL; // Default mode
  }

  public String getName() {
    return _name;
  }

  public @Nullable EncryptionAlgorithm getEspEncryption() {
    return _espEncryption;
  }

  public @Nullable IpsecAuthenticationAlgorithm getEspAuthentication() {
    return _espAuthentication;
  }

  public @Nullable IpsecAuthenticationAlgorithm getAhAuthentication() {
    return _ahAuthentication;
  }

  public IpsecEncapsulationMode getMode() {
    return _mode;
  }

  public void setEspEncryption(EncryptionAlgorithm espEncryption) {
    _espEncryption = espEncryption;
  }

  public void setEspAuthentication(IpsecAuthenticationAlgorithm espAuthentication) {
    _espAuthentication = espAuthentication;
  }

  public void setAhAuthentication(IpsecAuthenticationAlgorithm ahAuthentication) {
    _ahAuthentication = ahAuthentication;
  }

  public void setMode(IpsecEncapsulationMode mode) {
    _mode = mode;
  }
}
