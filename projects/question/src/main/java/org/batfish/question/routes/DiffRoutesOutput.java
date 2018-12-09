package org.batfish.question.routes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

/** Class to contain the difference and difference type in Routes per {@link RouteRowKey} */
public class DiffRoutesOutput {

  public enum KeyPresenceStatus {
    ONLY_IN_SNAPSHOT("Only in Snapshot"),
    ONLY_IN_REFERENCE("Only in Reference"),
    IN_BOTH("In both");

    private static final Map<String, KeyPresenceStatus> _map = buildMap();

    private static Map<String, KeyPresenceStatus> buildMap() {
      ImmutableMap.Builder<String, KeyPresenceStatus> map = ImmutableMap.builder();
      for (KeyPresenceStatus value : KeyPresenceStatus.values()) {
        String name = value._name;
        map.put(name, value);
      }
      return map.build();
    }

    @JsonCreator
    public static KeyPresenceStatus fromName(String name) {
      KeyPresenceStatus instance = _map.get(name);
      if (instance == null) {
        throw new BatfishException(
            String.format("No %s with name: '%s'", KeyPresenceStatus.class.getSimpleName(), name));
      }
      return instance;
    }

    private final String _name;

    KeyPresenceStatus(String name) {
      _name = name;
    }

    @JsonValue
    public String keyPresenceStatusName() {
      return _name;
    }
  }

  @Nonnull private RouteRowKey _routeRowKey;

  @Nonnull private List<List<RouteRowAttribute>> _diffInAttributes;

  @Nonnull private KeyPresenceStatus _keyPresenceStatusStatus;

  public DiffRoutesOutput(
      @Nonnull RouteRowKey routeRowKey,
      @Nonnull List<List<RouteRowAttribute>> diffInAttributes,
      @Nonnull KeyPresenceStatus keyPresenceStatusStatus) {
    _routeRowKey = routeRowKey;
    _diffInAttributes = diffInAttributes;
    _keyPresenceStatusStatus = keyPresenceStatusStatus;
  }

  @Nonnull
  public RouteRowKey getRouteRowKey() {
    return _routeRowKey;
  }

  @Nonnull
  public List<List<RouteRowAttribute>> getDiffInAttributes() {
    return _diffInAttributes;
  }

  @Nonnull
  public KeyPresenceStatus getKeyPresenceStatus() {
    return _keyPresenceStatusStatus;
  }
}
