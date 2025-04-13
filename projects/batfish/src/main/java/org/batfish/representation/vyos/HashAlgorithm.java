package org.batfish.representation.vyos;

import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;

public enum HashAlgorithm {
  MD5,
  SHA1,
  SHA256,
  SHA384,
  SHA512;

  public IkeHashingAlgorithm toIkeAuthenticationAlgorithm() {
    return switch (this) {
      case MD5 -> IkeHashingAlgorithm.MD5;
      case SHA1 -> IkeHashingAlgorithm.SHA1;
      case SHA256 -> IkeHashingAlgorithm.SHA_256;
      case SHA384 -> IkeHashingAlgorithm.SHA_384;
      case SHA512 -> IkeHashingAlgorithm.SHA_512;
    };
  }

  public IpsecAuthenticationAlgorithm toIpsecAuthenticationAlgorithm() {
    return switch (this) {
      case MD5 -> IpsecAuthenticationAlgorithm.HMAC_MD5_96;
      case SHA1 -> IpsecAuthenticationAlgorithm.HMAC_SHA1_96;
      case SHA256 -> IpsecAuthenticationAlgorithm.HMAC_SHA_256_128;
      case SHA384 -> IpsecAuthenticationAlgorithm.HMAC_SHA_384;
      case SHA512 -> IpsecAuthenticationAlgorithm.HMAC_SHA_512;
    };
  }
}
