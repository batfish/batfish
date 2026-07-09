package org.batfish.specifier.parboiled;

import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.util.isp.IspModelingUtils;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.parboiled.grammar.SpecifierLexer;
import org.batfish.specifier.parboiled.grammar.SpecifierParser;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.AddressGroupAndReferenceBookContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.AppIcmpTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.AppPortSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.AppPortTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.AppSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.AppTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.EnumSetBaseContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.EnumSetSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.EnumSetTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.FilterIntersectionContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.FilterSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.FilterTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.FilterWithNodeContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.FilterWithoutNodeContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.FilterWithoutNodeIntersectionContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.FilterWithoutNodeTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.InterfaceFuncContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.InterfaceIntersectionContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.InterfaceSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.InterfaceTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.InterfaceWithNodeContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.InterfaceWithoutNodeContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.InterfaceWithoutNodeIntersectionContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.InterfaceWithoutNodeTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.IpProtocolContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.IpProtocolSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.IpProtocolTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.IpSpaceIntersectionContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.IpSpaceSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.IpSpaceTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.LocationInterfaceContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.LocationIntersectionContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.LocationSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.LocationTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.NameContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.NameSetSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.NameSetTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.NodeIntersectionContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.NodeSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.NodeTermContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.OneAppSpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.RoutingPolicyIntersectionContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.RoutingPolicySpecContext;
import org.batfish.specifier.parboiled.grammar.SpecifierParser.RoutingPolicyTermContext;

/**
 * Builds the specifier {@link AstNode} tree from an ANTLR parse tree. Replaces the parboiled value
 * stack: each grammar rule that pushed a node in the parboiled parser has a corresponding {@code
 * build...} method here that assembles the same node from child contexts.
 */
final class SpecifierAstBuilder {

  private SpecifierAstBuilder() {}

  /** Parses {@code input} under {@code grammar} and returns the resulting AST. */
  static AstNode getAst(Grammar grammar, String input) {
    SpecifierParser parser = getParser(grammar, input);
    return switch (grammar) {
      case APPLICATION_SPECIFIER -> buildAppSpec(parser.appSpecInput().appSpec());
      case FILTER_SPECIFIER -> buildFilterSpec(parser.filterSpecInput().filterSpec());
      case INTERFACE_SPECIFIER -> buildInterfaceSpec(parser.interfaceSpecInput().interfaceSpec());
      case IP_PROTOCOL_SPECIFIER ->
          buildIpProtocolSpec(parser.ipProtocolSpecInput().ipProtocolSpec());
      case IP_SPACE_SPECIFIER -> buildIpSpaceSpec(parser.ipSpaceSpecInput().ipSpaceSpec());
      case LOCATION_SPECIFIER -> buildLocationSpec(parser.locationSpecInput().locationSpec());
      case NODE_SPECIFIER -> buildNodeSpec(parser.nodeSpecInput().nodeSpec());
      case ROUTING_POLICY_SPECIFIER ->
          buildRoutingPolicySpec(parser.routingPolicySpecInput().routingPolicySpec());
      case SINGLE_APPLICATION_SPECIFIER -> buildOneAppSpec(parser.oneAppSpecInput().oneAppSpec());
      case MLAG_ID_SPECIFIER -> buildNameSetSpec(parser.nameSetSpecInput().nameSetSpec());
      default -> buildEnumSetSpec(parser.enumSetSpecInput().enumSetSpec(), grammar);
    };
  }

  static SpecifierParser getParser(Grammar grammar, String input) {
    SpecifierLexer lexer = newLexer(grammar, input);
    return new SpecifierParser(new CommonTokenStream(lexer));
  }

  /** Creates a lexer configured for {@code grammar} (slash-in-names and app-keyword modes). */
  static SpecifierLexer newLexer(Grammar grammar, String input) {
    SpecifierLexer lexer = new SpecifierLexer(CharStreams.fromString(input));
    lexer.slashInNames = slashInNames(grammar);
    lexer.appKeywords = appKeywords(grammar);
    return lexer;
  }

  /** Whether icmp/tcp/udp are keyword tokens: only in the application specifiers. */
  static boolean appKeywords(Grammar grammar) {
    return grammar == Grammar.APPLICATION_SPECIFIER
        || grammar == Grammar.SINGLE_APPLICATION_SPECIFIER;
  }

  /**
   * Whether '/' is part of a name for this grammar. False for grammars where '/' is structural
   * (application and ip-protocol specs); true otherwise.
   */
  static boolean slashInNames(Grammar grammar) {
    return switch (grammar) {
      case APPLICATION_SPECIFIER, SINGLE_APPLICATION_SPECIFIER, IP_PROTOCOL_SPECIFIER -> false;
      default -> true;
    };
  }

  // ---- Shared leaves ----

  /** Returns the string value of a name context, stripping the surrounding quotes if quoted. */
  private static String nameText(NameContext ctx) {
    TerminalNode quoted = ctx.QUOTED_NAME();
    if (quoted != null) {
      // The parboiled NameLiteral quoted alternative pushed match() of the interior (between the
      // two EscapeChars), keeping any escaped quotes (\") verbatim. Strip only the outer quotes.
      String text = quoted.getText();
      return text.substring(1, text.length() - 1);
    }
    return ctx.NAME().getText();
  }

  private static RegexAstNode regex(TerminalNode regexToken) {
    // REGEX token text includes the surrounding slashes; the parboiled Regex() rule pushed only the
    // interior (via match() between the '/'s) and did not unescape \/. Strip the delimiters.
    String text = regexToken.getText();
    return new RegexAstNode(text.substring(1, text.length() - 1));
  }

  private static RegexAstNode deprecatedRegex(SpecifierParser.DeprecatedRegexContext ctx) {
    return new RegexAstNode(ctx.DEPRECATED_REGEX().getText());
  }

  /** The set-operator character for a union/difference step: ',' if comma is present, else '\'. */
  private static char setOp(TerminalNode comma) {
    return comma != null ? ',' : '\\';
  }

  // ---- Node ----

  static NodeAstNode buildNodeSpec(NodeSpecContext ctx) {
    NodeAstNode result = buildNodeIntersection(ctx.nodeIntersection(0));
    List<NodeIntersectionContext> rest = ctx.nodeIntersection();
    for (int i = 1; i < rest.size(); i++) {
      char op = setOp(ctx.COMMA(i - 1));
      result =
          (NodeAstNode) SetOpNodeAstNode.create(op, result, buildNodeIntersection(rest.get(i)));
    }
    return result;
  }

  private static NodeAstNode buildNodeIntersection(NodeIntersectionContext ctx) {
    NodeAstNode result = buildNodeTerm(ctx.nodeTerm(0));
    List<NodeTermContext> terms = ctx.nodeTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new IntersectionNodeAstNode(result, buildNodeTerm(terms.get(i)));
    }
    return result;
  }

  private static NodeAstNode buildNodeTerm(NodeTermContext ctx) {
    if (ctx.nodeRole() != null) {
      var r = ctx.nodeRole().nodeRoleAndDimension();
      String dim = nameText(r.nodeRoleDimensionName().name());
      String role = nameText(r.nodeRoleAndDimensionTail().nodeRoleName().name());
      return new RoleNodeAstNode(new StringAstNode(dim), new StringAstNode(role));
    }
    if (ctx.nodeType() != null) {
      return new TypeNodeAstNode(new StringAstNode(ctx.nodeType().nodeTypeSpec().NAME().getText()));
    }
    if (ctx.nodeNameRegexDeprecated() != null) {
      return new NameRegexNodeAstNode(
          deprecatedRegex(ctx.nodeNameRegexDeprecated().deprecatedRegex()));
    }
    if (ctx.nodeNameRegex() != null) {
      return new NameRegexNodeAstNode(regex(ctx.nodeNameRegex().REGEX()));
    }
    if (ctx.nodeName() != null) {
      return new NameNodeAstNode(new StringAstNode(nameText(ctx.nodeName().name())));
    }
    return buildNodeSpec(ctx.nodeParens().nodeSpec());
  }

  // ---- Interface ----

  static InterfaceAstNode buildInterfaceSpec(InterfaceSpecContext ctx) {
    InterfaceAstNode result = buildInterfaceIntersection(ctx.interfaceIntersection(0));
    List<InterfaceIntersectionContext> rest = ctx.interfaceIntersection();
    for (int i = 1; i < rest.size(); i++) {
      char op = setOp(ctx.COMMA(i - 1));
      result =
          (InterfaceAstNode)
              SetOpInterfaceAstNode.create(op, result, buildInterfaceIntersection(rest.get(i)));
    }
    return result;
  }

  private static InterfaceAstNode buildInterfaceIntersection(InterfaceIntersectionContext ctx) {
    InterfaceAstNode result = buildInterfaceTerm(ctx.interfaceTerm(0));
    List<InterfaceTermContext> terms = ctx.interfaceTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new IntersectionInterfaceAstNode(result, buildInterfaceTerm(terms.get(i)));
    }
    return result;
  }

  private static InterfaceAstNode buildInterfaceTerm(InterfaceTermContext ctx) {
    if (ctx.interfaceWithNode() != null) {
      InterfaceWithNodeContext c = ctx.interfaceWithNode();
      NodeAstNode node = buildNodeTerm(c.nodeTerm());
      InterfaceAstNode iface =
          buildInterfaceWithoutNode(c.interfaceWithNodeTail().interfaceWithoutNode());
      return new InterfaceWithNodeInterfaceAstNode(node, iface);
    }
    if (ctx.interfaceWithoutNode() != null) {
      return buildInterfaceWithoutNode(ctx.interfaceWithoutNode());
    }
    return buildInterfaceSpec(ctx.interfaceParens().interfaceSpec());
  }

  private static InterfaceAstNode buildInterfaceWithoutNode(InterfaceWithoutNodeContext ctx) {
    InterfaceAstNode result =
        buildInterfaceWithoutNodeIntersection(ctx.interfaceWithoutNodeIntersection(0));
    List<InterfaceWithoutNodeIntersectionContext> rest = ctx.interfaceWithoutNodeIntersection();
    for (int i = 1; i < rest.size(); i++) {
      char op = setOp(ctx.COMMA(i - 1));
      result =
          (InterfaceAstNode)
              SetOpInterfaceAstNode.create(
                  op, result, buildInterfaceWithoutNodeIntersection(rest.get(i)));
    }
    return result;
  }

  private static InterfaceAstNode buildInterfaceWithoutNodeIntersection(
      InterfaceWithoutNodeIntersectionContext ctx) {
    InterfaceAstNode result = buildInterfaceWithoutNodeTerm(ctx.interfaceWithoutNodeTerm(0));
    List<InterfaceWithoutNodeTermContext> terms = ctx.interfaceWithoutNodeTerm();
    for (int i = 1; i < terms.size(); i++) {
      result =
          new IntersectionInterfaceAstNode(result, buildInterfaceWithoutNodeTerm(terms.get(i)));
    }
    return result;
  }

  private static InterfaceAstNode buildInterfaceWithoutNodeTerm(
      InterfaceWithoutNodeTermContext ctx) {
    if (ctx.interfaceFunc() != null) {
      return buildInterfaceFunc(ctx.interfaceFunc());
    }
    if (ctx.interfaceNameRegexDeprecated() != null) {
      return new NameRegexInterfaceAstNode(
          deprecatedRegex(ctx.interfaceNameRegexDeprecated().deprecatedRegex()));
    }
    if (ctx.interfaceNameRegex() != null) {
      return new NameRegexInterfaceAstNode(regex(ctx.interfaceNameRegex().REGEX()));
    }
    if (ctx.interfaceName() != null) {
      return new NameInterfaceAstNode(new StringAstNode(nameText(ctx.interfaceName().name())));
    }
    return buildInterfaceWithoutNode(ctx.interfaceWithoutNodeParens().interfaceWithoutNode());
  }

  private static InterfaceAstNode buildInterfaceFunc(InterfaceFuncContext ctx) {
    if (ctx.interfaceConnectedTo() != null) {
      return new ConnectedToInterfaceAstNode(
          buildIpSpaceSpec(ctx.interfaceConnectedTo().ipSpaceSpec()));
    }
    if (ctx.interfaceInterfaceGroup() != null) {
      var g = ctx.interfaceInterfaceGroup().interfaceGroupAndReferenceBook();
      String book = nameText(g.referenceBook().name());
      String group = nameText(g.interfaceGroupAndReferenceBookTail().interfaceGroup().name());
      return new InterfaceGroupInterfaceAstNode(new StringAstNode(book), new StringAstNode(group));
    }
    if (ctx.interfaceType() != null) {
      return new TypeInterfaceAstNode(
          new StringAstNode(ctx.interfaceType().interfaceTypeSpec().NAME().getText()));
    }
    if (ctx.interfaceVrf() != null) {
      return new VrfInterfaceAstNode(
          new StringAstNode(nameText(ctx.interfaceVrf().vrfName().name())));
    }
    return new ZoneInterfaceAstNode(
        new StringAstNode(nameText(ctx.interfaceZone().zoneName().name())));
  }

  // ---- Filter ----

  static FilterAstNode buildFilterSpec(FilterSpecContext ctx) {
    FilterAstNode result = buildFilterIntersection(ctx.filterIntersection(0));
    List<FilterIntersectionContext> rest = ctx.filterIntersection();
    for (int i = 1; i < rest.size(); i++) {
      char op = setOp(ctx.COMMA(i - 1));
      result =
          (FilterAstNode)
              SetOpFilterAstNode.create(op, result, buildFilterIntersection(rest.get(i)));
    }
    return result;
  }

  private static FilterAstNode buildFilterIntersection(FilterIntersectionContext ctx) {
    FilterAstNode result = buildFilterTerm(ctx.filterTerm(0));
    List<FilterTermContext> terms = ctx.filterTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new IntersectionFilterAstNode(result, buildFilterTerm(terms.get(i)));
    }
    return result;
  }

  private static FilterAstNode buildFilterTerm(FilterTermContext ctx) {
    if (ctx.filterWithNode() != null) {
      FilterWithNodeContext c = ctx.filterWithNode();
      NodeAstNode node = buildNodeTerm(c.nodeTerm());
      FilterAstNode filter = buildFilterWithoutNode(c.filterWithNodeTail().filterWithoutNode());
      return new FilterWithNodeFilterAstNode(node, filter);
    }
    if (ctx.filterWithoutNode() != null) {
      return buildFilterWithoutNode(ctx.filterWithoutNode());
    }
    return buildFilterSpec(ctx.filterParens().filterSpec());
  }

  private static FilterAstNode buildFilterWithoutNode(FilterWithoutNodeContext ctx) {
    FilterAstNode result = buildFilterWithoutNodeIntersection(ctx.filterWithoutNodeIntersection(0));
    List<FilterWithoutNodeIntersectionContext> rest = ctx.filterWithoutNodeIntersection();
    for (int i = 1; i < rest.size(); i++) {
      char op = setOp(ctx.COMMA(i - 1));
      result =
          (FilterAstNode)
              SetOpFilterAstNode.create(
                  op, result, buildFilterWithoutNodeIntersection(rest.get(i)));
    }
    return result;
  }

  private static FilterAstNode buildFilterWithoutNodeIntersection(
      FilterWithoutNodeIntersectionContext ctx) {
    FilterAstNode result = buildFilterWithoutNodeTerm(ctx.filterWithoutNodeTerm(0));
    List<FilterWithoutNodeTermContext> terms = ctx.filterWithoutNodeTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new IntersectionFilterAstNode(result, buildFilterWithoutNodeTerm(terms.get(i)));
    }
    return result;
  }

  private static FilterAstNode buildFilterWithoutNodeTerm(FilterWithoutNodeTermContext ctx) {
    if (ctx.filterInterfaceIn() != null) {
      return new InFilterAstNode(buildInterfaceSpec(ctx.filterInterfaceIn().interfaceSpec()));
    }
    if (ctx.filterInterfaceOut() != null) {
      return new OutFilterAstNode(buildInterfaceSpec(ctx.filterInterfaceOut().interfaceSpec()));
    }
    if (ctx.filterNameRegexDeprecated() != null) {
      return new NameRegexFilterAstNode(
          deprecatedRegex(ctx.filterNameRegexDeprecated().deprecatedRegex()));
    }
    if (ctx.filterNameRegex() != null) {
      return new NameRegexFilterAstNode(regex(ctx.filterNameRegex().REGEX()));
    }
    if (ctx.filterName() != null) {
      return new NameFilterAstNode(new StringAstNode(nameText(ctx.filterName().name())));
    }
    return buildFilterWithoutNode(ctx.filterWithoutNodeParens().filterWithoutNode());
  }

  // ---- Ip space ----

  static IpSpaceAstNode buildIpSpaceSpec(IpSpaceSpecContext ctx) {
    IpSpaceAstNode result = buildIpSpaceIntersection(ctx.ipSpaceIntersection(0));
    List<IpSpaceIntersectionContext> rest = ctx.ipSpaceIntersection();
    for (int i = 1; i < rest.size(); i++) {
      char op = setOp(ctx.COMMA(i - 1));
      result =
          (IpSpaceAstNode)
              SetOpIpSpaceAstNode.create(op, result, buildIpSpaceIntersection(rest.get(i)));
    }
    return result;
  }

  private static IpSpaceAstNode buildIpSpaceIntersection(IpSpaceIntersectionContext ctx) {
    IpSpaceAstNode result = buildIpSpaceTerm(ctx.ipSpaceTerm(0));
    List<IpSpaceTermContext> terms = ctx.ipSpaceTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new IntersectionIpSpaceAstNode(result, buildIpSpaceTerm(terms.get(i)));
    }
    return result;
  }

  private static IpSpaceAstNode buildIpSpaceTerm(IpSpaceTermContext ctx) {
    if (ctx.ipPrefix() != null) {
      return new PrefixAstNode(ctx.ipPrefix().IP_PREFIX().getText());
    }
    if (ctx.ipWildcard() != null) {
      return new IpWildcardAstNode(
          new IpAstNode(ctx.ipWildcard().ipAddress().IP_ADDRESS().getText()),
          new IpAstNode(ctx.ipWildcard().ipAddressMask().IP_ADDRESS().getText()));
    }
    if (ctx.ipRange() != null) {
      return new IpRangeAstNode(
          new IpAstNode(ctx.ipRange().ipAddress(0).IP_ADDRESS().getText()),
          new IpAstNode(ctx.ipRange().ipAddress(1).IP_ADDRESS().getText()));
    }
    if (ctx.ipAddress() != null) {
      return new IpAstNode(ctx.ipAddress().IP_ADDRESS().getText());
    }
    if (ctx.ipSpaceAddressGroup() != null) {
      AddressGroupAndReferenceBookContext g =
          ctx.ipSpaceAddressGroup().addressGroupAndReferenceBook();
      String book = nameText(g.referenceBook().name());
      String group = nameText(g.addressGroupAndReferenceBookTail().addressGroup().name());
      return new AddressGroupIpSpaceAstNode(new StringAstNode(book), new StringAstNode(group));
    }
    if (ctx.ipSpaceLocation() != null) {
      return new LocationIpSpaceAstNode(buildLocationSpec(ctx.ipSpaceLocation().locationSpec()));
    }
    return buildIpSpaceSpec(ctx.ipSpaceParens().ipSpaceSpec());
  }

  // ---- Location ----

  static LocationAstNode buildLocationSpec(LocationSpecContext ctx) {
    LocationAstNode result = buildLocationIntersection(ctx.locationIntersection(0));
    List<LocationIntersectionContext> rest = ctx.locationIntersection();
    for (int i = 1; i < rest.size(); i++) {
      char op = setOp(ctx.COMMA(i - 1));
      result =
          (LocationAstNode)
              SetOpLocationAstNode.create(op, result, buildLocationIntersection(rest.get(i)));
    }
    return result;
  }

  private static LocationAstNode buildLocationIntersection(LocationIntersectionContext ctx) {
    LocationAstNode result = buildLocationTerm(ctx.locationTerm(0));
    List<LocationTermContext> terms = ctx.locationTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new IntersectionLocationAstNode(result, buildLocationTerm(terms.get(i)));
    }
    return result;
  }

  private static LocationAstNode buildLocationTerm(LocationTermContext ctx) {
    if (ctx.locationInternet() != null) {
      SpecifierParser.LocationInternetContext c = ctx.locationInternet();
      if (c.interfaceWithNodeTail() != null) {
        InterfaceAstNode iface =
            buildInterfaceWithoutNode(c.interfaceWithNodeTail().interfaceWithoutNode());
        return InterfaceLocationAstNode.createFromInterfaceWithNode(
            new InterfaceWithNodeInterfaceAstNode(
                new NameNodeAstNode(IspModelingUtils.INTERNET_HOST_NAME), iface));
      }
      return InternetLocationAstNode.INSTANCE;
    }
    if (ctx.locationEnter() != null) {
      return new EnterLocationAstNode(
          buildLocationInterface(ctx.locationEnter().locationInterface()));
    }
    if (ctx.locationInterface() != null) {
      return buildLocationInterface(ctx.locationInterface());
    }
    return buildLocationSpec(ctx.locationParens().locationSpec());
  }

  private static LocationAstNode buildLocationInterface(LocationInterfaceContext ctx) {
    if (ctx.interfaceWithNode() != null) {
      InterfaceWithNodeContext c = ctx.interfaceWithNode();
      NodeAstNode node = buildNodeTerm(c.nodeTerm());
      InterfaceAstNode iface =
          buildInterfaceWithoutNode(c.interfaceWithNodeTail().interfaceWithoutNode());
      return InterfaceLocationAstNode.createFromInterfaceWithNode(
          new InterfaceWithNodeInterfaceAstNode(node, iface));
    }
    if (ctx.nodeTerm() != null) {
      return InterfaceLocationAstNode.createFromNode(buildNodeTerm(ctx.nodeTerm()));
    }
    return InterfaceLocationAstNode.createFromInterface(buildInterfaceFunc(ctx.interfaceFunc()));
  }

  // ---- Routing policy ----

  static RoutingPolicyAstNode buildRoutingPolicySpec(RoutingPolicySpecContext ctx) {
    RoutingPolicyAstNode result = buildRoutingPolicyIntersection(ctx.routingPolicyIntersection(0));
    List<RoutingPolicyIntersectionContext> rest = ctx.routingPolicyIntersection();
    for (int i = 1; i < rest.size(); i++) {
      char op = setOp(ctx.COMMA(i - 1));
      result =
          (RoutingPolicyAstNode)
              SetOpRoutingPolicyAstNode.create(
                  op, result, buildRoutingPolicyIntersection(rest.get(i)));
    }
    return result;
  }

  private static RoutingPolicyAstNode buildRoutingPolicyIntersection(
      RoutingPolicyIntersectionContext ctx) {
    RoutingPolicyAstNode result = buildRoutingPolicyTerm(ctx.routingPolicyTerm(0));
    List<RoutingPolicyTermContext> terms = ctx.routingPolicyTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new IntersectionRoutingPolicyAstNode(result, buildRoutingPolicyTerm(terms.get(i)));
    }
    return result;
  }

  private static RoutingPolicyAstNode buildRoutingPolicyTerm(RoutingPolicyTermContext ctx) {
    if (ctx.routingPolicyNameRegexDeprecated() != null) {
      return new NameRegexRoutingPolicyAstNode(
          deprecatedRegex(ctx.routingPolicyNameRegexDeprecated().deprecatedRegex()));
    }
    if (ctx.routingPolicyNameRegex() != null) {
      return new NameRegexRoutingPolicyAstNode(regex(ctx.routingPolicyNameRegex().REGEX()));
    }
    if (ctx.routingPolicyName() != null) {
      return new NameRoutingPolicyAstNode(
          new StringAstNode(nameText(ctx.routingPolicyName().name())));
    }
    return buildRoutingPolicySpec(ctx.routingPolicyParens().routingPolicySpec());
  }

  // ---- Ip protocol ----

  static IpProtocolAstNode buildIpProtocolSpec(IpProtocolSpecContext ctx) {
    IpProtocolAstNode result = buildIpProtocolTerm(ctx.ipProtocolTerm(0));
    List<IpProtocolTermContext> terms = ctx.ipProtocolTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new UnionIpProtocolAstNode(result, buildIpProtocolTerm(terms.get(i)));
    }
    return result;
  }

  private static IpProtocolAstNode buildIpProtocolTerm(IpProtocolTermContext ctx) {
    if (ctx.ipProtocol() != null) {
      return buildIpProtocol(ctx.ipProtocol());
    }
    return new NotIpProtocolAstNode(buildIpProtocol(ctx.ipProtocolNot().ipProtocol()));
  }

  private static IpProtocolAstNode buildIpProtocol(IpProtocolContext ctx) {
    if (ctx.ipProtocolName() != null) {
      return new IpProtocolIpProtocolAstNode(ctx.ipProtocolName().NAME().getText());
    }
    return new IpProtocolIpProtocolAstNode(ctx.ipProtocolNumber().NUM().getText());
  }

  // ---- Application (set) ----

  static AppAstNode buildAppSpec(AppSpecContext ctx) {
    AppAstNode result = buildAppTerm(ctx.appTerm(0));
    List<AppTermContext> terms = ctx.appTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new UnionAppAstNode(result, buildAppTerm(terms.get(i)));
    }
    return result;
  }

  private static AppAstNode buildAppTerm(AppTermContext ctx) {
    if (ctx.appName() != null) {
      return new NameAppAstNode(ctx.appName().NAME().getText());
    }
    if (ctx.appNameRegex() != null) {
      return new RegexAppAstNode(regex(ctx.appNameRegex().REGEX()));
    }
    if (ctx.appIcmpTerm() != null) {
      return buildAppIcmpTerm(ctx.appIcmpTerm());
    }
    if (ctx.appTcpTerm() != null) {
      AppAstNode tcp = new TcpAppAstNode();
      AppPortSpecContext ports = ctx.appTcpTerm().appPortSpec();
      return ports == null ? tcp : applyPorts(tcp, ports);
    }
    AppAstNode udp = new UdpAppAstNode();
    AppPortSpecContext ports = ctx.appUdpTerm().appPortSpec();
    return ports == null ? udp : applyPorts(udp, ports);
  }

  private static AppAstNode buildAppIcmpTerm(AppIcmpTermContext ctx) {
    AppAstNode result = new IcmpAllAppAstNode();
    if (ctx.appIcmpType() != null) {
      int type = Integer.parseInt(ctx.appIcmpType().NUM().getText());
      AppAstNode typeNode = new IcmpTypeAppAstNode(type);
      if (ctx.appIcmpType().appIcmpTypeCode() != null) {
        int code = Integer.parseInt(ctx.appIcmpType().appIcmpTypeCode().NUM().getText());
        return (AppAstNode) IcmpTypeCodeAppAstNode.create(typeNode, code);
      }
      return typeNode;
    }
    return result;
  }

  private static AppAstNode applyPorts(AppAstNode base, AppPortSpecContext ports) {
    AppAstNode result = base;
    for (AppPortTermContext term : ports.appPortTerm()) {
      if (term.appPortRange() != null) {
        result =
            PortAppAstNode.createFrom(
                result, term.appPortRange().NUM(0).getText(), term.appPortRange().NUM(1).getText());
      } else {
        result = PortAppAstNode.createFrom(result, term.appPort().NUM().getText());
      }
    }
    return result;
  }

  // ---- Single application ----

  static AppAstNode buildOneAppSpec(OneAppSpecContext ctx) {
    if (ctx.appName() != null) {
      return new NameAppAstNode(ctx.appName().NAME().getText());
    }
    if (ctx.oneAppIcmp() != null) {
      int type = Integer.parseInt(ctx.oneAppIcmp().NUM().getText());
      AppAstNode typeNode = new IcmpTypeAppAstNode(type);
      int code = Integer.parseInt(ctx.oneAppIcmp().oneAppIcmpType().NUM().getText());
      return (AppAstNode) IcmpTypeCodeAppAstNode.create(typeNode, code);
    }
    if (ctx.oneAppTcp() != null) {
      return PortAppAstNode.createFrom(
          new TcpAppAstNode(), ctx.oneAppTcp().appPort().NUM().getText());
    }
    return PortAppAstNode.createFrom(
        new UdpAppAstNode(), ctx.oneAppUdp().appPort().NUM().getText());
  }

  // ---- Name set ----

  static NameSetAstNode buildNameSetSpec(NameSetSpecContext ctx) {
    NameSetAstNode result = buildNameSetTerm(ctx.nameSetTerm(0));
    List<NameSetTermContext> terms = ctx.nameSetTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new UnionNameSetAstNode(result, buildNameSetTerm(terms.get(i)));
    }
    return result;
  }

  private static NameSetAstNode buildNameSetTerm(NameSetTermContext ctx) {
    if (ctx.nameSetRegexDeprecated() != null) {
      return new RegexNameSetAstNode(
          deprecatedRegex(ctx.nameSetRegexDeprecated().deprecatedRegex()));
    }
    if (ctx.nameSetRegex() != null) {
      return new RegexNameSetAstNode(regex(ctx.nameSetRegex().REGEX()));
    }
    return new SingletonNameSetAstNode(new StringAstNode(nameText(ctx.nameSetName().name())));
  }

  // ---- Enum set ----

  static EnumSetAstNode buildEnumSetSpec(EnumSetSpecContext ctx, Grammar grammar) {
    java.util.Collection<?> values = Grammar.getEnumValues(grammar);
    EnumSetAstNode result = buildEnumSetTerm(ctx.enumSetTerm(0), values);
    List<EnumSetTermContext> terms = ctx.enumSetTerm();
    for (int i = 1; i < terms.size(); i++) {
      result = new UnionEnumSetAstNode(result, buildEnumSetTerm(terms.get(i), values));
    }
    return result;
  }

  private static EnumSetAstNode buildEnumSetTerm(
      EnumSetTermContext ctx, java.util.Collection<?> values) {
    if (ctx.enumSetBase() != null) {
      return buildEnumSetBase(ctx.enumSetBase(), values);
    }
    return new NotEnumSetAstNode(buildEnumSetBase(ctx.enumSetNotTerm().enumSetBase(), values));
  }

  private static EnumSetAstNode buildEnumSetBase(
      EnumSetBaseContext ctx, java.util.Collection<?> values) {
    if (ctx.enumSetRegexDeprecated() != null) {
      return new RegexEnumSetAstNode(
          deprecatedRegex(ctx.enumSetRegexDeprecated().deprecatedRegex()));
    }
    if (ctx.enumSetRegex() != null) {
      return new RegexEnumSetAstNode(regex(ctx.enumSetRegex().REGEX()));
    }
    return new ValueEnumSetAstNode<>(ctx.enumSetValue().NAME().getText(), values);
  }
}
