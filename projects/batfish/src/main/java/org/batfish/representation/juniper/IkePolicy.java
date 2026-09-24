package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public final class IkePolicy implements Serializable {

  public enum PeerCertificateType {
    PKCS7,
    X509_SIGNATURE
  }

  private final List<String> _localCertificates;

  private final String _name;

  private PeerCertificateType _peerCertificateType;

  private String _preSharedKeyHash;

  // In priority order
  private final List<String> _proposals;

  public IkePolicy(String name) {
    _localCertificates = new LinkedList<>();
    _name = name;
    _proposals = new LinkedList<>();
  }

  public List<String> getLocalCertificates() {
    return _localCertificates;
  }

  public String getName() {
    return _name;
  }

  public PeerCertificateType getPeerCertificateType() {
    return _peerCertificateType;
  }

  public String getPreSharedKeyHash() {
    return _preSharedKeyHash;
  }

  public List<String> getProposals() {
    return _proposals;
  }

  public void setPeerCertificateType(PeerCertificateType peerCertificateType) {
    _peerCertificateType = peerCertificateType;
  }

  public void setPreSharedKeyHash(String preSharedKeyHash) {
    _preSharedKeyHash = preSharedKeyHash;
  }
}
