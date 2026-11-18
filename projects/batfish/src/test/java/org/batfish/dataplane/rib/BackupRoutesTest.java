package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Prefix;
import org.junit.jupiter.api.Test;

/** Tests of {@link BackupRoutes} */
final class BackupRoutesTest {

  private static final Prefix P1 = Prefix.parse("1.1.1.0/24");
  private static final Prefix P2 = Prefix.parse("2.2.2.0/24");

  private static ConnectedRoute route(Prefix prefix, String iface) {
    return new ConnectedRoute(prefix, iface);
  }

  @Test
  void testEmpty() {
    BackupRoutes<ConnectedRoute> backups = new BackupRoutes<>();

    assertThat(backups.get(P1), empty());
    assertThat(backups.values(), empty());
    assertFalse(backups.containsEntry(P1, route(P1, "i1")));
  }

  @Test
  void testSingleRoutePerPrefix() {
    BackupRoutes<ConnectedRoute> backups = new BackupRoutes<>();
    ConnectedRoute r1 = route(P1, "i1");
    ConnectedRoute r2 = route(P2, "i2");
    backups.put(P1, r1);
    backups.put(P2, r2);

    assertThat(backups.get(P1), contains(r1));
    assertThat(backups.get(P2), contains(r2));
    assertThat(backups.values(), containsInAnyOrder(r1, r2));
    assertTrue(backups.containsEntry(P1, r1));
    assertFalse(backups.containsEntry(P1, r2));
  }

  /** Several routes for one prefix must be kept in insertion order. */
  @Test
  void testMultipleRoutesPerPrefix() {
    BackupRoutes<ConnectedRoute> backups = new BackupRoutes<>();
    ConnectedRoute r1 = route(P1, "i1");
    ConnectedRoute r2 = route(P1, "i2");
    ConnectedRoute r3 = route(P1, "i3");
    backups.put(P1, r1);
    backups.put(P1, r2);
    backups.put(P1, r3);

    assertThat(backups.get(P1), contains(r1, r2, r3));
    assertThat(backups.values(), contains(r1, r2, r3));
    assertTrue(backups.containsEntry(P1, r3));
  }

  /** Set semantics: re-adding an equal route is a no-op, whether stored alone or in a list. */
  @Test
  void testPutIsIdempotent() {
    BackupRoutes<ConnectedRoute> backups = new BackupRoutes<>();
    ConnectedRoute r1 = route(P1, "i1");
    ConnectedRoute r2 = route(P1, "i2");
    backups.put(P1, r1);
    backups.put(P1, route(P1, "i1"));

    assertThat(backups.get(P1), contains(r1));

    backups.put(P1, r2);
    backups.put(P1, route(P1, "i2"));

    assertThat(backups.get(P1), contains(r1, r2));
  }

  @Test
  void testRemoveSoleRoute() {
    BackupRoutes<ConnectedRoute> backups = new BackupRoutes<>();
    ConnectedRoute r1 = route(P1, "i1");
    backups.put(P1, r1);
    backups.remove(P1, r1);

    assertThat(backups.get(P1), empty());
    assertThat(backups.values(), empty());
  }

  /** Removing down to one route must still behave as a populated prefix. */
  @Test
  void testRemoveFromMultiple() {
    BackupRoutes<ConnectedRoute> backups = new BackupRoutes<>();
    ConnectedRoute r1 = route(P1, "i1");
    ConnectedRoute r2 = route(P1, "i2");
    backups.put(P1, r1);
    backups.put(P1, r2);

    backups.remove(P1, r1);
    assertThat(backups.get(P1), contains(r2));
    assertTrue(backups.containsEntry(P1, r2));

    backups.put(P1, r1);
    assertThat(backups.get(P1), contains(r2, r1));

    backups.remove(P1, r1);
    backups.remove(P1, r2);
    assertThat(backups.get(P1), empty());
  }

  @Test
  void testRemoveAbsent() {
    BackupRoutes<ConnectedRoute> backups = new BackupRoutes<>();
    ConnectedRoute r1 = route(P1, "i1");
    backups.put(P1, r1);

    backups.remove(P1, route(P1, "other"));
    backups.remove(P2, r1);

    assertThat(backups.get(P1), contains(r1));
  }

  @Test
  void testClear() {
    BackupRoutes<ConnectedRoute> backups = new BackupRoutes<>();
    backups.put(P1, route(P1, "i1"));
    backups.put(P2, route(P2, "i2"));
    backups.clear();

    assertThat(backups.values(), empty());
    assertThat(backups.get(P1), empty());
  }
}
