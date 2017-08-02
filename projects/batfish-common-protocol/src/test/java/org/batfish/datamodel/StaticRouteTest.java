package org.batfish.datamodel;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/** Tests for {@link StaticRoute} */
public class StaticRouteTest {

   StaticRoute.Builder _srBuilder;

   @Test public void checkAllAttrs() {
      _srBuilder.setNextHopIp(new Ip("192.168.1.1"));
      _srBuilder.setNetwork(Prefix.ZERO);
      _srBuilder.setNextHopInterface("192.168.1.2");
      _srBuilder.setAdministrativeCost(1);
      _srBuilder.setTag(0);
      StaticRoute sr = _srBuilder.build();
      assertThat(sr.getNextHopIp(), is(new Ip("192.168.1.1")));
      assertThat(sr.getNetwork(), is(Prefix.ZERO));
      assertThat(sr.getNextHopInterface(), is("192.168.1.2"));
      assertThat(sr.getAdministrativeCost(), is(1));
      assertThat(sr.getTag(), is(0));
   }

   @Test public void checkNullNextHop() {
      _srBuilder.setNetwork(Prefix.ZERO);
      _srBuilder.setNextHopIp(null);
      StaticRoute sr = _srBuilder.build();
      assertThat(sr.getNextHopIp(), is(Route.UNSET_ROUTE_NEXT_HOP_IP));
   }

   @Test public void checkNullable() {
      _srBuilder.setNetwork(Prefix.ZERO);
      _srBuilder.setNextHopInterface(null);
      StaticRoute sr = _srBuilder.build();
      assertThat(sr.getNextHopInterface(), is(nullValue()));
      assertThat(sr.getNetwork(), is(Prefix.ZERO));
   }

   @Test public void checkDefaults() {
      StaticRoute.Builder srBuilderTest = new StaticRoute.Builder();
      srBuilderTest.setNetwork(Prefix.ZERO);
      StaticRoute sr = srBuilderTest.build();
      assertThat(sr.getNextHopInterface(), is(nullValue()));
      assertThat(sr.getNetwork(), is(Prefix.ZERO));
      assertThat(sr.getNextHopIp(), is(Route.UNSET_ROUTE_NEXT_HOP_IP));
      assertThat(sr.getAdministrativeCost(), is(0));
      assertThat(sr.getTag(), is(0));
   }

   @Before public void setUp() {
      _srBuilder = new StaticRoute.Builder();
   }
}
