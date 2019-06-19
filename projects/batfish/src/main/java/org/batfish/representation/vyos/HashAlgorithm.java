package org.batfish.representation.vyos;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;

public enum HashAlgorithm {
  MD5,
  SHA1,
  SHA256,
  SHA384,
  SHA512;

  public IkeHashingAlgorithm toIkeAuthenticationAlgorithm() {
    switch (this) {
      case MD5:
        return IkeHashingAlgorithm.MD5;

      case SHA1:
        return IkeHashingAlgorithm.SHA1;

      case SHA256:
        return IkeHashingAlgorithm.SHA_256;

      case SHA384:
        return IkeHashingAlgorithm.SHA_384;

      case SHA512:
        return IkeHashingAlgorithm.SHA_512;

      default:
        throw new BatfishException("Missing conversion");
    }
  }

  public IpsecAuthenticationAlgorithm toIpsecAuthenticationAlgorithm() {
    switch (this) {
      case MD5:
        return IpsecAuthenticationAlgorithm.HMAC_MD5_96;

      case SHA1:
        return IpsecAuthenticationAlgorithm.HMAC_SHA1_96;

      case SHA256:
        return IpsecAuthenticationAlgorithm.HMAC_SHA_256_128;

      case SHA384:
      case SHA512:
      default:
        throw new BatfishException(
            "Missing conversion for this authentication algorithm: " + toString());
    }
  }
}
