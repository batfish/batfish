package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

public final class BgpGroupErrorToleranceTest {

  @Test
  public void testCascadeInheritance() {
    BgpGroup parent = new BgpGroup();
    parent.setErrorTolerance(true);
    parent.setMalformedRouteLimit(20L);
    parent.setMalformedUpdateLogInterval(300);
    BgpGroup inheritingChild = new BgpGroup();
    inheritingChild.setParent(parent);
    BgpGroup overridingChild = new BgpGroup();
    overridingChild.setParent(parent);
    overridingChild.setNoMalformedRouteLimit(true);

    inheritingChild.cascadeInheritance();
    overridingChild.cascadeInheritance();

    assertThat(inheritingChild.getErrorTolerance(), equalTo(true));
    assertThat(inheritingChild.getMalformedRouteLimit(), equalTo(20L));
    assertThat(inheritingChild.getMalformedUpdateLogInterval(), equalTo(300));
    assertThat(overridingChild.getMalformedRouteLimit(), nullValue());
    assertThat(overridingChild.getNoMalformedRouteLimit(), equalTo(true));
  }
}
