package org.batfish.datamodel;

import static org.batfish.datamodel.IntersectHeaderSpaces.intersect;
import static org.batfish.datamodel.IntersectHeaderSpaces.intersectSimpleSets;
import static org.batfish.datamodel.IntersectHeaderSpaces.intersectSubRangeSets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import java.util.SortedSet;
import org.batfish.datamodel.IntersectHeaderSpaces.NoIntersection;
import org.junit.Test;

public class IntersectHeaderSpacesTest {
  private static final IpSpace IP1 = new Ip("1.1.1.1").toIpSpace();
  private static final IpSpace IP2 = new Ip("2.2.2.2").toIpSpace();

  @Test
  public void testIntersect() {
    HeaderSpace h1 = HeaderSpace.builder().setDstIps(IP1).build();
    HeaderSpace h2 = HeaderSpace.builder().setSrcIps(IP2).build();
    HeaderSpace h3 = HeaderSpace.builder().setDstIps(IP1).setSrcIps(IP2).build();
    assertThat(intersect(h1, h2), equalTo(Optional.of(h3)));
    assertThat(intersect(h2, h1), equalTo(Optional.of(h3)));
  }

  @Test
  public void testIntersect_nonTrivialDstIpIntersection() {
    HeaderSpace h1 = HeaderSpace.builder().setDstIps(IP1).build();
    HeaderSpace h2 = HeaderSpace.builder().setDstIps(IP2).build();
    HeaderSpace h3 = HeaderSpace.builder().setDstIps(AclIpSpace.intersection(IP1, IP2)).build();
    assertThat(intersect(h1, h2), equalTo(Optional.of(h3)));
  }

  @Test
  public void testIntersectEmpty() {
    HeaderSpace h1 = HeaderSpace.builder().setDscps(ImmutableList.of(1)).build();
    HeaderSpace h2 = HeaderSpace.builder().setDscps(ImmutableList.of(2)).build();
    assertThat(intersect(h1, h2), equalTo(Optional.empty()));
  }

  @Test
  public void testIntersectSimpleSets() throws NoIntersection {
    SortedSet<Integer> sEmpty = ImmutableSortedSet.of();
    SortedSet<Integer> s123 = ImmutableSortedSet.of(1, 2, 3);
    SortedSet<Integer> s2 = ImmutableSortedSet.of(2);
    assertThat(intersectSimpleSets(s123, sEmpty), equalTo(s123));
    assertThat(intersectSimpleSets(sEmpty, s123), equalTo(s123));
    assertThat(intersectSimpleSets(s123, s2), equalTo(s2));
  }

  @Test
  public void testIntersectSubRanges() throws NoIntersection {
    SortedSet<SubRange> s1ToTen = ImmutableSortedSet.of(new SubRange(1, 10));
    SortedSet<SubRange> s5To7 = ImmutableSortedSet.of(new SubRange(5, 7));
    assertThat(intersectSubRangeSets(s1ToTen, s5To7), equalTo(s5To7));

    SortedSet<SubRange> s5To12 = ImmutableSortedSet.of(new SubRange(5, 12));
    SortedSet<SubRange> s5To10 = ImmutableSortedSet.of(new SubRange(5, 10));
    assertThat(intersectSubRangeSets(s1ToTen, s5To12), equalTo(s5To10));

    SortedSet<SubRange> s5To12And3To7 =
        ImmutableSortedSet.of(new SubRange(5, 12), new SubRange(3, 7));
    SortedSet<SubRange> s5To10And5To7 =
        ImmutableSortedSet.of(new SubRange(5, 10), new SubRange(5, 7));
    assertThat(intersectSubRangeSets(s5To12And3To7, s5To10), equalTo(s5To10And5To7));
  }
}
