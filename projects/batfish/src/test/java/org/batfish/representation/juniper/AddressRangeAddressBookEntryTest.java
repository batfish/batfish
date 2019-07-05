package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.AddressRangeAddressBookEntry.rangeToWildcards;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests of {@link AddressRangeAddressBookEntry}. */
public class AddressRangeAddressBookEntryTest {
  @Test
  public void testRange() {
    assertThat(
        rangeToWildcards(Ip.ZERO, Ip.MAX),
        equalTo(ImmutableSortedSet.of(IpWildcard.create(Prefix.ZERO))));
    assertThat(
        rangeToWildcards(Ip.ZERO, Ip.ZERO),
        equalTo(ImmutableSortedSet.of(IpWildcard.create(Ip.ZERO))));
    assertThat(
        rangeToWildcards(Ip.MAX, Ip.MAX),
        equalTo(ImmutableSortedSet.of(IpWildcard.create(Ip.MAX))));

    // A few cases around 1.2.3.4
    assertThat(
        rangeToWildcards(Ip.parse("1.2.3.4"), Ip.parse("1.2.3.4")),
        equalTo(ImmutableSortedSet.of(IpWildcard.parse("1.2.3.4"))));
    assertThat(
        rangeToWildcards(Ip.parse("1.2.3.4"), Ip.parse("1.2.3.5")),
        equalTo(ImmutableSortedSet.of(IpWildcard.parse("1.2.3.4/31"))));
    assertThat(
        rangeToWildcards(Ip.parse("1.2.3.4"), Ip.parse("1.2.3.6")),
        equalTo(
            ImmutableSortedSet.of(IpWildcard.parse("1.2.3.4/31"), IpWildcard.parse("1.2.3.6"))));
    assertThat(
        rangeToWildcards(Ip.parse("1.2.3.4"), Ip.parse("1.2.3.7")),
        equalTo(ImmutableSortedSet.of(IpWildcard.parse("1.2.3.4/30"))));
    assertThat(
        rangeToWildcards(Ip.parse("1.2.3.4"), Ip.parse("1.2.3.8")),
        equalTo(
            ImmutableSortedSet.of(IpWildcard.parse("1.2.3.4/30"), IpWildcard.parse("1.2.3.8"))));

    // Can't be a /30 since it doesn't start at 0 mod 4.
    assertThat(
        rangeToWildcards(Ip.parse("1.2.3.2"), Ip.parse("1.2.3.5")),
        equalTo(
            ImmutableSortedSet.of(IpWildcard.parse("1.2.3.2/31"), IpWildcard.parse("1.2.3.4/31"))));

    // An ugly case
    assertThat(
        rangeToWildcards(Ip.parse("1.2.3.1"), Ip.parse("1.2.3.14")),
        equalTo(
            ImmutableSortedSet.of(
                IpWildcard.parse("1.2.3.1/32"),
                IpWildcard.parse("1.2.3.2/31"),
                IpWildcard.parse("1.2.3.4/30"),
                IpWildcard.parse("1.2.3.8/30"),
                IpWildcard.parse("1.2.3.12/31"),
                IpWildcard.parse("1.2.3.14/32"))));
  }
}
