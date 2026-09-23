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
    overridingChild.setErrorTolerance(false);
    overridingChild.setMalformedRouteLimit(30L);
    overridingChild.setMalformedUpdateLogInterval(600);
    BgpGroup noLimitChild = new BgpGroup();
    noLimitChild.setParent(parent);
    noLimitChild.setNoMalformedRouteLimit(true);

    inheritingChild.cascadeInheritance();
    overridingChild.cascadeInheritance();
    noLimitChild.cascadeInheritance();

    assertThat(inheritingChild.getErrorTolerance(), equalTo(true));
    assertThat(inheritingChild.getMalformedRouteLimit(), equalTo(20L));
    assertThat(inheritingChild.getMalformedUpdateLogInterval(), equalTo(300));
    assertThat(overridingChild.getErrorTolerance(), equalTo(false));
    assertThat(overridingChild.getMalformedRouteLimit(), equalTo(30L));
    assertThat(overridingChild.getMalformedUpdateLogInterval(), equalTo(600));
    assertThat(noLimitChild.getMalformedRouteLimit(), nullValue());
    assertThat(noLimitChild.getNoMalformedRouteLimit(), equalTo(true));
  }
}
