package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests of {@link VrrpGroup} */
public class VrrpGroupTest {

  @Test
  public void testSerialization() {
    VrrpGroup obj =
        VrrpGroup.builder()
            .setPreempt(true)
            .setPriority(1)
            .setVirtualAddresses(Ip.parse("2.2.2.2"))
            .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 24))
            .build();
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    VrrpGroup.Builder builder = VrrpGroup.builder();
    VrrpGroup obj = builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, builder.build())
        .addEqualityGroup(builder.setPreempt(true).build())
        .addEqualityGroup(builder.setPriority(1).build())
        .addEqualityGroup(
            builder
                .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 24))
                .build())
        .addEqualityGroup(builder.setVirtualAddresses(Ip.parse("2.2.2.2")).build())
        .testEquals();
  }
}
