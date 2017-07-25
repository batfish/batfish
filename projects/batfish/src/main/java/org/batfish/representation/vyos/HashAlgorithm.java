package org.batfish.representation.vyos;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.IkeAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;

public enum HashAlgorithm {
  MD5,
  SHA1,
  SHA256,
  SHA384,
  SHA512;

  public IkeAuthenticationAlgorithm toIkeAuthenticationAlgorithm() {
    switch (this) {
      case MD5:
        return IkeAuthenticationAlgorithm.MD5;

      case SHA1:
        return IkeAuthenticationAlgorithm.SHA1;

      case SHA256:
        return IkeAuthenticationAlgorithm.SHA_256;

      case SHA384:
        return IkeAuthenticationAlgorithm.SHA_384;

      case SHA512:
        return IkeAuthenticationAlgorithm.SHA_512;

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
