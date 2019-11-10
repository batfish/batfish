package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_CODE;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_TYPE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a port range for a network acl entry */
@ParametersAreNonnullByDefault
final class IcmpTypeCode implements Serializable {

  private final int _code;

  private final int _type;

  @JsonCreator
  private static IcmpTypeCode create(
      @Nullable @JsonProperty(JSON_KEY_CODE) Integer code,
      @Nullable @JsonProperty(JSON_KEY_TYPE) Integer type) {
    checkArgument(code != null, "Code cannot be null in IcmpTypeCode");
    checkArgument(type != null, "Type  cannot be null in IcmpTypeCode");
    return new IcmpTypeCode(type, code);
  }

  IcmpTypeCode(int type, int code) {
    _code = code;
    _type = type;
  }

  int getCode() {
    return _code;
  }

  int getType() {
    return _type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IcmpTypeCode)) {
      return false;
    }
    IcmpTypeCode other = (IcmpTypeCode) o;
    return _code == other._code && _type == other._type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_code, _type);
  }
}
