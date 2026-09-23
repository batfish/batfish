package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public final class BgpGroupRemovePrivateTest {

  @Test
  public void testCascadeInheritance() {
    BgpGroup parent = new BgpGroup();
    parent.setRemovePrivate(true);
    parent.setRemovePrivateAll(true);
    parent.setRemovePrivateNoPeerLoopCheck(true);
    parent.setRemovePrivateReplace(true);
    BgpGroup inheritingChild = new BgpGroup();
    inheritingChild.setParent(parent);
    BgpGroup overridingChild = new BgpGroup();
    overridingChild.setParent(parent);
    overridingChild.setRemovePrivate(true);
    overridingChild.setRemovePrivateNearest(true);

    inheritingChild.cascadeInheritance();
    overridingChild.cascadeInheritance();

    assertThat(inheritingChild.getRemovePrivateAll(), equalTo(true));
    assertThat(inheritingChild.getRemovePrivateNoPeerLoopCheck(), equalTo(true));
    assertThat(inheritingChild.getRemovePrivateReplace(), equalTo(true));
    assertThat(overridingChild.getRemovePrivateAll(), equalTo(false));
    assertThat(overridingChild.getRemovePrivateNearest(), equalTo(true));
    assertThat(overridingChild.getRemovePrivateNoPeerLoopCheck(), equalTo(false));
    assertThat(overridingChild.getRemovePrivateReplace(), equalTo(false));
  }
}
