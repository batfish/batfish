package org.batfish.representation.vyos;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.EncryptionAlgorithm;

public class EspProposal extends ComparableStructure<Integer> {

  /** */
  private static final long serialVersionUID = 1L;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private HashAlgorithm _hashAlgorithm;

  public EspProposal(Integer name) {
    super(name);
  }

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
