package org.batfish.vendor.a10.grammar;

import static org.batfish.vendor.a10.grammar.A10Lexer.WORD;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.SubRange;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.a10.grammar.A10Parser.A10_configurationContext;
import org.batfish.vendor.a10.grammar.A10Parser.HostnameContext;
import org.batfish.vendor.a10.grammar.A10Parser.S_hostnameContext;
import org.batfish.vendor.a10.grammar.A10Parser.WordContext;
import org.batfish.vendor.a10.representation.A10Configuration;
import org.batfish.vendor.a10.representation.Interface;
import org.batfish.vendor.a10.representation.InterfaceReference;
import org.batfish.vendor.a10.representation.TrunkGroup;
import org.batfish.vendor.a10.representation.TrunkInterface;
import org.batfish.vendor.a10.representation.Vlan;

/** Given a parse tree, builds a {@link A10Configuration}. */
public final class A10ConfigurationBuilder extends A10ParserBaseListener
    implements SilentSyntaxListener {

  public A10ConfigurationBuilder(
      A10CombinedParser parser,
      String text,
      Warnings warnings,
      A10Configuration configuration,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _configuration = configuration;
    _silentSyntax = silentSyntax;
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _configuration.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      _w.redFlag(
          String.format(
              "Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
              line, lineText));
    }
  }

  @Nonnull
  public A10Configuration getConfiguration() {
    return _configuration;
  }

  @Nonnull
  @Override
  public String getInputText() {
    return _text;
  }

  @Nonnull
  @Override
  public BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Nonnull
  @Override
  public SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Nonnull
  @Override
  public Warnings getWarnings() {
    return _w;
  }

  @Override
  public void enterA10_configuration(A10_configurationContext ctx) {}

  @Override
  public void exitA10_configuration(A10_configurationContext ctx) {
    _configuration.finalizeStructures();
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    toString(ctx, ctx.hostname()).ifPresent(_configuration::setHostname);
  }

  @Override
  public void enterSid_ethernet(A10Parser.Sid_ethernetContext ctx) {
    Optional<Integer> num = toInteger(ctx.num);
    num.ifPresent(
        n ->
            _currentInterface =
                _configuration
                    .getInterfacesEthernet()
                    .computeIfAbsent(n, number -> new Interface(Interface.Type.ETHERNET, n)));
    if (!num.isPresent()) {
      _currentInterface = new Interface(Interface.Type.ETHERNET, -1); // dummy
    }
  }

  @Override
  public void exitSid_ethernet(A10Parser.Sid_ethernetContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void enterSid_loopback(A10Parser.Sid_loopbackContext ctx) {
    Optional<Integer> num = toInteger(ctx.num);
    num.ifPresent(
        n ->
            _currentInterface =
                _configuration
                    .getInterfacesLoopback()
                    .computeIfAbsent(n, number -> new Interface(Interface.Type.LOOPBACK, n)));
    if (!num.isPresent()) {
      _currentInterface = new Interface(Interface.Type.LOOPBACK, -1); // dummy
    }
  }

  @Override
  public void exitSid_trunk(A10Parser.Sid_trunkContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void enterSid_trunk(A10Parser.Sid_trunkContext ctx) {
    Optional<Integer> num = toInteger(ctx.num);
    num.ifPresent(
        n ->
            _currentInterface =
                _configuration
                    .getInterfacesTrunk()
                    .computeIfAbsent(n, number -> new TrunkInterface(n, null)));
    if (!num.isPresent()) {
      _currentInterface = new TrunkInterface(-1, null); // dummy
    }
  }

  @Override
  public void exitSid_loopback(A10Parser.Sid_loopbackContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitSid_enable(A10Parser.Sid_enableContext ctx) {
    _currentInterface.setEnabled(true);
  }

  @Override
  public void exitSid_disable(A10Parser.Sid_disableContext ctx) {
    _currentInterface.setEnabled(false);
  }

  @Override
  public void exitSid_mtu(A10Parser.Sid_mtuContext ctx) {
    toInteger(ctx.interface_mtu()).ifPresent(mtu -> _currentInterface.setMtu(mtu));
  }

  @Override
  public void exitSidi_address(A10Parser.Sidi_addressContext ctx) {
    if (_currentInterface.getTrunkGroup() != null) {
      warn(ctx, "Cannot configure an IP address on a trunk-group member");
      return;
    }
    _currentInterface.setIpAddress(toInterfaceAddress(ctx.ip_prefix()));
  }

  @Override
  public void exitSid_name(A10Parser.Sid_nameContext ctx) {
    toString(ctx, ctx.interface_name_str()).ifPresent(n -> _currentInterface.setName(n));
  }

  @Override
  public void enterSid_ve(A10Parser.Sid_veContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx.num);
    if (!maybeNum.isPresent()) {
      _currentInterface = new Interface(Interface.Type.VE, -1); // dummy
      return;
    }
    int num = maybeNum.get();
    Vlan vlan = _configuration.getVlans().get(num);
    if (vlan == null || !Objects.equals(num, vlan.getRouterInterface())) {
      warn(ctx, "Cannot create a ve interface for a non-existent or unassociated VLAN.");
      _currentInterface = new Interface(Interface.Type.VE, -1); // dummy
      return;
    }
    _currentInterface =
        _configuration
            .getInterfacesVe()
            .computeIfAbsent(num, n -> new Interface(Interface.Type.VE, n));
  }

  @Override
  public void exitSid_ve(A10Parser.Sid_veContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void enterSidl_trunk(A10Parser.Sidl_trunkContext ctx) {
    TrunkGroup.Type type = TrunkGroup.Type.LACP;
    toInteger(ctx.num)
        .ifPresent(
            n -> {
              Optional<String> maybeInvalidReason = isTrunkValidForCurrentIface(n, type);
              if (maybeInvalidReason.isPresent()) {
                warn(ctx, maybeInvalidReason.get());
              } else {
                setCurrentTrunkGroupAndReferences(n, type);
              }
            });

    if (_currentTrunkGroup == null) {
      _currentTrunkGroup = new TrunkGroup(-1, type); // dummy
    }
  }

  @Override
  public void exitSidl_trunk(A10Parser.Sidl_trunkContext ctx) {
    _currentTrunkGroup = null;
  }

  @Override
  public void exitSidl_timeout(A10Parser.Sidl_timeoutContext ctx) {
    TrunkGroup tg = _currentInterface.getTrunkGroup();
    if (tg == null) {
      warn(ctx, "Cannot configure timeout for non-existent trunk-group");
      return;
    }
    tg.setTimeout(toTimeout(ctx.trunk_timeout()));
  }

  @Override
  public void exitSidlt_mode(A10Parser.Sidlt_modeContext ctx) {
    _currentTrunkGroup.setMode(toMode(ctx.trunk_mode()));
  }

  @Override
  public void enterS_vlan(A10Parser.S_vlanContext ctx) {
    Optional<Integer> maybeVlanNum = toInteger(ctx.vlan_number());
    if (maybeVlanNum.isPresent()) {
      _currentVlan = _configuration.getVlans().computeIfAbsent(maybeVlanNum.get(), Vlan::new);
      return;
    }
    _currentVlan = new Vlan(0); // dummy
  }

  @Override
  public void exitS_vlan(A10Parser.S_vlanContext ctx) {
    _currentVlan = null;
  }

  @Override
  public void exitSvd_name(A10Parser.Svd_nameContext ctx) {
    toString(ctx, ctx.vlan_name()).ifPresent(n -> _currentVlan.setName(n));
  }

  @Override
  public void exitSvd_router_interface(A10Parser.Svd_router_interfaceContext ctx) {
    Optional<Integer> maybeNum = toInteger(ctx.vlan_number());
    if (maybeNum.isPresent()) {
      if (!maybeNum.get().equals(_currentVlan.getNumber())) {
        warn(ctx, "Virtual Ethernet interface number must be the same as VLAN ID.");
        return;
      }
      _currentVlan.setRouterInterface(maybeNum.get());
    }
  }

  @Override
  public void exitSvd_tagged(A10Parser.Svd_taggedContext ctx) {
    // TODO enforce interface restrictions
    //  e.g. untagged iface cannot be reused, cannot attach trunk members directly, etc.
    toInterfaceReferences(ctx.vlan_iface_references())
        .ifPresent(refs -> _currentVlan.addTagged(refs));
  }

  @Override
  public void exitSvd_untagged(A10Parser.Svd_untaggedContext ctx) {
    // TODO enforce interface restrictions
    //  e.g. untagged iface cannot be reused, cannot attach trunk members directly, etc.
    toInterfaceReferences(ctx.vlan_iface_references())
        .ifPresent(refs -> _currentVlan.addUntagged(refs));
  }

  /**
   * Check if specified trunk number and type are valid for the current interface.
   *
   * <p>If so, return {@link Optional#empty()}, otherwise returns a string indicating why it is not
   * valid.
   *
   * <ul>
   *   If {@link Optional#empty()} is returned, this function indicates that:
   *   <li>If a trunk group already exists on the interface, it is compatible with the supplied
   *       number and type.
   *   <li>If a trunk interface exists with the supplied number, it is compatible with the supplied
   *       number and type.
   *   <li>The {@code _currentInterface} can be added to the trunk group.
   * </ul>
   */
  private Optional<String> isTrunkValidForCurrentIface(int num, @Nullable TrunkGroup.Type type) {
    if (_currentInterface.getIpAddress() != null) {
      return Optional.of("Cannot add an interface with a configured IP address to a trunk-group");
    }

    TrunkGroup currentTrunk = _currentInterface.getTrunkGroup();
    if (currentTrunk != null) {
      // Existing TrunkGroup for this interface
      if (currentTrunk.getNumber() != num) {
        return Optional.of(
            String.format(
                "This interface is already a member of trunk-group %s", currentTrunk.getNumber()));
      } else if (type != null && currentTrunk.getTypeEffective() != type) {
        return Optional.of(
            String.format(
                "Trunk-group already exists as a different type (%s)",
                currentTrunk.getTypeEffective().toString().toLowerCase()));
      }
    } else {
      // New TrunkGroup for this interface
      currentTrunk = new TrunkGroup(num, type);
      TrunkInterface existingTrunk = _configuration.getInterfacesTrunk().get(num);
      if (existingTrunk != null
          && existingTrunk.getTrunkTypeEffective() != currentTrunk.getTypeEffective()) {
        return Optional.of(
            String.format(
                "Trunk-group already exists as a different type (%s)",
                existingTrunk.getTrunkTypeEffective().toString().toLowerCase()));
      }
      // TODO enforce invariants regarding trunk members
      //  e.g. must not be associated with different VLANs, must have same MTUs at least for LACP,
      // etc.
    }
    return Optional.empty();
  }

  /**
   * Set the current trunk-group and add it as the trunk-group for the current interface, creating
   * it if necessary. Also, perform related datamodel updates, like creating a corresponding trunk
   * interface if needed and updating its members.
   */
  private void setCurrentTrunkGroupAndReferences(int num, @Nullable TrunkGroup.Type type) {
    _currentTrunkGroup =
        Optional.ofNullable(_currentInterface.getTrunkGroup())
            .orElseGet(() -> new TrunkGroup(num, type));
    _currentInterface.setTrunkGroup(_currentTrunkGroup);
    TrunkInterface trunkInterface =
        _configuration.getInterfacesTrunk().computeIfAbsent(num, n -> new TrunkInterface(n, type));
    trunkInterface
        .getMembers()
        .add(new InterfaceReference(_currentInterface.getType(), _currentInterface.getNumber()));
  }

  @Override
  public void enterSid_trunk_group(A10Parser.Sid_trunk_groupContext ctx) {
    TrunkGroup.Type type = ctx.trunk_type() != null ? toType(ctx.trunk_type()) : null;
    Optional<Integer> maybeNum = toInteger(ctx.trunk_number());
    if (!maybeNum.isPresent()) {
      _currentTrunkGroup = new TrunkGroup(-1, type); // dummy
      return;
    }

    int num = maybeNum.get();
    Optional<String> maybeInvalidReason = isTrunkValidForCurrentIface(num, type);
    if (maybeInvalidReason.isPresent()) {
      warn(ctx, maybeInvalidReason.get());
      _currentTrunkGroup = new TrunkGroup(-1, type); // dummy
      return;
    }
    setCurrentTrunkGroupAndReferences(num, type);
  }

  @Override
  public void exitSid_trunk_group(A10Parser.Sid_trunk_groupContext ctx) {
    _currentTrunkGroup = null;
  }

  @Override
  public void exitSidtg_mode(A10Parser.Sidtg_modeContext ctx) {
    _currentTrunkGroup.setMode(toMode(ctx.trunk_mode()));
  }

  @Override
  public void exitSidtg_timeout(A10Parser.Sidtg_timeoutContext ctx) {
    _currentTrunkGroup.setTimeout(toTimeout(ctx.trunk_timeout()));
  }

  @Override
  public void exitSidtg_user_tag(A10Parser.Sidtg_user_tagContext ctx) {
    toString(ctx, ctx.user_tag()).ifPresent(ut -> _currentTrunkGroup.setUserTag(ut));
  }

  TrunkGroup.Mode toMode(A10Parser.Trunk_modeContext ctx) {
    if (ctx.ACTIVE() != null) {
      return TrunkGroup.Mode.ACTIVE;
    }
    assert ctx.PASSIVE() != null;
    return TrunkGroup.Mode.PASSIVE;
  }

  TrunkGroup.Timeout toTimeout(A10Parser.Trunk_timeoutContext ctx) {
    if (ctx.SHORT() != null) {
      return TrunkGroup.Timeout.SHORT;
    }
    assert ctx.LONG() != null;
    return TrunkGroup.Timeout.LONG;
  }

  TrunkGroup.Type toType(A10Parser.Trunk_typeContext ctx) {
    if (ctx.LACP() != null) {
      return TrunkGroup.Type.LACP;
    } else if (ctx.LACP_UDLD() != null) {
      return TrunkGroup.Type.LACP_UDLD;
    }
    assert ctx.STATIC() != null;
    return TrunkGroup.Type.STATIC;
  }

  Optional<List<InterfaceReference>> toInterfaceReferences(
      A10Parser.Vlan_iface_referencesContext ctx) {
    if (ctx.vlan_ifaces_list() != null) {
      return toInterfaces(ctx.vlan_ifaces_list());
    }
    assert ctx.vlan_ifaces_range() != null;
    Interface.Type type = toInterfaceType(ctx.vlan_ifaces_range());
    return toSubRange(ctx.vlan_ifaces_range())
        .map(
            subRange ->
                subRange
                    .asStream()
                    .mapToObj(i -> new InterfaceReference(type, i))
                    .collect(ImmutableList.toImmutableList()));
  }

  Interface.Type toInterfaceType(A10Parser.Vlan_ifaces_rangeContext ctx) {
    assert ctx.vlan_iface_ethernet_range() != null;
    return Interface.Type.ETHERNET;
  }

  Optional<SubRange> toSubRange(A10Parser.Vlan_ifaces_rangeContext ctx) {
    assert ctx.vlan_iface_ethernet_range() != null;
    Optional<Integer> maybeFrom = toInteger(ctx.vlan_iface_ethernet_range().num);
    Optional<Integer> maybeTo = toInteger(ctx.vlan_iface_ethernet_range().to);
    if (!maybeFrom.isPresent() || !maybeTo.isPresent()) {
      // Already warned
      return Optional.empty();
    }
    int from = maybeFrom.get();
    int to = maybeTo.get();
    if (from > to) {
      warn(
          ctx, "Invalid range for VLAN interface reference, 'from' must not be greater than 'to'.");
      return Optional.empty();
    }
    return Optional.of(new SubRange(from, to));
  }

  Optional<List<InterfaceReference>> toInterfaces(A10Parser.Vlan_ifaces_listContext ctx) {
    ImmutableList.Builder<InterfaceReference> ifaces = ImmutableList.builder();
    for (A10Parser.Vlan_iface_ethernetContext iface : ctx.vlan_iface_ethernet()) {
      Optional<Integer> maybeNum = toInteger(iface.num);
      if (!maybeNum.isPresent()) {
        // Already warned
        return Optional.empty();
      }
      ifaces.add(new InterfaceReference(Interface.Type.ETHERNET, maybeNum.get()));
    }
    return Optional.of(ifaces.build());
  }

  Optional<Integer> toInteger(A10Parser.Trunk_numberContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint16(), TRUNK_NUMBER_RANGE, "trunk number");
  }

  Optional<Integer> toInteger(A10Parser.Vlan_numberContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint16(), VLAN_NUMBER_RANGE, "vlan number");
  }

  private @Nonnull ConcreteInterfaceAddress toInterfaceAddress(A10Parser.Ip_prefixContext ctx) {
    if (ctx.subnet_mask() != null) {
      return ConcreteInterfaceAddress.create(toIp(ctx.ip_address()), toIp(ctx.subnet_mask()));
    }
    assert ctx.ip_slash_prefix() != null;
    return ConcreteInterfaceAddress.parse(
        ctx.ip_address().getText() + ctx.ip_slash_prefix().getText());
  }

  private @Nonnull Ip toIp(A10Parser.Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private @Nonnull Ip toIp(A10Parser.Subnet_maskContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private @Nonnull Optional<Integer> toInteger(A10Parser.Interface_mtuContext ctx) {
    return toIntegerInSpace(ctx, ctx.uint16(), INTERFACE_MTU_RANGE, "interface mtu");
  }

  private @Nonnull Optional<Integer> toInteger(A10Parser.Ethernet_numberContext ctx) {
    return toIntegerInSpace(
        ctx, ctx.uint8(), INTERFACE_NUMBER_ETHERNET_RANGE, "interface ethernet number");
  }

  private @Nonnull Optional<Integer> toInteger(A10Parser.Loopback_numberContext ctx) {
    return toIntegerInSpace(
        ctx, ctx.uint8(), INTERFACE_NUMBER_LOOPBACK_RANGE, "interface loopback number");
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, A10Parser.Uint8Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, A10Parser.Uint16Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, A10Parser.Uint32Context ctx, IntegerSpace space, String name) {
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

  /**
   * Return the text of the provided {@code ctx} if its length is within the provided {@link
   * IntegerSpace lengthSpace}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<String> toStringWithLengthInSpace(
      ParserRuleContext messageCtx, WordContext ctx, IntegerSpace lengthSpace, String name) {
    String text = toString(ctx);
    if (!lengthSpace.contains(text.length())) {
      warn(
          messageCtx,
          String.format(
              "Expected %s with length in range %s, but got '%s'", name, lengthSpace, text));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, HostnameContext ctx) {
    return toString(messageCtx, ctx.word(), "hostname", HOSTNAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.User_tagContext ctx) {
    return toStringWithLengthInSpace(messageCtx, ctx.word(), USER_TAG_LENGTH_RANGE, "user-tag");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Vlan_nameContext ctx) {
    return toStringWithLengthInSpace(messageCtx, ctx.word(), VLAN_NAME_LENGTH_RANGE, "vlan name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, A10Parser.Interface_name_strContext ctx) {
    return toStringWithLengthInSpace(
        messageCtx, ctx.word(), INTERFACE_NAME_LENGTH_RANGE, "interface name");
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type, Pattern pattern) {
    return toString(messageCtx, ctx, type, s -> pattern.matcher(s).matches());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type, Predicate<String> predicate) {
    String text = toString(ctx);
    if (!predicate.test(text)) {
      warn(messageCtx, String.format("Illegal value for %s", type));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  private static @Nonnull String toString(WordContext ctx) {
    return ctx.word_content().children.stream()
        .map(
            child -> {
              if (child instanceof A10Parser.Double_quoted_stringContext) {
                return toString(((A10Parser.Double_quoted_stringContext) child).text);
              } else if (child instanceof A10Parser.Single_quoted_stringContext) {
                return toString(((A10Parser.Single_quoted_stringContext) child).text);
              } else {
                assert child instanceof TerminalNode;
                int type = ((TerminalNode) child).getSymbol().getType();
                assert type == WORD;
                return child.getText();
              }
            })
        .collect(Collectors.joining(""));
  }

  private static @Nonnull String toString(@Nullable A10Parser.Quoted_textContext text) {
    if (text == null) {
      return "";
    }
    // Device appears to just remove backslashes from quoted strings
    return text.getText().replaceAll("\\\\", "");
  }

  private static final IntegerSpace INTERFACE_MTU_RANGE = IntegerSpace.of(Range.closed(434, 1500));
  private static final IntegerSpace INTERFACE_NUMBER_ETHERNET_RANGE =
      IntegerSpace.of(Range.closed(1, 40));
  private static final IntegerSpace INTERFACE_NUMBER_LOOPBACK_RANGE =
      IntegerSpace.of(Range.closed(0, 10));
  private static final IntegerSpace INTERFACE_NAME_LENGTH_RANGE =
      IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace TRUNK_NUMBER_RANGE = IntegerSpace.of(Range.closed(1, 4096));
  private static final IntegerSpace USER_TAG_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 127));
  private static final IntegerSpace VLAN_NAME_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 63));
  private static final IntegerSpace VLAN_NUMBER_RANGE = IntegerSpace.of(Range.closed(2, 4094));

  private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

  @Nonnull private A10Configuration _configuration;

  private Interface _currentInterface;

  private TrunkGroup _currentTrunkGroup;

  private Vlan _currentVlan;

  @Nonnull private A10CombinedParser _parser;

  @Nonnull private final String _text;

  @Nonnull private final Warnings _w;

  @Nonnull private final SilentSyntaxCollection _silentSyntax;
}
