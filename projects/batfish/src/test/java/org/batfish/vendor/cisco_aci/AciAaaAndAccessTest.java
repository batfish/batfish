package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.Tenant;
import org.junit.Test;

/** Tests of {@link AciConfiguration} for authentication, authorization, and access control. */
public class AciAaaAndAccessTest {

  private static String getAaaConfigurationJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"aaa-tenant\","
        + "          \"descr\": \"AAA Configuration Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"aaa-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"aaa-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"aaa-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.20.0.0/24\""
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
        + "      \"aaaDomain\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"default\","
        + "          \"descr\": \"Default AAA Domain\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getRbacJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"rbac-tenant\","
        + "          \"descr\": \"RBAC Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"rbac-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"rbac-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"rbac-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.21.0.0/24\""
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
        + "      \"aaaRole\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"admin-role\","
        + "          \"descr\": \"Administrator Role\""
        + "        }"
        + "      }"
        + "    },"
        + "    {"
        + "      \"aaaRole\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"read-only-role\","
        + "          \"descr\": \"Read Only Role\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getUserAuthenticationJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"auth-tenant\","
        + "          \"descr\": \"User Authentication Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"auth-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"auth-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"auth-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.22.0.0/24\""
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
        + "      \"aaaUser\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"admin-user\","
        + "          \"descr\": \"Admin User\","
        + "          \"accountStatus\": \"active\""
        + "        }"
        + "      }"
        + "    },"
        + "    {"
        + "      \"aaaUser\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"readonly-user\","
        + "          \"descr\": \"Read-Only User\","
        + "          \"accountStatus\": \"active\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getRoleAssignmentJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"role-tenant\","
        + "          \"descr\": \"Role Assignment Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"role-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"role-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"role-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.23.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getAccessControlJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"access-tenant\","
        + "          \"descr\": \"Access Control Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"access-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"access-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"access-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.24.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getAuditLoggingJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"audit-tenant\","
        + "          \"descr\": \"Audit Logging Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"audit-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"audit-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"audit-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.25.0.0/24\""
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
        + "      \"aaaAuditLog\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"audit-log\","
        + "          \"logLevel\": \"info\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getCertificateConfigurationJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"cert-tenant\","
        + "          \"descr\": \"Certificate Configuration Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"cert-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"cert-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"cert-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.26.0.0/24\""
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
        + "      \"pkiTP\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"server-cert\","
        + "          \"certChain\": \"-----BEGIN CERTIFICATE-----\","
        + "          \"keyType\": \"rsa\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getLocalAuthenticationJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"local-auth-tenant\","
        + "          \"descr\": \"Local Authentication Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"local-auth-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"local-auth-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"local-auth-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.27.0.0/24\""
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
        + "      \"aaaLocalUser\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"local-admin\","
        + "          \"accountStatus\": \"active\","
        + "          \"pwd\": \"encrypted-password\""
        + "        }"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  /** Test parsing AAA configuration JSON */
  @Test
  public void testParseJson_aaaConfiguration() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getAaaConfigurationJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("aaa-tenant"));
    Tenant tenant = config.getTenants().get("aaa-tenant");
    assertThat(tenant.getName(), equalTo("aaa-tenant"));
  }

  /** Test parsing RBAC JSON */
  @Test
  public void testParseJson_rbac() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getRbacJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("rbac-tenant"));
    Tenant tenant = config.getTenants().get("rbac-tenant");
    assertThat(tenant.getVrfs(), hasKey("rbac-tenant:rbac-vrf"));
  }

  /** Test user authentication */
  @Test
  public void testUserAuthentication() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getUserAuthenticationJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("auth-tenant"));
    Tenant tenant = config.getTenants().get("auth-tenant");
    assertThat(tenant.getBridgeDomains(), hasKey("auth-tenant:auth-bd"));
  }

  /** Test role assignment */
  @Test
  public void testRoleAssignment() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getRoleAssignmentJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("role-tenant"));
    Tenant tenant = config.getTenants().get("role-tenant");
    assertThat(tenant.getVrfs(), hasKey("role-tenant:role-vrf"));
  }

  /** Test access control */
  @Test
  public void testAccessControl() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getAccessControlJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("access-tenant"));
    Tenant tenant = config.getTenants().get("access-tenant");
    assertThat(tenant.getBridgeDomains(), hasKey("access-tenant:access-bd"));
  }

  /** Test audit logging */
  @Test
  public void testAuditLogging() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getAuditLoggingJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("audit-tenant"));
    Tenant tenant = config.getTenants().get("audit-tenant");
    assertThat(tenant.getVrfs(), hasKey("audit-tenant:audit-vrf"));
  }

  /** Test certificate configuration */
  @Test
  public void testCertificateConfiguration() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(
                BatfishObjectMapper.mapper().readTree(getCertificateConfigurationJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("cert-tenant"));
    Tenant tenant = config.getTenants().get("cert-tenant");
    assertThat(tenant.getName(), equalTo("cert-tenant"));
  }

  /** Test local authentication */
  @Test
  public void testLocalAuthentication() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(
                BatfishObjectMapper.mapper().readTree(getLocalAuthenticationJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("local-auth-tenant"));
    Tenant tenant = config.getTenants().get("local-auth-tenant");
    assertThat(tenant.getBridgeDomains(), hasKey("local-auth-tenant:local-auth-bd"));
  }
}
