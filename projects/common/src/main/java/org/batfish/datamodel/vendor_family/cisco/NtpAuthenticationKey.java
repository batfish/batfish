package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;

/**
 * Represents a Cisco IOS NTP authentication key defined via {@code ntp authentication-key <number>
 * md5 <key> [encryption-type]}.
 */
public class NtpAuthenticationKey extends ComparableStructure<Long> {

  private static final String PROP_MD5_KEY = "md5Key";
  private static final String PROP_ENCRYPTION_TYPE = "encryptionType";

  private @Nullable String _md5Key;
  private @Nullable Long _encryptionType;

  @JsonCreator
  public NtpAuthenticationKey(@JsonProperty(PROP_NAME) Long number) {
    super(number);
  }

  @JsonProperty(PROP_MD5_KEY)
  public @Nullable String getMd5Key() {
    return _md5Key;
  }

  public void setMd5Key(@Nullable String md5Key) {
    _md5Key = md5Key;
  }

  @JsonProperty(PROP_ENCRYPTION_TYPE)
  public @Nullable Long getEncryptionType() {
    return _encryptionType;
  }

  public void setEncryptionType(@Nullable Long encryptionType) {
    _encryptionType = encryptionType;
  }
}
