package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Prefix;

/**
 * Maps a prefix to the insertion-ordered set of alternative routes a RIB holds for it, so a route
 * can be restored when a better one is withdrawn.
 *
 * <p>A RIB records a backup for every route it is ever offered, making this one of the larger
 * structures in a dataplane computation. Nearly every prefix has only one or two backups, so a value
 * is the route itself when there is exactly one and a small list otherwise, rather than a per-prefix
 * set carrying its own hash table.
 *
 * <p>Ordering is per prefix: {@link #values} returns routes grouped by prefix, not in global
 * insertion order.
 */
@ParametersAreNonnullByDefault
final class BackupRoutes<R extends AbstractRouteDecorator> {

  private final Map<Prefix, Object> _byPrefix = new HashMap<>();

  /** Adds {@code route} under {@code prefix}, if not already present. */
  @SuppressWarnings("unchecked")
  void put(Prefix prefix, R route) {
    Object existing = _byPrefix.get(prefix);
    if (existing == null) {
      _byPrefix.put(prefix, route);
      return;
    }
    if (!(existing instanceof List)) {
      if (existing.equals(route)) {
        return;
      }
      List<R> grown = new ArrayList<>(2);
      grown.add((R) existing);
      grown.add(route);
      _byPrefix.put(prefix, grown);
      return;
    }
    List<R> routes = (List<R>) existing;
    if (!routes.contains(route)) {
      routes.add(route);
    }
  }

  /** The routes stored under {@code prefix} in insertion order, or empty if there are none. */
  @SuppressWarnings("unchecked")
  @Nonnull
  Set<R> get(Prefix prefix) {
    Object existing = _byPrefix.get(prefix);
    if (existing == null) {
      return ImmutableSet.of();
    }
    if (!(existing instanceof List)) {
      return ImmutableSet.of((R) existing);
    }
    return ImmutableSet.copyOf((List<R>) existing);
  }

  /** Whether {@code route} is stored under {@code prefix}. */
  @SuppressWarnings("unchecked")
  boolean containsEntry(Prefix prefix, R route) {
    Object existing = _byPrefix.get(prefix);
    if (existing == null) {
      return false;
    }
    if (!(existing instanceof List)) {
      return existing.equals(route);
    }
    return ((List<R>) existing).contains(route);
  }

  /** Removes {@code route} from {@code prefix}, if present. */
  @SuppressWarnings("unchecked")
  void remove(Prefix prefix, R route) {
    Object existing = _byPrefix.get(prefix);
    if (existing == null) {
      return;
    }
    if (!(existing instanceof List)) {
      if (existing.equals(route)) {
        _byPrefix.remove(prefix);
      }
      return;
    }
    List<R> routes = (List<R>) existing;
    routes.remove(route);
    if (routes.isEmpty()) {
      _byPrefix.remove(prefix);
    } else if (routes.size() == 1) {
      _byPrefix.put(prefix, routes.get(0));
    }
  }

  /** Every stored route. See the class javadoc on ordering. */
  @SuppressWarnings("unchecked")
  @Nonnull
  Collection<R> values() {
    List<R> out = new ArrayList<>(_byPrefix.size());
    for (Object value : _byPrefix.values()) {
      if (value instanceof List) {
        out.addAll((List<R>) value);
      } else {
        out.add((R) value);
      }
    }
    return out;
  }

  void clear() {
    _byPrefix.clear();
  }
}
