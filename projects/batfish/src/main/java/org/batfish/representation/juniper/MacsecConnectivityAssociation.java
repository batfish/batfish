package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/** MACsec connectivity association settings. */
public final class MacsecConnectivityAssociation implements Serializable {

  public enum MkaSecurityMode {
    MUST_SECURE,
    SHOULD_SECURE
  }

  public enum SecurityMode {
    DYNAMIC,
    STATIC_CONNECTIVITY_ASSOCIATION_KEY
  }

  public MacsecConnectivityAssociation(String name) {
    _name = name;
  }

  public @Nullable String getCakHash() {
    return _cakHash;
  }

  public @Nullable String getCipherSuite() {
    return _cipherSuite;
  }

  public @Nullable String getCkn() {
    return _ckn;
  }

  public boolean getIncludeSci() {
    return _includeSci;
  }

  public @Nullable Integer getKeyServerPriority() {
    return _keyServerPriority;
  }

  public @Nullable MkaSecurityMode getMkaSecurityMode() {
    return _mkaSecurityMode;
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getPreSharedKeyCakHash() {
    return _preSharedKeyCakHash;
  }

  public @Nullable String getPreSharedKeyChain() {
    return _preSharedKeyChain;
  }

  public @Nullable String getPreSharedKeyCkn() {
    return _preSharedKeyCkn;
  }

  public @Nullable SecurityMode getSecurityMode() {
    return _securityMode;
  }

  public void setCakHash(String cakHash) {
    _cakHash = cakHash;
  }

  public void setCipherSuite(String cipherSuite) {
    _cipherSuite = cipherSuite;
  }

  public void setCkn(String ckn) {
    _ckn = ckn;
  }

  public void setIncludeSci() {
    _includeSci = true;
  }

  public void setKeyServerPriority(int keyServerPriority) {
    _keyServerPriority = keyServerPriority;
  }

  public void setMkaSecurityMode(MkaSecurityMode mkaSecurityMode) {
    _mkaSecurityMode = mkaSecurityMode;
  }

  public void setPreSharedKeyCakHash(String preSharedKeyCakHash) {
    _preSharedKeyCakHash = preSharedKeyCakHash;
  }

  public void setPreSharedKeyChain(String preSharedKeyChain) {
    _preSharedKeyChain = preSharedKeyChain;
  }

  public void setPreSharedKeyCkn(String preSharedKeyCkn) {
    _preSharedKeyCkn = preSharedKeyCkn;
  }

  public void setSecurityMode(SecurityMode securityMode) {
    _securityMode = securityMode;
  }

  private @Nullable String _cakHash;
  private @Nullable String _cipherSuite;
  private @Nullable String _ckn;
  private boolean _includeSci;
  private @Nullable Integer _keyServerPriority;
  private @Nullable MkaSecurityMode _mkaSecurityMode;
  private final String _name;
  private @Nullable String _preSharedKeyCakHash;
  private @Nullable String _preSharedKeyChain;
  private @Nullable String _preSharedKeyCkn;
  private @Nullable SecurityMode _securityMode;
}
