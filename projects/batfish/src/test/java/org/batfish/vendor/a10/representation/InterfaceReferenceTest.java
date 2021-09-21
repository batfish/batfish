package org.batfish.vendor.a10.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests of {@link InterfaceReference}. */
public class InterfaceReferenceTest {
  @Test
  public void testSerialization() {
    InterfaceReference obj = new InterfaceReference(Interface.Type.ETHERNET, 1);
    InterfaceReference clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }

  @Test
  public void testEquality() {
    InterfaceReference obj = new InterfaceReference(Interface.Type.ETHERNET, 1);
    new EqualsTester()
        .addEqualityGroup(obj, new InterfaceReference(Interface.Type.ETHERNET, 1))
        .addEqualityGroup(new InterfaceReference(Interface.Type.ETHERNET, 2))
        .addEqualityGroup(new InterfaceReference(Interface.Type.LOOPBACK, 2))
        .testEquals();
  }
}
