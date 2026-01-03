package org.batfish.datamodel.acl;

import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.batfish.datamodel.ExprAclLine.rejectingHeaderSpace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.junit.Before;
import org.junit.Test;

public class CanonicalAclTest {

  private Configuration _c;
  private Configuration _c2;

  private IpAccessList.Builder _aclb;
  private IpAccessList.Builder _aclb2;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c = cb.build();
    _c2 = cb.build();
    _aclb = nf.aclBuilder().setOwner(_c);
    _aclb2 = nf.aclBuilder().setOwner(_c2);
  }

  @Test
  public void testIdenticalAclsWithIdenticalDependenciesEqual() {
    // acl1 & acl2 are identical acls on different hosts that reference identical ACLs & interfaces
    IpAccessList acl1 =
        _aclb
            .setName("acl1")
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting()
                        .setMatchCondition(new PermittedByAcl("referencedAcl"))
                        .build()))
            .build();
    IpAccessList acl2 =
        _aclb2
            .setName("acl2")
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting()
                        .setMatchCondition(new PermittedByAcl("referencedAcl"))
                        .build()))
            .build();
    IpAccessList referencedAcl1 =
        _aclb
            .setName("referencedAcl")
            .setLines(
                ImmutableList.of(
                    rejectingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .build();
    IpAccessList referencedAcl2 =
        _aclb2
            .setName("referencedAcl")
            .setLines(
                ImmutableList.of(
                    rejectingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .build();

    // Canonical acls for acl1 and acl2 should match
    CanonicalAcl canonicalAcl1 =
        new CanonicalAcl(
            acl1,
            acl1,
            ImmutableMap.of("referencedAcl", referencedAcl1),
            ImmutableSet.of("iface"),
            ImmutableSet.of(),
            ImmutableSet.of());
    CanonicalAcl canonicalAcl2 =
        new CanonicalAcl(
            acl2,
            acl2,
            ImmutableMap.of("referencedAcl", referencedAcl2),
            ImmutableSet.of("iface"),
            ImmutableSet.of(),
            ImmutableSet.of());

    assertThat(canonicalAcl1, equalTo(canonicalAcl2));
  }

  @Test
  public void testDifferentAclsNotEqual() {
    // acl1 and acl2 are different.
    IpAccessList acl1 =
        _aclb
            .setName("acl1")
            .setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("10.10.10.10/8").toIpSpace())
                            .build())))
            .build();
    IpAccessList acl2 =
        _aclb2
            .setName("acl2")
            .setLines(
                ImmutableList.of(
                    rejectingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .build();

    // Canonical acls for acl1 and acl2 shouldn't match since they are different
    CanonicalAcl canonicalAcl1 =
        new CanonicalAcl(
            acl1, acl1, ImmutableMap.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());
    CanonicalAcl canonicalAcl2 =
        new CanonicalAcl(
            acl2, acl2, ImmutableMap.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());

    assertThat(canonicalAcl1, not(equalTo(canonicalAcl2)));
  }

  @Test
  public void testAclsWithDifferentDependenciesNotEqual() {
    // acl1 and acl2 are identical acls on different configs that both reference referencedAcl, but
    // the two versions of referencedAcl are different
    IpAccessList acl1 =
        _aclb
            .setName("acl1")
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting()
                        .setMatchCondition(new PermittedByAcl("referencedAcl"))
                        .build()))
            .build();
    IpAccessList acl2 =
        _aclb2
            .setName("acl2")
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting()
                        .setMatchCondition(new PermittedByAcl("referencedAcl"))
                        .build()))
            .build();
    IpAccessList referencedAcl1 =
        _aclb
            .setName("referencedAcl")
            .setLines(
                ImmutableList.of(
                    rejectingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .build();
    IpAccessList referencedAcl2 =
        _aclb2
            .setName("referencedAcl")
            .setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("10.10.10.10/8").toIpSpace())
                            .build())))
            .build();

    // Canonical acls for acl1 and acl2 shouldn't match since references are different
    CanonicalAcl canonicalAcl1 =
        new CanonicalAcl(
            acl1,
            acl1,
            ImmutableMap.of("referencedAcl", referencedAcl1),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of());
    CanonicalAcl canonicalAcl2 =
        new CanonicalAcl(
            acl2,
            acl2,
            ImmutableMap.of("referencedAcl", referencedAcl2),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of());

    assertThat(canonicalAcl1, not(equalTo(canonicalAcl2)));
  }

  @Test
  public void testAclsWithDifferentInterfacesNotEqual() {
    IpAccessList acl1 =
        _aclb
            .setName("acl1")
            .setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .build();

    // Since interface dependencies don't match, canonical acls shouldn't be equal
    CanonicalAcl canonicalAcl1 =
        new CanonicalAcl(
            acl1,
            acl1,
            ImmutableMap.of(),
            ImmutableSet.of("iface"),
            ImmutableSet.of(),
            ImmutableSet.of());
    CanonicalAcl canonicalAcl2 =
        new CanonicalAcl(
            acl1,
            acl1,
            ImmutableMap.of(),
            ImmutableSet.of("iface", "iface2"),
            ImmutableSet.of(),
            ImmutableSet.of());

    assertThat(canonicalAcl1, not(equalTo(canonicalAcl2)));
  }
}
