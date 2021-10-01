package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class IspAttachmentTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IspAttachment(null, "iface", null), new IspAttachment(null, "iface", null))
        .addEqualityGroup(new IspAttachment("other", "iface", null))
        .addEqualityGroup(new IspAttachment(null, "other", null))
        .addEqualityGroup(new IspAttachment(null, "iface", 1))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    IspAttachment attachment = new IspAttachment("host", "iface", 1);
    assertThat(BatfishObjectMapper.clone(attachment, IspAttachment.class), equalTo(attachment));
  }
}
