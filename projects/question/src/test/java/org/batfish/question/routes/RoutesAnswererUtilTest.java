package org.batfish.question.routes;

import static org.batfish.question.routes.RoutesAnswererUtil.alignRouteRowAttributes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link RoutesAnswererUtil} */
public class RoutesAnswererUtilTest {

  @Test
  public void testAlignRtRowAttrsNHNonnulls() {
    RouteRowAttribute rra1 =
        RouteRowAttribute.builder().setNextHop("nh1").setNextHopIp(Ip.AUTO).build();
    RouteRowAttribute rra3 =
        RouteRowAttribute.builder().setNextHop("nh3").setNextHopIp(Ip.AUTO).build();
    RouteRowAttribute rra5 =
        RouteRowAttribute.builder().setNextHop("nh5").setNextHopIp(Ip.AUTO).build();

    RouteRowAttribute rra2 =
        RouteRowAttribute.builder().setNextHop("nh2").setNextHopIp(Ip.AUTO).build();
    RouteRowAttribute rra4 =
        RouteRowAttribute.builder().setNextHop("nh4").setNextHopIp(Ip.AUTO).build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(
            ImmutableList.of(rra1, rra3, rra5), ImmutableList.of(rra2, rra4, rra5));

    // expected result after merging the two lists
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(rra1, null),
            Lists.newArrayList(null, rra2),
            Lists.newArrayList(rra3, null),
            Lists.newArrayList(null, rra4),
            Lists.newArrayList(rra5, rra5));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testAlignRtRowAttrsNonNullNHIP() {
    RouteRowAttribute rraNullNH =
        RouteRowAttribute.builder().setNextHopIp(new Ip("1.1.1.1")).build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setNextHop("nh3").build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(ImmutableList.of(rraNullNH), ImmutableList.of(rra3));

    // non-null next hop should come first
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(Lists.newArrayList(null, rra3), Lists.newArrayList(rraNullNH, null));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testAlignRtRowAttrsTrailingNullNHops1() {
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setNextHop("nh1").build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setNextHop("nh3").build();
    RouteRowAttribute rra1NulllNH = RouteRowAttribute.builder().build();
    RouteRowAttribute rra2NulllNH = RouteRowAttribute.builder().build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(
            ImmutableList.of(rra1, rra1NulllNH, rra2NulllNH), ImmutableList.of(rra3));

    // non-null next hop should come first
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(rra1, null),
            Lists.newArrayList(null, rra3),
            Lists.newArrayList(rra1NulllNH, null),
            Lists.newArrayList(rra2NulllNH, null));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }

  @Test
  public void testAlignRtRowAttrsTrailingNullNHops2() {
    RouteRowAttribute rra1 = RouteRowAttribute.builder().setNextHop("nh1").build();
    RouteRowAttribute rra3 = RouteRowAttribute.builder().setNextHop("nh3").build();
    RouteRowAttribute rra1NulllNH = RouteRowAttribute.builder().build();
    RouteRowAttribute rra2NulllNH = RouteRowAttribute.builder().build();

    List<List<RouteRowAttribute>> alignedRouteRowattrs =
        alignRouteRowAttributes(
            ImmutableList.of(rra3), ImmutableList.of(rra1, rra1NulllNH, rra2NulllNH));

    // non-null next hop should come first
    List<List<RouteRowAttribute>> expectedOutput =
        ImmutableList.of(
            Lists.newArrayList(null, rra1),
            Lists.newArrayList(rra3, null),
            Lists.newArrayList(null, rra1NulllNH),
            Lists.newArrayList(null, rra2NulllNH));

    assertThat(alignedRouteRowattrs, equalTo(expectedOutput));
  }
}
