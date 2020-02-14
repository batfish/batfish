package org.batfish.representation.palo_alto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Range;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.Prefix;
import org.batfish.representation.palo_alto.AddressObject.Type;
import org.junit.Test;

/** Tests of {@link AddressObject} */
public class AddressObjectTest {

  @Test
  public void testSetClearsTypeAndMembers() {
    AddressObject a = new AddressObject("name");
    assertNull(a.getIp());
    assertNull(a.getType());

    a.setIp(Ip.ZERO);
    assertThat(a.getIp(), equalTo(Ip.ZERO));
    assertThat(a.getType(), equalTo(Type.IP));

    // Setting prefix clears members and updates type
    a.setPrefix(Prefix.ZERO);
    assertNull(a.getIp());
    assertThat(a.getPrefix(), equalTo(Prefix.ZERO));
    assertThat(a.getType(), equalTo(Type.PREFIX));

    // Setting range clears members and updates type
    Range<Ip> range = Range.closed(Ip.ZERO, Ip.parse("1.1.1.1"));
    a.setIpRange(range);
    assertNull(a.getPrefix());
    assertThat(a.getIpRange(), equalTo(range));
    assertThat(a.getType(), equalTo(Type.IP_RANGE));

    // Setting IP clears members and updates type
    a.setIp(Ip.ZERO);
    assertNull(a.getIpRange());
    assertThat(a.getIp(), equalTo(Ip.ZERO));
    assertThat(a.getType(), equalTo(Type.IP));

    // Setting IP to null clears type and members
    a.setIp(null);
    assertNull(a.getIp());
    assertNull(a.getType());
  }
}
