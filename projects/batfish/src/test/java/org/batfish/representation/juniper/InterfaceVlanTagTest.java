package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class InterfaceVlanTagTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new InterfaceVlanTag(null, 100), new InterfaceVlanTag(null, 100))
        .addEqualityGroup(new InterfaceVlanTag(0x8100, 100))
        .addEqualityGroup(new InterfaceVlanTag(0x8100, 101))
        .testEquals();
  }

  @Test
  public void testProperties() {
    InterfaceVlanTag numeric = new InterfaceVlanTag(null, 100);
    InterfaceVlanTag qualified = new InterfaceVlanTag(0x88A8, 200);

    assertThat(numeric.getTpid(), nullValue());
    assertThat(numeric.getVlanId(), equalTo(100));
    assertThat(qualified.getTpid(), equalTo(0x88A8));
    assertThat(qualified.getVlanId(), equalTo(200));
  }
}
