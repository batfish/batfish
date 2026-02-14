package org.batfish.vendor.cisco_ftd.grammar;

import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Access_group_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Access_group_tailContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Access_list_actionContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Access_list_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Acl_address_specContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Acl_advancedContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Acl_extendedContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Acl_implicit_extendedContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Acl_remarkContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Arp_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Bgp_address_familyContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Bgp_neighborContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Bgp_router_idContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Cip_setContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Class_map_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Class_map_tailContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Cm_match_addressContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Cm_setContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Crypto_ikev2_policyContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Crypto_ipsec_profileContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Crypto_ipsec_transform_setContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Crypto_mapContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Crypto_map_interfaceContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Failover_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Hostname_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.If_descriptionContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.If_ip_addressContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.If_nameifContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.If_no_shutdownContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.If_security_levelContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.If_shutdownContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.If_vlanContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.If_vrfContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Ikev2_policy_attrContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Interface_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Mtu_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Names_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Nat_addressContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Nat_destinationContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Nat_ruleContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Nat_serviceContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Nat_service_portContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Nat_sourceContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Nat_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Object_fqdnContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Object_group_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Object_hostContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Object_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Object_subnetContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Og_group_objectContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Og_network_objectContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Og_port_objectContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Og_service_objectContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Og_service_object_paramsContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Ospf_networkContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Ospf_passive_interfaceContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Ospf_router_idContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Policy_map_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Policy_map_tailContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Port_specContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Route_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Router_bgp_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Router_ospf_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Service_policy_scopeContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Service_policy_stanzaContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Tg_ikev2_attrContext;
import org.batfish.vendor.cisco_ftd.grammar.FtdParser.Tunnel_group_stanzaContext;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessGroup;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessList;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListAddressSpecifier;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListLine;
import org.batfish.vendor.cisco_ftd.representation.FtdBgpNeighbor;
import org.batfish.vendor.cisco_ftd.representation.FtdBgpProcess;
import org.batfish.vendor.cisco_ftd.representation.FtdClassMap;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.batfish.vendor.cisco_ftd.representation.FtdCryptoMapEntry;
import org.batfish.vendor.cisco_ftd.representation.FtdCryptoMapSet;
import org.batfish.vendor.cisco_ftd.representation.FtdIkev2Policy;
import org.batfish.vendor.cisco_ftd.representation.FtdInterface;
import org.batfish.vendor.cisco_ftd.representation.FtdIpsecProfile;
import org.batfish.vendor.cisco_ftd.representation.FtdIpsecTransformSet;
import org.batfish.vendor.cisco_ftd.representation.FtdNatAddress;
import org.batfish.vendor.cisco_ftd.representation.FtdNatDestination;
import org.batfish.vendor.cisco_ftd.representation.FtdNatRule;
import org.batfish.vendor.cisco_ftd.representation.FtdNatService;
import org.batfish.vendor.cisco_ftd.representation.FtdNatSource;
import org.batfish.vendor.cisco_ftd.representation.FtdNetworkObject;
import org.batfish.vendor.cisco_ftd.representation.FtdNetworkObjectGroup;
import org.batfish.vendor.cisco_ftd.representation.FtdNetworkObjectGroupMember;
import org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork;
import org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess;
import org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap;
import org.batfish.vendor.cisco_ftd.representation.FtdRoute;
import org.batfish.vendor.cisco_ftd.representation.FtdServiceObjectGroup;
import org.batfish.vendor.cisco_ftd.representation.FtdServiceObjectGroupMember;
import org.batfish.vendor.cisco_ftd.representation.FtdServicePolicy;
import org.batfish.vendor.cisco_ftd.representation.FtdStructureType;
import org.batfish.vendor.cisco_ftd.representation.FtdStructureUsage;
import org.batfish.vendor.cisco_ftd.representation.FtdTunnelGroup;

/**
 * Parse tree extractor that walks the FTD parse tree and populates an {@link FtdConfiguration}
 * object.
 */
@ParametersAreNonnullByDefault
public class FtdControlPlaneExtractor extends FtdParserBaseListener
    implements ControlPlaneExtractor {

  public FtdControlPlaneExtractor(
      String text,
      FtdCombinedParser parser,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = text;
    _textLines = text.split("\\r?\\n", -1);
    _parser = parser;
    _w = warnings;
    _silentSyntax = silentSyntax;
    _configuration = new FtdConfiguration();
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(this, tree);
  }

  // ==================== Hostname ====================

  @Override
  public void exitHostname_stanza(Hostname_stanzaContext ctx) {
    if (ctx.raw_text != null) {
      _configuration.setHostname(ctx.raw_text.getText().trim());
    }
  }

  // ==================== Interface ====================

  @Override
  public void enterInterface_stanza(Interface_stanzaContext ctx) {
    String name = ctx.name.getText();
    FtdInterface iface = new FtdInterface(name);
    _configuration.getInterfaces().put(name, iface);
    _currentInterface = iface;
  }

  @Override
  public void exitInterface_stanza(Interface_stanzaContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitIf_description(If_descriptionContext ctx) {
    // Interface description parsing (not extracted - cosmetic only)
  }

  @Override
  public void exitIf_shutdown(If_shutdownContext ctx) {
    if (_currentInterface != null) {
      _currentInterface.setActive(false);
    }
  }

  @Override
  public void exitIf_no_shutdown(If_no_shutdownContext ctx) {
    if (_currentInterface != null) {
      _currentInterface.setActive(true);
    }
  }

  @Override
  public void exitIf_nameif(If_nameifContext ctx) {
    if (_currentInterface != null) {
      StringBuilder sb = new StringBuilder();
      for (Token namePart : ctx.name_parts) {
        sb.append(namePart.getText());
      }
      String nameif = sb.toString();
      _currentInterface.setNameif(nameif);
    }
  }

  @Override
  public void exitIf_ip_address(If_ip_addressContext ctx) {
    if (_currentInterface == null || ctx.ip == null || ctx.mask == null) {
      return;
    }
    Ip ip = parseIpSafely(ctx.ip.getText(), "interface IP address");
    Ip mask = parseIpSafely(ctx.mask.getText(), "interface netmask");
    if (ip != null && mask != null) {
      _currentInterface.setAddress(ConcreteInterfaceAddress.create(ip, mask));
    }
  }

  @Override
  public void exitIf_security_level(If_security_levelContext ctx) {
    if (_currentInterface != null) {
      Integer level = parseIntegerBounded(ctx.level.getText(), "security level", 0, 100);
      if (level != null) {
        _currentInterface.setSecurityLevel(level);
      }
    }
  }

  @Override
  public void exitIf_vlan(If_vlanContext ctx) {
    if (_currentInterface != null) {
      Integer vlan = parseIntegerBounded(ctx.vlan_id.getText(), "VLAN ID", 0, 4095);
      if (vlan != null) {
        _currentInterface.setVlan(vlan);
      }
    }
  }

  @Override
  public void exitIf_vrf(If_vrfContext ctx) {
    if (_currentInterface != null) {
      String vrfName = ctx.name.getText();
      _currentInterface.setVrf(vrfName);
      initVrf(vrfName);
    }
  }

  // ==================== Access Lists ====================

  @Override
  public void enterAccess_list_stanza(Access_list_stanzaContext ctx) {
    String aclName = getAclName(ctx);
    _currentAclName = aclName;
    // Get or create the access list
    _currentAcl =
        _configuration.getAccessLists().computeIfAbsent(aclName, k -> new FtdAccessList(k));
    defineStructure(FtdStructureType.ACCESS_LIST, aclName, ctx);
  }

  @Override
  public void exitAccess_list_stanza(Access_list_stanzaContext ctx) {
    _currentAcl = null;
    _currentAclName = null;
  }

  @Override
  public void exitAcl_remark(Acl_remarkContext ctx) {
    if (_currentAcl != null && _currentAclName != null) {
      String remarkText = ctx.remark_text != null ? ctx.remark_text.getText().trim() : "";
      FtdAccessListLine line = FtdAccessListLine.createRemark(_currentAclName, remarkText);
      _currentAcl.addLine(line);
    }
  }

  @Override
  public void exitAcl_extended(Acl_extendedContext ctx) {
    if (_currentAcl != null && _currentAclName != null) {
      LineAction action = toLineAction(ctx.action);
      String protocol = ctx.protocol() != null ? ctx.protocol().getText() : "ip";

      FtdAccessListAddressSpecifier srcSpec = null;
      FtdAccessListAddressSpecifier dstSpec = null;

      if (ctx.src_spec_null() != null && ctx.src_spec_null().acl_address_spec() != null) {
        srcSpec = toAddressSpecifier(ctx.src_spec_null().acl_address_spec());
      }
      if (ctx.dst_spec() != null && ctx.dst_spec().acl_address_spec() != null) {
        dstSpec = toAddressSpecifier(ctx.dst_spec().acl_address_spec());
      }

      FtdAccessListLine line =
          FtdAccessListLine.createExtended(_currentAclName, action, protocol, srcSpec, dstSpec);
      if (ctx.action != null && ctx.action.TRUST() != null) {
        line.setTrust(true);
      }
      Port_specContext dstPortSpec = ctx.dst_spec() != null ? ctx.dst_spec().port_spec() : null;
      line.setDestinationPortSpecifier(getPortSpecText(dstPortSpec));
      referenceServiceObjectGroupFromPortSpec(dstPortSpec);

      // Handle options
      if (ctx.acl_options() != null) {
        for (var opt : ctx.acl_options()) {
          if (opt.RULE_ID() != null && opt.id != null) {
            line.setRuleId(Long.parseLong(opt.id.getText()));
          }
          if (opt.INACTIVE() != null) {
            line.setInactive(true);
          }
          if (opt.LOG() != null) {
            line.setLog(true);
          }
          if (opt.TIME_RANGE() != null && opt.time_range_name() != null) {
            line.setTimeRange(opt.time_range_name().getText());
          }
        }
      }

      _currentAcl.addLine(line);
    }
  }

  @Override
  public void exitAcl_implicit_extended(Acl_implicit_extendedContext ctx) {
    // Implicit extended = same as acl_extended but without the EXTENDED keyword
    if (_currentAcl != null && _currentAclName != null) {
      LineAction action = toLineAction(ctx.action);
      String protocol = ctx.protocol() != null ? ctx.protocol().getText() : "ip";

      FtdAccessListAddressSpecifier srcSpec = null;
      FtdAccessListAddressSpecifier dstSpec = null;

      if (ctx.src_spec_null() != null && ctx.src_spec_null().acl_address_spec() != null) {
        srcSpec = toAddressSpecifier(ctx.src_spec_null().acl_address_spec());
      }
      if (ctx.dst_spec() != null && ctx.dst_spec().acl_address_spec() != null) {
        dstSpec = toAddressSpecifier(ctx.dst_spec().acl_address_spec());
      }

      FtdAccessListLine line =
          FtdAccessListLine.createExtended(_currentAclName, action, protocol, srcSpec, dstSpec);
      if (ctx.action != null && ctx.action.TRUST() != null) {
        line.setTrust(true);
      }
      Port_specContext dstPortSpec = ctx.dst_spec() != null ? ctx.dst_spec().port_spec() : null;
      line.setDestinationPortSpecifier(getPortSpecText(dstPortSpec));
      referenceServiceObjectGroupFromPortSpec(dstPortSpec);

      // Handle options
      if (ctx.acl_options() != null) {
        for (var opt : ctx.acl_options()) {
          if (opt.RULE_ID() != null && opt.id != null) {
            line.setRuleId(Long.parseLong(opt.id.getText()));
          }
          if (opt.INACTIVE() != null) {
            line.setInactive(true);
          }
          if (opt.LOG() != null) {
            line.setLog(true);
          }
          if (opt.TIME_RANGE() != null && opt.time_range_name() != null) {
            line.setTimeRange(opt.time_range_name().getText());
          }
        }
      }

      _currentAcl.addLine(line);
    }
  }

  @Override
  public void exitAcl_advanced(Acl_advancedContext ctx) {
    if (_currentAcl != null && _currentAclName != null) {
      LineAction action = toLineAction(ctx.action);
      String protocol = ctx.protocol() != null ? ctx.protocol().getText() : "ip";

      FtdAccessListAddressSpecifier srcSpec = null;
      FtdAccessListAddressSpecifier dstSpec = null;

      if (ctx.src_spec_null() != null && ctx.src_spec_null().acl_address_spec() != null) {
        srcSpec = toAddressSpecifier(ctx.src_spec_null().acl_address_spec());
      }
      if (ctx.dst_spec() != null && ctx.dst_spec().acl_address_spec() != null) {
        dstSpec = toAddressSpecifier(ctx.dst_spec().acl_address_spec());
      }

      FtdAccessListLine line =
          FtdAccessListLine.createAdvanced(_currentAclName, action, protocol, srcSpec, dstSpec);
      if (ctx.action != null && ctx.action.TRUST() != null) {
        line.setTrust(true);
      }
      if (ctx.ifc_clause_null() != null && ctx.ifc_clause_null().acl_ifc_name_null() != null) {
        line.setInterfaceName(ctx.ifc_clause_null().acl_ifc_name_null().getText());
      }
      Port_specContext dstPortSpec = ctx.dst_spec() != null ? ctx.dst_spec().port_spec() : null;
      line.setDestinationPortSpecifier(getPortSpecText(dstPortSpec));
      referenceServiceObjectGroupFromPortSpec(dstPortSpec);

      // Handle options
      if (ctx.acl_options() != null) {
        for (var opt : ctx.acl_options()) {
          if (opt.RULE_ID() != null && opt.id != null) {
            line.setRuleId(Long.parseLong(opt.id.getText()));
          }
          if (opt.INACTIVE() != null) {
            line.setInactive(true);
          }
          if (opt.LOG() != null) {
            line.setLog(true);
          }
          if (opt.TIME_RANGE() != null && opt.time_range_name() != null) {
            line.setTimeRange(opt.time_range_name().getText());
          }
        }
      }

      _currentAcl.addLine(line);
    }
  }

  // ==================== Access Groups ====================

  @Override
  public void enterAccess_group_stanza(Access_group_stanzaContext ctx) {
    String aclName = ctx.name.getText();
    Access_group_tailContext tail = ctx.access_group_tail();
    String direction = null;
    String interfaceName = null;

    if (tail != null) {
      if (tail.GLOBAL() != null) {
        direction = "global";
      } else if (tail.access_group_direction() != null) {
        direction = tail.access_group_direction().getText().toLowerCase();
        if (tail.interface_name_value != null) {
          interfaceName = tail.interface_name_value.getText();
        }
      }
    }

    if (direction == null) {
      return;
    }
    if ("global".equals(direction)) {
      _configuration.getAccessGroups().add(new FtdAccessGroup(aclName, null, "global"));
    } else if (interfaceName != null) {
      _configuration.getAccessGroups().add(new FtdAccessGroup(aclName, interfaceName, direction));
    }
    referenceStructure(
        FtdStructureType.ACCESS_LIST,
        aclName,
        FtdStructureUsage.ACCESS_GROUP_ACCESS_LIST,
        ctx.getStart().getLine());
  }

  // ==================== MPF ====================

  @Override
  public void enterClass_map_stanza(Class_map_stanzaContext ctx) {
    String name = ctx.name.getText();
    FtdClassMap classMap = new FtdClassMap(name);
    if (ctx.class_map_type() != null && ctx.class_map_type().type != null) {
      classMap.setType(ctx.class_map_type().type.getText().toLowerCase());
    }
    _configuration.addClassMap(classMap);
    _currentClassMap = classMap;
    defineStructure(FtdStructureType.CLASS_MAP, classMap.getName(), ctx);
  }

  @Override
  public void exitClass_map_stanza(Class_map_stanzaContext ctx) {
    _currentClassMap = null;
  }

  @Override
  public void exitClass_map_tail(Class_map_tailContext ctx) {
    if (_currentClassMap == null) {
      return;
    }
    if (ctx.MATCH() != null) {
      String line = getLineText(ctx.getStart().getLine());
      if (line == null) {
        line = ctx.getText();
      }
      if (line == null) {
        return;
      }
      String trimmed = line.trim();
      _currentClassMap.addMatchLine(trimmed);
      String aclName = parseClassMapAccessList(trimmed);
      if (aclName != null) {
        _currentClassMap.addAccessListReference(aclName);
        referenceStructure(
            FtdStructureType.ACCESS_LIST,
            aclName,
            FtdStructureUsage.CLASS_MAP_ACCESS_LIST,
            ctx.getStart().getLine());
      }
    }
  }

  @Override
  public void enterPolicy_map_stanza(Policy_map_stanzaContext ctx) {
    String name = ctx.name.getText();
    FtdPolicyMap policyMap = new FtdPolicyMap(name);
    if (ctx.policy_map_type() != null && ctx.policy_map_type().type != null) {
      policyMap.setType(ctx.policy_map_type().type.getText().toLowerCase());
    }
    _configuration.addPolicyMap(policyMap);
    _currentPolicyMap = policyMap;
    _currentPolicyMapClassName = null;
    _currentPolicyMapInParameters = false;
    defineStructure(FtdStructureType.POLICY_MAP, policyMap.getName(), ctx);
  }

  @Override
  public void exitPolicy_map_stanza(Policy_map_stanzaContext ctx) {
    _currentPolicyMap = null;
    _currentPolicyMapClassName = null;
    _currentPolicyMapInParameters = false;
  }

  @Override
  public void exitPolicy_map_tail(Policy_map_tailContext ctx) {
    if (_currentPolicyMap == null) {
      return;
    }
    String line = getLineText(ctx.getStart().getLine());
    if (line == null) {
      line = ctx.getText();
    }
    if (line == null) {
      return;
    }
    String trimmed = line.trim();
    if (trimmed.isEmpty()) {
      return;
    }
    String[] parts = trimmed.split("\\s+");
    if (parts.length >= 2 && parts[0].equals("class")) {
      String className = parts[parts.length - 1];
      _currentPolicyMap.addClassName(className);
      _currentPolicyMapClassName = className;
      _currentPolicyMapInParameters = false;
      referenceStructure(
          FtdStructureType.CLASS_MAP,
          className,
          FtdStructureUsage.POLICY_MAP_CLASS,
          ctx.getStart().getLine());
      return;
    }
    if (_currentPolicyMapClassName != null) {
      if (parts[0].equals("parameters")) {
        _currentPolicyMapInParameters = true;
        _currentPolicyMap.addClassActionLine(_currentPolicyMapClassName, trimmed);
        return;
      }
      if (parts[0].equals("inspect") || parts[0].equals("set")) {
        _currentPolicyMap.addClassActionLine(_currentPolicyMapClassName, trimmed);
        return;
      }
      if (_currentPolicyMapInParameters) {
        _currentPolicyMap.addClassActionLine(_currentPolicyMapClassName, trimmed);
      }
    } else if (ctx.PARAMETERS() != null) {
      _currentPolicyMap.addParameterLine(trimmed);
    }
  }

  @Override
  public void enterService_policy_stanza(Service_policy_stanzaContext ctx) {
    String policyName = ctx.policy_name.getText();
    FtdServicePolicy.Scope scope = FtdServicePolicy.Scope.UNKNOWN;
    String interfaceName = null;
    Service_policy_scopeContext scopeCtx = ctx.service_policy_scope();
    if (scopeCtx != null) {
      if (scopeCtx.GLOBAL() != null) {
        scope = FtdServicePolicy.Scope.GLOBAL;
      } else if (scopeCtx.INTERFACE() != null) {
        scope = FtdServicePolicy.Scope.INTERFACE;
        if (scopeCtx.interface_name_value != null) {
          interfaceName = scopeCtx.interface_name_value.getText();
        }
      }
    }
    FtdServicePolicy policy = new FtdServicePolicy(policyName, scope, interfaceName);
    _configuration.addServicePolicy(policy);
    referenceStructure(
        FtdStructureType.POLICY_MAP,
        policy.getPolicyMapName(),
        FtdStructureUsage.SERVICE_POLICY_POLICY_MAP,
        ctx.getStart().getLine());
  }

  @Override
  public void enterCrypto_ikev2_policy(Crypto_ikev2_policyContext ctx) {
    Integer priority =
        parseIntegerBounded(ctx.priority.getText(), "IKEv2 policy priority", 1, 65535);
    if (priority != null) {
      _currentIkev2Policy =
          _configuration.getIkev2Policies().computeIfAbsent(priority, FtdIkev2Policy::new);
      defineStructure(FtdStructureType.IKEV2_POLICY, String.valueOf(priority), ctx);
    }
  }

  @Override
  public void exitCrypto_ikev2_policy(Crypto_ikev2_policyContext ctx) {
    _currentIkev2Policy = null;
  }

  @Override
  public void exitIkev2_policy_attr(Ikev2_policy_attrContext ctx) {
    if (_currentIkev2Policy == null) {
      return;
    }
    if (ctx.ENCRYPTION() != null && ctx.enc_algs != null) {
      String text =
          ctx.enc_algs.stream().map(Token::getText).collect(Collectors.joining("")).trim();
      EncryptionAlgorithm alg = parseEncryptionAlgorithm(text);
      if (alg != null) {
        _currentIkev2Policy.getEncryptionAlgorithms().add(alg);
      }
    } else if (ctx.INTEGRITY() != null && ctx.int_algs != null) {
      String text =
          ctx.int_algs.stream().map(Token::getText).collect(Collectors.joining("")).trim();
      IkeHashingAlgorithm alg = parseIkeHashingAlgorithm(text);
      if (alg != null) {
        _currentIkev2Policy.getIntegrityAlgorithms().add(alg);
      }
    } else if (ctx.PRF() != null && ctx.prf_algs != null) {
      String text =
          ctx.prf_algs.stream().map(Token::getText).collect(Collectors.joining("")).trim();
      IkeHashingAlgorithm alg = parseIkeHashingAlgorithm(text);
      if (alg != null) {
        _currentIkev2Policy.getPrfAlgorithms().add(alg);
      }
    } else if (ctx.GROUP() != null && ctx.dh_groups != null) {
      String text =
          ctx.dh_groups.stream().map(Token::getText).collect(Collectors.joining("")).trim();
      DiffieHellmanGroup group = parseDhGroup(text);
      if (group != null) {
        _currentIkev2Policy.getDhGroups().add(group);
      }
    } else if (ctx.LIFETIME() != null && ctx.dec() != null) {
      Integer lifetime = parseIntegerBounded(ctx.dec().getText(), "IKEv2 lifetime", 1, 86400);
      if (lifetime != null) {
        _currentIkev2Policy.setLifetimeSeconds(lifetime);
      }
    }
  }

  @Override
  public void enterTunnel_group_stanza(Tunnel_group_stanzaContext ctx) {
    String name =
        ctx.name_parts.stream()
            .map(token -> token.getText())
            .collect(Collectors.joining(" "))
            .trim();
    _currentTunnelGroup =
        _configuration.getTunnelGroups().computeIfAbsent(name, FtdTunnelGroup::new);
    defineStructure(FtdStructureType.TUNNEL_GROUP, name, ctx);
  }

  @Override
  public void exitTunnel_group_stanza(Tunnel_group_stanzaContext ctx) {
    if (_currentTunnelGroup != null && ctx.TYPE() != null) {
      if (ctx.IPSEC_L2L() != null) {
        _currentTunnelGroup.setType(FtdTunnelGroup.Type.IPSEC_L2L);
      } else if (ctx.REMOTE_ACCESS() != null) {
        _currentTunnelGroup.setType(FtdTunnelGroup.Type.REMOTE_ACCESS);
      }
    }
    _currentTunnelGroup = null;
  }

  @Override
  public void exitTg_ikev2_attr(Tg_ikev2_attrContext ctx) {
    if (_currentTunnelGroup == null) {
      return;
    }
    if (ctx.PRE_SHARED_KEY() != null && ctx.key_parts != null && !ctx.key_parts.isEmpty()) {
      String key =
          ctx.key_parts.stream().map(Token::getText).collect(Collectors.joining("")).trim();
      if (ctx.REMOTE_AUTHENTICATION() != null) {
        _currentTunnelGroup.setPresharedKey(key);
      } else if (ctx.LOCAL_AUTHENTICATION() != null) {
        _currentTunnelGroup.setPresharedKeyStandby(key);
      }
    }
  }

  private @Nullable EncryptionAlgorithm parseEncryptionAlgorithm(String text) {
    switch (text.toLowerCase()) {
      case "aes":
      case "aes-128":
        return EncryptionAlgorithm.AES_128_CBC;
      case "aes-192":
        return EncryptionAlgorithm.AES_192_CBC;
      case "aes-256":
        return EncryptionAlgorithm.AES_256_CBC;
      case "3des":
        return EncryptionAlgorithm.THREEDES_CBC;
      case "des":
        return EncryptionAlgorithm.DES_CBC;
      default:
        return null;
    }
  }

  private @Nullable IkeHashingAlgorithm parseIkeHashingAlgorithm(String text) {
    switch (text.toLowerCase()) {
      case "sha":
      case "sha1":
      case "sha-1":
        return IkeHashingAlgorithm.SHA1;
      case "sha256":
      case "sha-256":
        return IkeHashingAlgorithm.SHA_256;
      case "sha384":
      case "sha-384":
        return IkeHashingAlgorithm.SHA_384;
      case "sha512":
      case "sha-512":
        return IkeHashingAlgorithm.SHA_512;
      case "md5":
        return IkeHashingAlgorithm.MD5;
      default:
        return null;
    }
  }

  private @Nullable DiffieHellmanGroup parseDhGroup(String text) {
    switch (text.toLowerCase()) {
      case "1":
        return DiffieHellmanGroup.GROUP1;
      case "2":
        return DiffieHellmanGroup.GROUP2;
      case "5":
        return DiffieHellmanGroup.GROUP5;
      case "14":
        return DiffieHellmanGroup.GROUP14;
      case "19":
        return DiffieHellmanGroup.GROUP19;
      case "20":
        return DiffieHellmanGroup.GROUP20;
      case "21":
        return DiffieHellmanGroup.GROUP21;
      case "24":
        return DiffieHellmanGroup.GROUP24;
      default:
        return null;
    }
  }

  // ==================== Safe Parsing Helper Methods ====================

  /**
   * Safely parse an integer with bounds checking.
   *
   * @param text The text to parse
   * @param fieldName Field name for error messages
   * @param minValue Minimum valid value (inclusive)
   * @param maxValue Maximum valid value (inclusive)
   * @return Parsed integer, or null if parsing fails or value is out of range
   */
  private @Nullable Integer parseIntegerBounded(
      String text, String fieldName, int minValue, int maxValue) {
    try {
      int value = Integer.parseInt(text);
      if (value < minValue || value > maxValue) {
        _w.redFlagf(
            "%s %d out of valid range [%d-%d], skipping", fieldName, value, minValue, maxValue);
        return null;
      }
      return value;
    } catch (NumberFormatException e) {
      _w.redFlagf("Invalid %s value: %s, skipping", fieldName, text);
      return null;
    }
  }

  /**
   * Safely parse a long with bounds checking.
   *
   * @param text The text to parse
   * @param fieldName Field name for error messages
   * @param minValue Minimum valid value (inclusive)
   * @param maxValue Maximum valid value (inclusive)
   * @return Parsed long, or null if parsing fails or value is out of range
   */
  private @Nullable Long parseLongBounded(
      String text, String fieldName, long minValue, long maxValue) {
    try {
      long value = Long.parseLong(text);
      if (value < minValue || value > maxValue) {
        _w.redFlagf(
            "%s %d out of valid range [%d-%d], skipping", fieldName, value, minValue, maxValue);
        return null;
      }
      return value;
    } catch (NumberFormatException e) {
      _w.redFlagf("Invalid %s value: %s, skipping", fieldName, text);
      return null;
    }
  }

  /**
   * Safely parse an IP address.
   *
   * @param text The text to parse
   * @param fieldName Field name for error messages
   * @return Parsed IP, or null if parsing fails
   */
  private @Nullable Ip parseIpSafely(String text, String fieldName) {
    try {
      return Ip.parse(text);
    } catch (IllegalArgumentException e) {
      _w.redFlagf("Invalid %s IP address: %s, skipping", fieldName, text);
      return null;
    }
  }

  // ==================== VPN / Crypto ====================

  @Override
  public void exitCrypto_ipsec_transform_set(Crypto_ipsec_transform_setContext ctx) {
    String name =
        ctx.name_parts.stream()
            .map(token -> token.getText())
            .collect(Collectors.joining(" "))
            .trim();
    FtdIpsecTransformSet transformSet = new FtdIpsecTransformSet(name);
    if (ctx.algs != null) {
      for (Token token : ctx.algs) {
        String text = token.getText().toLowerCase();
        switch (text) {
          case "esp-aes":
            transformSet.setEspEncryption(EncryptionAlgorithm.AES_128_CBC);
            break;
          case "esp-aes-192":
            transformSet.setEspEncryption(EncryptionAlgorithm.AES_192_CBC);
            break;
          case "esp-aes-256":
            transformSet.setEspEncryption(EncryptionAlgorithm.AES_256_CBC);
            break;
          case "esp-3des":
            transformSet.setEspEncryption(EncryptionAlgorithm.THREEDES_CBC);
            break;
          case "esp-des":
            transformSet.setEspEncryption(EncryptionAlgorithm.DES_CBC);
            break;
          case "esp-none":
            transformSet.setEspEncryption(EncryptionAlgorithm.NULL);
            break;
          case "esp-sha-hmac":
            transformSet.setEspAuthentication(IpsecAuthenticationAlgorithm.HMAC_SHA1_96);
            break;
          case "esp-md5-hmac":
            transformSet.setEspAuthentication(IpsecAuthenticationAlgorithm.HMAC_MD5_96);
            break;
          case "transport":
            transformSet.setMode(IpsecEncapsulationMode.TRANSPORT);
            break;
          case "tunnel":
            transformSet.setMode(IpsecEncapsulationMode.TUNNEL);
            break;
          case "mode":
            break;
          default:
            break;
        }
      }
    }
    _configuration.getIpsecTransformSets().put(name, transformSet);
    defineStructure(FtdStructureType.IPSEC_TRANSFORM_SET, name, ctx);
  }

  @Override
  public void enterCrypto_ipsec_profile(Crypto_ipsec_profileContext ctx) {
    String name =
        ctx.name_parts.stream()
            .map(token -> token.getText())
            .collect(Collectors.joining(" "))
            .trim();
    FtdIpsecProfile profile = new FtdIpsecProfile(name);
    _configuration.getIpsecProfiles().put(name, profile);
    _currentIpsecProfile = profile;
    defineStructure(FtdStructureType.IPSEC_PROFILE, name, ctx);
  }

  @Override
  public void exitCrypto_ipsec_profile(Crypto_ipsec_profileContext ctx) {
    _currentIpsecProfile = null;
  }

  @Override
  public void exitCip_set(Cip_setContext ctx) {
    if (_currentIpsecProfile == null) {
      return;
    }
    if (ctx.TRANSFORM_SET() != null && ctx.transform_names != null) {
      for (Token token : ctx.transform_names) {
        String transformSetList = token.getText().trim();
        for (String transformSetName : transformSetList.split("\\s+")) {
          if (transformSetName.isEmpty()) {
            continue;
          }
          _currentIpsecProfile.getTransformSets().add(transformSetName);
          referenceStructure(
              FtdStructureType.IPSEC_TRANSFORM_SET,
              transformSetName,
              FtdStructureUsage.IPSEC_PROFILE_TRANSFORM_SET,
              token.getLine());
        }
      }
    } else if (ctx.PFS() != null && ctx.GROUP() != null) {
      _currentIpsecProfile.setPfsGroup(parseDhGroup(ctx.dec().getText()));
    }
  }

  @Override
  public void enterCrypto_map(Crypto_mapContext ctx) {
    if (ctx.name_parts == null || ctx.name_parts.isEmpty()) {
      return;
    }
    String name =
        ctx.name_parts.stream()
            .map(token -> token.getText())
            .collect(Collectors.joining(" "))
            .trim();
    Integer seq = null;
    if (ctx.seq != null) {
      seq = parseIntegerBounded(ctx.seq.getText(), "crypto map sequence", 1, 65535);
    }
    if (seq != null) {
      FtdCryptoMapSet mapSet =
          _configuration.getCryptoMaps().computeIfAbsent(name, FtdCryptoMapSet::new);
      _currentCryptoMapEntry =
          mapSet.getEntries().computeIfAbsent(seq, s -> new FtdCryptoMapEntry(name, s));
      defineStructure(FtdStructureType.CRYPTO_MAP, name, ctx);
    }
  }

  @Override
  public void exitCrypto_map(Crypto_mapContext ctx) {
    _currentCryptoMapEntry = null;
  }

  @Override
  public void exitCrypto_map_interface(Crypto_map_interfaceContext ctx) {
    if (ctx.name_parts == null || ctx.name_parts.isEmpty() || ctx.iface_name == null) {
      return;
    }
    String name =
        ctx.name_parts.stream()
            .map(token -> token.getText())
            .collect(Collectors.joining(" "))
            .trim();
    String ifaceName = ctx.iface_name.getText().trim();
    if (!name.isEmpty() && !ifaceName.isEmpty()) {
      _configuration.addCryptoMapInterfaceBinding(name, ifaceName);
    }
  }

  @Override
  public void exitCm_match_address(Cm_match_addressContext ctx) {
    if (_currentCryptoMapEntry != null && ctx.acl_parts != null && !ctx.acl_parts.isEmpty()) {
      // Extract ACL name from the captured token sequence
      String aclName =
          ctx.acl_parts.stream()
              .map(token -> token.getText())
              .collect(Collectors.joining(" "))
              .trim();
      _currentCryptoMapEntry.setAccessList(aclName);
      referenceStructure(
          FtdStructureType.ACCESS_LIST,
          aclName,
          FtdStructureUsage.CRYPTO_MAP_ACL,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void exitCm_set(Cm_setContext ctx) {
    if (_currentCryptoMapEntry == null) {
      return;
    }
    if (ctx.PEER() != null && ctx.ip_address() != null) {
      // Parse peer IP from ip_address context
      Ip peer = parseIpSafely(ctx.ip_address().getText(), "crypto map peer IP");
      if (peer != null) {
        _currentCryptoMapEntry.setPeer(peer);
      }
    } else if (ctx.TRANSFORM_SET() != null && ctx.transform_names != null) {
      for (Token token : ctx.transform_names) {
        String transformSetList = token.getText().trim();
        for (String transformSetName : transformSetList.split("\\s+")) {
          if (transformSetName.isEmpty()) {
            continue;
          }
          _currentCryptoMapEntry.getTransforms().add(transformSetName);
          referenceStructure(
              FtdStructureType.IPSEC_TRANSFORM_SET,
              transformSetName,
              FtdStructureUsage.CRYPTO_MAP_TRANSFORM_SET,
              token.getLine());
        }
      }
    } else if (ctx.PFS() != null && ctx.GROUP() != null) {
      _currentCryptoMapEntry.setPfsKeyGroup(parseDhGroup(ctx.dec().getText()));
    }
  }

  // ==================== Network Objects ====================

  @Override
  public void enterObject_stanza(Object_stanzaContext ctx) {
    if (ctx.object_type().NETWORK() != null) {
      String name = getObjectName(ctx);
      _currentNetworkObject =
          _configuration.getNetworkObjects().computeIfAbsent(name, FtdNetworkObject::new);
      defineStructure(FtdStructureType.NETWORK_OBJECT, name, ctx);
    }
  }

  @Override
  public void exitObject_stanza(Object_stanzaContext ctx) {
    _currentNetworkObject = null;
  }

  @Override
  public void exitObject_host(Object_hostContext ctx) {
    if (_currentNetworkObject != null && ctx.ip != null) {
      Ip ip = Ip.parse(ctx.ip.getText());
      _currentNetworkObject.setHost(ip);
    }
  }

  @Override
  public void exitObject_subnet(Object_subnetContext ctx) {
    if (_currentNetworkObject != null && ctx.network != null && ctx.mask != null) {
      Ip network = Ip.parse(ctx.network.getText());
      Ip mask = Ip.parse(ctx.mask.getText());
      _currentNetworkObject.setSubnet(network, mask);
    }
  }

  @Override
  public void exitObject_fqdn(Object_fqdnContext ctx) {
    if (_currentNetworkObject != null && ctx.fqdn_name_null() != null) {
      String fqdn = ctx.fqdn_name_null().getText();
      _currentNetworkObject.setFqdn(fqdn);
    }
  }

  // ==================== Network Object Groups ====================

  @Override
  public void enterObject_group_stanza(Object_group_stanzaContext ctx) {
    if (ctx.group_type().NETWORK() != null) {
      String name = getObjectGroupName(ctx);
      _currentNetworkObjectGroup =
          _configuration.getNetworkObjectGroups().computeIfAbsent(name, FtdNetworkObjectGroup::new);
      defineStructure(FtdStructureType.NETWORK_OBJECT_GROUP, name, ctx);
    } else if (ctx.group_type().SERVICE() != null) {
      String name = getObjectGroupName(ctx);
      _currentServiceObjectGroup =
          _configuration.getServiceObjectGroups().computeIfAbsent(name, FtdServiceObjectGroup::new);
      if (ctx.group_type().protocol() != null) {
        _currentServiceObjectGroup.setProtocol(ctx.group_type().protocol().getText().toLowerCase());
      }
      defineStructure(FtdStructureType.SERVICE_OBJECT_GROUP, name, ctx);
    }
  }

  @Override
  public void exitObject_group_stanza(Object_group_stanzaContext ctx) {
    _currentNetworkObjectGroup = null;
    _currentServiceObjectGroup = null;
  }

  @Override
  public void exitOg_network_object(Og_network_objectContext ctx) {
    if (_currentNetworkObjectGroup != null) {
      FtdNetworkObjectGroupMember member = null;

      if (ctx.HOST() != null && ctx.ip != null) {
        Ip ip = Ip.parse(ctx.ip.getText());
        member = FtdNetworkObjectGroupMember.host(ip);
      } else if (ctx.OBJECT() != null && ctx.obj_name_null() != null) {
        String objName = ctx.obj_name_null().getText();
        member = FtdNetworkObjectGroupMember.object(objName);
        referenceStructure(
            FtdStructureType.NETWORK_OBJECT,
            objName,
            FtdStructureUsage.NETWORK_OBJECT_GROUP_OBJECT,
            ctx.obj_name_null().getStart().getLine());
      } else if (ctx.network != null && ctx.mask != null) {
        Ip network = Ip.parse(ctx.network.getText());
        Ip mask = Ip.parse(ctx.mask.getText());
        member = FtdNetworkObjectGroupMember.networkMask(network, mask);
      } else {
        assert false;
      }

      if (member != null) {
        _currentNetworkObjectGroup.addMember(member);
      }
    }
  }

  @Override
  public void exitOg_group_object(Og_group_objectContext ctx) {
    if (ctx.name == null) {
      return;
    }
    String groupName = ctx.name.getText();
    if (_currentNetworkObjectGroup != null) {
      FtdNetworkObjectGroupMember member = FtdNetworkObjectGroupMember.groupObject(groupName);
      _currentNetworkObjectGroup.addMember(member);
      referenceStructure(
          FtdStructureType.NETWORK_OBJECT_GROUP,
          groupName,
          FtdStructureUsage.NETWORK_OBJECT_GROUP_GROUP,
          ctx.name.getStart().getLine());
    } else if (_currentServiceObjectGroup != null) {
      FtdServiceObjectGroupMember member = FtdServiceObjectGroupMember.groupObject(groupName);
      _currentServiceObjectGroup.addMember(member);
      referenceStructure(
          FtdStructureType.SERVICE_OBJECT_GROUP,
          groupName,
          FtdStructureUsage.SERVICE_OBJECT_GROUP_OBJECT,
          ctx.name.getStart().getLine());
    }
  }

  @Override
  public void exitOg_service_object(Og_service_objectContext ctx) {
    if (_currentServiceObjectGroup == null || ctx.protocol() == null) {
      return;
    }
    String protocol = ctx.protocol().getText().toLowerCase();
    String portSpec = null;
    Og_service_object_paramsContext params = ctx.og_service_object_params();
    if (params != null) {
      // If DESTINATION is present, use its port_spec; otherwise use SOURCE's port_spec
      if (params.DESTINATION() != null && params.port_spec().size() > 1) {
        portSpec = getPortSpecText(params.port_spec(1));
      } else if (params.SOURCE() != null && !params.port_spec().isEmpty()) {
        portSpec = getPortSpecText(params.port_spec(0));
      } else if (params.DESTINATION() != null && !params.port_spec().isEmpty()) {
        // DESTINATION without SOURCE
        portSpec = getPortSpecText(params.port_spec(0));
      }
    }
    _currentServiceObjectGroup.addMember(
        FtdServiceObjectGroupMember.serviceObject(protocol, portSpec));
  }

  @Override
  public void exitOg_port_object(Og_port_objectContext ctx) {
    if (_currentServiceObjectGroup == null) {
      return;
    }
    String portSpec = null;
    if (ctx.EQ() != null && ctx.port != null) {
      portSpec = "eq " + ctx.port.getText();
    } else if (ctx.RANGE() != null && ctx.port_low != null && ctx.port_high != null) {
      portSpec = "range " + ctx.port_low.getText() + " " + ctx.port_high.getText();
    }
    String protocol = _currentServiceObjectGroup.getProtocol();
    _currentServiceObjectGroup.addMember(
        FtdServiceObjectGroupMember.portObject(
            protocol != null ? protocol.toLowerCase() : null, portSpec));
  }

  // ==================== NAT ====================

  @Override
  public void exitNat_stanza(Nat_stanzaContext ctx) {
    String sourceInterface = ctx.src_ifc.getText();
    String destinationInterface = ctx.dst_ifc.getText();
    FtdNatRule.NatPosition position = FtdNatRule.NatPosition.AUTO;
    if (ctx.nat_position() != null) {
      if (ctx.nat_position().BEFORE_AUTO() != null) {
        position = FtdNatRule.NatPosition.BEFORE_AUTO;
      } else if (ctx.nat_position().AFTER_AUTO() != null) {
        position = FtdNatRule.NatPosition.AFTER_AUTO;
      }
    }
    FtdNatRule rule = new FtdNatRule(sourceInterface, destinationInterface, position);

    Nat_ruleContext ruleCtx = ctx.nat_rule();
    if (ruleCtx.nat_source() != null) {
      rule.setSourceTranslation(toNatSource(ruleCtx.nat_source()));
    }
    if (ruleCtx.nat_destination() != null) {
      rule.setDestinationTranslation(toNatDestination(ruleCtx.nat_destination()));
    }
    if (ruleCtx.nat_service() != null) {
      rule.setServiceTranslation(toNatService(ruleCtx.nat_service()));
    }

    _configuration.addNatRule(rule);
  }

  private FtdNatSource toNatSource(Nat_sourceContext ctx) {
    FtdNatSource.Type type =
        ctx.STATIC() != null ? FtdNatSource.Type.STATIC : FtdNatSource.Type.DYNAMIC;
    FtdNatAddress real = toNatAddress(ctx.real);
    FtdNatAddress mapped = toNatAddress(ctx.mapped);
    return new FtdNatSource(type, real, mapped);
  }

  private FtdNatDestination toNatDestination(Nat_destinationContext ctx) {
    FtdNatAddress real = toNatAddress(ctx.real);
    FtdNatAddress mapped = toNatAddress(ctx.mapped);
    return new FtdNatDestination(real, mapped);
  }

  private FtdNatService toNatService(Nat_serviceContext ctx) {
    String realService;
    String mappedService;
    if (ctx.protocol() != null) {
      String protocol = ctx.protocol().getText().toLowerCase();
      String realPort = getNatServicePortText(ctx.real_port);
      String mappedPort = getNatServicePortText(ctx.mapped_port);
      realService = (protocol + " " + realPort).trim();
      mappedService = mappedPort != null ? mappedPort.trim() : "";
    } else {
      realService = ctx.real_service.getText();
      mappedService = ctx.mapped_service.getText();
    }
    return new FtdNatService(realService, mappedService);
  }

  private @Nonnull String getNatServicePortText(@Nonnull Nat_service_portContext ctx) {
    if (ctx.children == null || ctx.children.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ctx.children.size(); i++) {
      if (i > 0) {
        sb.append(' ');
      }
      sb.append(ctx.children.get(i).getText());
    }
    return sb.toString().trim();
  }

  private FtdNatAddress toNatAddress(Nat_addressContext ctx) {
    if (ctx.ip != null) {
      return new FtdNatAddress.FtdNatAddressIp(Ip.parse(ctx.ip.getText()));
    } else {
      String name = ctx.nat_object_name_null().getText();
      // NAT address specifications reference network objects (not service objects).
      // Service objects are used for port/protocol specifications in ACLs, not IP addresses.
      // We track the reference here for validation and undefined reference detection.
      referenceStructure(
          FtdStructureType.NETWORK_OBJECT,
          name,
          FtdStructureUsage.NAT_SOURCE_OBJECT, // Usage depends on context (source vs destination)
          ctx.nat_object_name_null().getStart().getLine());
      return new FtdNatAddress.FtdNatAddressName(name);
    }
  }

  // ==================== Routes ====================

  @Override
  public void exitRoute_stanza(Route_stanzaContext ctx) {
    if (ctx.network != null
        && ctx.mask != null
        && ctx.gateway != null
        && ctx.interface_name() != null) {
      String iface = ctx.interface_name().getText();
      Ip network = parseIpSafely(ctx.network.getText(), "route network");
      Ip mask = parseIpSafely(ctx.mask.getText(), "route netmask");
      Ip gateway = parseIpSafely(ctx.gateway.getText(), "route gateway");
      Integer metric = parseIntegerBounded(ctx.metric.getText(), "route metric", 0, 255);
      if (network != null && mask != null && gateway != null && metric != null) {
        FtdRoute route = new FtdRoute(iface, network, mask, gateway, metric);
        _configuration.getRoutes().add(route);
      }
    }
  }

  // ==================== MTU ====================

  @Override
  public void exitMtu_stanza(Mtu_stanzaContext ctx) {
    if (ctx.iface_name != null && ctx.mtu_value != null) {
      String ifaceName = ctx.iface_name.getText();
      Integer mtu = parseIntegerBounded(ctx.mtu_value.getText(), "MTU", 68, 9000);
      if (mtu != null) {
        FtdInterface iface = _configuration.getInterfaces().get(ifaceName);
        if (iface != null) {
          iface.setMtu(mtu);
        }
      }
    }
  }

  // ==================== NAMES ====================

  @Override
  public void exitNames_stanza(Names_stanzaContext ctx) {
    _configuration.setNamesEnabled(ctx.NO() == null); // names enabled unless "no names"
  }

  // ==================== ARP ====================

  @Override
  public void exitArp_stanza(Arp_stanzaContext ctx) {
    // ARP is handled via null_rest_of_line, just store that it was seen
    // The actual timeout value would be extracted from the line text if needed
  }

  // ==================== FAILOVER ====================

  @Override
  public void exitFailover_stanza(Failover_stanzaContext ctx) {
    // Store failover configuration line for later processing
    String failoverText = ctx.getText();
    _configuration.getFailoverLines().add(failoverText);
  }

  // ==================== OSPF ====================

  @Override
  public void enterRouter_ospf_stanza(Router_ospf_stanzaContext ctx) {
    String pid = ctx.process_id.getText();
    _currentOspfProcess =
        _configuration.getOspfProcesses().computeIfAbsent(pid, FtdOspfProcess::new);
  }

  @Override
  public void exitRouter_ospf_stanza(Router_ospf_stanzaContext ctx) {
    _currentOspfProcess = null;
  }

  @Override
  public void exitOspf_network(Ospf_networkContext ctx) {
    if (_currentOspfProcess != null && ctx.ip != null && ctx.mask != null && ctx.area != null) {
      Ip ip = parseIpSafely(ctx.ip.getText(), "OSPF network IP");
      Ip mask = parseIpSafely(ctx.mask.getText(), "OSPF network mask");
      Long area = parseLongBounded(ctx.area.getText(), "OSPF area", 0, 0xFFFFFFFFL);
      if (ip != null && mask != null && area != null) {
        FtdOspfNetwork net = new FtdOspfNetwork(ip, mask, area);
        _currentOspfProcess.getNetworks().add(net);
      }
    }
  }

  @Override
  public void exitOspf_router_id(Ospf_router_idContext ctx) {
    if (_currentOspfProcess != null && ctx.id != null) {
      Ip routerId = parseIpSafely(ctx.id.getText(), "OSPF router ID");
      if (routerId != null) {
        _currentOspfProcess.setRouterId(routerId);
      }
    }
  }

  @Override
  public void exitOspf_passive_interface(Ospf_passive_interfaceContext ctx) {
    if (_currentOspfProcess != null && ctx.interface_name() != null) {
      _currentOspfProcess.getPassiveInterfaces().add(ctx.interface_name().getText());
    }
  }

  // ==================== BGP ====================

  @Override
  public void enterRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
    Long asn = parseLongBounded(ctx.asn.getText(), "BGP ASN", 1L, 4294967295L);
    if (asn != null) {
      _currentBgpProcess = new FtdBgpProcess(asn);
      _configuration.setBgpProcess(_currentBgpProcess);
    }
  }

  @Override
  public void exitRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
    _currentBgpProcess = null;
  }

  @Override
  public void enterBgp_address_family(Bgp_address_familyContext ctx) {
    _inBgpAddressFamily = true;
    if (_currentBgpProcess != null) {
      _currentBgpProcess.setHasIpv4AddressFamily(true);
    }
  }

  @Override
  public void exitBgp_address_family(Bgp_address_familyContext ctx) {
    _inBgpAddressFamily = false;
  }

  @Override
  public void exitBgp_router_id(Bgp_router_idContext ctx) {
    if (_currentBgpProcess != null && ctx.id != null) {
      Ip routerId = parseIpSafely(ctx.id.getText(), "BGP router ID");
      if (routerId != null) {
        _currentBgpProcess.setRouterId(routerId);
      }
    }
  }

  @Override
  public void exitBgp_neighbor(Bgp_neighborContext ctx) {
    if (_currentBgpProcess != null && ctx.ip != null) {
      Ip ip = parseIpSafely(ctx.ip.getText(), "BGP neighbor IP");
      if (ip == null) {
        return;
      }
      FtdBgpNeighbor neighbor =
          _currentBgpProcess.getNeighbors().computeIfAbsent(ip, k -> new FtdBgpNeighbor(k));

      if (ctx.remote_as != null) {
        Long remoteAs = parseLongBounded(ctx.remote_as.getText(), "BGP remote AS", 1L, 4294967295L);
        if (remoteAs != null) {
          neighbor.setRemoteAs(remoteAs);
        }
      }
      if (ctx.description != null) {
        neighbor.setDescription(ctx.description.getText().trim());
      }
      if (ctx.bgp_neighbor_timers() != null) {
        var timersCtx = ctx.bgp_neighbor_timers();
        if (timersCtx.keepalive != null) {
          Integer keepalive =
              parseIntegerBounded(timersCtx.keepalive.getText(), "BGP keepalive", 0, 65535);
          if (keepalive != null) {
            neighbor.setKeepalive(keepalive);
          }
        }
        if (timersCtx.holdtime != null) {
          Integer holdTime =
              parseIntegerBounded(timersCtx.holdtime.getText(), "BGP holdtime", 0, 65535);
          if (holdTime != null) {
            neighbor.setHoldTime(holdTime);
          }
        }
      }
      if (ctx.bgp_neighbor_route_map() != null) {
        var routeMapCtx = ctx.bgp_neighbor_route_map();
        if (routeMapCtx.IN() != null && routeMapCtx.map_name != null) {
          String mapName = routeMapCtx.map_name.getText().trim();
          neighbor.setRouteMapIn(mapName);
          _w.redFlag("BGP route-map in is not supported in conversion: " + mapName);
          referenceStructure(
              FtdStructureType.ROUTE_MAP,
              mapName,
              FtdStructureUsage.BGP_ROUTE_MAP_IN,
              routeMapCtx.getStart().getLine());
        } else if (routeMapCtx.OUT() != null && routeMapCtx.map_name != null) {
          String mapName = routeMapCtx.map_name.getText().trim();
          neighbor.setRouteMapOut(mapName);
          _w.redFlag("BGP route-map out is not supported in conversion: " + mapName);
          referenceStructure(
              FtdStructureType.ROUTE_MAP,
              mapName,
              FtdStructureUsage.BGP_ROUTE_MAP_OUT,
              routeMapCtx.getStart().getLine());
        }
      }
      if (ctx.ACTIVATE() != null && _inBgpAddressFamily) {
        neighbor.setIpv4UnicastActive(true);
      }
    }
  }

  // ==================== Helper Methods ====================

  private String getAclName(Access_list_stanzaContext ctx) {
    if (ctx.acl_name() == null) {
      return "unnamed";
    }
    StringBuilder sb = new StringBuilder();
    ctx.acl_name().children.forEach(child -> sb.append(child.getText()));
    return sb.toString().trim();
  }

  private @Nullable String parseClassMapAccessList(@Nonnull String line) {
    String trimmed = line.trim();
    if (!trimmed.toLowerCase().startsWith("match")) {
      return null;
    }
    String[] parts = trimmed.split("\\s+");
    if (parts.length >= 3 && parts[1].equalsIgnoreCase("access-list")) {
      return joinRemainder(parts, 2);
    }
    return null;
  }

  private @Nonnull String joinRemainder(@Nonnull String[] parts, int startIndex) {
    StringBuilder sb = new StringBuilder();
    for (int i = startIndex; i < parts.length; i++) {
      if (i > startIndex) {
        sb.append(' ');
      }
      sb.append(parts[i]);
    }
    return sb.toString().trim();
  }

  private @Nullable String getLineText(int lineNumber) {
    if (lineNumber <= 0 || lineNumber > _textLines.length) {
      return null;
    }
    return _textLines[lineNumber - 1];
  }

  private String getObjectName(Object_stanzaContext ctx) {
    if (ctx.name == null) {
      return "unnamed";
    }
    return ctx.name.getText().trim();
  }

  private String getObjectGroupName(Object_group_stanzaContext ctx) {
    if (ctx.name == null) {
      return "unnamed";
    }
    return ctx.name.getText().trim();
  }

  private @Nullable String getPortSpecText(@Nullable Port_specContext ctx) {
    if (ctx == null || ctx.children == null || ctx.children.isEmpty()) {
      return null;
    }
    if (ctx.port_specifier() != null) {
      var spec = ctx.port_specifier();
      if (spec.EQ() != null && spec.port != null) {
        return "eq " + spec.port.getText();
      }
      if (spec.GT() != null && spec.port != null) {
        return "gt " + spec.port.getText();
      }
      if (spec.LT() != null && spec.port != null) {
        return "lt " + spec.port.getText();
      }
      if (spec.NEQ() != null && spec.port != null) {
        return "neq " + spec.port.getText();
      }
      if (spec.RANGE() != null && spec.port_low != null && spec.port_high != null) {
        return "range " + spec.port_low.getText() + " " + spec.port_high.getText();
      }
    }
    if (ctx.OBJECT_GROUP() != null && ctx.port_object_group_name_null() != null) {
      return "object-group " + ctx.port_object_group_name_null().getText();
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ctx.children.size(); i++) {
      if (i > 0) {
        sb.append(' ');
      }
      sb.append(ctx.children.get(i).getText());
    }
    return sb.toString();
  }

  private void referenceServiceObjectGroupFromPortSpec(@Nullable Port_specContext ctx) {
    String portSpec = getPortSpecText(ctx);
    if (portSpec == null) {
      return;
    }
    String trimmed = portSpec.trim();
    if (!trimmed.toLowerCase().startsWith("object-group")) {
      return;
    }
    String groupName = trimmed.substring("object-group".length()).trim();
    if (!groupName.isEmpty() && ctx != null) {
      referenceStructure(
          FtdStructureType.SERVICE_OBJECT_GROUP,
          groupName,
          FtdStructureUsage.ACCESS_LIST_SERVICE_OBJECT_GROUP,
          ctx.getStart().getLine());
    }
  }

  private LineAction toLineAction(Access_list_actionContext ctx) {
    if (ctx == null) {
      return LineAction.PERMIT;
    }
    if (ctx.DENY() != null) {
      return LineAction.DENY;
    }
    return LineAction.PERMIT;
  }

  private FtdAccessListAddressSpecifier toAddressSpecifier(Acl_address_specContext ctx) {
    if (ctx.HOST() != null && ctx.ip != null) {
      return FtdAccessListAddressSpecifier.host(Ip.parse(ctx.ip.getText()));
    }
    if (ctx.OBJECT() != null && ctx.object_name_null() != null) {
      String name = getObjectReferenceName(ctx.object_name_null());
      referenceStructure(
          FtdStructureType.NETWORK_OBJECT,
          name,
          FtdStructureUsage.ACCESS_LIST_NETWORK_OBJECT,
          ctx.object_name_null().getStart().getLine());
      return FtdAccessListAddressSpecifier.object(name);
    }
    if (ctx.OBJECT_GROUP() != null && ctx.object_group_name_null() != null) {
      String name = getObjectReferenceName(ctx.object_group_name_null());
      referenceStructure(
          FtdStructureType.NETWORK_OBJECT_GROUP,
          name,
          FtdStructureUsage.ACCESS_LIST_NETWORK_OBJECT_GROUP,
          ctx.object_group_name_null().getStart().getLine());
      return FtdAccessListAddressSpecifier.objectGroup(name);
    }
    if (ctx.ANY() != null) {
      return FtdAccessListAddressSpecifier.any();
    }
    if (ctx.ANY4() != null) {
      return FtdAccessListAddressSpecifier.any4();
    }
    if (ctx.ANY6() != null) {
      return FtdAccessListAddressSpecifier.any6();
    }
    if (ctx.ip != null && ctx.mask != null) {
      return FtdAccessListAddressSpecifier.networkMask(
          Ip.parse(ctx.ip.getText()), Ip.parse(ctx.mask.getText()));
    }
    assert false;
    return null;
  }

  private String getObjectReferenceName(ParserRuleContext ctx) {
    if (ctx.children == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (ParseTree child : ctx.children) {
      String text = child.getText();
      if (PORT_OPERATORS.contains(text.toLowerCase())) {
        break;
      }
      sb.append(text);
    }
    return sb.toString();
  }

  // ==================== State Fields ====================

  private final String _text;
  private final String[] _textLines;
  private final FtdCombinedParser _parser;
  private final Warnings _w;
  private final SilentSyntaxCollection _silentSyntax;
  private final FtdConfiguration _configuration;
  private static final Set<String> PORT_OPERATORS = Set.of("eq", "gt", "lt", "neq", "range");

  // Interface state
  private @Nullable FtdInterface _currentInterface;

  // ACL state
  private @Nullable String _currentAclName;
  private @Nullable FtdAccessList _currentAcl;

  private @Nullable FtdNetworkObject _currentNetworkObject;
  private @Nullable FtdNetworkObjectGroup _currentNetworkObjectGroup;
  private @Nullable FtdServiceObjectGroup _currentServiceObjectGroup;
  private @Nullable FtdClassMap _currentClassMap;
  private @Nullable FtdPolicyMap _currentPolicyMap;
  private @Nullable String _currentPolicyMapClassName;
  private boolean _currentPolicyMapInParameters;
  private @Nullable FtdCryptoMapEntry _currentCryptoMapEntry;
  private @Nullable FtdIpsecProfile _currentIpsecProfile;
  private @Nullable FtdIkev2Policy _currentIkev2Policy;
  private @Nullable FtdTunnelGroup _currentTunnelGroup;

  // NAT state

  // OSPF state
  private @Nullable FtdOspfProcess _currentOspfProcess;
  private @Nullable FtdBgpProcess _currentBgpProcess;
  private boolean _inBgpAddressFamily;

  private void defineStructure(FtdStructureType type, String name, ParserRuleContext ctx) {
    _configuration.defineStructure(type, name, ctx);
  }

  private void referenceStructure(
      FtdStructureType type, String name, FtdStructureUsage usage, int line) {
    _configuration.referenceStructure(type, name, usage, line);
  }

  private void initVrf(String vrfName) {
    _configuration.getVrfs().computeIfAbsent(vrfName, Vrf::new);
  }
}
