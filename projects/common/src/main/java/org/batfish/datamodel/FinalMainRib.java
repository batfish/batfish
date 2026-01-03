package org.batfish.datamodel;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

/**
 * A version of {@link org.batfish.dataplane.rib.Rib} that is used for analyzing the main RIB after
 * routing simulation is complete.
 *
 * <p>It omits things like {@link AnnotatedRoute annotations} of the contained routes, backup
 * routes, and more.
 */
public final class FinalMainRib implements Serializable {
  public static FinalMainRib of(Iterable<? extends AbstractRoute> routes) {
    return of(StreamSupport.stream(routes.spliterator(), false));
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <T extends AbstractRoute> FinalMainRib of(T... routes) {
    return of(Arrays.stream(routes));
  }

  public static FinalMainRib of(Stream<? extends AbstractRoute> routes) {
    return new FinalMainRib(toPrefixTrie(routes));
  }

  /**
   * Returns all the routes in this RIB.
   *
   * @see PrefixTrieMultiMap#getAllElements()
   */
  public @Nonnull Set<AbstractRoute> getRoutes() {
    return _routeTree.getAllElements();
  }

  /**
   * Returns all the routes in this RIB that advertise the given network.
   *
   * @see PrefixTrieMultiMap#get(Prefix)
   */
  public @Nonnull Set<AbstractRoute> getRoutes(Prefix p) {
    return _routeTree.get(p);
  }

  /**
   * Performs a longest prefix match on the route tree.
   *
   * @see PrefixTrieMultiMap#longestPrefixMatch(Ip)
   */
  public @Nonnull Set<AbstractRoute> longestPrefixMatch(@Nonnull Ip address) {
    return _routeTree.longestPrefixMatch(address);
  }

  /**
   * Performs a longest prefix match on the route tree.
   *
   * @see PrefixTrieMultiMap#longestPrefixMatch(Ip, int)
   */
  public @Nonnull Set<AbstractRoute> longestPrefixMatch(@Nonnull Prefix prefix) {
    return _routeTree.longestPrefixMatch(prefix.getStartIp(), prefix.getPrefixLength());
  }

  //

  private final PrefixTrieMultiMap<AbstractRoute> _routeTree;

  private static PrefixTrieMultiMap<AbstractRoute> toPrefixTrie(
      Stream<? extends AbstractRoute> routes) {
    PrefixTrieMultiMap<AbstractRoute> ret = new PrefixTrieMultiMap<>();
    List<AbstractRoute> curRoutes = new LinkedList<>();
    Prefix[] curPrefix = new Prefix[] {null};
    routes
        .sequential() // forEach is not threadsafe
        .forEach(
            r -> {
              if (curPrefix[0] == null) {
                curPrefix[0] = r.getNetwork();
              } else if (!r.getNetwork().equals(curPrefix[0])) {
                ret.putAll(curPrefix[0], curRoutes);
                curPrefix[0] = r.getNetwork();
                curRoutes.clear();
              }
              curRoutes.add(r);
            });
    if (!curRoutes.isEmpty()) {
      ret.putAll(curPrefix[0], curRoutes);
    }
    return ret;
  }

  private FinalMainRib(PrefixTrieMultiMap<AbstractRoute> tree) {
    _routeTree = tree;
  }

  @Serial
  private Object writeReplace() throws ObjectStreamException {
    return new SerializedForm(_routeTree.getAllElements());
  }

  private static class SerializedForm implements Serializable {
    final Set<AbstractRoute> _routes;

    public SerializedForm(Set<AbstractRoute> routes) {
      _routes = routes;
    }

    @Serial
    private Object readResolve() throws ObjectStreamException {
      return FinalMainRib.of(_routes);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof FinalMainRib)) {
      return false;
    }
    FinalMainRib that = (FinalMainRib) o;
    return _routeTree.equals(that._routeTree);
  }

  @Override
  public int hashCode() {
    return _routeTree.hashCode();
  }
}
