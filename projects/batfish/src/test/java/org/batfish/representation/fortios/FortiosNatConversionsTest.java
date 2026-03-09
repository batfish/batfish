package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link FortiosNatConversions}. */
public class FortiosNatConversionsTest {

  private Warnings _w;
  private static final String FILENAME = "test_config";

  @Before
  public void setup() {
    _w = new Warnings(true, true, true);
  }

  @Test
  public void testToSourceNatTransformation_startipEndip() {
    Ippool pool = new Ippool("pool1", new BatfishUUID(1));
    pool.setStartip(Ip.parse("10.0.0.1"));
    pool.setEndip(Ip.parse("10.0.0.10"));

    Optional<Transformation> result =
        FortiosNatConversions.toSourceNatTransformation(pool, _w, FILENAME);

    assertThat(result.isPresent(), equalTo(true));
    Transformation transformation = result.get();
    assertThat(
        transformation.getTransformationSteps(),
        hasItem(instanceOf(AssignIpAddressFromPool.class)));

    AssignIpAddressFromPool step =
        (AssignIpAddressFromPool) transformation.getTransformationSteps().get(0);
    assertThat(step.getType(), equalTo(TransformationType.SOURCE_NAT));
    assertThat(step.getIpField(), equalTo(IpField.SOURCE));

    RangeSet<Ip> ranges = step.getIpRanges();
    assertThat(
        ranges.encloses(Range.closed(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.10"))), equalTo(true));
  }

  @Test
  public void testToSourceNatTransformation_prefix() {
    Ippool pool = new Ippool("pool2", new BatfishUUID(2));
    pool.setPrefixIp(Ip.parse("192.168.1.0"));
    pool.setPrefixNetmask(Ip.parse("255.255.255.0"));

    Optional<Transformation> result =
        FortiosNatConversions.toSourceNatTransformation(pool, _w, FILENAME);

    assertThat(result.isPresent(), equalTo(true));
    AssignIpAddressFromPool step =
        (AssignIpAddressFromPool) result.get().getTransformationSteps().get(0);

    RangeSet<Ip> ranges = step.getIpRanges();
    // Should contain the full /24 range
    assertThat(
        ranges.encloses(Range.closed(Ip.parse("192.168.1.0"), Ip.parse("192.168.1.255"))),
        equalTo(true));
  }

  @Test
  public void testToSourceNatTransformation_invalidRange() {
    Ippool pool = new Ippool("pool3", new BatfishUUID(3));
    pool.setStartip(Ip.parse("10.0.0.10"));
    pool.setEndip(Ip.parse("10.0.0.1")); // end < start

    Optional<Transformation> result =
        FortiosNatConversions.toSourceNatTransformation(pool, _w, FILENAME);

    assertThat(result.isPresent(), equalTo(false));
    // Check that a warning was generated
    assertThat(
        _w.getRedFlagWarnings().stream().anyMatch(w -> w.getText().contains("invalid range")),
        equalTo(true));
  }

  @Test
  public void testToSourceNatTransformation_noConfig() {
    Ippool pool = new Ippool("pool4", new BatfishUUID(4));
    // No startip/endip or prefix configured

    Optional<Transformation> result =
        FortiosNatConversions.toSourceNatTransformation(pool, _w, FILENAME);

    assertThat(result.isPresent(), equalTo(false));
    assertThat(
        _w.getRedFlagWarnings().stream()
            .anyMatch(w -> w.getText().contains("no IP range configuration")),
        equalTo(true));
  }

  @Test
  public void testComputeOutgoingTransformation_noMatchingPolicy() {
    Policy policy = new Policy("1");
    policy.setStatus(Policy.Status.ENABLE);
    policy.setNat(false); // NAT disabled
    policy.getDstIntf().add("port1");

    ImmutableMap<String, Policy> policies = ImmutableMap.of("1", policy);
    ImmutableMap<String, Ippool> ippools = ImmutableMap.of();

    org.batfish.datamodel.Configuration c =
        org.batfish.datamodel.Configuration.builder()
            .setHostname("test")
            .setConfigurationFormat(org.batfish.datamodel.ConfigurationFormat.FORTIOS)
            .build();

    Transformation result =
        FortiosNatConversions.computeOutgoingTransformation(
            "port1", policies, ippools, c, _w, FILENAME);

    assertThat(result, nullValue());
  }

  @Test
  public void testComputeOutgoingTransformation_withNatAndIppool() {
    // Create IP pool
    Ippool pool = new Ippool("nat_pool", new BatfishUUID(1));
    pool.setStartip(Ip.parse("203.0.113.1"));
    pool.setEndip(Ip.parse("203.0.113.10"));

    // Create policy with NAT and IP pool
    Policy policy = new Policy("1");
    policy.setStatus(Policy.Status.ENABLE);
    policy.setNat(true);
    policy.setIppool(true);
    policy.setPoolnames(ImmutableSet.of("nat_pool"));
    policy.getDstIntf().add("wan1");

    ImmutableMap<String, Policy> policies = ImmutableMap.of("1", policy);
    ImmutableMap<String, Ippool> ippools = ImmutableMap.of("nat_pool", pool);

    org.batfish.datamodel.Configuration c =
        org.batfish.datamodel.Configuration.builder()
            .setHostname("test")
            .setConfigurationFormat(org.batfish.datamodel.ConfigurationFormat.FORTIOS)
            .build();

    Transformation result =
        FortiosNatConversions.computeOutgoingTransformation(
            "wan1", policies, ippools, c, _w, FILENAME);

    assertThat(result, notNullValue());
    AssignIpAddressFromPool step = (AssignIpAddressFromPool) result.getTransformationSteps().get(0);
    assertThat(step.getType(), equalTo(TransformationType.SOURCE_NAT));
    assertThat(step.getIpField(), equalTo(IpField.SOURCE));
  }

  @Test
  public void testComputeOutgoingTransformation_wrongInterface() {
    // Create IP pool
    Ippool pool = new Ippool("nat_pool", new BatfishUUID(1));
    pool.setStartip(Ip.parse("203.0.113.1"));
    pool.setEndip(Ip.parse("203.0.113.10"));

    // Create policy for wan1
    Policy policy = new Policy("1");
    policy.setStatus(Policy.Status.ENABLE);
    policy.setNat(true);
    policy.setIppool(true);
    policy.setPoolnames(ImmutableSet.of("nat_pool"));
    policy.getDstIntf().add("wan1");

    ImmutableMap<String, Policy> policies = ImmutableMap.of("1", policy);
    ImmutableMap<String, Ippool> ippools = ImmutableMap.of("nat_pool", pool);

    org.batfish.datamodel.Configuration c =
        org.batfish.datamodel.Configuration.builder()
            .setHostname("test")
            .setConfigurationFormat(org.batfish.datamodel.ConfigurationFormat.FORTIOS)
            .build();

    // Query for different interface
    Transformation result =
        FortiosNatConversions.computeOutgoingTransformation(
            "lan1", policies, ippools, c, _w, FILENAME);

    assertThat(result, nullValue());
  }

  @Test
  public void testComputeOutgoingTransformation_missingPool() {
    // Create policy referencing non-existent pool
    Policy policy = new Policy("1");
    policy.setStatus(Policy.Status.ENABLE);
    policy.setNat(true);
    policy.setIppool(true);
    policy.setPoolnames(ImmutableSet.of("missing_pool"));
    policy.getDstIntf().add("wan1");

    ImmutableMap<String, Policy> policies = ImmutableMap.of("1", policy);
    ImmutableMap<String, Ippool> ippools = ImmutableMap.of(); // Empty pools

    org.batfish.datamodel.Configuration c =
        org.batfish.datamodel.Configuration.builder()
            .setHostname("test")
            .setConfigurationFormat(org.batfish.datamodel.ConfigurationFormat.FORTIOS)
            .build();

    Transformation result =
        FortiosNatConversions.computeOutgoingTransformation(
            "wan1", policies, ippools, c, _w, FILENAME);

    assertThat(result, nullValue());
    assertThat(
        _w.getRedFlagWarnings().stream()
            .anyMatch(w -> w.getText().contains("non-existent IP pool")),
        equalTo(true));
  }
}
