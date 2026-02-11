package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/** Tests of {@link AciConfiguration} for monitoring, logging, and observability. */
public class AciMonitoringTest {

  private static String getMonitoringJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"monitoring-tenant\","
        + "          \"descr\": \"Monitoring Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"monitoring-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"monitoring-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"monitoring-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.100.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    },"
        + "    {"
        + "      \"syslog\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"syslog-config\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getSyslogJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"syslog-tenant\","
        + "          \"descr\": \"Syslog Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"syslog-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"syslog-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"syslog-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.101.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    },"
        + "    {"
        + "      \"syslogRemoteDest\": {"
        + "        \"attributes\": {"
        + "          \"hostname\": \"syslog-server1\","
        + "          \"port\": \"514\""
        + "        }"
        + "      }"
        + "    },"
        + "    {"
        + "      \"syslogRemoteDest\": {"
        + "        \"attributes\": {"
        + "          \"hostname\": \"syslog-server2\","
        + "          \"port\": \"514\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getMonitoringPolicyJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"monitor-policy-tenant\","
        + "          \"descr\": \"Monitoring Policy Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"monitor-policy-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"monitor-policy-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"monitor-policy-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.102.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    },"
        + "    {"
        + "      \"monitorPolicy\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"monitor-policy-1\","
        + "          \"monitoringLevel\": \"detailed\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getStatisticsCollectionJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"stats-tenant\","
        + "          \"descr\": \"Statistics Collection Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"stats-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"stats-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"stats-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.103.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    },"
        + "    {"
        + "      \"statsCollectionPolicy\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"stats-policy\","
        + "          \"granularity\": \"60\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getLoggingConfigurationJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"logging-tenant\","
        + "          \"descr\": \"Logging Configuration Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"logging-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"logging-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"logging-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.104.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    },"
        + "    {"
        + "      \"loggingPolicy\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"logging-policy\","
        + "          \"severity\": \"info\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getHealthScoringJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"health-tenant\","
        + "          \"descr\": \"Health Scoring Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"health-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"health-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"health-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.105.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    },"
        + "    {"
        + "      \"healthScore\": {"
        + "        \"attributes\": {"
        + "          \"score\": \"95\","
        + "          \"timestamp\": \"2024-01-01T00:00:00Z\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getFaultDetectionJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"fault-tenant\","
        + "          \"descr\": \"Fault Detection Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"fault-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"fault-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"fault-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.106.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    },"
        + "    {"
        + "      \"fault\": {"
        + "        \"attributes\": {"
        + "          \"code\": \"F0001\","
        + "          \"severity\": \"critical\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getMonitoringProfilesJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"profile-tenant\","
        + "          \"descr\": \"Monitoring Profiles Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"profile-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"profile-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"profile-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.107.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    },"
        + "    {"
        + "      \"monitoringProfile\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"profile-1\","
        + "          \"level\": \"advanced\""
        + "        }"
        + "      }"
        + "    },"
        + "    {"
        + "      \"monitoringProfile\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"profile-2\","
        + "          \"level\": \"standard\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  /** Test parsing monitoring JSON */
  @Test
  public void testParseJson_monitoring() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getMonitoringJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("monitoring-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("monitoring-tenant");
    assertThat(tenant.getName(), equalTo("monitoring-tenant"));
    assertThat(tenant.getVrfs(), hasKey("monitoring-tenant:monitoring-vrf"));
  }

  /** Test parsing syslog JSON */
  @Test
  public void testParseJson_syslog() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getSyslogJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("syslog-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("syslog-tenant");
    assertThat(tenant.getBridgeDomains(), hasKey("syslog-tenant:syslog-bd"));
  }

  /** Test monitoring policy */
  @Test
  public void testMonitoringPolicy() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getMonitoringPolicyJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("monitor-policy-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("monitor-policy-tenant");
    assertThat(tenant.getVrfs(), hasKey("monitor-policy-tenant:monitor-policy-vrf"));
  }

  /** Test statistics collection */
  @Test
  public void testStatisticsCollection() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(
                BatfishObjectMapper.mapper().readTree(getStatisticsCollectionJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("stats-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("stats-tenant");
    assertThat(tenant.getBridgeDomains(), hasKey("stats-tenant:stats-bd"));
  }

  /** Test logging configuration */
  @Test
  public void testLoggingConfiguration() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(
                BatfishObjectMapper.mapper().readTree(getLoggingConfigurationJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("logging-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("logging-tenant");
    assertThat(tenant.getVrfs(), hasKey("logging-tenant:logging-vrf"));
  }

  /** Test health scoring */
  @Test
  public void testHealthScoring() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getHealthScoringJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("health-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("health-tenant");
    assertThat(tenant.getBridgeDomains(), hasKey("health-tenant:health-bd"));
  }

  /** Test fault detection */
  @Test
  public void testFaultDetection() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getFaultDetectionJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("fault-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("fault-tenant");
    assertThat(tenant.getVrfs(), hasKey("fault-tenant:fault-vrf"));
  }

  /** Test monitoring profiles */
  @Test
  public void testMonitoringProfiles() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getMonitoringProfilesJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("profile-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("profile-tenant");
    assertThat(tenant.getBridgeDomains(), hasKey("profile-tenant:profile-bd"));
  }
}
