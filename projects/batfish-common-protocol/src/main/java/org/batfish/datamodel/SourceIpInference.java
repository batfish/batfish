package org.batfish.datamodel;

import java.io.Serializable;
import javax.annotation.Nullable;

public abstract class SourceIpInference implements Serializable {
  public static final class InferFromFib extends SourceIpInference {}

  public static final class UseConstantIp extends SourceIpInference {
    @Nullable private final Ip _ip;

    public UseConstantIp(@Nullable Ip ip) {
      _ip = ip;
    }

    @Nullable
    public Ip getIp() {
      return _ip;
    }
  }
}
