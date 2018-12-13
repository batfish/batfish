package org.batfish.question.routes;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.table.TableDiff;

/** Class to contain the difference and type of the difference in Routes per {@link RouteRowKey} */
public class DiffRoutesOutput {

  public enum PresenceStatus {
    ONLY_IN_SNAPSHOT(TableDiff.COL_KEY_STATUS_ONLY_BASE),
    ONLY_IN_REFERENCE(TableDiff.COL_KEY_STATUS_ONLY_DELTA),
    IN_BOTH(TableDiff.COL_KEY_STATUS_BOTH);

    private final String _name;

    PresenceStatus(String name) {
      _name = name;
    }

    @JsonValue
    public String presenceStatusName() {
      return _name;
    }
  }

  @Nonnull private final RouteRowKey _routeRowKey;

  @Nonnull private final List<List<RouteRowAttribute>> _diffInAttributes;

  @Nonnull private final PresenceStatus _networkPresenceStatus;

  public DiffRoutesOutput(
      @Nonnull RouteRowKey routeRowKey,
      @Nonnull List<List<RouteRowAttribute>> diffInAttributes,
      @Nonnull PresenceStatus networkPresenceStatus) {
    _networkPresenceStatus = networkPresenceStatus;
    _routeRowKey = routeRowKey;
    _diffInAttributes = diffInAttributes;
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
  public PresenceStatus getNetworkPresenceStatus() {
    return _networkPresenceStatus;
  }
}
