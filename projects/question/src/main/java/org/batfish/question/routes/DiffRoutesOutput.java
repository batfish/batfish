package org.batfish.question.routes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.table.TableDiff;

/** Class to contain the difference and type of the difference in Routes per {@link RouteRowKey} */
public class DiffRoutesOutput {

  public enum KeyPresenceStatus {
    ONLY_IN_SNAPSHOT(TableDiff.COL_KEY_STATUS_ONLY_BASE),
    ONLY_IN_REFERENCE(TableDiff.COL_KEY_STATUS_ONLY_DELTA),
    IN_BOTH(TableDiff.COL_KEY_STATUS_BOTH);

    private static final Map<String, KeyPresenceStatus> _map = buildMap();

    private static Map<String, KeyPresenceStatus> buildMap() {
      ImmutableMap.Builder<String, KeyPresenceStatus> map = ImmutableMap.builder();
      for (KeyPresenceStatus value : KeyPresenceStatus.values()) {
        map.put(value._name, value);
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
    public String presenceStatusName() {
      return _name;
    }
  }

  @Nonnull private final RouteRowKey _routeRowKey;

  @Nonnull private final RouteRowSecondaryKey _routeRowSecondaryKey;

  @Nonnull private final KeyPresenceStatus _routeRowSecondaryKeyStatus;

  @Nonnull private final List<List<RouteRowAttribute>> _diffInAttributes;

  @Nonnull private final KeyPresenceStatus _networkKeyPresenceStatus;

  public DiffRoutesOutput(
      @Nonnull RouteRowKey routeRowKey,
      @Nonnull RouteRowSecondaryKey routeRowSecondaryKey,
      @Nonnull KeyPresenceStatus routeRowSecondaryKeyStatus,
      @Nonnull List<List<RouteRowAttribute>> diffInAttributes,
      @Nonnull KeyPresenceStatus networkKeyPresenceStatus) {
    _networkKeyPresenceStatus = networkKeyPresenceStatus;
    _routeRowKey = routeRowKey;
    _routeRowSecondaryKeyStatus = routeRowSecondaryKeyStatus;
    _routeRowSecondaryKey = routeRowSecondaryKey;
    _diffInAttributes = diffInAttributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiffRoutesOutput that = (DiffRoutesOutput) o;
    return Objects.equals(_networkKeyPresenceStatus, that._networkKeyPresenceStatus)
        && Objects.equals(_routeRowKey, that._routeRowKey)
        && Objects.equals(_routeRowSecondaryKey, that._routeRowSecondaryKey)
        && Objects.equals(_routeRowSecondaryKeyStatus, that._routeRowSecondaryKeyStatus)
        && Objects.equals(_diffInAttributes, that._diffInAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _networkKeyPresenceStatus,
        _routeRowKey,
        _routeRowSecondaryKey,
        _routeRowSecondaryKeyStatus,
        _diffInAttributes);
  }

  @Nonnull
  public RouteRowKey getRouteRowKey() {
    return _routeRowKey;
  }

  @Nonnull
  public RouteRowSecondaryKey getRouteRowSecondaryKey() {
    return _routeRowSecondaryKey;
  }

  @Nonnull
  public KeyPresenceStatus getRouteRowSecondaryKeyStatus() {
    return _routeRowSecondaryKeyStatus;
  }

  @Nonnull
  public List<List<RouteRowAttribute>> getDiffInAttributes() {
    return _diffInAttributes;
  }

  @Nonnull
  public KeyPresenceStatus getNetworkKeyPresenceStatus() {
    return _networkKeyPresenceStatus;
  }
}
