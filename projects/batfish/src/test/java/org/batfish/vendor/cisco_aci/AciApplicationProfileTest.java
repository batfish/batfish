package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/** Tests of {@link AciConfiguration} for application profiles, EPGs, and deployments. */
public class AciApplicationProfileTest {

  private static String getApplicationProfileJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"app-tenant\","
        + "          \"descr\": \"Application Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"app-vrf\","
        + "                \"descr\": \"Application VRF\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"app-bd\","
        + "                \"descr\": \"Application BD\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"app-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"192.168.1.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"web-app\","
        + "                \"descr\": \"Web Application Profile\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"web-frontend\","
        + "                      \"descr\": \"Web Frontend EPG\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"app-bd\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
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

  private static String getMultipleEpgsJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"multi-tenant\","
        + "          \"descr\": \"Multi-EPG Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"multi-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"multi-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"multi-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.10.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"multi-tier-app\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"epg1\","
        + "                      \"descr\": \"First EPG\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"multi-bd\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"epg2\","
        + "                      \"descr\": \"Second EPG\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"multi-bd\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"epg3\","
        + "                      \"descr\": \"Third EPG\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"multi-bd\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
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

  private static String getEpgProviderConsumerJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"contract-tenant\","
        + "          \"descr\": \"Contract Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"contract-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"contract-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"contract-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"172.16.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"client-server-app\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"client-epg\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"contract-bd\""
        + "                          }"
        + "                        }"
        + "                      },"
        + "                      {"
        + "                        \"fvRsCons\": {"
        + "                          \"attributes\": {"
        + "                            \"tnVzBrCPName\": \"client-server-contract\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"server-epg\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"contract-bd\""
        + "                          }"
        + "                        }"
        + "                      },"
        + "                      {"
        + "                        \"fvRsProv\": {"
        + "                          \"attributes\": {"
        + "                            \"tnVzBrCPName\": \"client-server-contract\""
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
        + "                \"name\": \"client-server-contract\","
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

  private static String getEpgWithMultipleBdsJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"multi-bd-tenant\","
        + "          \"descr\": \"Multi-BD Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"multi-bd-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"bd-web\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"multi-bd-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.20.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"bd-app\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"multi-bd-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.21.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"app-profile\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"web-epg\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"bd-web\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"app-epg\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"bd-app\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
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

  private static String getEpgNamingConventionsJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"naming-tenant\","
        + "          \"descr\": \"Naming Convention Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"naming-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"bd-naming\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"naming-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.30.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"tier-1-app-profile-naming\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"web-frontend-epg-v1\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"bd-naming\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
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

  private static String getNestedApplicationProfilesJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"nested-tenant\","
        + "          \"descr\": \"Nested Application Profile Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"nested-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"nested-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"nested-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.40.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"parent-app-profile\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"child-epg-1\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"nested-bd\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"child-epg-2\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"nested-bd\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
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

  private static String getEpgIsolationJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"isolation-tenant\","
        + "          \"descr\": \"EPG Isolation Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"isolation-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"isolation-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"isolation-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.50.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"isolated-app\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"isolated-epg-1\","
        + "                      \"isAttrBasedEPgDn\": \"no\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"isolation-bd\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"isolated-epg-2\","
        + "                      \"isAttrBasedEPgDn\": \"no\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"isolation-bd\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
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

  private static String getEpgDomainBindingJson() {
    return "{"
        + "\"polUni\": {"
        + "  \"children\": ["
        + "    {"
        + "      \"fvTenant\": {"
        + "        \"attributes\": {"
        + "          \"name\": \"domain-tenant\","
        + "          \"descr\": \"EPG Domain Binding Tenant\""
        + "        },"
        + "        \"children\": ["
        + "          {"
        + "            \"fvCtx\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"domain-vrf\""
        + "              }"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvBD\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"domain-bd\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvRsCtx\": {"
        + "                    \"attributes\": {"
        + "                      \"tnFvCtxName\": \"domain-vrf\""
        + "                    }"
        + "                  }"
        + "                },"
        + "                {"
        + "                  \"fvSubnet\": {"
        + "                    \"attributes\": {"
        + "                      \"ip\": \"10.60.0.0/24\""
        + "                    }"
        + "                  }"
        + "                }"
        + "              ]"
        + "            }"
        + "          },"
        + "          {"
        + "            \"fvAp\": {"
        + "              \"attributes\": {"
        + "                \"name\": \"domain-app\""
        + "              },"
        + "              \"children\": ["
        + "                {"
        + "                  \"fvAEPg\": {"
        + "                    \"attributes\": {"
        + "                      \"name\": \"domain-epg\""
        + "                    },"
        + "                    \"children\": ["
        + "                      {"
        + "                        \"fvRsBd\": {"
        + "                          \"attributes\": {"
        + "                            \"tnFvBDName\": \"domain-bd\""
        + "                          }"
        + "                        }"
        + "                      },"
        + "                      {"
        + "                        \"fvRsDomAtt\": {"
        + "                          \"attributes\": {"
        + "                            \"tDn\": \"uni/phys-phys-domain\""
        + "                          }"
        + "                        }"
        + "                      }"
        + "                    ]"
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

  /** Test parsing application profile JSON */
  @Test
  public void testParseJson_applicationProfile() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getApplicationProfileJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("app-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("app-tenant");
    assertThat(tenant.getName(), equalTo("app-tenant"));
    assertThat(tenant.getEpgs().keySet(), hasSize(1));
    assertThat(tenant.getEpgs(), hasKey("app-tenant:web-app:web-frontend"));
  }

  /** Test parsing multiple EPGs in a single application profile */
  @Test
  public void testParseJson_multipleEpgs() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getMultipleEpgsJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("multi-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("multi-tenant");
    assertThat(tenant.getEpgs().keySet(), hasSize(3));
    assertThat(tenant.getEpgs(), hasKey("multi-tenant:multi-tier-app:epg1"));
    assertThat(tenant.getEpgs(), hasKey("multi-tenant:multi-tier-app:epg2"));
    assertThat(tenant.getEpgs(), hasKey("multi-tenant:multi-tier-app:epg3"));

    AciConfiguration.Epg epg1 = tenant.getEpgs().get("multi-tenant:multi-tier-app:epg1");
    assertThat(epg1.getDescription(), equalTo("First EPG"));
  }

  /** Test EPG provider and consumer relationships */
  @Test
  public void testEpgProviderConsumer() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(
                BatfishObjectMapper.mapper().readTree(getEpgProviderConsumerJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("contract-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("contract-tenant");
    assertThat(tenant.getEpgs().keySet(), hasSize(2));
    assertThat(tenant.getEpgs(), hasKey("contract-tenant:client-server-app:client-epg"));
    assertThat(tenant.getEpgs(), hasKey("contract-tenant:client-server-app:server-epg"));
    assertThat(tenant.getContracts(), hasKey("contract-tenant:client-server-contract"));
  }

  /** Test EPGs with multiple bridge domains */
  @Test
  public void testEpgWithMultipleBds() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getEpgWithMultipleBdsJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("multi-bd-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("multi-bd-tenant");
    assertThat(tenant.getBridgeDomains().keySet(), hasSize(2));
    assertThat(tenant.getBridgeDomains(), hasKey("multi-bd-tenant:bd-web"));
    assertThat(tenant.getBridgeDomains(), hasKey("multi-bd-tenant:bd-app"));

    AciConfiguration.Epg webEpg = tenant.getEpgs().get("multi-bd-tenant:app-profile:web-epg");
    assertThat(webEpg.getBridgeDomain(), equalTo("multi-bd-tenant:bd-web"));

    AciConfiguration.Epg appEpg = tenant.getEpgs().get("multi-bd-tenant:app-profile:app-epg");
    assertThat(appEpg.getBridgeDomain(), equalTo("multi-bd-tenant:bd-app"));
  }

  /** Test EPG naming conventions */
  @Test
  public void testEpgNamingConventions() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(
                BatfishObjectMapper.mapper().readTree(getEpgNamingConventionsJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("naming-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("naming-tenant");
    assertThat(
        tenant.getEpgs(), hasKey("naming-tenant:tier-1-app-profile-naming:web-frontend-epg-v1"));

    AciConfiguration.Epg epg =
        tenant.getEpgs().get("naming-tenant:tier-1-app-profile-naming:web-frontend-epg-v1");
    assertThat(
        epg.getName(), equalTo("naming-tenant:tier-1-app-profile-naming:web-frontend-epg-v1"));
  }

  /** Test nested application profiles */
  @Test
  public void testNestedApplicationProfiles() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(
                BatfishObjectMapper.mapper().readTree(getNestedApplicationProfilesJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("nested-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("nested-tenant");
    assertThat(tenant.getEpgs().keySet(), hasSize(2));
    assertThat(tenant.getEpgs(), hasKey("nested-tenant:parent-app-profile:child-epg-1"));
    assertThat(tenant.getEpgs(), hasKey("nested-tenant:parent-app-profile:child-epg-2"));

    for (AciConfiguration.Epg epg : tenant.getEpgs().values()) {
      assertThat(epg.getBridgeDomain(), equalTo("nested-tenant:nested-bd"));
    }
  }

  /** Test EPG isolation */
  @Test
  public void testEpgIsolation() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getEpgIsolationJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("isolation-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("isolation-tenant");
    assertThat(tenant.getEpgs().keySet(), hasSize(2));
    assertThat(tenant.getEpgs(), hasKey("isolation-tenant:isolated-app:isolated-epg-1"));
    assertThat(tenant.getEpgs(), hasKey("isolation-tenant:isolated-app:isolated-epg-2"));
  }

  /** Test EPG domain binding */
  @Test
  public void testEpgDomainBinding() throws Exception {
    String configText =
        BatfishObjectMapper.mapper()
            .writeValueAsString(BatfishObjectMapper.mapper().readTree(getEpgDomainBindingJson()));

    AciConfiguration config =
        AciConfiguration.fromJson("test-config.json", configText, new Warnings());

    assertThat(config.getTenants(), hasKey("domain-tenant"));
    AciConfiguration.Tenant tenant = config.getTenants().get("domain-tenant");
    assertThat(tenant.getEpgs(), hasKey("domain-tenant:domain-app:domain-epg"));

    AciConfiguration.Epg epg = tenant.getEpgs().get("domain-tenant:domain-app:domain-epg");
    assertThat(epg.getBridgeDomain(), equalTo("domain-tenant:domain-bd"));
  }
}
