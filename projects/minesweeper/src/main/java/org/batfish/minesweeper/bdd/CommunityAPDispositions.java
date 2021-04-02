package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.IntStream;

public class CommunityAPDispositions {

  private final Set<Integer> _mustExist;

  private final Set<Integer> _mustNotExist;

  public CommunityAPDispositions(Set<Integer> mustExist, Set<Integer> mustNotExist) {
    _mustExist = ImmutableSet.copyOf(mustExist);
    _mustNotExist = ImmutableSet.copyOf(mustNotExist);
  }

  public CommunityAPDispositions complement() {
    return new CommunityAPDispositions(_mustNotExist, _mustExist);
  }

  public CommunityAPDispositions intersect(CommunityAPDispositions other) {
    return new CommunityAPDispositions(
        setIntersect(_mustExist, other.getMustExist()),
        setUnion(_mustNotExist, other.getMustNotExist()));
  }

  public CommunityAPDispositions diff(CommunityAPDispositions other) {
    return this.intersect(other.complement());
  }

  public CommunityAPDispositions union(CommunityAPDispositions other) {
    return new CommunityAPDispositions(
        setUnion(_mustExist, other.getMustExist()),
        setIntersect(_mustNotExist, other.getMustNotExist()));
  }

  public static CommunityAPDispositions empty(int numAtomicPredicates) {
    return new CommunityAPDispositions(
        ImmutableSet.of(),
        IntStream.range(0, numAtomicPredicates).boxed().collect(ImmutableSet.toImmutableSet()));
  }

  public static CommunityAPDispositions exactly(Set<Integer> aps, int numAtomicPredicates) {
    return new CommunityAPDispositions(
        aps,
        IntStream.range(0, numAtomicPredicates)
            .filter(i -> !aps.contains(i))
            .boxed()
            .collect(ImmutableSet.toImmutableSet()));
  }

  public Set<Integer> getMustExist() {
    return _mustExist;
  }

  public Set<Integer> getMustNotExist() {
    return _mustNotExist;
  }

  private static ImmutableSet<Integer> setUnion(Set<Integer> s1, Set<Integer> s2) {
    return ImmutableSet.<Integer>builder().addAll(s1).addAll(s2).build();
  }

  private static ImmutableSet<Integer> setIntersect(Set<Integer> s1, Set<Integer> s2) {
    return s1.stream().filter(s2::contains).collect(ImmutableSet.toImmutableSet());
  }
}
