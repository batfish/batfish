package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Shorthands for ICMP types */
public enum NamedIcmpType {
  ECHO_REPLY("echo-reply", 0, 0),
  ECHO_REQUEST("echo-request", 8, 0);

  private static final Map<String, NamedIcmpType> MAP = initMap();

  @JsonCreator
  public static NamedIcmpType fromString(@Nullable String name) {
    requireNonNull(name, "Cannot instantiate NamedIcmpType from null");
    NamedIcmpType value = MAP.get(name.toLowerCase());
    if (value == null) {
      throw new IllegalArgumentException(
          "No " + NamedIcmpType.class.getSimpleName() + " with name: '" + name + "'");
    }
    return value;
  }

  private static Map<String, NamedIcmpType> initMap() {
    ImmutableMap.Builder<String, NamedIcmpType> map = ImmutableMap.builder();
    for (NamedIcmpType value : NamedIcmpType.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return map.build();
  }

  @Nonnull private final String _name;

  private final int _type;

  private final int _code;

  NamedIcmpType(@Nonnull String name, int type, int code) {
    _name = name;
    _type = type;
    _code = code;
  }

  @Nonnull
  @JsonValue
  public String getName() {
    return _name;
  }

  public int getCode() {
    return _code;
  }

  public int getType() {
    return _type;
  }
}
