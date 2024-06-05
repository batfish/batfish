package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FinalMainRibTest {
  @Test
  public void testEquals() {
    StaticRoute sr = StaticRoute.testBuilder().setNetwork(Prefix.ZERO).build();
    StaticRoute sr2 = StaticRoute.testBuilder().setNetwork(Prefix.MULTICAST).build();
    new EqualsTester()
        .addEqualityGroup(FinalMainRib.of(sr), FinalMainRib.of(sr))
        .addEqualityGroup(FinalMainRib.of(sr, sr2), FinalMainRib.of(sr2, sr))
        .addEqualityGroup(FinalMainRib.of())
        .testEquals();
  }

  @Test
  public void testSerialization() {
    FinalMainRib empty = FinalMainRib.of();
    assertThat(empty, equalTo(SerializationUtils.clone(empty)));

    FinalMainRib someRoutes =
        FinalMainRib.of(
            StaticRoute.testBuilder().setNetwork(Prefix.ZERO).build(),
            StaticRoute.testBuilder().setNetwork(Prefix.parse("1.2.3.0/24")).build(),
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("1.2.3.0/24"))
                .setAdministrativeCost(5)
                .build(),
            StaticRoute.testBuilder().setNetwork(Prefix.parse("1.2.3.4/28")).build());
    assertThat(someRoutes.getRoutes(), hasSize(4));
    assertThat(someRoutes.getRoutes(Prefix.MULTICAST), empty());
    assertThat(someRoutes.getRoutes(Prefix.ZERO), hasSize(1));
    assertThat(someRoutes.getRoutes(Prefix.parse("1.2.3.0/24")), hasSize(2));
    assertThat(someRoutes, equalTo(SerializationUtils.clone(someRoutes)));
  }
}
