package org.batfish.representation.vyos;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;

public class IkeProposal extends ComparableStructure<Integer> {

  /** */
  private static final long serialVersionUID = 1L;

  private DiffieHellmanGroup _dhGroup;

  private EncryptionAlgorithm _encryptionAlgorithm;

  private HashAlgorithm _hashAlgorithm;

  public IkeProposal(Integer name) {
    super(name);
  }

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
