package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/** Tests of {@link AciConfiguration} for policy objects and enforcement. */
public class AciPolicyTest {

  private static String getPolicyObjectsJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"policy-tenant\","
        + "          \"descr\": \"Policy Objects Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"policy-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"policy-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"policy-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"192.168.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"policy-contract\","
        + "                \"scope\": \"tenant\","
        + "                \"intent\": \"install\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzFilter\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"policy-filter\","
        + "                \"descr\": \"Policy Filter\""
        + "              }"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getPolicyEnforcementJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"enforcement-tenant\","
        + "          \"descr\": \"Policy Enforcement Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"enforcement-vrf\","
        + "                \"pcEnfPref\": \"enforced\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"enforcement-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"enforcement-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.1.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"enforcement-app\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"epg-a\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"enforcement-bd\""
        + "                          }"
        + "                        }"
        + "                      },"
        + "                      {"
        + "                        \"fvRsCons\": {"
        + "                          \"attributes\": {"
        + "                            \"tnVzBrCPName\": \"enforce-contract\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"epg-b\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"enforcement-bd\""
        + "                          }"
        + "                        }"
        + "                      },"
        + "                      {"
        + "                        \"fvRsProv\": {"
        + "                          \"attributes\": {"
        + "                            \"tnVzBrCPName\": \"enforce-contract\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"enforce-contract\","
        + "                \"scope\": \"tenant\""
        + "              }"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getPolicyTargetsJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"targets-tenant\","
        + "          \"descr\": \"Policy Targets Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"targets-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"targets-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"targets-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.2.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"target-contract\","
        + "                \"scope\": \"global\""
        + "              }"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getPolicyScopesJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"scopes-tenant\","
        + "          \"descr\": \"Policy Scopes Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"scopes-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"scopes-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"scopes-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.3.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"tenant-scoped-contract\","
        + "                \"scope\": \"tenant\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"app-scoped-contract\","
        + "                \"scope\": \"application-profile\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"epg-scoped-contract\","
        + "                \"scope\": \"epg\""
        + "              }"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getPolicyPriorityJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"priority-tenant\","
        + "          \"descr\": \"Policy Priority Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"priority-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"priority-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"priority-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.4.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"high-priority-contract\","
        + "                \"priority\": \"level1\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"low-priority-contract\","
        + "                \"priority\": \"level3\""
        + "              }"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getPolicyCombinationsJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"combo-tenant\","
        + "          \"descr\": \"Policy Combinations Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"combo-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"combo-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"combo-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.5.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"contract1\","
        + "                \"scope\": \"tenant\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"contract2\","
        + "                \"scope\": \"tenant\""
        + "              }"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  private static String getPolicyDefaultsJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"defaults-tenant\","
        + "          \"descr\": \"Policy Defaults Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"defaults-vrf\","
        + "                \"pcEnfPref\": \"enforced\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"defaults-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"defaults-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.6.0.0/24\""
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

  private static String getPolicyInheritanceJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"inheritance-tenant\","
        + "          \"descr\": \"Policy Inheritance Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"inheritance-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"inheritance-bd\","
        + "                \"intersectionPolicyMode\": \"policy-undef\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"inheritance-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.7.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"vzBrCP\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"inherit-contract\","
        + "                \"scope\": \"context\""
        + "              }"
        + "            }"
        + "          }"
        + "        ]"
        + "      }"
        + "    }"
        + "  ]"
        + "}"
        + "}";
  }

  /** Test parsing policy objects JSON */
  @Test
  public void testParseJson_policyObjects() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getPolicyObjectsJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("policy-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("policy-tenant");
    assertThat(tenant.getContracts(), hasKey("policy-tenant:policy-contract"));

    AciConfiguration.Contract contract = tenant.getContracts().get("policy-tenant:policy-contract");
    assertThat(contract.getName(), equalTo("policy-tenant:policy-contract"));
    assertThat(contract.getScope(), equalTo("tenant"));
  }

  /** Test parsing policy enforcement JSON */
  @Test
  public void testParseJson_policyEnforcement() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getPolicyEnforcementJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("enforcement-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("enforcement-tenant");
    assertThat(tenant.getEpgs().keySet(), hasSize(2));
    assertThat(tenant.getEpgs(), hasKey("enforcement-tenant:enforcement-app:epg-a"));
    assertThat(tenant.getEpgs(), hasKey("enforcement-tenant:enforcement-app:epg-b"));
  }

  /** Test policy targets */
  @Test
  public void testPolicyTargets() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getPolicyTargetsJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("targets-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("targets-tenant");
    assertThat(tenant.getContracts(), hasKey("targets-tenant:target-contract"));

    AciConfiguration.Contract contract =
        tenant.getContracts().get("targets-tenant:target-contract");
    assertThat(contract.getScope(), equalTo("global"));
  }

  /** Test policy scopes */
  @Test
  public void testPolicyScopes() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getPolicyScopesJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("scopes-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("scopes-tenant");
    assertThat(tenant.getContracts().keySet(), hasSize(3));
    assertThat(tenant.getContracts(), hasKey("scopes-tenant:tenant-scoped-contract"));
    assertThat(tenant.getContracts(), hasKey("scopes-tenant:app-scoped-contract"));
    assertThat(tenant.getContracts(), hasKey("scopes-tenant:epg-scoped-contract"));
  }

  /** Test policy priority */
  @Test
  public void testPolicyPriority() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getPolicyPriorityJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("priority-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("priority-tenant");
    assertThat(tenant.getContracts().keySet(), hasSize(2));
    assertThat(tenant.getContracts(), hasKey("priority-tenant:high-priority-contract"));
    assertThat(tenant.getContracts(), hasKey("priority-tenant:low-priority-contract"));
  }

  /** Test policy combinations */
  @Test
  public void testPolicyCombinations() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getPolicyCombinationsJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("combo-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("combo-tenant");
    assertThat(tenant.getContracts().keySet(), hasSize(2));
    assertThat(tenant.getContracts(), hasKey("combo-tenant:contract1"));
    assertThat(tenant.getContracts(), hasKey("combo-tenant:contract2"));
  }

  /** Test policy defaults */
  @Test
  public void testPolicyDefaults() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getPolicyDefaultsJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("defaults-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("defaults-tenant");
    assertThat(tenant.getVrfs(), hasKey("defaults-tenant:defaults-vrf"));
  }

  /** Test policy inheritance */
  @Test
  public void testPolicyInheritance() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getPolicyInheritanceJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("inheritance-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("inheritance-tenant");
    assertThat(tenant.getContracts(), hasKey("inheritance-tenant:inherit-contract"));

    AciConfiguration.Contract contract =
        tenant.getContracts().get("inheritance-tenant:inherit-contract");
    assertThat(contract.getScope(), equalTo("context"));
  }
}
