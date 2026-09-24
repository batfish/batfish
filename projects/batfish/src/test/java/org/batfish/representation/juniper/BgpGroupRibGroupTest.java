package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public final class BgpGroupRibGroupTest {

  @Test
  public void testCascadeInheritance() {
    BgpGroup parent = new BgpGroup();
    parent.setRibGroup("IPV4-RIBS");
    parent.setRibGroup6("IPV6-RIBS");
    BgpGroup child = new BgpGroup();
    child.setParent(parent);
    BgpGroup overridingChild = new BgpGroup();
    overridingChild.setParent(parent);
    overridingChild.setRibGroup("OVERRIDE-IPV4-RIBS");
    overridingChild.setRibGroup6("OVERRIDE-IPV6-RIBS");

    child.cascadeInheritance();
    overridingChild.cascadeInheritance();

    assertThat(child.getRibGroup(), equalTo("IPV4-RIBS"));
    assertThat(child.getRibGroup6(), equalTo("IPV6-RIBS"));
    assertThat(overridingChild.getRibGroup(), equalTo("OVERRIDE-IPV4-RIBS"));
    assertThat(overridingChild.getRibGroup6(), equalTo("OVERRIDE-IPV6-RIBS"));
  }
}
