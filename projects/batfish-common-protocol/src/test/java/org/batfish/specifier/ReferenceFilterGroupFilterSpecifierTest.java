package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.referencelibrary.FilterGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReferenceFilterGroupFilterSpecifierTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  private static final String _refBookName = "book1";
  private static final String _filterGroupName = "filterGroup1";
  private static final String _interfaceName = "eth0";
  private static final String _nodeName = "node0";
  private static final MockSpecifierContext _ctxt;

  private static final IpAccessList _filter1 = IpAccessList.builder().setName("filter1").build();
  private static final IpAccessList _filter2 = IpAccessList.builder().setName("filter2").build();
  private static final IpAccessList _filter3 = IpAccessList.builder().setName("filter3").build();

  static {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder().setHostname(_nodeName);
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    // Vrf.Builder vb = nf.vrfBuilder();
    Interface.Builder ib = nf.interfaceBuilder();

    Configuration n1 = cb.build();

    ib.setOwner(n1)
        .setName(_interfaceName)
        .setIncomingFilter(_filter1)
        .setIncomingFilter(_filter2)
        .setOutgoingFilter(_filter3)
        .build();

    n1.getIpAccessLists()
        .putAll(
            ImmutableMap.of(
                _filter1.getName(),
                _filter1,
                _filter2.getName(),
                _filter2,
                _filter3.getName(),
                _filter3));

    ReferenceBook book =
        ReferenceBook.builder(_refBookName)
            .setFilterGroups(
                ImmutableList.of(
                    new FilterGroup(
                        ImmutableList.of(
                            _filter1.getName(),
                            "@out(" + _interfaceName + ")", // should match _filter3
                            ""), // should match nothing; shouldn't accidentally match everything
                        _filterGroupName)))
            .build();

    _ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of(_nodeName, n1))
            .setReferenceBooks(ImmutableSortedSet.of(book))
            .build();
  }

  @Test
  public void resolve() {
    assertThat(
        new ReferenceFilterGroupFilterSpecifier(_filterGroupName, _refBookName)
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_filter1, _filter3)));
  }
}
