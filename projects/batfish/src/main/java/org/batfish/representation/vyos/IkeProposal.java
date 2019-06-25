package org.batfish.representation.vyos;

import java.io.Serializable;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;

public class IkeProposal implements Serializable {

  private DiffieHellmanGroup _dhGroup;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private HashAlgorithm _hashAlgorithm;

  public DiffieHellmanGroup getDhGroup() {
    return _dhGroup;
  }

  public EncryptionAlgorithm getEncryptionAlgorithm() {
    return _encryptionAlgorithm;
  }

  public HashAlgorithm getHashAlgorithm() {
    return _hashAlgorithm;
  }

  public void setDhGroup(DiffieHellmanGroup dhGroup) {
    _dhGroup = dhGroup;
  }

  public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
    _encryptionAlgorithm = encryptionAlgorithm;
  }

  public void setHashAlgorithm(HashAlgorithm hashAlgorithm) {
    _hashAlgorithm = hashAlgorithm;
  }
}
