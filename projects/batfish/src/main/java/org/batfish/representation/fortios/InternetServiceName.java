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

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Long getInternetServiceId() {
    return _internetServiceId;
  }

  public @Nullable Type getType() {
    return _type;
  }

  public @Nonnull Type getTypeEffective() {
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

  private final @Nonnull String _name;
  private @Nullable Type _type;
  private @Nullable Long _internetServiceId;
}
