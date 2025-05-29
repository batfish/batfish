package org.batfish.grammar.fortios;

import static org.batfish.grammar.fortios.FortiosLexer.UNQUOTED_WORD_CHARS;
import static org.batfish.representation.fortios.FortiosStructureUsage.BGP_UPDATE_SOURCE_INTERFACE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.fortios.FortiosParser.Access_list_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Access_list_or_prefix_list_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Acl_rule_numberContext;
import org.batfish.grammar.fortios.FortiosParser.Address_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Address_namesContext;
import org.batfish.grammar.fortios.FortiosParser.Address_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Addrgrp_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Allow_or_denyContext;
import org.batfish.grammar.fortios.FortiosParser.Bgp_asContext;
import org.batfish.grammar.fortios.FortiosParser.Bgp_neighbor_idContext;
import org.batfish.grammar.fortios.FortiosParser.Bgp_network_idContext;
import org.batfish.grammar.fortios.FortiosParser.Bgp_remote_asContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_editContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_renameContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_allow_routingContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_associated_interfaceContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_commentContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_end_ipContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_fabric_objectContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_interfaceContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_start_ipContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_subnetContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfa_set_wildcardContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_append_exclude_memberContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_append_memberContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_editContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_renameContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_set_commentContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_set_excludeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_set_exclude_memberContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_set_fabric_objectContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_set_memberContext;
import org.batfish.grammar.fortios.FortiosParser.Cfaddrgrp_set_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfisn_editContext;
import org.batfish.grammar.fortios.FortiosParser.Cfisne_set_internet_service_idContext;
import org.batfish.grammar.fortios.FortiosParser.Cfisne_set_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_append_dstaddrContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_append_dstintfContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_append_serviceContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_append_srcaddrContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_append_srcintfContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_cloneContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_deleteContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_editContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_moveContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_actionContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_commentsContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_dstaddrContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_dstintfContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_serviceContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_srcaddrContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_srcintfContext;
import org.batfish.grammar.fortios.FortiosParser.Cfp_set_statusContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_editContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_renameContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_set_commentContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_set_icmpcodeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_set_icmptypeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_set_protocolContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_set_protocol_numberContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_set_sctp_portrangeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_set_tcp_portrangeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_set_udp_portrangeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_unset_icmpcodeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsc_unset_icmptypeContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsg_editContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsg_renameContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsg_set_commentContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsg_set_memberContext;
import org.batfish.grammar.fortios.FortiosParser.Cfsga_memberContext;
import org.batfish.grammar.fortios.FortiosParser.Cr_bgpContext;
import org.batfish.grammar.fortios.FortiosParser.Cral_editContext;
import org.batfish.grammar.fortios.FortiosParser.Crale_set_commentsContext;
import org.batfish.grammar.fortios.FortiosParser.Cralecr_editContext;
import org.batfish.grammar.fortios.FortiosParser.Cralecre_set_actionContext;
import org.batfish.grammar.fortios.FortiosParser.Cralecre_set_exact_matchContext;
import org.batfish.grammar.fortios.FortiosParser.Cralecre_set_prefixContext;
import org.batfish.grammar.fortios.FortiosParser.Cralecre_set_wildcardContext;
import org.batfish.grammar.fortios.FortiosParser.Crb_set_asContext;
import org.batfish.grammar.fortios.FortiosParser.Crb_set_router_idContext;
import org.batfish.grammar.fortios.FortiosParser.Crbcn_editContext;
import org.batfish.grammar.fortios.FortiosParser.Crbcne_set_remote_asContext;
import org.batfish.grammar.fortios.FortiosParser.Crbcne_set_route_map_inContext;
import org.batfish.grammar.fortios.FortiosParser.Crbcne_set_route_map_outContext;
import org.batfish.grammar.fortios.FortiosParser.Crbcr_set_statusContext;
import org.batfish.grammar.fortios.FortiosParser.Crrm_editContext;
import org.batfish.grammar.fortios.FortiosParser.Crrme_set_commentsContext;
import org.batfish.grammar.fortios.FortiosParser.Crrmecr_editContext;
import org.batfish.grammar.fortios.FortiosParser.Crrmecre_set_actionContext;
import org.batfish.grammar.fortios.FortiosParser.Crrmecre_set_match_ip_addressContext;
import org.batfish.grammar.fortios.FortiosParser.Crs_editContext;
import org.batfish.grammar.fortios.FortiosParser.Crs_set_deviceContext;
import org.batfish.grammar.fortios.FortiosParser.Crs_set_distanceContext;
import org.batfish.grammar.fortios.FortiosParser.Crs_set_dstContext;
import org.batfish.grammar.fortios.FortiosParser.Crs_set_gatewayContext;
import org.batfish.grammar.fortios.FortiosParser.Crs_set_sdwanContext;
import org.batfish.grammar.fortios.FortiosParser.Crs_set_statusContext;
import org.batfish.grammar.fortios.FortiosParser.Cs_replacemsgContext;
import org.batfish.grammar.fortios.FortiosParser.Csg_hostnameContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_editContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_aliasContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_descriptionContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_interfaceContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_ipContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_memberContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_mtuContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_mtu_overrideContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_secondary_ipContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_speedContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_statusContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_vdomContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_vlanidContext;
import org.batfish.grammar.fortios.FortiosParser.Csi_set_vrfContext;
import org.batfish.grammar.fortios.FortiosParser.Csiecsip_editContext;
import org.batfish.grammar.fortios.FortiosParser.Csiecsipe_set_ipContext;
import org.batfish.grammar.fortios.FortiosParser.Csr_set_bufferContext;
import org.batfish.grammar.fortios.FortiosParser.Csr_unset_bufferContext;
import org.batfish.grammar.fortios.FortiosParser.Csz_append_interfaceContext;
import org.batfish.grammar.fortios.FortiosParser.Csz_editContext;
import org.batfish.grammar.fortios.FortiosParser.Csz_renameContext;
import org.batfish.grammar.fortios.FortiosParser.Csz_set_descriptionContext;
import org.batfish.grammar.fortios.FortiosParser.Csz_set_interfaceContext;
import org.batfish.grammar.fortios.FortiosParser.Csz_set_intrazoneContext;
import org.batfish.grammar.fortios.FortiosParser.Device_hostnameContext;
import org.batfish.grammar.fortios.FortiosParser.Double_quoted_stringContext;
import org.batfish.grammar.fortios.FortiosParser.Enable_or_disableContext;
import org.batfish.grammar.fortios.FortiosParser.Fortios_configurationContext;
import org.batfish.grammar.fortios.FortiosParser.Interface_aliasContext;
import org.batfish.grammar.fortios.FortiosParser.Interface_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Interface_namesContext;
import org.batfish.grammar.fortios.FortiosParser.Interface_or_zone_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Interface_or_zone_namesContext;
import org.batfish.grammar.fortios.FortiosParser.Interface_speedContext;
import org.batfish.grammar.fortios.FortiosParser.Interface_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Internet_service_idContext;
import org.batfish.grammar.fortios.FortiosParser.Internet_service_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Internet_service_name_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Ip_addressContext;
import org.batfish.grammar.fortios.FortiosParser.Ip_address_with_mask_or_prefixContext;
import org.batfish.grammar.fortios.FortiosParser.Ip_address_with_mask_or_prefix_or_anyContext;
import org.batfish.grammar.fortios.FortiosParser.Ip_prefixContext;
import org.batfish.grammar.fortios.FortiosParser.Ip_protocol_numberContext;
import org.batfish.grammar.fortios.FortiosParser.Ip_wildcardContext;
import org.batfish.grammar.fortios.FortiosParser.Ipv6_addressContext;
import org.batfish.grammar.fortios.FortiosParser.MtuContext;
import org.batfish.grammar.fortios.FortiosParser.Permit_or_denyContext;
import org.batfish.grammar.fortios.FortiosParser.Policy_actionContext;
import org.batfish.grammar.fortios.FortiosParser.Policy_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Policy_numberContext;
import org.batfish.grammar.fortios.FortiosParser.Policy_statusContext;
import org.batfish.grammar.fortios.FortiosParser.Port_rangeContext;
import org.batfish.grammar.fortios.FortiosParser.Replacemsg_major_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Replacemsg_minor_typeContext;
import org.batfish.grammar.fortios.FortiosParser.Route_distanceContext;
import org.batfish.grammar.fortios.FortiosParser.Route_map_actionContext;
import org.batfish.grammar.fortios.FortiosParser.Route_map_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Service_nameContext;
import org.batfish.grammar.fortios.FortiosParser.Service_namesContext;
import org.batfish.grammar.fortios.FortiosParser.Service_port_rangeContext;
import org.batfish.grammar.fortios.FortiosParser.Service_port_rangesContext;
import org.batfish.grammar.fortios.FortiosParser.Service_protocolContext;
import org.batfish.grammar.fortios.FortiosParser.Single_quoted_stringContext;
import org.batfish.grammar.fortios.FortiosParser.StrContext;
import org.batfish.grammar.fortios.FortiosParser.Subnet_maskContext;
import org.batfish.grammar.fortios.FortiosParser.Uint16Context;
import org.batfish.grammar.fortios.FortiosParser.Uint8Context;
import org.batfish.grammar.fortios.FortiosParser.Up_or_downContext;
import org.batfish.grammar.fortios.FortiosParser.VlanidContext;
import org.batfish.grammar.fortios.FortiosParser.VrfContext;
import org.batfish.grammar.fortios.FortiosParser.WordContext;
import org.batfish.grammar.fortios.FortiosParser.Zone_nameContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.fortios.AccessList;
import org.batfish.representation.fortios.AccessListRule;
import org.batfish.representation.fortios.Address;
import org.batfish.representation.fortios.Addrgrp;
import org.batfish.representation.fortios.BatfishUUID;
import org.batfish.representation.fortios.BgpNeighbor;
import org.batfish.representation.fortios.BgpNetwork;
import org.batfish.representation.fortios.FortiosConfiguration;
import org.batfish.representation.fortios.FortiosStructureType;
import org.batfish.representation.fortios.FortiosStructureUsage;
import org.batfish.representation.fortios.Interface;
import org.batfish.representation.fortios.Interface.Speed;
import org.batfish.representation.fortios.Interface.Type;
import org.batfish.representation.fortios.InternetServiceName;
import org.batfish.representation.fortios.Policy;
import org.batfish.representation.fortios.Policy.Action;
import org.batfish.representation.fortios.Policy.Status;
import org.batfish.representation.fortios.Replacemsg;
import org.batfish.representation.fortios.RouteMap;
import org.batfish.representation.fortios.RouteMapRule;
import org.batfish.representation.fortios.SecondaryIp;
import org.batfish.representation.fortios.Service;
import org.batfish.representation.fortios.Service.Protocol;
import org.batfish.representation.fortios.ServiceGroup;
import org.batfish.representation.fortios.StaticRoute;
import org.batfish.representation.fortios.Zone;
import org.batfish.representation.fortios.Zone.IntrazoneAction;

/**
 * Given a parse tree, builds a {@link FortiosConfiguration} that has been prepopulated with
 * metadata and defaults by {@link FortiosPreprocessor}.
 */
public final class FortiosConfigurationBuilder extends FortiosParserBaseListener
    implements SilentSyntaxListener {

  public FortiosConfigurationBuilder(
      String text,
      FortiosCombinedParser parser,
      Warnings warnings,
      FortiosConfiguration configuration,
      SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _w = warnings;
    _c = configuration;
    _silentSyntax = silentSyntax;
  }

  /** Get a new, unique BatfishUUID. */
  public @Nonnull BatfishUUID getUUID() {
    return new BatfishUUID(_uuidSequenceNumber++);
  }

  @Override
  public String getInputText() {
    return _text;
  }

  @Override
  public BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public Warnings getWarnings() {
    return _w;
  }

  @Override
  public void exitFortios_configuration(Fortios_configurationContext ctx) {
    // After renaming is complete, generate object names from UUIDs
    for (Policy policy : _c.getPolicies().values()) {
      policy.setSrcAddr(toNames(policy.getSrcAddrUUIDs()));
      policy.setDstAddr(toNames(policy.getDstAddrUUIDs()));
      policy.setService(toNames(policy.getServiceUUIDs()));
      policy.setDstIntfZones(toNames(policy.getDstIntfZoneUUIDs()));
      policy.setSrcIntfZones(toNames(policy.getSrcIntfZoneUUIDs()));
    }

    for (ServiceGroup group : _c.getServiceGroups().values()) {
      group.setMember(toNames(group.getMemberUUIDs()));
    }

    for (Addrgrp group : _c.getAddrgrps().values()) {
      group.setExcludeMember(toNames(group.getExcludeMemberUUIDs()));
      group.setMember(toNames(group.getMemberUUIDs()));
    }

    for (Address address : _c.getAddresses().values()) {
      BatfishUUID uuid = address.getAssociatedInterfaceZoneUUID();
      if (uuid != null) {
        address.setAssociatedInterfaceZone(toName(uuid));
      }
    }
  }

  /**
   * Convert set of {@link BatfishUUID} to a set of {@link
   * org.batfish.representation.fortios.FortiosRenameableObject} names
   */
  private Set<String> toNames(Set<BatfishUUID> uuids) {
    return uuids.stream().map(this::toName).collect(ImmutableSet.toImmutableSet());
  }

  private String toName(BatfishUUID uuid) {
    return _c.getRenameableObjects().get(uuid).getName();
  }

  @Override
  public void exitCsg_hostname(Csg_hostnameContext ctx) {
    toString(ctx, ctx.host).ifPresent(_c::setHostname);
  }

  @Override
  public void enterCs_replacemsg(Cs_replacemsgContext ctx) {
    String majorType = toString(ctx.major_type);
    Optional<String> maybeMinorType = toString(ctx, ctx.minor_type);
    if (!maybeMinorType.isPresent()) {
      _currentReplacemsg = new Replacemsg(); // dummy
      return;
    }
    _currentReplacemsg =
        _c.getReplacemsgs()
            .computeIfAbsent(majorType, n -> new HashMap<>())
            .computeIfAbsent(maybeMinorType.get(), n -> new Replacemsg());
  }

  @Override
  public void exitCs_replacemsg(Cs_replacemsgContext ctx) {
    _currentReplacemsg = null;
  }

  @Override
  public void exitCsr_set_buffer(Csr_set_bufferContext ctx) {
    _currentReplacemsg.setBuffer(toString(ctx.buffer));
  }

  @Override
  public void exitCsr_unset_buffer(Csr_unset_bufferContext ctx) {
    _currentReplacemsg.setBuffer(null);
  }

  @Override
  public void exitCfa_rename(Cfa_renameContext ctx) {
    Optional<String> currentNameOpt = toString(ctx, ctx.current_name);
    Optional<String> newNameOpt = toString(ctx, ctx.new_name);
    assert currentNameOpt.isPresent();
    assert newNameOpt.isPresent();

    String currentName = currentNameOpt.get();
    String newName = newNameOpt.get();
    if (!_c.getAddresses().containsKey(currentName)) {
      warnRenameNonExistent(ctx, currentName, FortiosStructureType.ADDRESS);
      return;
    }
    if (_c.getAddresses().containsKey(newName) || _c.getAddrgrps().containsKey(newName)) {
      // TODO handle conflicting renames
      warnRenameConflict(ctx, currentName, newName, FortiosStructureType.ADDRESS);
      return;
    }
    // Rename refs / def
    _c.renameStructure(
        currentName,
        newName,
        FortiosStructureType.ADDRESS,
        ImmutableSet.of(FortiosStructureType.ADDRESS, FortiosStructureType.ADDRGRP));
    // Rename the object itself
    Address currentAddress = _c.getAddresses().remove(currentName);
    currentAddress.setName(newName);
    _c.getAddresses().put(newName, currentAddress);
    // Add the rename as part of the def
    _c.defineStructure(FortiosStructureType.ADDRESS, newName, ctx);
  }

  @Override
  public void enterCfa_edit(FortiosParser.Cfa_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.address_name());
    Address existingAddress = name.map(_c.getAddresses()::get).orElse(null);
    if (existingAddress != null) {
      // Make a clone to edit
      _currentAddress = SerializationUtils.clone(existingAddress);
    } else {
      _currentAddress = new Address(toString(ctx.address_name().str()), getUUID());
    }
    _currentAddressNameValid = name.isPresent();
  }

  @Override
  public void exitCfa_edit(Cfa_editContext ctx) {
    // If edited address is valid, add/update the entry in VS addresses map.
    String invalidReason = addressValid(_currentAddress, _currentAddressNameValid);
    if (invalidReason == null) {
      _c.defineStructure(FortiosStructureType.ADDRESS, _currentAddress.getName(), ctx);
      _c.getAddresses().put(_currentAddress.getName(), _currentAddress);
      _c.getRenameableObjects().put(_currentAddress.getBatfishUUID(), _currentAddress);
    } else {
      warn(ctx, String.format("Address edit block ignored: %s", invalidReason));
    }
    _currentAddress = null;
  }

  @Override
  public void exitCfa_set_allow_routing(Cfa_set_allow_routingContext ctx) {
    _currentAddress.setAllowRouting(toBoolean(ctx.value));
    todo(ctx);
  }

  @Override
  public void exitCfa_set_associated_interface(Cfa_set_associated_interfaceContext ctx) {
    Optional<String> optName = toString(ctx, ctx.name);
    if (!optName.isPresent()) {
      return;
    }
    // Permitted zone names are a superset of permitted interface names, so at this point we know
    // the name is a valid zone name, but it may or may not be a valid interface name.
    String name = optName.get();
    int line = ctx.start.getLine();

    if (_c.getZones().containsKey(name)) {
      _currentAddress.setAssociatedInterfaceZoneUUID(_c.getZones().get(name).getBatfishUUID());
      _c.referenceStructure(
          FortiosStructureType.ZONE,
          name,
          FortiosStructureUsage.ADDRESS_ASSOCIATED_INTERFACE,
          line);
      return;
    }

    if (_c.getInterfaces().containsKey(name)) {
      // Zoned interfaces can't be referenced in this context
      ImmutableSet<String> zonedIfaceNames =
          _c.getZones().values().stream()
              .map(Zone::getInterface)
              .flatMap(Collection::stream)
              .collect(ImmutableSet.toImmutableSet());
      if (zonedIfaceNames.contains(name)) {
        warn(ctx, "Cannot reference zoned interface " + name);
        return;
      }

      _currentAddress.setAssociatedInterface(name);
      _c.referenceStructure(
          FortiosStructureType.INTERFACE,
          name,
          FortiosStructureUsage.ADDRESS_ASSOCIATED_INTERFACE,
          line);
      return;
    }

    warn(ctx, "No interface or zone named " + name);
    _c.undefined(
        FortiosStructureType.INTERFACE_OR_ZONE,
        name,
        FortiosStructureUsage.ADDRESS_ASSOCIATED_INTERFACE,
        line);
  }

  @Override
  public void exitCfa_set_comment(Cfa_set_commentContext ctx) {
    _currentAddress.setComment(toString(ctx.comment));
  }

  @Override
  public void exitCfa_set_fabric_object(Cfa_set_fabric_objectContext ctx) {
    todo(ctx);
    _currentAddress.setFabricObject(toBoolean(ctx.value));
  }

  @Override
  public void exitCfa_set_start_ip(Cfa_set_start_ipContext ctx) {
    if (_currentAddress.getType() == Address.Type.IPRANGE) {
      _currentAddress.getTypeSpecificFields().setIp1(toIp(ctx.ip));
    } else {
      warn(ctx, "Cannot set start-ip for address type " + _currentAddress.getTypeEffective());
    }
  }

  @Override
  public void exitCfa_set_end_ip(Cfa_set_end_ipContext ctx) {
    if (_currentAddress.getType() == Address.Type.IPRANGE) {
      _currentAddress.getTypeSpecificFields().setIp2(toIp(ctx.ip));
    } else {
      warn(ctx, "Cannot set end-ip for address type " + _currentAddress.getTypeEffective());
    }
  }

  @Override
  public void exitCfa_set_interface(Cfa_set_interfaceContext ctx) {
    if (_currentAddress.getType() != Address.Type.INTERFACE_SUBNET) {
      warn(ctx, "Cannot set interface for address type " + _currentAddress.getTypeEffective());
      return;
    }
    Optional<String> name = toString(ctx, ctx.name);
    if (name.isPresent()) {
      if (_c.getInterfaces().containsKey(name.get())) {
        _currentAddress.getTypeSpecificFields().setInterface(name.get());
      } else {
        // TODO File undefined reference to interface
        warn(ctx, "No interface named " + name.get());
      }
    }
  }

  @Override
  public void exitCfa_set_subnet(Cfa_set_subnetContext ctx) {
    Address.Type currentType = _currentAddress.getTypeEffective();
    if (currentType == Address.Type.IPMASK || currentType == Address.Type.INTERFACE_SUBNET) {
      if (ctx.subnet.ip_prefix() != null) {
        Prefix prefix = toPrefix(ctx.subnet.ip_prefix());
        _currentAddress.getTypeSpecificFields().setIp1(prefix.getStartIp());
        // getPrefixWildcard returns a mask where the 1s indicate bits that DON'T matter,
        // so invert it to get the correct mask for FortiOS
        _currentAddress.getTypeSpecificFields().setIp2(prefix.getPrefixWildcard().inverted());
      } else {
        assert ctx.subnet.ip != null && ctx.subnet.mask != null;
        // Convert to wildcard to get canonicalized IP (CLI automatically zeroes out bits in the IP
        // that are zeros in the mask, even if the mask is invalid).
        IpWildcard wildcard = toIpWildcard(ctx.subnet.ip, ctx.subnet.mask, true);
        _currentAddress.getTypeSpecificFields().setIp1(wildcard.getIp());
        _currentAddress.getTypeSpecificFields().setIp2(toIp(ctx.subnet.mask));
      }
    } else {
      warn(ctx, "Cannot set subnet for address type " + currentType);
    }
  }

  @Override
  public void exitCfa_set_type(Cfa_set_typeContext ctx) {
    Address.Type newType = toAddressType(ctx.type);
    Address prevAddress = _c.getAddresses().get(_currentAddress.getName());
    if (prevAddress != null
        && validExcludeMemberType(prevAddress.getTypeEffective())
        && !validExcludeMemberType(newType)) {
      // Note: this can warn even when the type-change is okay / isn't used as addrgrp exclude
      warn(
          ctx,
          "If this address is used as an addrgrp exclude, the FortiOS CLI will reject this line");
    }
    _currentAddress.setType(newType);
  }

  @Override
  public void exitCfa_set_wildcard(Cfa_set_wildcardContext ctx) {
    if (_currentAddress.getType() == Address.Type.WILDCARD) {
      // Convert to wildcard; canonicalizes IP based on mask bits
      IpWildcard wildcard = toIpWildcard(ctx.wildcard, true);
      // Set IP and mask in _currentAddress. Invert mask bits because IpWildcard interprets set bits
      // as "don't matter" while FortiOS interprets unset bits as "don't matter".
      _currentAddress.getTypeSpecificFields().setIp1(wildcard.getIp());
      _currentAddress.getTypeSpecificFields().setIp2(wildcard.getWildcardMaskAsIp().inverted());
    } else {
      warn(ctx, "Cannot set wildcard for address type " + _currentAddress.getTypeEffective());
    }
  }

  @Override
  public void enterCfaddrgrp_edit(Cfaddrgrp_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.address_name());
    Addrgrp existingAddrgrp = name.map(_c.getAddrgrps()::get).orElse(null);
    if (existingAddrgrp != null) {
      // Make a clone to edit
      _currentAddrgrp = SerializationUtils.clone(existingAddrgrp);
    } else {
      _currentAddrgrp = new Addrgrp(toString(ctx.address_name().str()), getUUID());
    }
    _currentAddrgrpNameValid = name.isPresent();
  }

  @Override
  public void exitCfaddrgrp_edit(Cfaddrgrp_editContext ctx) {
    // If edited addrgrp is valid, add/update the entry in VS addresses map.
    String invalidReason = addrgrpValid(_currentAddrgrp, _currentAddrgrpNameValid);
    if (invalidReason == null) {
      _c.defineStructure(FortiosStructureType.ADDRGRP, _currentAddrgrp.getName(), ctx);
      _c.getAddrgrps().put(_currentAddrgrp.getName(), _currentAddrgrp);
      _c.getRenameableObjects().put(_currentAddrgrp.getBatfishUUID(), _currentAddrgrp);
    } else {
      warn(ctx, String.format("Addrgrp edit block ignored: %s", invalidReason));
    }
    _currentAddrgrp = null;
  }

  @Override
  public void exitCfaddrgrp_set_comment(Cfaddrgrp_set_commentContext ctx) {
    _currentAddrgrp.setComment(toString(ctx.comment));
  }

  @Override
  public void exitCfaddrgrp_set_exclude(Cfaddrgrp_set_excludeContext ctx) {
    // TODO better validation
    // some types of address object members will cause `exclude enable` to be rejected
    _currentAddrgrp.setExclude(toBoolean(ctx.exclude));
  }

  @Override
  public void exitCfaddrgrp_set_fabric_object(Cfaddrgrp_set_fabric_objectContext ctx) {
    todo(ctx);
    _currentAddrgrp.setFabricObject(toBoolean(ctx.value));
  }

  @Override
  public void exitCfaddrgrp_set_type(Cfaddrgrp_set_typeContext ctx) {
    Addrgrp prevGrp = _c.getAddrgrps().get(_currentAddrgrp.getName());
    if (prevGrp != null) {
      warn(ctx, "The type of address group cannot be changed");
      return;
    }
    _currentAddrgrp.setType(toAddrgrpType(ctx, ctx.type));
  }

  @Override
  public void exitCfaddrgrp_set_exclude_member(Cfaddrgrp_set_exclude_memberContext ctx) {
    if (!_currentAddrgrp.getExcludeEffective()) {
      warn(ctx, "Cannot set exclude-member when exclude is not enabled");
      return;
    }

    toAddressUUIDs(ctx.address_names(), FortiosStructureUsage.ADDRGRP_EXCLUDE_MEMBER)
        .ifPresent(
            addresses -> {
              if (!checkAddrgrpMembersValidAndWarn(ctx, addresses)) {
                return;
              }

              Set<BatfishUUID> addrs = _currentAddrgrp.getExcludeMemberUUIDs();
              addrs.clear();
              addrs.addAll(addresses);
            });
  }

  @Override
  public void exitCfaddrgrp_set_member(Cfaddrgrp_set_memberContext ctx) {
    toAddrgrpMemberUUIDs(ctx.address_names(), FortiosStructureUsage.ADDRGRP_MEMBER, false)
        .ifPresent(
            addresses -> {
              if (!checkAddrgrpMembersValidAndWarn(ctx, addresses)) {
                return;
              }

              Set<BatfishUUID> addrs = _currentAddrgrp.getMemberUUIDs();
              addrs.clear();
              addrs.addAll(addresses);
            });
  }

  @Override
  public void exitCfaddrgrp_append_exclude_member(Cfaddrgrp_append_exclude_memberContext ctx) {
    if (!_currentAddrgrp.getExcludeEffective()) {
      warn(ctx, "Cannot set exclude-member when exclude is not enabled");
      return;
    }

    toAddressUUIDs(ctx.address_names(), FortiosStructureUsage.ADDRGRP_EXCLUDE_MEMBER)
        .ifPresent(
            addresses -> {
              if (!checkAddrgrpMembersValidAndWarn(ctx, addresses)) {
                return;
              }

              Set<BatfishUUID> addrs = _currentAddrgrp.getExcludeMemberUUIDs();
              addrs.addAll(addresses);
            });
  }

  @Override
  public void exitCfaddrgrp_append_member(Cfaddrgrp_append_memberContext ctx) {
    toAddrgrpMemberUUIDs(ctx.address_names(), FortiosStructureUsage.ADDRGRP_MEMBER, false)
        .ifPresent(
            addresses -> {
              if (!checkAddrgrpMembersValidAndWarn(ctx, addresses)) {
                return;
              }

              Set<BatfishUUID> addrs = _currentAddrgrp.getMemberUUIDs();
              addrs.addAll(addresses);
            });
  }

  /**
   * Check if the supplied addrgrp members are valid for the current addrgrp. Warns and returns
   * {@code false} if not valid, otherwise returns {@code true}.
   */
  private boolean checkAddrgrpMembersValidAndWarn(
      ParserRuleContext ctx, Set<BatfishUUID> newMembers) {
    Addrgrp parent =
        getParentAddrgrp(_currentAddrgrp.getBatfishUUID(), newMembers, _c.getAddrgrps().values());
    if (parent != null) {
      warn(
          ctx,
          String.format(
              "Addrgrp %s cannot be added to %s as it would create a cycle",
              parent.getName(), _currentAddrgrp.getName()));
      return false;
    }
    return true;
  }

  @Override
  public void exitCfaddrgrp_rename(Cfaddrgrp_renameContext ctx) {
    Optional<String> currentNameOpt = toString(ctx, ctx.current_name);
    Optional<String> newNameOpt = toString(ctx, ctx.new_name);
    if (!newNameOpt.isPresent() || !currentNameOpt.isPresent()) {
      return;
    }

    String currentName = currentNameOpt.get();
    String newName = newNameOpt.get();
    if (!_c.getAddrgrps().containsKey(currentName)) {
      warnRenameNonExistent(ctx, currentName, FortiosStructureType.ADDRGRP);
      return;
    }
    if (_c.getAddresses().containsKey(newName) || _c.getAddrgrps().containsKey(newName)) {
      // TODO handle conflicting renames
      warnRenameConflict(ctx, currentName, newName, FortiosStructureType.ADDRGRP);
      return;
    }
    // Rename refs / def
    _c.renameStructure(
        currentName,
        newName,
        FortiosStructureType.ADDRGRP,
        ImmutableSet.of(FortiosStructureType.ADDRESS, FortiosStructureType.ADDRGRP));
    // Rename the object itself
    Addrgrp current = _c.getAddrgrps().remove(currentName);
    current.setName(newName);
    _c.getAddrgrps().put(newName, current);
    // Add the rename as part of the def
    _c.defineStructure(FortiosStructureType.ADDRGRP, newName, ctx);
  }

  @Override
  public void enterCsiecsip_edit(Csiecsip_editContext ctx) {
    Optional<Long> number = toLong(ctx, ctx.sip_number());
    _currentSecondaryip =
        number
            .map(Objects::toString)
            .map(i -> _currentInterface.getSecondaryip().get(i))
            .map(SerializationUtils::clone)
            .orElseGet(() -> new SecondaryIp(toString(ctx.sip_number().str())));
    _currentSecondaryipNameValid = number.isPresent();

    // TODO 0 is allowed as a special case, representing some currently unused number
    if (number.isPresent() && number.get() == 0L) {
      warn(ctx, "Secondaryip number 0 is not yet supported");
      _currentSecondaryipNameValid = false;
    }
  }

  /** Returns message indicating why secondaryip can't be committed in the CLI, or null if it can */
  private static @Nullable String secondaryipValid(Interface iface, boolean nameValid) {
    if (!iface.getSecondaryIpEffective()) {
      return "cannot configure a secondaryip when secondary-IP is not enabled";
    }
    if (!nameValid) {
      return "name is invalid";
    }
    return null;
  }

  @Override
  public void exitCsiecsip_edit(Csiecsip_editContext ctx) {
    String number = _currentSecondaryip.getName();
    String invalidReason = secondaryipValid(_currentInterface, _currentSecondaryipNameValid);
    if (invalidReason == null) {
      _currentInterface.getSecondaryip().put(number, _currentSecondaryip);
    } else {
      warn(ctx, String.format("Secondaryip edit block ignored: %s", invalidReason));
    }
    _currentSecondaryip = null;
  }

  @Override
  public void exitCsiecsipe_set_ip(Csiecsipe_set_ipContext ctx) {
    ConcreteInterfaceAddress pip = _currentInterface.getIp();
    if (pip == null) {
      warn(ctx, "Primary ip address must be configured before a secondaryip can be configured");
      return;
    }

    ConcreteInterfaceAddress sip = toConcreteInterfaceAddress(ctx.ip);
    if (pip.getIp().equals(sip.getIp())) {
      warn(
          ctx,
          "This secondaryip will be ignored; must use a secondaryip other than the primary ip"
              + " address");
      return;
    }
    for (SecondaryIp otherSip : _currentInterface.getSecondaryip().values()) {
      ConcreteInterfaceAddress otherIp = otherSip.getIp();
      if (otherIp != null && otherIp.getIp().equals(sip.getIp())) {
        warn(
            ctx,
            "This secondaryip will be ignored; must use a secondaryip other than existing"
                + " secondaryip addresses");
        return;
      }
    }

    _currentSecondaryip.setIp(sip);
  }

  @Override
  public void enterCsi_edit(Csi_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.interface_name());
    _currentInterface =
        name.map(_c.getInterfaces()::get)
            .orElseGet(() -> new Interface(toString(ctx.interface_name().str())));
    _currentInterfaceNameValid = name.isPresent();
  }

  /** Returns message indicating why interface can't be committed in the CLI, or null if it can */
  public static @Nullable String interfaceValid(
      Interface iface, Set<String> zoneNames, boolean nameValid) {
    if (!nameValid) {
      return "name is invalid";
    }
    if (zoneNames.contains(iface.getName())) {
      return "name conflicts with a zone name";
    }
    if (iface.getTypeEffective() == Type.VLAN) {
      if (iface.getInterface() == null) {
        return "interface must be set";
      }
      if (iface.getVlanid() == null) {
        return "vlanid must be set";
      }
    }
    // TODO better validation for types other than VLAN
    return null;
  }

  @Override
  public void exitCsi_edit(Csi_editContext ctx) {
    String name = _currentInterface.getName();
    String invalidReason =
        interfaceValid(_currentInterface, _c.getZones().keySet(), _currentInterfaceNameValid);
    if (invalidReason == null) {
      _c.defineStructure(FortiosStructureType.INTERFACE, name, ctx);
      _c.referenceStructure(
          FortiosStructureType.INTERFACE,
          name,
          FortiosStructureUsage.INTERFACE_SELF_REF,
          ctx.start.getLine());
      _c.getInterfaces().put(name, _currentInterface);
    } else {
      warn(ctx, String.format("Interface edit block ignored: %s", invalidReason));
    }
    _currentInterface = null;
  }

  @Override
  public void exitCsi_set_vdom(Csi_set_vdomContext ctx) {
    _currentInterface.setVdom(toString(ctx.vdom));
  }

  @Override
  public void exitCsi_set_ip(Csi_set_ipContext ctx) {
    _currentInterface.setIp(toConcreteInterfaceAddress(ctx.ip));
  }

  @Override
  public void exitCsi_set_type(Csi_set_typeContext ctx) {
    Interface prev = _c.getInterfaces().get(_currentInterface.getName());
    if (prev != null) {
      warn(ctx, "The type of an interface cannot be changed");
      return;
    }
    _currentInterface.setType(toInterfaceType(ctx.type));
  }

  @Override
  public void exitCsi_set_alias(Csi_set_aliasContext ctx) {
    toString(ctx, ctx.alias).ifPresent(s -> _currentInterface.setAlias(s));
  }

  @Override
  public void exitCsi_set_secondary_ip(Csi_set_secondary_ipContext ctx) {
    _currentInterface.setSecondaryIp(toBoolean(ctx.value));
  }

  @Override
  public void exitCsi_set_speed(Csi_set_speedContext ctx) {
    _currentInterface.setSpeed(toSpeed(ctx.interface_speed()));
  }

  @Override
  public void exitCsi_set_status(Csi_set_statusContext ctx) {
    _currentInterface.setStatus(toStatus(ctx.status));
  }

  @Override
  public void exitCsi_set_mtu_override(Csi_set_mtu_overrideContext ctx) {
    _currentInterface.setMtuOverride(toBoolean(ctx.value));
  }

  @Override
  public void exitCsi_set_description(Csi_set_descriptionContext ctx) {
    _currentInterface.setDescription(toString(ctx.description));
  }

  @Override
  public void exitCsi_set_interface(Csi_set_interfaceContext ctx) {
    toInterface(ctx, ctx.interface_name(), FortiosStructureUsage.INTERFACE_INTERFACE)
        .ifPresent(_currentInterface::setInterface);
  }

  @Override
  public void exitCsi_set_mtu(Csi_set_mtuContext ctx) {
    toInteger(ctx, ctx.value).ifPresent(m -> _currentInterface.setMtu(m));
  }

  @Override
  public void exitCsi_set_vrf(Csi_set_vrfContext ctx) {
    toInteger(ctx, ctx.value).ifPresent(v -> _currentInterface.setVrf(v));
  }

  @Override
  public void exitCsi_set_vlanid(Csi_set_vlanidContext ctx) {
    toInteger(ctx, ctx.vlanid()).ifPresent(v -> _currentInterface.setVlanid(v));
  }

  @Override
  public void exitCsi_set_member(Csi_set_memberContext ctx) {
    toInterfaceMembers(ctx.members)
        .ifPresent(
            newMembers -> {
              Set<String> members = _currentInterface.getMembers();
              members.clear();
              members.addAll(newMembers);
            });
  }

  @Override
  public void enterCr_bgp(Cr_bgpContext ctx) {
    _c.initBgpProcess();
  }

  @Override
  public void exitCrb_set_as(Crb_set_asContext ctx) {
    toLong(ctx, ctx.bgp_as()).ifPresent(_c.getBgpProcess()::setAs);
  }

  @Override
  public void exitCrb_set_ebgp_multipath(FortiosParser.Crb_set_ebgp_multipathContext ctx) {
    _c.getBgpProcess().setEbgpMultipath(toBoolean(ctx.enable_or_disable()));
  }

  @Override
  public void exitCrb_set_ibgp_multipath(FortiosParser.Crb_set_ibgp_multipathContext ctx) {
    _c.getBgpProcess().setIbgpMultipath(toBoolean(ctx.enable_or_disable()));
  }

  @Override
  public void exitCrb_set_router_id(Crb_set_router_idContext ctx) {
    Ip routerId = toIp(ctx.router_id);
    if (routerId.equals(Ip.ZERO)) {
      warn(ctx, "Cannot use 0.0.0.0 as BGP router-id");
    } else {
      _c.getBgpProcess().setRouterId(routerId);
    }
  }

  @Override
  public void enterCrbcn_edit(Crbcn_editContext ctx) {
    Optional<Ip> neighborId = toIp(ctx, ctx.bgp_neighbor_id());
    BgpNeighbor existing = neighborId.map(_c.getBgpProcess().getNeighbors()::get).orElse(null);
    if (existing != null) {
      // Make a clone to edit
      _currentBgpNeighbor = SerializationUtils.clone(existing);
    } else {
      // If neighbor ID can't be interpreted as an IP, make a dummy BgpNeighbor.
      // TODO: Shouldn't need a dummy once neighbor ID is strictly parsed as an IP.
      // Also, the BgpNeighbor constructor can then enforce that neighbor ID isn't 0.0.0.0.
      _currentBgpNeighbor = new BgpNeighbor(neighborId.orElse(Ip.ZERO));
    }
  }

  @Override
  public void exitCrbcn_edit(Crbcn_editContext ctx) {
    String invalidReason = bgpNeighborValid(_currentBgpNeighbor);
    if (invalidReason == null) {
      _c.getBgpProcess().getNeighbors().put(_currentBgpNeighbor.getIp(), _currentBgpNeighbor);
    } else {
      warn(ctx, String.format("BGP neighbor edit block ignored: %s", invalidReason));
    }
    _currentBgpNeighbor = null;
  }

  @Override
  public void enterCrbcnet_edit(FortiosParser.Crbcnet_editContext ctx) {
    Optional<Long> networkId = toLong(ctx, ctx.bgp_network_id());
    if (networkId.isPresent() && networkId.get() == 0) {
      // "edit 0" picks a new non-zero ID based on the existing network IDs
      networkId = getNextVal(_c.getBgpProcess().getNetworks().keySet(), BGP_NETWORK_ID_SPACE);
    }
    // Using 0 as a dummy ID for edit blocks with invalid network IDs
    long id = networkId.orElse(0L);
    _currentBgpNetwork =
        Optional.ofNullable(_c.getBgpProcess().getNetworks().get(id))
            .map(SerializationUtils::clone)
            .orElseGet(() -> new BgpNetwork(id));
  }

  /**
   * Gets the default next value that the CLI will select in certain contexts when the user enters
   * "edit 0". The CLI is interprets this as shorthand for "make a new edit block with any ID".
   *
   * <p>The CLI will select the number of currently-configured values plus one, unless that value is
   * already in use, in which case it will select the next higher value that is not in use.
   *
   * @param existing Values that are already taken
   * @param permitted Space of permitted values
   * @return the value selected by the CLI, or empty optional if the selected value would be outside
   *     of the permitted values. (Not expected to ever happen in practice.)
   */
  private static Optional<Long> getNextVal(Set<Long> existing, LongSpace permitted) {
    long candidate = existing.size() + 1;
    while (existing.contains(candidate)) {
      candidate++;
    }
    return permitted.contains(candidate) ? Optional.of(candidate) : Optional.empty();
  }

  @Override
  public void exitCrbcnet_edit(FortiosParser.Crbcnet_editContext ctx) {
    String invalidReason = bgpNetworkValid(_currentBgpNetwork);
    if (invalidReason == null) {
      _c.getBgpProcess().getNetworks().put(_currentBgpNetwork.getId(), _currentBgpNetwork);
    } else {
      warn(ctx, String.format("BGP network edit block ignored: %s", invalidReason));
    }
    _currentBgpNetwork = null;
  }

  private static String bgpNetworkValid(BgpNetwork bgpNetwork) {
    if (bgpNetwork.getId() == 0L) {
      // Indicates invalid name
      return "name is invalid";
    } else if (bgpNetwork.getPrefix() == null) {
      return "prefix must be set";
    }
    return null;
  }

  @Override
  public void exitCrbcnete_set_prefix(FortiosParser.Crbcnete_set_prefixContext ctx) {
    Prefix network = toPrefix(ctx.network);
    if (network.equals(Prefix.ZERO)) {
      warn(ctx, "Prefix 0.0.0.0/0 is not allowed for a BGP network prefix");
    } else if (_c.getBgpProcess().getNetworks().values().stream()
        .anyMatch(bgpNetwork -> network.equals(bgpNetwork.getPrefix()))) {
      warn(
          ctx,
          String.format(
              "Cannot set BGP network prefix %s: Another BGP network already has this prefix",
              network));
    } else {
      _currentBgpNetwork.setPrefix(network);
    }
  }

  @Override
  public void exitCrbcne_set_remote_as(Crbcne_set_remote_asContext ctx) {
    toLong(ctx, ctx.bgp_remote_as()).ifPresent(_currentBgpNeighbor::setRemoteAs);
  }

  @Override
  public void exitCrbcne_set_route_map_in(Crbcne_set_route_map_inContext ctx) {
    toRouteMap(ctx.route_map_name(), FortiosStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_IN)
        .ifPresent(_currentBgpNeighbor::setRouteMapIn);
  }

  @Override
  public void exitCrbcne_set_route_map_out(Crbcne_set_route_map_outContext ctx) {
    toRouteMap(ctx.route_map_name(), FortiosStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_OUT)
        .ifPresent(_currentBgpNeighbor::setRouteMapOut);
  }

  private Interface.Speed toSpeed(Interface_speedContext ctx) {
    if (ctx.AUTO() != null) {
      return Speed.AUTO;
    } else if (ctx.TEN_FULL() != null) {
      return Speed.TEN_FULL;
    } else if (ctx.TEN_HALF() != null) {
      return Speed.TEN_HALF;
    } else if (ctx.HUNDRED_FULL() != null) {
      return Speed.HUNDRED_FULL;
    } else if (ctx.HUNDRED_HALF() != null) {
      return Speed.HUNDRED_HALF;
    } else if (ctx.THOUSAND_FULL() != null) {
      return Speed.THOUSAND_FULL;
    } else if (ctx.THOUSAND_HALF() != null) {
      return Speed.THOUSAND_HALF;
    } else if (ctx.TEN_THOUSAND_FULL() != null) {
      return Speed.TEN_THOUSAND_FULL;
    } else if (ctx.TEN_THOUSAND_HALF() != null) {
      return Speed.TEN_THOUSAND_HALF;
    } else if (ctx.HUNDRED_GFULL() != null) {
      return Speed.HUNDRED_GFULL;
    }
    assert ctx.HUNDRED_GHALF() != null;
    return Speed.HUNDRED_GHALF;
  }

  private Optional<String> toRouteMap(Route_map_nameContext ctx, FortiosStructureUsage usage) {
    String name = toString(ctx.str());
    int line = ctx.start.getLine();
    if (_c.getRouteMaps().containsKey(name)) {
      _c.referenceStructure(FortiosStructureType.ROUTE_MAP, name, usage, line);
      return Optional.of(name);
    }
    warn(ctx, String.format("Route-map %s is undefined and cannot be referenced", name));
    _c.undefined(FortiosStructureType.ROUTE_MAP, name, usage, line);
    return Optional.empty();
  }

  @Override
  public void exitCrbcne_set_update_source(FortiosParser.Crbcne_set_update_sourceContext ctx) {
    toInterface(ctx, ctx.interface_name(), BGP_UPDATE_SOURCE_INTERFACE)
        .ifPresent(_currentBgpNeighbor::setUpdateSource);
  }

  @Override
  public void exitCrbcr_set_status(Crbcr_set_statusContext ctx) {
    if (toBoolean(ctx.enable_or_disable())) {
      warn(ctx, "Redistribution into BGP is not yet supported");
    }
  }

  @Override
  public void enterCrs_edit(Crs_editContext ctx) {
    Optional<Long> routeNum = toLong(ctx, ctx.route_num());
    StaticRoute existing =
        routeNum.map(Object::toString).map(_c.getStaticRoutes()::get).orElse(null);
    if (existing != null) {
      // Make a clone to edit
      _currentStaticRoute = SerializationUtils.clone(existing);
    } else {
      _currentStaticRoute = new StaticRoute(toString(ctx.route_num().str()));
    }
    _currentStaticRouteNumValid = routeNum.isPresent();
  }

  @Override
  public void exitCrs_edit(Crs_editContext ctx) {
    String invalidReason = staticRouteValid(_currentStaticRoute, _currentStaticRouteNumValid);
    if (invalidReason == null) {
      _c.getStaticRoutes().put(_currentStaticRoute.getSeqNum(), _currentStaticRoute);
    } else {
      warn(ctx, String.format("Static route edit block ignored: %s", invalidReason));
    }
    _currentStaticRoute = null;
  }

  @Override
  public void exitCrs_set_device(Crs_set_deviceContext ctx) {
    toInterface(ctx, ctx.iface, FortiosStructureUsage.STATIC_ROUTE_DEVICE)
        .ifPresent(_currentStaticRoute::setDevice);
  }

  @Override
  public void exitCrs_set_distance(Crs_set_distanceContext ctx) {
    toInteger(ctx, ctx.route_distance()).ifPresent(_currentStaticRoute::setDistance);
  }

  @Override
  public void exitCrs_set_dst(Crs_set_dstContext ctx) {
    _currentStaticRoute.setDst(toPrefix(ctx.dst));
  }

  @Override
  public void exitCrs_set_gateway(Crs_set_gatewayContext ctx) {
    _currentStaticRoute.setGateway(toIp(ctx.gateway));
  }

  @Override
  public void exitCrs_set_sdwan(Crs_set_sdwanContext ctx) {
    _currentStaticRoute.setSdwanEnabled(toBoolean(ctx.enabled));
  }

  @Override
  public void exitCrs_set_status(Crs_set_statusContext ctx) {
    _currentStaticRoute.setStatus(
        toBoolean(ctx.enabled) ? StaticRoute.Status.ENABLE : StaticRoute.Status.DISABLE);
  }

  @Override
  public void enterCrrm_edit(Crrm_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.route_map_name());
    _currentRouteMap =
        name.map(_c.getRouteMaps()::get)
            .orElseGet(() -> new RouteMap(toString(ctx.route_map_name().str())));
    _currentRouteMapNameValid = name.isPresent();
  }

  @Override
  public void exitCrrm_edit(Crrm_editContext ctx) {
    // If edited item is valid, add/update the entry in VS map
    if (_currentRouteMapNameValid) {
      String name = _currentRouteMap.getName();
      _c.defineStructure(FortiosStructureType.ROUTE_MAP, name, ctx);
      _c.getRouteMaps().put(name, _currentRouteMap);
    } else {
      warn(ctx, "Route-map edit block ignored: name is invalid");
    }
    _currentRouteMap = null;
  }

  @Override
  public void exitCrrme_set_comments(Crrme_set_commentsContext ctx) {
    _currentRouteMap.setComments(toString(ctx.comment));
  }

  @Override
  public void enterCrrmecr_edit(Crrmecr_editContext ctx) {
    Optional<Long> name = toLong(ctx, ctx.route_map_rule_number());
    RouteMapRule existing =
        name.map(l -> _currentRouteMap.getRules().get(l.toString())).orElse(null);
    _currentRouteMapRuleNameValid = name.isPresent();
    if (existing != null) {
      // Make a clone to edit
      _currentRouteMapRule = SerializationUtils.clone(existing);
    } else {
      _currentRouteMapRule = new RouteMapRule(toString(ctx.route_map_rule_number().str()));
    }
  }

  @Override
  public void exitCrrmecr_edit(Crrmecr_editContext ctx) {
    // If edited item is valid, add/update the entry in VS map
    if (_currentRouteMapRuleNameValid) {
      String name = _currentRouteMapRule.getNumber();
      _currentRouteMap.getRules().put(name, _currentRouteMapRule);
    } else {
      warn(ctx, "Route-map rule edit block ignored: name is invalid");
    }
    _currentRouteMapRule = null;
  }

  @Override
  public void exitCrrmecre_set_action(Crrmecre_set_actionContext ctx) {
    _currentRouteMapRule.setAction(toAction(ctx.route_map_action()));
  }

  @Override
  public void exitCrrmecre_set_match_ip_address(Crrmecre_set_match_ip_addressContext ctx) {
    toAccessListOrPrefixList(ctx.access_list_or_prefix_list_name())
        .ifPresent(_currentRouteMapRule::setMatchIpAddress);
  }

  private Optional<String> toAccessListOrPrefixList(Access_list_or_prefix_list_nameContext ctx) {
    String name = toString(ctx.access_list_name().str());
    int line = ctx.start.getLine();
    FortiosStructureUsage usage = FortiosStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS;
    if (_c.getAccessLists().containsKey(name)) {
      _c.referenceStructure(FortiosStructureType.ACCESS_LIST, name, usage, line);
      return Optional.of(name);
    }
    // TODO check if name exists in prefix-lists
    warn(
        ctx,
        String.format("Access-list or prefix-list %s is undefined and cannot be referenced", name));
    _c.undefined(FortiosStructureType.ACCESS_LIST_OR_PREFIX_LIST, name, usage, line);
    return Optional.empty();
  }

  @Override
  public void enterCfisn_edit(Cfisn_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.internet_service_name());
    _currentInternetServiceName =
        name.map(_c.getInternetServiceNames()::get)
            .orElseGet(() -> new InternetServiceName(toString(ctx.internet_service_name().str())));
    _currentInternetServiceNameNameValid = name.isPresent();
  }

  @Override
  public void exitCfisn_edit(Cfisn_editContext ctx) {
    String name = _currentInternetServiceName.getName();
    // TODO better validation
    if (_currentInternetServiceNameNameValid) {
      _c.getInternetServiceNames().put(name, _currentInternetServiceName);
    } else {
      warn(ctx, "Internet-service-name edit block ignored: name is invalid");
    }
    _currentInternetServiceName = null;
  }

  @Override
  public void exitCfisne_set_internet_service_id(Cfisne_set_internet_service_idContext ctx) {
    toLong(ctx, ctx.internet_service_id())
        .ifPresent(_currentInternetServiceName::setInternetServiceId);
  }

  @Override
  public void exitCfisne_set_type(Cfisne_set_typeContext ctx) {
    _currentInternetServiceName.setType(toType(ctx.internet_service_name_type()));
  }

  private InternetServiceName.Type toType(Internet_service_name_typeContext ctx) {
    if (ctx.DEFAULT() != null) {
      return InternetServiceName.Type.DEFAULT;
    }
    assert ctx.LOCATION() != null;
    return InternetServiceName.Type.LOCATION;
  }

  @Override
  public void enterCfp_edit(Cfp_editContext ctx) {
    Optional<Long> number = toLong(ctx, ctx.policy_number());
    Policy existing = number.map(Object::toString).map(_c.getPolicies()::get).orElse(null);
    if (existing != null) {
      // Make a clone to edit
      _currentPolicy = SerializationUtils.clone(existing);
    } else {
      _currentPolicy = new Policy(toString(ctx.policy_number().str()));
    }
    _currentPolicyValid = number.isPresent();
  }

  @Override
  public void exitCfp_edit(Cfp_editContext ctx) {
    // If edited policy is valid, add/update the entry in VS map
    String number = _currentPolicy.getNumber();
    String invalidReason = policyValid(_currentPolicy, _currentPolicyValid);
    if (invalidReason == null) { // policy is valid
      _c.defineStructure(FortiosStructureType.POLICY, number, ctx);
      _c.referenceStructure(
          FortiosStructureType.POLICY,
          number,
          FortiosStructureUsage.POLICY_SELF_REF,
          ctx.start.getLine());
      _c.getPolicies().put(number, _currentPolicy);
    } else {
      warn(ctx, String.format("Policy edit block ignored: %s", invalidReason));
    }
    _currentPolicy = null;
  }

  @Override
  public void exitCfp_clone(Cfp_cloneContext ctx) {
    String name = toString(ctx.name.str());
    Policy existing = _c.getPolicies().get(name);
    if (existing == null) {
      warn(ctx, String.format("Cannot clone a non-existent policy %s", name));
      _c.undefined(
          FortiosStructureType.POLICY,
          name,
          FortiosStructureUsage.POLICY_CLONE,
          ctx.start.getLine());
      return;
    }

    Optional<Long> to = toLong(ctx, ctx.to);
    if (!to.isPresent()) {
      warn(ctx, "Cannot create a cloned policy with an invalid name");
      return;
    }

    String toNumber = to.get().toString();
    if (_c.getPolicies().containsKey(toNumber)) {
      warn(ctx, String.format("Cannot clone, policy %s already exists", toNumber));
      return;
    }
    Policy clone = SerializationUtils.clone(existing);
    clone.setNumber(toNumber);
    _c.getPolicies().put(toNumber, clone);
    _c.defineStructure(FortiosStructureType.POLICY, toNumber, ctx);
    _c.referenceStructure(
        FortiosStructureType.POLICY,
        toNumber,
        FortiosStructureUsage.POLICY_SELF_REF,
        ctx.start.getLine());
  }

  @Override
  public void exitCfp_delete(Cfp_deleteContext ctx) {
    String name = toString(ctx.name.str());
    if (!_c.getPolicies().containsKey(name)) {
      warn(ctx, String.format("Cannot delete a non-existent policy %s", name));
      _c.undefined(
          FortiosStructureType.POLICY,
          name,
          FortiosStructureUsage.POLICY_DELETE,
          ctx.start.getLine());
      return;
    }
    _c.deleteStructure(name, FortiosStructureType.POLICY);
    _c.getPolicies().remove(name);
  }

  @Override
  public void exitCfp_move(Cfp_moveContext ctx) {
    String name = toString(ctx.name.str());
    String pivot = toString(ctx.pivot.str());
    if (!_c.getPolicies().containsKey(name)) {
      warn(ctx, String.format("Cannot move a non-existent policy %s", name));
      _c.undefined(
          FortiosStructureType.POLICY,
          name,
          FortiosStructureUsage.POLICY_MOVE,
          ctx.start.getLine());
      return;
    }
    if (!_c.getPolicies().containsKey(pivot)) {
      warn(ctx, String.format("Cannot move around a non-existent policy %s", pivot));
      _c.undefined(
          FortiosStructureType.POLICY,
          name,
          FortiosStructureUsage.POLICY_MOVE_PIVOT,
          ctx.start.getLine());
      return;
    }

    if (ctx.after_or_before().AFTER() != null) {
      _c.getPolicies().moveAfter(name, pivot);
    } else {
      assert ctx.after_or_before().BEFORE() != null;
      _c.getPolicies().moveBefore(name, pivot);
    }
  }

  @Override
  public void exitCfp_set_action(Cfp_set_actionContext ctx) {
    _currentPolicy.setAction(toAction(ctx.policy_action()));
  }

  @Override
  public void exitCfp_set_comments(Cfp_set_commentsContext ctx) {
    _currentPolicy.setComments(toString(ctx.comments));
  }

  @Override
  public void exitCfp_set_name(Cfp_set_nameContext ctx) {
    toString(ctx, ctx.name).ifPresent(_currentPolicy::setName);
  }

  @Override
  public void exitCfp_set_status(Cfp_set_statusContext ctx) {
    _currentPolicy.setStatus(toStatus(ctx.status));
  }

  // List items
  @Override
  public void exitCfp_set_dstaddr(Cfp_set_dstaddrContext ctx) {
    toAddrgrpMemberUUIDs(ctx.addresses, FortiosStructureUsage.POLICY_DSTADDR, true)
        .ifPresent(
            addresses -> {
              Set<BatfishUUID> addrs = _currentPolicy.getDstAddrUUIDs();
              addrs.clear();
              addrs.addAll(addresses);
            });
  }

  @Override
  public void exitCfp_set_srcaddr(Cfp_set_srcaddrContext ctx) {
    toAddrgrpMemberUUIDs(ctx.addresses, FortiosStructureUsage.POLICY_SRCADDR, true)
        .ifPresent(
            addresses -> {
              Set<BatfishUUID> addrs = _currentPolicy.getSrcAddrUUIDs();
              addrs.clear();
              addrs.addAll(addresses);
            });
  }

  @Override
  public void exitCfp_set_dstintf(Cfp_set_dstintfContext ctx) {
    toInterfacesAndZones(ctx.interfaces, FortiosStructureUsage.POLICY_DSTINTF, false)
        .ifPresent(
            i -> {
              Set<String> ifaces = _currentPolicy.getDstIntf();
              ifaces.clear();
              ifaces.addAll(i.getInterfaces());

              Set<BatfishUUID> zones = _currentPolicy.getDstIntfZoneUUIDs();
              zones.clear();
              zones.addAll(i.getZones());
            });
  }

  @Override
  public void exitCfp_set_srcintf(Cfp_set_srcintfContext ctx) {
    toInterfacesAndZones(ctx.interfaces, FortiosStructureUsage.POLICY_SRCINTF, true)
        .ifPresent(
            i -> {
              Set<String> ifaces = _currentPolicy.getSrcIntf();
              ifaces.clear();
              ifaces.addAll(i.getInterfaces());

              Set<BatfishUUID> zones = _currentPolicy.getSrcIntfZoneUUIDs();
              zones.clear();
              zones.addAll(i.getZones());
            });
  }

  @Override
  public void exitCfp_set_service(Cfp_set_serviceContext ctx) {
    toServiceGroupMemberUUIDs(ctx.services, FortiosStructureUsage.POLICY_SERVICE, true)
        .ifPresent(
            s -> {
              Set<BatfishUUID> service = _currentPolicy.getServiceUUIDs();
              service.clear();
              service.addAll(s);
            });
  }

  @Override
  public void exitCfp_append_dstaddr(Cfp_append_dstaddrContext ctx) {
    toAddrgrpMemberUUIDs(ctx.addresses, FortiosStructureUsage.POLICY_DSTADDR, true)
        .ifPresent(
            a -> {
              Set<BatfishUUID> addrs = _currentPolicy.getDstAddrUUIDs();
              addrs.addAll(a);
            });
  }

  @Override
  public void exitCfp_append_srcaddr(Cfp_append_srcaddrContext ctx) {
    toAddrgrpMemberUUIDs(ctx.addresses, FortiosStructureUsage.POLICY_SRCADDR, true)
        .ifPresent(
            a -> {
              Set<BatfishUUID> addrs = _currentPolicy.getSrcAddrUUIDs();
              addrs.addAll(a);
            });
  }

  @Override
  public void exitCfp_append_dstintf(Cfp_append_dstintfContext ctx) {
    toInterfacesAndZones(ctx.interfaces, FortiosStructureUsage.POLICY_DSTINTF, false)
        .ifPresent(
            i -> {
              Set<String> ifaces = _currentPolicy.getDstIntf();
              ifaces.addAll(i.getInterfaces());

              Set<BatfishUUID> zones = _currentPolicy.getDstIntfZoneUUIDs();
              zones.addAll(i.getZones());
            });
  }

  @Override
  public void exitCfp_append_srcintf(Cfp_append_srcintfContext ctx) {
    toInterfacesAndZones(ctx.interfaces, FortiosStructureUsage.POLICY_SRCINTF, true)
        .ifPresent(
            i -> {
              Set<String> ifaces = _currentPolicy.getSrcIntf();
              ifaces.addAll(i.getInterfaces());

              Set<BatfishUUID> zones = _currentPolicy.getSrcIntfZoneUUIDs();
              zones.addAll(i.getZones());
            });
  }

  @Override
  public void exitCfp_append_service(Cfp_append_serviceContext ctx) {
    toServiceGroupMemberUUIDs(ctx.services, FortiosStructureUsage.POLICY_SERVICE, true)
        .ifPresent(
            s -> {
              Set<BatfishUUID> service = _currentPolicy.getServiceUUIDs();
              service.addAll(s);
            });
  }

  @Override
  public void enterCfsg_edit(Cfsg_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.service_name());
    ServiceGroup existing = name.map(_c.getServiceGroups()::get).orElse(null);
    if (existing != null) {
      // Make a clone to edit
      _currentServiceGroup = SerializationUtils.clone(existing);
    } else {
      _currentServiceGroup = new ServiceGroup(toString(ctx.service_name().str()), getUUID());
    }
    _currentServiceGroupNameValid = name.isPresent();
  }

  @Override
  public void exitCfsg_edit(Cfsg_editContext ctx) {
    String name = _currentServiceGroup.getName();
    String invalidReason = serviceGroupValid(_currentServiceGroup, _currentServiceGroupNameValid);
    if (invalidReason == null) { // service group edit block is valid
      _c.getRenameableObjects().put(_currentServiceGroup.getBatfishUUID(), _currentServiceGroup);
      _c.defineStructure(FortiosStructureType.SERVICE_GROUP, name, ctx);
      _c.getServiceGroups().put(name, _currentServiceGroup);
    } else {
      warn(ctx, String.format("Service group edit block ignored: %s", invalidReason));
    }
    _currentServiceGroup = null;
  }

  @Override
  public void exitCfsg_rename(Cfsg_renameContext ctx) {
    Optional<String> currentNameOpt = toString(ctx, ctx.current_name);
    Optional<String> newNameOpt = toString(ctx, ctx.new_name);
    if (!newNameOpt.isPresent() || !currentNameOpt.isPresent()) {
      return;
    }

    String currentName = currentNameOpt.get();
    String newName = newNameOpt.get();
    if (!_c.getServiceGroups().containsKey(currentName)) {
      warnRenameNonExistent(ctx, currentName, FortiosStructureType.SERVICE_GROUP);
      return;
    }
    if (_c.getServices().containsKey(newName) || _c.getServiceGroups().containsKey(newName)) {
      // TODO handle conflicting renames
      warnRenameConflict(ctx, currentName, newName, FortiosStructureType.SERVICE_GROUP);
      return;
    }
    // Rename refs / def
    _c.renameStructure(
        currentName,
        newName,
        FortiosStructureType.SERVICE_GROUP,
        ImmutableSet.of(FortiosStructureType.SERVICE_CUSTOM, FortiosStructureType.SERVICE_GROUP));
    // Rename the object itself
    ServiceGroup current = _c.getServiceGroups().remove(currentName);
    current.setName(newName);
    _c.getServiceGroups().put(newName, current);
    // Add the rename as part of the def
    _c.defineStructure(FortiosStructureType.SERVICE_GROUP, newName, ctx);
  }

  @Override
  public void exitCfsg_set_comment(Cfsg_set_commentContext ctx) {
    _currentServiceGroup.setComment(toString(ctx.comment));
  }

  @Override
  public void exitCfsg_set_member(Cfsg_set_memberContext ctx) {
    toServiceGroupMemberUUIDs(
            ctx.service_names(), FortiosStructureUsage.SERVICE_GROUP_MEMBER, false)
        .ifPresent(
            newMembers -> {
              // See if any of the new members is invalid / the parent of the current group
              ServiceGroup parent =
                  getParentServiceGroup(
                      _currentServiceGroup.getBatfishUUID(),
                      newMembers,
                      _c.getServiceGroups().values());
              if (parent != null) {
                warn(
                    ctx,
                    String.format(
                        "Service group %s cannot be added to %s as it would create a cycle",
                        parent.getName(), _currentServiceGroup.getName()));
                return;
              }

              Set<BatfishUUID> members = _currentServiceGroup.getMemberUUIDs();
              members.clear();
              members.addAll(newMembers);
            });
  }

  @Override
  public void exitCfsga_member(Cfsga_memberContext ctx) {
    toServiceGroupMemberUUIDs(
            ctx.service_names(), FortiosStructureUsage.SERVICE_GROUP_MEMBER, false)
        .ifPresent(
            newMembers -> {
              // See if any of the new members is invalid / the parent of the current group
              ServiceGroup parent =
                  getParentServiceGroup(
                      _currentServiceGroup.getBatfishUUID(),
                      newMembers,
                      _c.getServiceGroups().values());
              if (parent != null) {
                warn(
                    ctx,
                    String.format(
                        "Service group %s cannot be added to %s as it would create a cycle",
                        parent.getName(), _currentServiceGroup.getName()));
                return;
              }

              _currentServiceGroup.getMemberUUIDs().addAll(newMembers);
            });
  }

  @Override
  public void enterCfsc_edit(Cfsc_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.service_name());
    Service existing = name.map(_c.getServices()::get).orElse(null);
    if (existing != null) {
      // Make a clone to edit
      _currentService = SerializationUtils.clone(existing);
    } else {
      _currentService = new Service(toString(ctx.service_name().str()), getUUID());
    }
    _currentServiceValid = name.isPresent();
  }

  @Override
  public void exitCfsc_edit(Cfsc_editContext ctx) {
    String name = _currentService.getName();
    String invalidReason = serviceValid(_currentService, _currentServiceValid);
    if (invalidReason == null) { // service edit block is valid
      _c.getRenameableObjects().put(_currentService.getBatfishUUID(), _currentService);
      _c.defineStructure(FortiosStructureType.SERVICE_CUSTOM, name, ctx);
      _c.getServices().put(name, _currentService);
    } else {
      warn(ctx, String.format("Service edit block ignored: %s", invalidReason));
    }
    _currentService = null;
  }

  @Override
  public void exitCfsc_rename(Cfsc_renameContext ctx) {
    Optional<String> currentNameOpt = toString(ctx, ctx.current_name);
    Optional<String> newNameOpt = toString(ctx, ctx.new_name);
    assert currentNameOpt.isPresent();
    assert newNameOpt.isPresent();

    String currentName = currentNameOpt.get();
    String newName = newNameOpt.get();
    if (!_c.getServices().containsKey(currentName)) {
      warnRenameNonExistent(ctx, currentName, FortiosStructureType.SERVICE_CUSTOM);
      return;
    }
    if (_c.getServices().containsKey(newName) || _c.getServiceGroups().containsKey(newName)) {
      // TODO handle conflicting renames
      warnRenameConflict(ctx, currentName, newName, FortiosStructureType.SERVICE_CUSTOM);
      return;
    }
    // Rename refs / def
    _c.renameStructure(
        currentName,
        newName,
        FortiosStructureType.SERVICE_CUSTOM,
        ImmutableSet.of(FortiosStructureType.SERVICE_CUSTOM, FortiosStructureType.SERVICE_GROUP));
    // Rename the object itself
    Service currentService = _c.getServices().remove(currentName);
    currentService.setName(newName);
    _c.getServices().put(newName, currentService);
    // Add the rename as part of the def
    _c.defineStructure(FortiosStructureType.SERVICE_CUSTOM, newName, ctx);
  }

  @Override
  public void exitCfsc_set_comment(Cfsc_set_commentContext ctx) {
    _currentService.setComment(toString(ctx.comment));
  }

  @Override
  public void exitCfsc_set_icmpcode(Cfsc_set_icmpcodeContext ctx) {
    if (_currentService.getProtocolEffective() != Protocol.ICMP
        && _currentService.getProtocolEffective() != Protocol.ICMP6) {
      warn(
          ctx,
          String.format(
              "Cannot set ICMP code for service %s when protocol is not set to ICMP or ICMP6.",
              _currentService.getName()));
      return;
    } else if (_currentService.getIcmpType() == null) {
      warn(
          ctx,
          String.format(
              "Cannot set ICMP code for service %s when ICMP type is not set.",
              _currentService.getName()));
      return;
    }
    _currentService.setIcmpCode(toInteger(ctx.code));
  }

  @Override
  public void exitCfsc_set_icmptype(Cfsc_set_icmptypeContext ctx) {
    if (_currentService.getProtocolEffective() != Protocol.ICMP
        && _currentService.getProtocolEffective() != Protocol.ICMP6) {
      warn(
          ctx,
          String.format(
              "Cannot set ICMP type for service %s when protocol is not set to ICMP or ICMP6.",
              _currentService.getName()));
      return;
    }
    _currentService.setIcmpType(toInteger(ctx.type));
  }

  @Override
  public void exitCfsc_set_protocol(Cfsc_set_protocolContext ctx) {
    _currentService.setProtocol(toProtocol(ctx.protocol));
  }

  @Override
  public void exitCfsc_set_protocol_number(Cfsc_set_protocol_numberContext ctx) {
    if (_currentService.getProtocolEffective() != Protocol.IP) {
      warn(
          ctx,
          String.format(
              "Cannot set IP protocol number for service %s when protocol is not set to IP.",
              _currentService.getName()));
      return;
    }
    toInteger(ctx, ctx.ip_protocol_number()).ifPresent(_currentService::setProtocolNumber);
  }

  @Override
  public void exitCfsc_set_sctp_portrange(Cfsc_set_sctp_portrangeContext ctx) {
    if (_currentService.getProtocolEffective() != Protocol.TCP_UDP_SCTP) {
      warn(
          ctx,
          String.format(
              "Cannot set SCTP port range for service %s when protocol is not set to TCP/UDP/SCTP.",
              _currentService.getName()));
      return;
    }
    _currentService.setSctpPortRangeDst(toDstIntegerSpace(ctx.service_port_ranges()));
    _currentService.setSctpPortRangeSrc(toSrcIntegerSpace(ctx.service_port_ranges()).orElse(null));
  }

  @Override
  public void exitCfsc_set_tcp_portrange(Cfsc_set_tcp_portrangeContext ctx) {
    if (_currentService.getProtocolEffective() != Protocol.TCP_UDP_SCTP) {
      warn(
          ctx,
          String.format(
              "Cannot set TCP port range for service %s when protocol is not set to TCP/UDP/SCTP.",
              _currentService.getName()));
      return;
    }
    _currentService.setTcpPortRangeDst(toDstIntegerSpace(ctx.service_port_ranges()));
    _currentService.setTcpPortRangeSrc(toSrcIntegerSpace(ctx.service_port_ranges()).orElse(null));
  }

  @Override
  public void exitCfsc_set_udp_portrange(Cfsc_set_udp_portrangeContext ctx) {
    if (_currentService.getProtocolEffective() != Protocol.TCP_UDP_SCTP) {
      warn(
          ctx,
          String.format(
              "Cannot set UDP port range for service %s when protocol is not set to TCP/UDP/SCTP.",
              _currentService.getName()));
      return;
    }
    _currentService.setUdpPortRangeDst(toDstIntegerSpace(ctx.service_port_ranges()));
    _currentService.setUdpPortRangeSrc(toSrcIntegerSpace(ctx.service_port_ranges()).orElse(null));
  }

  @Override
  public void enterCfsc_unset_icmpcode(Cfsc_unset_icmpcodeContext ctx) {
    if (_currentService.getProtocolEffective() != Protocol.ICMP
        && _currentService.getProtocolEffective() != Protocol.ICMP6) {
      warn(
          ctx,
          String.format(
              "Cannot unset ICMP code for service %s when protocol is not set to ICMP or ICMP6.",
              _currentService.getName()));
      return;
    } else if (_currentService.getIcmpType() == null) {
      warn(
          ctx,
          String.format(
              "Cannot unset ICMP code for service %s when icmptype is unset.",
              _currentService.getName()));
      return;
    }
    _currentService.setIcmpCode(null);
  }

  @Override
  public void enterCfsc_unset_icmptype(Cfsc_unset_icmptypeContext ctx) {
    if (_currentService.getProtocolEffective() != Protocol.ICMP
        && _currentService.getProtocolEffective() != Protocol.ICMP6) {
      warn(
          ctx,
          String.format(
              "Cannot unset ICMP type for service %s when protocol is not set to ICMP or ICMP6.",
              _currentService.getName()));
      return;
    }
    _currentService.setIcmpType(null);
    // device also clears code
    _currentService.setIcmpCode(null);
  }

  @Override
  public void enterCsz_edit(Csz_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.zone_name());
    Zone existing = name.map(_c.getZones()::get).orElse(null);
    _currentZoneNameValid = name.isPresent();
    if (existing != null) {
      // Make a clone to edit
      _currentZone = SerializationUtils.clone(existing);
    } else {
      _currentZone = new Zone(toString(ctx.zone_name().str()), getUUID());
    }
  }

  /** Returns message indicating why this zone can't be committed in the CLI, or null if it can */
  private static @Nullable String getZoneInvalidReason(
      Zone zone, boolean nameValid, Set<String> interfaceNames) {
    if (!nameValid) {
      return "name is invalid";
    } else if (interfaceNames.contains(zone.getName())) {
      return "name conflicts with a system interface name";
    } else if (zone.getInterface().isEmpty()) {
      return "interface must be set";
    }
    return null;
  }

  @Override
  public void exitCsz_edit(Csz_editContext ctx) {
    // If edited item is valid, add/update the entry in VS map
    String invalidReason =
        getZoneInvalidReason(_currentZone, _currentZoneNameValid, _c.getInterfaces().keySet());
    if (invalidReason == null) { // is valid
      String name = _currentZone.getName();
      _c.defineStructure(FortiosStructureType.ZONE, name, ctx);
      _c.referenceStructure(
          FortiosStructureType.ZONE,
          name,
          FortiosStructureUsage.ZONE_SELF_REF,
          ctx.start.getLine());
      _c.getZones().put(name, _currentZone);
      _c.getRenameableObjects().put(_currentZone.getBatfishUUID(), _currentZone);
    } else {
      warn(ctx, String.format("Zone edit block ignored: %s", invalidReason));
    }
    _currentZone = null;
  }

  @Override
  public void exitCsz_rename(Csz_renameContext ctx) {
    Optional<String> currentNameOpt = toString(ctx, ctx.current_name);
    Optional<String> newNameOpt = toString(ctx, ctx.new_name);
    if (!newNameOpt.isPresent() || !currentNameOpt.isPresent()) {
      return;
    }

    String currentName = currentNameOpt.get();
    String newName = newNameOpt.get();
    if (!_c.getZones().containsKey(currentName)) {
      warnRenameNonExistent(ctx, currentName, FortiosStructureType.ZONE);
      return;
    }
    if (_c.getZones().containsKey(newName) || _c.getInterfaces().containsKey(newName)) {
      // TODO handle conflicting renames
      warnRenameConflict(ctx, currentName, newName, FortiosStructureType.ZONE);
      return;
    }
    // Rename refs / def
    _c.renameStructure(
        currentName,
        newName,
        FortiosStructureType.ZONE,
        ImmutableSet.of(FortiosStructureType.ZONE, FortiosStructureType.INTERFACE));
    // Rename the object itself
    Zone current = _c.getZones().remove(currentName);
    current.setName(newName);
    _c.getZones().put(newName, current);
    // Add the rename as part of the def
    _c.defineStructure(FortiosStructureType.ZONE, newName, ctx);
  }

  @Override
  public void exitCsz_set_description(Csz_set_descriptionContext ctx) {
    _currentZone.setDescription(toString(ctx.description));
  }

  @Override
  public void exitCsz_set_intrazone(Csz_set_intrazoneContext ctx) {
    _currentZone.setIntrazone(toIntrazoneAction(ctx.value));
  }

  @Override
  public void exitCsz_set_interface(Csz_set_interfaceContext ctx) {
    toZoneInterfaces(ctx.interfaces)
        .ifPresent(
            newInterfaces -> {
              Set<String> ifaces = _currentZone.getInterface();
              ifaces.clear();
              ifaces.addAll(newInterfaces);
            });
  }

  @Override
  public void exitCsz_append_interface(Csz_append_interfaceContext ctx) {
    toZoneInterfaces(ctx.interfaces)
        .ifPresent(newInterfaces -> _currentZone.getInterface().addAll(newInterfaces));
  }

  @Override
  public void enterCral_edit(Cral_editContext ctx) {
    Optional<String> name = toString(ctx, ctx.access_list_name());
    AccessList existing = name.map(_c.getAccessLists()::get).orElse(null);
    _currentAccessListNameValid = name.isPresent();
    _currentAccessList =
        existing == null ? new AccessList(toString(ctx.access_list_name().str())) : existing;
  }

  @Override
  public void exitCral_edit(Cral_editContext ctx) {
    // If edited item is valid, add/update the entry in VS map
    if (_currentAccessListNameValid) {
      String name = _currentAccessList.getName();
      _c.defineStructure(FortiosStructureType.ACCESS_LIST, name, ctx);
      _c.getAccessLists().put(name, _currentAccessList);
    } else {
      warn(ctx, "Access-list edit block ignored: name is invalid");
    }
    _currentAccessList = null;
  }

  @Override
  public void exitCrale_set_comments(Crale_set_commentsContext ctx) {
    _currentAccessList.setComments(toString(ctx.comment));
  }

  @Override
  public void enterCralecr_edit(Cralecr_editContext ctx) {
    Optional<Long> name = toLong(ctx, ctx.acl_rule_number());
    AccessListRule existing =
        name.map(l -> _currentAccessList.getRules().get(l.toString())).orElse(null);
    _currentAccessListRuleNameValid = name.isPresent();
    if (existing != null) {
      // Make a clone to edit
      _currentAccessListRule = SerializationUtils.clone(existing);
    } else {
      _currentAccessListRule = new AccessListRule(toString(ctx.acl_rule_number().str()));
    }
  }

  /**
   * Returns message indicating why this access-list rule can't be committed in the CLI, or null if
   * it can
   */
  private static @Nullable String getAccessListRuleInvalidReason(
      AccessListRule rule, boolean nameValid) {
    if (!nameValid) {
      return "name is invalid";
    } else if (rule.getWildcard() == null && rule.getPrefix() == null) {
      return "prefix or wildcard must be set";
    }
    return null;
  }

  @Override
  public void exitCralecr_edit(Cralecr_editContext ctx) {
    // If edited item is valid, add/update the entry in VS map
    String invalidReason =
        getAccessListRuleInvalidReason(_currentAccessListRule, _currentAccessListRuleNameValid);
    if (invalidReason == null) { // is valid
      String name = _currentAccessListRule.getNumber();
      _currentAccessList.getRules().put(name, _currentAccessListRule);
    } else {
      warn(ctx, String.format("Access-list rule edit block ignored: %s", invalidReason));
    }
    _currentAccessListRule = null;
  }

  @Override
  public void exitCralecre_set_action(Cralecre_set_actionContext ctx) {
    _currentAccessListRule.setAction(toAction(ctx.permit_or_deny()));
  }

  @Override
  public void exitCralecre_set_exact_match(Cralecre_set_exact_matchContext ctx) {
    _currentAccessListRule.setExactMatch(toBoolean(ctx.enable_or_disable()));
  }

  @Override
  public void exitCralecre_set_prefix(Cralecre_set_prefixContext ctx) {
    _currentAccessListRule.setPrefix(toPrefix(ctx.ip_address_with_mask_or_prefix_or_any()));
  }

  @Override
  public void exitCralecre_set_wildcard(Cralecre_set_wildcardContext ctx) {
    // In this context, FortiOS defies their usual wildcard convention and uses a "Cisco-style"
    // wildcard, meaning that 0s in the mask indicate bits that matter. This matches the convention
    // in the VI IpWildcard class, so do not invert the mask bits when converting this wildcard.
    _currentAccessListRule.setWildcard(toIpWildcard(ctx.ip_wildcard(), false));
  }

  private IntrazoneAction toIntrazoneAction(Allow_or_denyContext ctx) {
    if (ctx.ALLOW() != null) {
      return IntrazoneAction.ALLOW;
    }
    assert ctx.DENY() != null;
    return IntrazoneAction.DENY;
  }

  /**
   * Generate a list of service group member UUIDs for the supplied Service_names context. Returns
   * {@link Optional#empty()} if invalid. If {@code pruneAll} is true, then the special {@code ALL}
   * service is removed when specified with other services.
   *
   * <p>If any service group member contains the current service group as a descendant, then the
   * context is considered invalid as a cycle would be introduced.
   */
  private Optional<Set<BatfishUUID>> toServiceGroupMemberUUIDs(
      Service_namesContext ctx, FortiosStructureUsage usage, boolean pruneAll) {
    int line = ctx.start.getLine();
    Map<String, Service> servicesMap = _c.getServices();
    Map<String, ServiceGroup> serviceGroupsMap = _c.getServiceGroups();

    ImmutableSet.Builder<BatfishUUID> uuidsBuilder = ImmutableSet.builder();
    Set<String> members =
        ctx.service_name().stream()
            .map(n -> toString(n.str()))
            .collect(ImmutableSet.toImmutableSet());
    for (String name : members) {
      if (pruneAll && name.equals(Policy.ALL_SERVICE) && members.size() > 1) {
        warn(ctx, "Cannot combine 'ALL' with other services");
        return Optional.empty();
      }
      if (servicesMap.containsKey(name)) {
        uuidsBuilder.add(servicesMap.get(name).getBatfishUUID());
        _c.referenceStructure(FortiosStructureType.SERVICE_CUSTOM, name, usage, line);
      } else if (serviceGroupsMap.containsKey(name)) {
        ServiceGroup serviceGroup = serviceGroupsMap.get(name);
        uuidsBuilder.add(serviceGroup.getBatfishUUID());
        _c.referenceStructure(FortiosStructureType.SERVICE_GROUP, name, usage, line);
      } else {
        _c.undefined(FortiosStructureType.SERVICE_CUSTOM_OR_SERVICE_GROUP, name, usage, line);
        warn(
            ctx,
            String.format(
                "Service or service group %s is undefined and cannot be referenced", name));
        return Optional.empty();
      }
    }
    return Optional.of(uuidsBuilder.build());
  }

  /**
   * Returns a parent Addrgrp which directly or indirectly contains the specified AddrgrpMember
   * UUID, or {@code null} if none contain it. Searches only the specified parent UUIDs and their
   * descendants and uses the provided collection of groups to expand indirect descendants/map UUIDs
   * to objects.
   */
  private static @Nullable Addrgrp getParentAddrgrp(
      BatfishUUID childUuid,
      Collection<BatfishUUID> candidateParents,
      Collection<Addrgrp> allAddrgrps) {
    Map<BatfishUUID, Addrgrp> allAddrgrpsByUUID =
        allAddrgrps.stream()
            .collect(ImmutableMap.toImmutableMap(Addrgrp::getBatfishUUID, Function.identity()));

    for (BatfishUUID parentUUID : candidateParents) {
      if (!allAddrgrpsByUUID.containsKey(parentUUID)) {
        // If the candidate parent doesn't exist (e.g. is an Address, not a group) skip it
        continue;
      }
      Addrgrp parent = allAddrgrpsByUUID.get(parentUUID);
      if (addrgrpContains(parent, childUuid, allAddrgrpsByUUID)) {
        return parent;
      }
    }
    return null;
  }

  /**
   * Helper function that returns a boolean indicating if the specified parent Addrgrp directly or
   * indirectly contains the a member with the specified UUID. Uses the provided map of groups to
   * expand indirect descendants.
   */
  static boolean addrgrpContains(
      Addrgrp parent, BatfishUUID uuid, Map<BatfishUUID, Addrgrp> allAddrgrps) {
    Set<BatfishUUID> members = parent.getMemberUUIDs();
    if (parent.getBatfishUUID().equals(uuid) || members.contains(uuid)) {
      return true;
    }
    return members.stream()
        .anyMatch(
            m ->
                allAddrgrps.containsKey(m)
                    && addrgrpContains(allAddrgrps.get(m), uuid, allAddrgrps));
  }

  /**
   * Returns a parent ServiceGroup which directly or indirectly contains the specified
   * ServiceGroupMember UUID, or {@code null} if none contain it. Searches only the specified parent
   * UUIDs and their descendants and uses the provided collection of service groups to expand
   * indirect descendants/map UUIDs to objects.
   */
  private static @Nullable ServiceGroup getParentServiceGroup(
      BatfishUUID childUuid,
      Collection<BatfishUUID> candidateParents,
      Collection<ServiceGroup> allServiceGroups) {
    Map<BatfishUUID, ServiceGroup> allServiceGroupsByUUID =
        allServiceGroups.stream()
            .collect(
                ImmutableMap.toImmutableMap(ServiceGroup::getBatfishUUID, Function.identity()));

    for (BatfishUUID parentUUID : candidateParents) {
      if (!allServiceGroupsByUUID.containsKey(parentUUID)) {
        // If the candidate parent doesn't exist (e.g. is a Service, not a group) skip it
        continue;
      }
      ServiceGroup parent = allServiceGroupsByUUID.get(parentUUID);
      if (serviceGroupContains(parent, childUuid, allServiceGroupsByUUID)) {
        return parent;
      }
    }
    return null;
  }

  /**
   * Helper function that returns a boolean indicating if the specified parent ServiceGroup directly
   * or indirectly contains the a member with the specified UUID. Uses the provided map of service
   * groups to expand indirect descendants.
   */
  static boolean serviceGroupContains(
      ServiceGroup parent, BatfishUUID uuid, Map<BatfishUUID, ServiceGroup> allServiceGroups) {
    Set<BatfishUUID> members = parent.getMemberUUIDs();
    if (parent.getBatfishUUID().equals(uuid) || members.contains(uuid)) {
      return true;
    }
    return members.stream()
        .anyMatch(
            m ->
                allServiceGroups.containsKey(m)
                    && serviceGroupContains(allServiceGroups.get(m), uuid, allServiceGroups));
  }

  /**
   * Generate a list of address UUIDs for the supplied Address_names context. Returns {@link
   * Optional#empty()} if invalid.
   */
  private Optional<Set<BatfishUUID>> toAddressUUIDs(
      Address_namesContext ctx, FortiosStructureUsage usage) {
    int line = ctx.start.getLine();
    Map<String, Address> addressesMap = _c.getAddresses();
    ImmutableSet.Builder<BatfishUUID> addressUuidsBuilder = ImmutableSet.builder();
    Set<String> addresses =
        ctx.address_name().stream()
            .map(n -> toString(n.str()))
            .collect(ImmutableSet.toImmutableSet());
    for (String name : addresses) {
      if (addressesMap.containsKey(name)) {
        addressUuidsBuilder.add(addressesMap.get(name).getBatfishUUID());
        _c.referenceStructure(FortiosStructureType.ADDRESS, name, usage, line);
      } else {
        _c.undefined(FortiosStructureType.ADDRESS, name, usage, line);
        warn(ctx, String.format("Address %s is undefined and cannot be referenced", name));
        return Optional.empty();
      }
    }
    return Optional.of(addressUuidsBuilder.build());
  }

  /**
   * Generate a list of addrgrp member UUIDs for the supplied Address_names context. Returns {@link
   * Optional#empty()} if invalid.
   */
  private Optional<Set<BatfishUUID>> toAddrgrpMemberUUIDs(
      Address_namesContext ctx, FortiosStructureUsage usage, boolean pruneAll) {
    int line = ctx.start.getLine();
    Map<String, Address> addressesMap = _c.getAddresses();
    Map<String, Addrgrp> addrgrpsMap = _c.getAddrgrps();
    ImmutableSet.Builder<BatfishUUID> addressUuidsBuilder = ImmutableSet.builder();
    Set<String> addresses =
        ctx.address_name().stream()
            .map(n -> toString(n.str()))
            .collect(ImmutableSet.toImmutableSet());
    for (String name : addresses) {
      if (pruneAll && name.equals(Policy.ALL_ADDRESSES) && addresses.size() > 1) {
        warn(ctx, "When 'all' is set together with other address(es), it is removed");
        continue;
      }
      if (addressesMap.containsKey(name)) {
        addressUuidsBuilder.add(addressesMap.get(name).getBatfishUUID());
        _c.referenceStructure(FortiosStructureType.ADDRESS, name, usage, line);
      } else if (addrgrpsMap.containsKey(name)) {
        addressUuidsBuilder.add(addrgrpsMap.get(name).getBatfishUUID());
        _c.referenceStructure(FortiosStructureType.ADDRGRP, name, usage, line);
      } else {
        _c.undefined(FortiosStructureType.ADDRESS_OR_ADDRGRP, name, usage, line);
        warn(
            ctx,
            String.format("Address or addrgrp %s is undefined and cannot be referenced", name));
        return Optional.empty();
      }
    }
    return Optional.of(addressUuidsBuilder.build());
  }

  /**
   * Convert names in the specified context into an optional set of interface names. Returns an
   * empty optional if the line would not be accepted.
   *
   * <p>Note: we are simplifying allowed interface specification here - any valid interface not
   * already zoned is permitted. This means we are ignoring some (buggy) interface-restriction
   * applied by real devices here; specifically: some versions of fortiOS cli will prevent you from
   * using an interface you just removed.
   */
  private Optional<Set<String>> toZoneInterfaces(Interface_namesContext ctx) {
    int line = ctx.start.getLine();
    Map<String, Interface> ifacesMap = _c.getInterfaces();
    String currentZoneName = _currentZone.getName();

    ImmutableSet<String> usedIfaceNames =
        _c.getZones().values().stream()
            .filter(z -> !z.getName().equals(currentZoneName))
            .map(Zone::getInterface)
            .flatMap(Collection::stream)
            .collect(ImmutableSet.toImmutableSet());

    ImmutableSet.Builder<String> ifaceNameBuilder = ImmutableSet.builder();
    Set<String> newIfaces =
        ctx.interface_name().stream()
            .map(n -> toString(n.str()))
            .collect(ImmutableSet.toImmutableSet());

    for (String name : newIfaces) {
      if (usedIfaceNames.contains(name)) {
        warn(
            ctx,
            String.format(
                "Interface %s is already in another zone and cannot be added to zone %s",
                name, currentZoneName));
        return Optional.empty();
      } else if (ifacesMap.containsKey(name)) {
        ifaceNameBuilder.add(name);
        _c.referenceStructure(
            FortiosStructureType.INTERFACE, name, FortiosStructureUsage.ZONE_INTERFACE, line);
      } else {
        _c.undefined(
            FortiosStructureType.INTERFACE, name, FortiosStructureUsage.ZONE_INTERFACE, line);
        warn(
            ctx,
            String.format(
                "Interface %s is undefined and cannot be added to zone %s", name, currentZoneName));
        return Optional.empty();
      }
    }

    return Optional.of(ifaceNameBuilder.build());
  }

  /**
   * Convert names in the specified context into an optional set of interface names. Returns an
   * empty optional if the line would not be accepted. Usable interface for aggregate and redundant
   * interfaces as to follow criteria:
   * https://docs.fortinet.com/document/fortigate/7.6.2/administration-guide/567758/aggregation-and-redundancy
   * We test for the first 4 points: 1. Interface must exist and is PHYSICAL 2. Interface must not
   * be in an other aggregate or redundant interface 3. Interface must be in the same VDOM as the
   * aggregate/redundant interface 4. Interface must not have an IP address
   */
  private Optional<Set<String>> toInterfaceMembers(Interface_namesContext ctx) {
    int line = ctx.start.getLine();
    Map<String, Interface> ifacesMap = _c.getInterfaces();
    String currentInterfaceName = _currentInterface.getName();

    ImmutableSet<String> usedIfaceNames =
        _c.getInterfaces().values().stream()
            .filter(i -> !i.getName().equals(currentInterfaceName))
            .map(Interface::getMembers)
            .flatMap(Collection::stream)
            .collect(ImmutableSet.toImmutableSet());

    ImmutableSet.Builder<String> ifaceNameBuilder = ImmutableSet.builder();
    Set<String> newIfaces =
        ctx.interface_name().stream()
            .map(n -> toString(n.str()))
            .collect(ImmutableSet.toImmutableSet());

    for (String name : newIfaces) {
      if (usedIfaceNames.contains(name)) {
        warn(
            ctx,
            String.format(
                "Interface %s is already in another aggregate/redundant interface and cannot be"
                    + " added to %s",
                name, currentInterfaceName));
        return Optional.empty();
      } else if (ifacesMap.containsKey(name)) {
        // set current interface as parent of the new member
        Interface iface = ifacesMap.get(name);

        if (iface.getType() != Interface.Type.PHYSICAL) {
          warn(
              ctx,
              String.format(
                  "Interface %s is not a physical interface and cannot be added to %s",
                  name, currentInterfaceName));
          return Optional.empty();
        }

        if (!Objects.equals(iface.getVdom(), _currentInterface.getVdom())) {
          warn(
              ctx,
              String.format(
                  "Interface %s is in a different vdom (%s) than the current interface (%s) and"
                      + " cannot be added to %s",
                  name, iface.getVdom(), _currentInterface.getVdom(), currentInterfaceName));
          return Optional.empty();
        }

        if (iface.getIp() != null) {
          warn(
              ctx,
              String.format(
                  "Interface %s has an IP address (%s) and cannot be added to %s",
                  name, iface.getIp(), currentInterfaceName));
          return Optional.empty();
        }

        iface.setParent(_currentInterface);
        ifaceNameBuilder.add(name);
        _c.referenceStructure(
            FortiosStructureType.INTERFACE, name, FortiosStructureUsage.INTERFACE_INTERFACE, line);
      } else {
        _c.undefined(
            FortiosStructureType.INTERFACE, name, FortiosStructureUsage.INTERFACE_INTERFACE, line);
        warn(
            ctx,
            String.format(
                "Interface %s is undefined and cannot be added to %s", name, currentInterfaceName));
        return Optional.empty();
      }
    }

    return Optional.of(ifaceNameBuilder.build());
  }

  /**
   * Convert specified interface or zone names context into interface and zone identifiers. If
   * {@code pruneAny} is true, then the special 'any' interface will be removed if specified with
   * other interfaces.
   */
  private Optional<InterfacesAndZones> toInterfacesAndZones(
      Interface_or_zone_namesContext ctx, FortiosStructureUsage usage, boolean pruneAny) {
    int line = ctx.start.getLine();
    Map<String, Interface> ifacesMap = _c.getInterfaces();
    Map<String, Zone> zonesMap = _c.getZones();
    ImmutableSet.Builder<String> ifaceNameBuilder = ImmutableSet.builder();
    ImmutableSet.Builder<BatfishUUID> zonesUuidBuilder = ImmutableSet.builder();

    Set<String> ifaces =
        ctx.interface_or_zone_name().stream()
            .map(n -> toString(n.str()))
            .collect(ImmutableSet.toImmutableSet());
    for (String name : ifaces) {
      if (name.equals(Policy.ANY_INTERFACE)) {
        if (pruneAny && ifaces.size() > 1) {
          warn(ctx, "When 'any' is set together with other interfaces, it is removed");
          continue;
        }
        ifaceNameBuilder.add(Policy.ANY_INTERFACE);
      } else if (ifacesMap.containsKey(name)) {
        ifaceNameBuilder.add(name);
        _c.referenceStructure(FortiosStructureType.INTERFACE, name, usage, line);
      } else if (zonesMap.containsKey(name)) {
        zonesUuidBuilder.add(zonesMap.get(name).getBatfishUUID());
        _c.referenceStructure(FortiosStructureType.ZONE, name, usage, line);
      } else {
        _c.undefined(FortiosStructureType.INTERFACE_OR_ZONE, name, usage, line);
        warn(
            ctx,
            String.format(
                "Interface/zone %s is undefined and cannot be added to policy %s",
                name, _currentPolicy.getNumber()));
        return Optional.empty();
      }
    }
    return Optional.of(new InterfacesAndZones(ifaceNameBuilder.build(), zonesUuidBuilder.build()));
  }

  /**
   * Convert specified interface name context into the corresponding interface name, or return empty
   * optional if there is no such interface.
   */
  private Optional<String> toInterface(
      ParserRuleContext ctx, Interface_nameContext ifaceNameCtx, FortiosStructureUsage usage) {
    String ifaceName = toString(ifaceNameCtx.str());
    if (!_c.getInterfaces().containsKey(ifaceName)) {
      warn(ctx, String.format("Interface %s is undefined", ifaceName));
      _c.undefined(FortiosStructureType.INTERFACE, ifaceName, usage, ctx.start.getLine());
      return Optional.empty();
    }
    _c.referenceStructure(FortiosStructureType.INTERFACE, ifaceName, usage, ctx.start.getLine());
    return Optional.of(ifaceName);
  }

  /**
   * Convert specified service_port_ranges context into an IntegerSpace representing the destination
   * ports specified by the context.
   */
  private @Nonnull IntegerSpace toDstIntegerSpace(Service_port_rangesContext ctx) {
    IntegerSpace.Builder spaces = IntegerSpace.builder();
    for (Service_port_rangeContext range : ctx.service_port_range()) {
      assert range.dst_ports != null;
      spaces.including(toIntegerSpace(range.dst_ports));
    }
    return spaces.build();
  }

  /**
   * Convert specified service_port_ranges context into an optional IntegerSpace representing the
   * source ports specified by the context. An IntegerSpace is only returned if a source port space
   * is specified.
   */
  private Optional<IntegerSpace> toSrcIntegerSpace(Service_port_rangesContext ctx) {
    IntegerSpace.Builder spaces = IntegerSpace.builder();
    boolean isSet = false;
    for (Service_port_rangeContext range : ctx.service_port_range()) {
      if (range.src_ports != null) {
        isSet = true;
        spaces.including(toIntegerSpace(range.src_ports));
      }
    }
    return isSet ? Optional.of(spaces.build()) : Optional.empty();
  }

  private IntegerSpace toIntegerSpace(Port_rangeContext ctx) {
    int low = toInteger(ctx.port_low);
    if (ctx.port_high != null) {
      int high = toInteger(ctx.port_high);
      return IntegerSpace.of(Range.closed(low, high));
    }
    return IntegerSpace.of(low);
  }

  private @Nonnull RouteMapRule.Action toAction(Route_map_actionContext ctx) {
    if (ctx.permit_or_deny().PERMIT() != null) {
      return RouteMapRule.Action.PERMIT;
    }
    assert ctx.permit_or_deny().DENY() != null;
    return RouteMapRule.Action.DENY;
  }

  private @Nonnull AccessListRule.Action toAction(Permit_or_denyContext ctx) {
    if (ctx.PERMIT() != null) {
      return AccessListRule.Action.PERMIT;
    }
    assert ctx.DENY() != null;
    return AccessListRule.Action.DENY;
  }

  private @Nonnull Policy.Action toAction(Policy_actionContext ctx) {
    if (ctx.ACCEPT() != null) {
      return Action.ACCEPT;
    } else if (ctx.DENY() != null) {
      return Action.DENY;
    } else {
      assert ctx.IPSEC() != null;
      return Action.IPSEC;
    }
  }

  private Service.Protocol toProtocol(Service_protocolContext ctx) {
    if (ctx.ICMP() != null) {
      return Protocol.ICMP;
    } else if (ctx.ICMP6() != null) {
      return Protocol.ICMP6;
    } else if (ctx.IP_UPPER() != null) {
      return Protocol.IP;
    } else {
      assert ctx.TCP_UDP_SCTP() != null;
      return Protocol.TCP_UDP_SCTP;
    }
  }

  private boolean toBoolean(Enable_or_disableContext ctx) {
    if (ctx.ENABLE() != null) {
      return true;
    }
    assert ctx.DISABLE() != null;
    return false;
  }

  private @Nonnull Policy.Status toStatus(Policy_statusContext ctx) {
    return toBoolean(ctx.enable_or_disable()) ? Status.ENABLE : Status.DISABLE;
  }

  private Interface.Status toStatus(Up_or_downContext ctx) {
    if (ctx.UP() != null) {
      return Interface.Status.UP;
    }
    assert ctx.DOWN() != null;
    return Interface.Status.DOWN;
  }

  private Address.Type toAddressType(Address_typeContext ctx) {
    if (ctx.INTERFACE_SUBNET() != null) {
      return Address.Type.INTERFACE_SUBNET;
    } else if (ctx.IPMASK() != null) {
      return Address.Type.IPMASK;
    } else if (ctx.IPRANGE() != null) {
      return Address.Type.IPRANGE;
    } else if (ctx.WILDCARD() != null) {
      return Address.Type.WILDCARD;
    } else if (ctx.DYNAMIC() != null) {
      return Address.Type.DYNAMIC;
    } else if (ctx.FQDN() != null) {
      return Address.Type.FQDN;
    } else if (ctx.GEOGRAPHY() != null) {
      return Address.Type.GEOGRAPHY;
    } else {
      assert ctx.MAC() != null;
      return Address.Type.MAC;
    }
  }

  private Addrgrp.Type toAddrgrpType(ParserRuleContext ctx, Addrgrp_typeContext addrgrpCtx) {
    if (addrgrpCtx.DEFAULT() != null) {
      return Addrgrp.Type.DEFAULT;
    } else {
      assert addrgrpCtx.FOLDER() != null;
      warn(ctx, "Addrgrp type folder is not yet supported");
      return Addrgrp.Type.FOLDER;
    }
  }

  private Interface.Type toInterfaceType(Interface_typeContext ctx) {
    if (ctx.AGGREGATE() != null) {
      return Type.AGGREGATE;
    } else if (ctx.EMAC_VLAN() != null) {
      return Type.EMAC_VLAN;
    } else if (ctx.LOOPBACK() != null) {
      return Type.LOOPBACK;
    } else if (ctx.PHYSICAL() != null) {
      return Type.PHYSICAL;
    } else if (ctx.REDUNDANT() != null) {
      return Type.REDUNDANT;
    } else if (ctx.TUNNEL() != null) {
      return Type.TUNNEL;
    } else if (ctx.VLAN() != null) {
      return Type.VLAN;
    } else {
      assert ctx.WL_MESH() != null;
      return Type.WL_MESH;
    }
  }

  private @Nonnull ConcreteInterfaceAddress toConcreteInterfaceAddress(
      Ip_address_with_mask_or_prefixContext ctx) {
    if (ctx.ip_prefix() != null) {
      return ConcreteInterfaceAddress.parse(ctx.ip_prefix().getText());
    } else {
      assert ctx.ip_address() != null && ctx.subnet_mask() != null;
      return ConcreteInterfaceAddress.create(toIp(ctx.ip_address()), toIp(ctx.subnet_mask()));
    }
  }

  private @Nonnull Prefix toPrefix(Ip_address_with_mask_or_prefixContext ctx) {
    if (ctx.ip_prefix() != null) {
      return toPrefix(ctx.ip_prefix());
    } else {
      assert ctx.ip_address() != null && ctx.subnet_mask() != null;
      return Prefix.create(toIp(ctx.ip_address()), toIp(ctx.subnet_mask()));
    }
  }

  private @Nonnull Prefix toPrefix(Ip_address_with_mask_or_prefix_or_anyContext ctx) {
    if (ctx.ANY() != null) {
      return Prefix.ZERO;
    }
    assert ctx.ip_address_with_mask_or_prefix() != null;
    return toPrefix(ctx.ip_address_with_mask_or_prefix());
  }

  private @Nonnull Prefix toPrefix(Ip_prefixContext ctx) {
    return Prefix.parse(ctx.getText());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Access_list_nameContext ctx) {
    return toString(messageCtx, ctx.str(), "access-list name", ACCESS_LIST_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Address_nameContext ctx) {
    return toString(messageCtx, ctx.str(), "address name", ADDRESS_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, Zone_nameContext ctx) {
    return toString(messageCtx, ctx.str(), "zone name", ZONE_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, Policy_nameContext ctx) {
    return toString(messageCtx, ctx.str(), "policy name", POLICY_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Route_map_nameContext ctx) {
    return toString(messageCtx, ctx.str(), "route-map name", ROUTE_MAP_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Service_nameContext ctx) {
    return toString(messageCtx, ctx.str(), "service name", SERVICE_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Interface_nameContext ctx) {
    return toString(messageCtx, ctx.str(), "interface name", INTERFACE_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Internet_service_nameContext ctx) {
    return toString(
        messageCtx, ctx.str(), "internet-service-name name", INTERNET_SERVICE_NAME_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Interface_or_zone_nameContext ctx) {
    return toString(messageCtx, ctx.str(), "zone or interface name", ZONE_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Interface_aliasContext ctx) {
    return toString(messageCtx, ctx.str(), "interface alias", INTERFACE_ALIAS_PATTERN);
  }

  private @Nonnull String toString(Replacemsg_major_typeContext ctx) {
    return ctx.getText();
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Replacemsg_minor_typeContext ctx) {
    return toString(messageCtx, ctx.word(), "replacemsg minor type");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Device_hostnameContext ctx) {
    return toString(messageCtx, ctx.str(), "device hostname", DEVICE_HOSTNAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, StrContext ctx, String type, Pattern pattern) {
    return toString(messageCtx, ctx, type, s -> pattern.matcher(s).matches());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, StrContext ctx, String type, Predicate<String> predicate) {
    String text = toString(ctx);
    if (!predicate.test(text)) {
      warn(messageCtx, String.format("Illegal value for %s", type));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type) {
    return toString(messageCtx, ctx.str(), type, WORD_PATTERN);
  }

  private static @Nonnull String toString(StrContext ctx) {
    /*
     * Extract the text from a str.
     *
     * A str is composed of a sequence of single-quoted strings, double-quoted strings,
     * and unquoted non-whitespace characters.
     * - single-quoted strings do not interpret any characters specially
     * - double-quoted strings recognize the following three escape sequences:
     *   \" -> "
     *   \' -> ' <---Note that single-quotes are canonically escaped in double-quotes, but need not be.
     *   \\ -> \
     *   A backslash followed by any other character is treated as a literal backslash.
     *   So e.g.
     *   \n -> \n <---The letter 'n', not newline.
     * - outside of quotes, a backslash followed by any character other than a newline is stripped.
     *   E.g.
     *   \n -> n
     *   \" -> "
     *   \(space) -> (space)
     *   A backslash followed immediately by a newline character indicates a line continuation.
     *   That is, the backslash and the newline are both stripped.
     */
    return ctx.str_content().children.stream()
        .map(
            child -> {
              if (child instanceof Double_quoted_stringContext) {
                return toString((Double_quoted_stringContext) child);
              } else if (child instanceof Single_quoted_stringContext) {
                return toString((Single_quoted_stringContext) child);
              } else {
                assert child instanceof TerminalNode;
                int type = ((TerminalNode) child).getSymbol().getType();
                assert type == UNQUOTED_WORD_CHARS;
                return ESCAPED_UNQUOTED_CHAR_PATTERN.matcher(child.getText()).replaceAll("$1");
              }
            })
        .collect(Collectors.joining(""));
  }

  private static @Nonnull String toString(Double_quoted_stringContext ctx) {
    if (ctx.text == null) {
      return "";
    }
    String quotedText = ctx.text.getText();
    return ESCAPED_DOUBLE_QUOTED_CHAR_PATTERN.matcher(quotedText).replaceAll("$1");
  }

  private static @Nonnull String toString(Single_quoted_stringContext ctx) {
    return ctx.text != null ? ctx.text.getText() : "";
  }

  private @Nonnull Optional<Long> toLong(ParserRuleContext messageCtx, Bgp_asContext ctx) {
    return toLongInSpace(messageCtx, ctx.str(), BGP_AS_SPACE, "BGP AS");
  }

  private @Nonnull Optional<Long> toLong(ParserRuleContext messageCtx, Bgp_network_idContext ctx) {
    return toLongInSpace(messageCtx, ctx.str(), BGP_NETWORK_ID_SPACE, "BGP network ID");
  }

  private @Nonnull Optional<Long> toLong(ParserRuleContext messageCtx, Bgp_remote_asContext ctx) {
    return toLongInSpace(messageCtx, ctx.str(), BGP_REMOTE_AS_SPACE, "BGP remote AS");
  }

  private @Nonnull Optional<Long> toLong(ParserRuleContext messageCtx, Acl_rule_numberContext ctx) {
    return toLongInSpace(
        messageCtx, ctx.str(), ACCESS_LIST_RULE_NUMBER_SPACE, "access-list rule number");
  }

  private @Nonnull Optional<Long> toLong(
      ParserRuleContext messageCtx, Internet_service_idContext ctx) {
    return toLongInSpace(
        messageCtx, ctx.str(), INTERNET_SERVICE_ID_NUMBER_SPACE, "internet-service-id");
  }

  private @Nonnull Optional<Long> toLong(ParserRuleContext messageCtx, Policy_numberContext ctx) {
    return toLongInSpace(messageCtx, ctx.str(), POLICY_NUMBER_SPACE, "policy number");
  }

  private Optional<Long> toLong(
      ParserRuleContext ctx, FortiosParser.Route_map_rule_numberContext num) {
    return toLongInSpace(ctx, num.str(), ROUTE_MAP_RULE_NUM_SPACE, "route-map rule number");
  }

  private Optional<Long> toLong(ParserRuleContext ctx, FortiosParser.Sip_numberContext num) {
    return toLongInSpace(ctx, num.str(), SECONDARY_IP_NUM_SPACE, "secondaryip number");
  }

  private Optional<Long> toLong(ParserRuleContext ctx, FortiosParser.Route_numContext routeNum) {
    return toLongInSpace(
        ctx, routeNum.str(), STATIC_ROUTE_NUM_SPACE, "static route sequence number");
  }

  private @Nonnull Optional<Long> toLongInSpace(
      ParserRuleContext messageCtx, StrContext ctx, LongSpace space, String name) {
    return toLongInSpace(messageCtx, toString(ctx), space, name);
  }

  /**
   * Convert a {@link String} to a {@link Long} if it represents a number that is contained in the
   * provided {@code space}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<Long> toLongInSpace(
      ParserRuleContext messageCtx, String str, LongSpace space, String name) {
    Long num = Longs.tryParse(str);
    if (num == null || !space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%s'", name, space, str));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, Uint8Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, Uint16Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  /**
   * Convert a {@link String} to an {@link Integer} if it represents a number that is contained in
   * the provided {@code space}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, String str, IntegerSpace space, String name) {
    Integer num = Ints.tryParse(str);
    if (num == null || !space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  /** Generate a warning for trying to rename a non-existent structure. */
  private void warnRenameNonExistent(
      ParserRuleContext ctx, String name, FortiosStructureType type) {
    warn(ctx, String.format("Cannot rename non-existent %s %s", type.getDescription(), name));
  }

  /** Generate a warning for trying to rename a structure with a name already in use. */
  private void warnRenameConflict(
      ParserRuleContext ctx, String currentName, String newName, FortiosStructureType type) {
    warn(
        ctx,
        String.format(
            "Renaming %s %s conflicts with an existing object %s, ignoring this rename operation",
            type.getDescription(), currentName, newName));
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _c.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      String msg = String.format("Unrecognized Line: %d: %s", line, lineText);
      _w.redFlag(msg + " SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY");
    }
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, Ip_protocol_numberContext num) {
    return toIntegerInSpace(ctx, num.uint8(), IP_PROTOCOL_NUMBER_SPACE, "ip protocol-number");
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, MtuContext mtu) {
    return toIntegerInSpace(ctx, mtu.uint16(), MTU_SPACE, "mtu");
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, Route_distanceContext routeDistance) {
    return toIntegerInSpace(
        ctx, routeDistance.uint8(), ADMIN_DISTANCE_SPACE, "route administrative distance");
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, VlanidContext vlanid) {
    return toIntegerInSpace(ctx, vlanid.uint16(), VLANID_SPACE, "vlanid");
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, VrfContext vrf) {
    return toIntegerInSpace(ctx, vrf.uint8(), VRF_SPACE, "vrf");
  }

  private static int toInteger(Subnet_maskContext ctx) {
    return Ip.parse(ctx.getText()).numSubnetBits();
  }

  private static int toInteger(Uint16Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private static int toInteger(Uint8Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private @Nonnull Optional<Ip> toIp(ParserRuleContext ctx, Bgp_neighbor_idContext neighborIdCtx) {
    String ipStr = toString(neighborIdCtx.str());
    try {
      return Optional.of(Ip.parse(ipStr));
    } catch (IllegalArgumentException e) {
      warn(ctx, String.format("Cannot parse %s as an IP", ipStr));
      return Optional.empty();
    }
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull Ip toIp(Subnet_maskContext ctx) {
    return Ip.parse(ctx.getText());
  }

  /**
   * Creates an {@link IpWildcard} from the given wildcard context.
   *
   * @param invertMask Whether to invert the mask when converting. In {@link IpWildcard}, 0s
   *     indicate bits that matter, so this param should be true if {@code ctx} is from a context
   *     where the 1s in the mask indicate bits that matter.
   */
  private static @Nonnull IpWildcard toIpWildcard(Ip_wildcardContext ctx, boolean invertMask) {
    return toIpWildcard(ctx.ip, ctx.mask, invertMask);
  }

  /**
   * Creates an {@link IpWildcard} from the given IP and mask.
   *
   * @param invertMask Whether to invert the mask when converting. In {@link IpWildcard}, 0s
   *     indicate bits that matter, so this param should be true if {@code mask} is from a context
   *     where the 1s in the mask indicate bits that matter.
   */
  private static @Nonnull IpWildcard toIpWildcard(
      Ip_addressContext ip, Ip_addressContext mask, boolean invertMask) {
    // Invert mask because in FortiOS, bits that are set matter, whereas the opposite is true for
    // the mask in IpWildcard
    Ip maskIp = toIp(mask);
    return IpWildcard.ipWithWildcardMask(toIp(ip), invertMask ? maskIp.inverted() : maskIp);
  }

  private static @Nonnull Ip6 toIp6(Ipv6_addressContext ctx) {
    return Ip6.parse(ctx.getText());
  }

  /** Returns message indicating why address can't be committed in the CLI, or null if it can */
  @VisibleForTesting
  public static @Nullable String addressValid(Address a, boolean nameValid) {
    if (!nameValid) {
      return "name is invalid";
    }
    switch (a.getTypeEffective()) {
      case IPMASK:
        Ip subnetMask = a.getTypeSpecificFields().getIp2Effective();
        if (!subnetMask.isValidNetmask1sLeading()) {
          return String.format("%s is not a valid subnet mask", subnetMask);
        }
        return null;
      case IPRANGE:
        Ip endIp = a.getTypeSpecificFields().getIp2Effective();
        if (endIp.equals(Ip.ZERO)) {
          // This is the warning the CLI gives if end-ip is not set
          return "end-ip cannot be 0";
        }
        Ip startIp = a.getTypeSpecificFields().getIp1Effective();
        if (endIp.asLong() < startIp.asLong()) {
          return "end-ip must be greater than start-ip";
        }
        return null;
      case WILDCARD: // Any IPs are valid for wildcard
      case INTERFACE_SUBNET: // All cases from here on are unsupported
      case DYNAMIC:
      case FQDN:
      case GEOGRAPHY:
      case MAC:
        return null;
    }
    throw new IllegalStateException("This line should be unreachable");
  }

  /** Returns message indicating why addrgrp can't be committed in the CLI, or null if it can */
  @VisibleForTesting
  public static @Nullable String addrgrpValid(Addrgrp a, boolean nameValid) {
    if (!nameValid) {
      return "name is invalid";
    }
    if (a.getMemberUUIDs().isEmpty()) {
      return "addrgrp requires at least one member";
    }
    if (a.getExcludeEffective() && a.getExcludeMemberUUIDs().isEmpty()) {
      return "addrgrp requires at least one exclude-member when exclude is enabled";
    }
    return null;
  }

  private static @Nullable String bgpNeighborValid(BgpNeighbor bgpNeighbor) {
    if (bgpNeighbor.getIp().equals(Ip.ZERO)) {
      return "neighbor ID is invalid";
    } else if (bgpNeighbor.getRemoteAs() == null) {
      return "remote-as must be set";
    }
    return null;
  }

  /** Returns message indicating why policy can't be committed in the CLI, or null if it can */
  @VisibleForTesting
  public static @Nullable String policyValid(Policy p, boolean valid) {
    // _valid indicates whether any invalid lines have gone into current policy that would cause the
    // CLI to pop out of its edit block
    if (!valid) {
      return "name is invalid"; // currently, only invalid name can cause valid to be false
    } else if (p.getSrcIntf().isEmpty() && p.getSrcIntfZoneUUIDs().isEmpty()) {
      return "srcintf must be set";
    } else if (p.getDstIntf().isEmpty() && p.getDstIntfZoneUUIDs().isEmpty()) {
      return "dstintf must be set";
    } else if (p.getSrcAddrUUIDs().isEmpty()) {
      return "srcaddr must be set";
    } else if (p.getDstAddrUUIDs().isEmpty()) {
      return "dstaddr must be set";
    } else if (p.getServiceUUIDs().isEmpty()) {
      return "service must be set";
    }
    // TODO "schedule" must be set to commit policy, but we don't parse it. Should we?
    return null;
  }

  /** Returns message indicating why service can't be committed in the CLI, or null if it can */
  @VisibleForTesting
  public static @Nullable String serviceValid(Service s, boolean valid) {
    // Indicates whether any invalid lines have gone into current service that would cause the
    // CLI to pop out of its edit block
    if (!valid) {
      return "name is invalid"; // currently, only invalid name can cause valid to be false
    }
    // TODO Check validity of _ipRange; it is not yet used in conversion
    return switch (s.getProtocolEffective()) {
      case TCP_UDP_SCTP -> {
        if (s.getTcpPortRangeDst() == null
            && s.getUdpPortRangeDst() == null
            && s.getSctpPortRangeDst() == null) {
          yield "TCP/UDP/SCTP portrange cannot all be empty";
        }
        yield null;
      }
      // both ICMP type and ICMP code are allowed to be unset
      case ICMP, ICMP6, IP ->
          // protocol-number is allowed to be unset
          null;
    };
  }

  /**
   * Returns message indicating why service group can't be committed in the CLI, or null if it can
   */
  @VisibleForTesting
  public static @Nullable String serviceGroupValid(ServiceGroup s, boolean nameValid) {
    if (!nameValid) {
      return "name is invalid";
    }
    if (s.getMemberUUIDs().isEmpty()) {
      return "service group requires at least one member";
    }

    return null;
  }

  private static @Nullable String staticRouteValid(StaticRoute staticRoute, boolean seqNumValid) {
    if (!seqNumValid) {
      return "sequence number is invalid";
    } else if (staticRoute.getDevice() == null) {
      return "device must be set";
    }
    return null;
  }

  /**
   * Returns a boolean indicating if the specified address type is valid to use for an exclude
   * member for an addrgrp.
   */
  private static boolean validExcludeMemberType(Address.Type type) {
    return type == Address.Type.IPRANGE || type == Address.Type.IPMASK;
  }

  @Override
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  /**
   * Class representing a set of interfaces as well as a set of zones. For use with firewall
   * policies srcintf and dstintf references.
   */
  private static final class InterfacesAndZones {
    Set<String> getInterfaces() {
      return _interfaces;
    }

    Set<BatfishUUID> getZones() {
      return _zones;
    }

    InterfacesAndZones(Set<String> interfaces, Set<BatfishUUID> zones) {
      _interfaces = ImmutableSet.copyOf(interfaces);
      _zones = ImmutableSet.copyOf(zones);
    }

    private final Set<String> _interfaces;
    private final Set<BatfishUUID> _zones;
  }

  // TODO determine more precise limitations on allowed chars; at least no spaces or quotes
  private static final Pattern ACCESS_LIST_NAME_PATTERN =
      Pattern.compile("^[^ \r\n()'\"#<>]{1,35}$");
  private static final Pattern ADDRESS_NAME_PATTERN = Pattern.compile("^[^\r\n]{1,79}$");
  private static final Pattern DEVICE_HOSTNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
  private static final Pattern ESCAPED_DOUBLE_QUOTED_CHAR_PATTERN =
      Pattern.compile("\\\\(['\"\\\\])");
  private static final Pattern ESCAPED_UNQUOTED_CHAR_PATTERN = Pattern.compile("\\\\([^\\r\\n])");
  private static final Pattern INTERFACE_NAME_PATTERN = Pattern.compile("^[^\r\n]{1,15}$");
  private static final Pattern INTERNET_SERVICE_NAME_NAME_PATTERN =
      Pattern.compile("^[^\n" + "]{1,63}$");
  private static final Pattern INTERFACE_ALIAS_PATTERN = Pattern.compile("^[^\r\n]{0,25}$");
  private static final Pattern POLICY_NAME_PATTERN = Pattern.compile("^[^\r\n]{1,35}$");
  // TODO determine more precise limitations on allowed chars for route-map name
  private static final Pattern ROUTE_MAP_NAME_PATTERN = Pattern.compile("^[^ \r\n()'\"#<>]{1,35}$");
  private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^[^\r\n]{1,79}$");
  private static final Pattern WORD_PATTERN = Pattern.compile("^[^ \t\r\n]+$");
  private static final Pattern ZONE_NAME_PATTERN = Pattern.compile("^[^\r\n]{1,35}$");

  private static final LongSpace ACCESS_LIST_RULE_NUMBER_SPACE =
      LongSpace.of(Range.closed(0L, 4294967295L));
  private static final LongSpace BGP_AS_SPACE = LongSpace.of(Range.closed(0L, 4294967295L));
  private static final LongSpace BGP_NETWORK_ID_SPACE = LongSpace.of(Range.closed(0L, 4294967295L));
  private static final LongSpace BGP_REMOTE_AS_SPACE = LongSpace.of(Range.closed(1L, 4294967295L));
  private static final IntegerSpace IP_PROTOCOL_NUMBER_SPACE =
      IntegerSpace.of(Range.closed(0, 254));
  private static final IntegerSpace MTU_SPACE = IntegerSpace.of(Range.closed(68, 65535));
  private static final LongSpace INTERNET_SERVICE_ID_NUMBER_SPACE =
      LongSpace.of(Range.closed(0L, 4294967295L));
  private static final LongSpace POLICY_NUMBER_SPACE = LongSpace.of(Range.closed(0L, 4294967294L));
  private static final LongSpace ROUTE_MAP_RULE_NUM_SPACE =
      LongSpace.of(Range.closed(0L, 4294967295L));
  private static final IntegerSpace ADMIN_DISTANCE_SPACE = IntegerSpace.of(Range.closed(1, 255));
  private static final LongSpace SECONDARY_IP_NUM_SPACE =
      LongSpace.of(Range.closed(0L, 4294967295L));
  private static final LongSpace STATIC_ROUTE_NUM_SPACE =
      LongSpace.of(Range.closed(0L, 4294967295L));
  private static final IntegerSpace VLANID_SPACE = IntegerSpace.of(Range.closed(1, 4094));
  private static final IntegerSpace VRF_SPACE = IntegerSpace.of(Range.closed(0, 31));

  private AccessList _currentAccessList;
  private boolean _currentAccessListNameValid;
  private AccessListRule _currentAccessListRule;
  private boolean _currentAccessListRuleNameValid;

  private Address _currentAddress;

  /**
   * Whether the current address has invalid lines that would prevent committing the address in CLI.
   * This field being true does not guarantee the current address is valid; use {@link
   * #addressValid(Address, boolean)}.
   */
  private boolean _currentAddressNameValid;

  private Addrgrp _currentAddrgrp;
  private boolean _currentAddrgrpNameValid;
  private BgpNeighbor _currentBgpNeighbor;
  private BgpNetwork _currentBgpNetwork;
  private Interface _currentInterface;
  private boolean _currentInterfaceNameValid;
  private InternetServiceName _currentInternetServiceName;
  private boolean _currentInternetServiceNameNameValid;
  private Policy _currentPolicy;

  /**
   * Whether the current policy has invalid lines that would prevent committing the policy in CLI.
   * This field being true does not guarantee the current policy is valid; use {@link
   * #policyValid(Policy, boolean)}.
   */
  private boolean _currentPolicyValid;

  private Replacemsg _currentReplacemsg;

  private RouteMap _currentRouteMap;
  private boolean _currentRouteMapNameValid;

  private RouteMapRule _currentRouteMapRule;
  private boolean _currentRouteMapRuleNameValid;

  private SecondaryIp _currentSecondaryip;
  private boolean _currentSecondaryipNameValid;
  private Service _currentService;

  /**
   * Whether the current service has invalid lines that would prevent committing the service in CLI.
   * This field being true does not guarantee the current service is valid; use {@link
   * #serviceValid(Service, boolean)}.
   */
  private boolean _currentServiceValid;

  private ServiceGroup _currentServiceGroup;
  private boolean _currentServiceGroupNameValid;

  private StaticRoute _currentStaticRoute;
  private boolean _currentStaticRouteNumValid;
  private Zone _currentZone;

  /** Whether the current zone has an invalid name. */
  private boolean _currentZoneNameValid;

  private final @Nonnull FortiosConfiguration _c;
  private final @Nonnull FortiosCombinedParser _parser;
  private final @Nonnull String _text;
  // Internal sequence number to generate unique UUIDs for structure that may be renamed or cloned
  private int _uuidSequenceNumber = 0;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }
}
