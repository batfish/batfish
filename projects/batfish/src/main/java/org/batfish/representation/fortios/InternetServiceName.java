package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing internet-service-name configuration */
public class InternetServiceName implements Serializable {

  public enum Type {
    DEFAULT,
    LOCATION,
  }

  public static final Type DEFAULT_TYPE = Type.DEFAULT;

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public Long getInternetServiceId() {
    return _internetServiceId;
  }

  @Nullable
  public Type getType() {
    return _type;
  }

  @Nonnull
  public Type getTypeEffective() {
    return firstNonNull(_type, DEFAULT_TYPE);
  }

  public void setInternetServiceId(Long internetServiceId) {
    _internetServiceId = internetServiceId;
  }

  public void setType(Type type) {
    _type = type;
  }

  public InternetServiceName(String name) {
    _name = name;
  }

  @Nonnull private final String _name;
  @Nullable private Type _type;
  @Nullable private Long _internetServiceId;
}
