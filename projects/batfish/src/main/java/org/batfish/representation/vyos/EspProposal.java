package org.batfish.representation.vyos;

import java.io.Serializable;
import org.batfish.datamodel.EncryptionAlgorithm;

public class EspProposal implements Serializable {

  private EncryptionAlgorithm _encryptionAlgorithm;

  private HashAlgorithm _hashAlgorithm;

  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return _encryptionAlgorithm;
  }

  public HashAlgorithm getHashAlgorithm() {
    return _hashAlgorithm;
  }

  public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
  }

  public void setHashAlgorithm(HashAlgorithm hashAlgorithm) {
    _hashAlgorithm = hashAlgorithm;
  }
}
