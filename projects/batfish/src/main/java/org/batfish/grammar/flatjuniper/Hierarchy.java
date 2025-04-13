package org.batfish.grammar.flatjuniper;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.grammar.flatjuniper.ConfigurationBuilder.unquote;
import static org.batfish.grammar.flatjuniper.JuniperListPaths.getJuniperListPaths;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.batfish.common.BatfishException;
import org.batfish.common.util.PatternProvider;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Activate_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.StatementContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_host_nameContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyChildNode;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyNode;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import org.batfish.representation.juniper.GroupWildcard;

@ParametersAreNonnullByDefault
final class Hierarchy {

  /** Dump all set lines and error nodes from the main tree. */
  @Nonnull
  List<ParseTree> extractParseTrees() {
    ImmutableList.Builder<ParseTree> builder =
        ImmutableList.builderWithExpectedSize(countParseTrees());
    _masterTree._root.dumpParseTrees(builder);
    return builder.build();
  }

  /** Returns the number of parse trees that would be dumped by {@link #extractParseTrees()}. */
  private int countParseTrees() {
    return _masterTree._root.countParseTrees();
  }

  /**
   * Merge hierarchy from the group trees into the main tree where main tree nodes have been
   * annotated with {@code apply-groups}. Returns {@code true} iff the hierarchy was modified.
   */
  boolean inheritGroups(Flat_juniper_configurationContext ctx) {
    HierarchyPath globalPath = new HierarchyPath();
    return inheritGroups(ctx, _masterTree._root, globalPath, ImmutableList.of(), ImmutableSet.of());
  }

  /**
   * Insert new hierarchy and configuration lines for each applied group into the main tree so that:
   *
   * <ol>
   *   <li>Inherited lines resulting from the first applied group come after lines from subsequent
   *       applied groups at the same level of hierarchy. This conforms to priority indicated in <a
   *       href="https://www.juniper.net/documentation/us/en/software/junos/bgp/topics/ref/statement/apply-groups.html#apply-groups__d65612e42">Juniper
   *       apply-groups documentation</a>
   *   <li>Ordinary non-inherited lines take precedence over group lines. This means that list-like
   *       values from groups come after non-inherited list-like values, while non-list-like values
   *       from groups come before non-inherited non-list-like values.
   * </ol>
   *
   * Returns {@code true} iff the hierarchy was modified.
   */
  private boolean inheritGroups(
      Flat_juniper_configurationContext ctx,
      HierarchyNode inheritorNode,
      HierarchyPath inheritorNodePath,
      List<String> ancestralPrioritizedGroups,
      Set<String> ancestralExceptGroups) {
    List<String> prioritizedGroups =
        inheritorNode.prependPrioritizedGroups(ancestralPrioritizedGroups);
    Set<String> exceptGroups =
        inheritorNode._exceptGroups.isEmpty()
            ? ancestralExceptGroups
            : ImmutableSet.<String>builder()
                .addAll(inheritorNode._exceptGroups)
                .addAll(ancestralExceptGroups)
                .build();
    boolean isListNode = IS_LIST_PATH_TREE.isListPath(inheritorNodePath);
    // Inherit just the immediate children that should be added to this node.
    boolean modified =
        isListNode
            ? inheritGroupsIntoListNode(
                ctx, inheritorNode, inheritorNodePath, prioritizedGroups, exceptGroups)
            : inheritGroupsIntoNonListNode(
                ctx, inheritorNode, inheritorNodePath, prioritizedGroups, exceptGroups);
    // Now that this level is done, recursively inherit at all of this node's children.
    for (HierarchyChildNode child : inheritorNode._children.values()) {
      inheritorNodePath._nodes.add(child);
      modified =
          inheritGroups(ctx, child, inheritorNodePath, prioritizedGroups, exceptGroups) || modified;
      inheritorNodePath._nodes.remove(inheritorNodePath._nodes.size() - 1);
    }
    return modified;
  }

  /**
   * Inherit groups applied at this node whose children are either a scalar value, or whose order
   * does not matter. Returns {@code true} iff the hierarchy was modified.
   */
  private boolean inheritGroupsIntoNonListNode(
      Flat_juniper_configurationContext ctx,
      HierarchyNode inheritorNode,
      HierarchyPath inheritorNodePath,
      List<String> prioritizedGroups,
      Set<String> exceptGroups) {
    // save old children and lines of old children
    Map<String, HierarchyChildNode> oldChildren = inheritorNode._children;
    Map<String, ParseTree> oldChildrenLines = new HashMap<>();
    for (Entry<String, HierarchyChildNode> oldChildEntry : oldChildren.entrySet()) {
      String key = oldChildEntry.getKey();
      HierarchyChildNode oldChild = oldChildEntry.getValue();
      oldChildrenLines.put(key, oldChild._line);
    }

    // clear children and inherit from groups
    inheritorNode.resetChildren();
    for (String group : Lists.reverse(prioritizedGroups)) {
      HierarchyNode groupNode = findApplicableGroupNode(inheritorNodePath, group, exceptGroups);
      if (groupNode == null) {
        continue;
      }
      for (Entry<String, HierarchyChildNode> groupNodeChildEntry : groupNode._children.entrySet()) {
        inheritGroupNodeChildIntoNonListNode(
            ctx, inheritorNode, inheritorNodePath, group, groupNodeChildEntry, oldChildren);
      }
    }

    // re-apply old main tree children and lines (where present) on top of inherited children
    reapplyMainTreeNonListValues(inheritorNode, oldChildren);

    // record new children lines for comparison
    Map<String, ParseTree> newChildrenLines = new HashMap<>();
    for (Entry<String, HierarchyChildNode> newChildEntry : inheritorNode._children.entrySet()) {
      String key = newChildEntry.getKey();
      HierarchyChildNode newChild = newChildEntry.getValue();
      newChildrenLines.put(key, newChild._line);
    }
    // Tree is modified if there are new children or new lines in existing children; the latter may
    // occur if an inherited group terminal path was a non-terminal path in the main tree.
    return !oldChildren.keySet().equals(inheritorNode._children.keySet())
        || !oldChildrenLines.equals(newChildrenLines);
  }

  private static void reapplyMainTreeNonListValues(
      HierarchyNode inheritorNode, Map<String, HierarchyChildNode> preInheritanceChildren) {
    for (HierarchyChildNode childToReapply : preInheritanceChildren.values()) {
      HierarchyChildNode removed = inheritorNode.putLast(childToReapply);
      if (removed != null && removed._line != null && childToReapply._line == null) {
        childToReapply._line = removed._line;
      }
    }
  }

  /**
   * Inherit from a given group node's child into a main tree node such that an existing child in
   * the main tree with the same key will appear afterward. For a scalar-valued child, this means
   * the existing main tree child will have higher priority; for list nodes, this means the
   * inherited child will have higher priority. In that latter case, this may not be correct for
   * lists where order matters.
   *
   * <p>See <a href="https://github.com/batfish/batfish/issues/8818">GH8818</a>
   */
  private void inheritGroupNodeChildIntoNonListNode(
      Flat_juniper_configurationContext ctx,
      HierarchyNode inheritorNode,
      HierarchyPath inheritorNodePath,
      String group,
      Entry<String, HierarchyChildNode> groupNodeChildEntry,
      Map<String, HierarchyChildNode> oldChildren) {
    String key = groupNodeChildEntry.getKey();
    HierarchyChildNode groupChild = groupNodeChildEntry.getValue();
    if (groupChild.isWildcard()) {
      return;
    }
    HierarchyChildNode groupNodeToAdd = groupChild.copy();
    HierarchyChildNode removed = inheritorNode.putLast(groupNodeToAdd);
    HierarchyChildNode oldChild = oldChildren.get(key);
    // prefer old line, then previous line from this loop, then new line
    if (oldChild != null && oldChild._line != null) {
      groupNodeToAdd._line = oldChild._line;
    } else if (removed != null && removed._line != null) {
      groupNodeToAdd._line = removed._line;
    } else if (groupChild._line != null) {
      Set_lineContext newSetLine = inheritSetLineHelper(ctx, inheritorNodePath, groupNodeToAdd);
      if (isClusterGroup(group) && IsHostnameStatement.isHostnameStatement(newSetLine)) {
        return;
      }
      groupNodeToAdd._line = newSetLine;
    }
  }

  /**
   * Inherit groups applied at this node whose children are an ordered list. Returns {@code true}
   * iff the hierarchy was modified.
   */
  private boolean inheritGroupsIntoListNode(
      Flat_juniper_configurationContext ctx,
      HierarchyNode inheritorNode,
      HierarchyPath globalPath,
      List<String> prioritizedGroups,
      Set<String> exceptGroups) {
    boolean modified = false;
    for (String group : prioritizedGroups) {
      HierarchyNode groupNode = findApplicableGroupNode(globalPath, group, exceptGroups);
      if (groupNode == null) {
        continue;
      }
      for (HierarchyChildNode groupNodeChild : groupNode._children.values()) {
        modified =
            inheritGroupNodeChildIntoListNode(ctx, inheritorNode, globalPath, group, groupNodeChild)
                || modified;
      }
    }
    return modified;
  }

  /**
   * Inherit from a given group node's child into a main tree node whose children are an ordered
   * list. Returns {@code true} iff the hierarchy was modified.
   */
  private boolean inheritGroupNodeChildIntoListNode(
      Flat_juniper_configurationContext ctx,
      HierarchyNode inheritorNode,
      HierarchyPath inheritorNodePath,
      String group,
      HierarchyChildNode groupChild) {
    if (groupChild.isWildcard()) {
      return false;
    }
    HierarchyChildNode existing = inheritorNode._children.get(groupChild._unquotedText);
    if (existing != null && (existing._line != null || groupChild._line == null)) {
      // nothing to change
      return false;
    }
    HierarchyChildNode groupNodeToAdd = groupChild.copy();
    // either no existing child, or this group has a line to add that wasn't there before
    Set_lineContext newSetLine = inheritSetLineHelper(ctx, inheritorNodePath, groupNodeToAdd);
    if (isClusterGroup(group) && IsHostnameStatement.isHostnameStatement(newSetLine)) {
      return false;
    }
    inheritorNode.putLast(groupNodeToAdd);
    groupNodeToAdd._line = newSetLine;
    return true;
  }

  /**
   * Non-thread-safe helper for generating a set line in group inheritance code. Modifies supplied
   * {@link HierarchyPath} temporarily for efficiency.
   */
  private @Nonnull Set_lineContext inheritSetLineHelper(
      Flat_juniper_configurationContext ctx,
      HierarchyPath pathToPenultimateNode,
      HierarchyChildNode terminalNode) {
    pathToPenultimateNode._nodes.add(terminalNode);
    Set_lineContext newSetLine =
        _masterTree.generateSetLine(
            pathToPenultimateNode, ctx, terminalNode._lineNumber, _tokenInputs);
    pathToPenultimateNode._nodes.remove(pathToPenultimateNode._nodes.size() - 1);
    return newSetLine;
  }

  /**
   * Return the group node to apply during inheritance, or {code @null} if there is nothing to
   * inherit.
   */
  private @Nullable HierarchyNode findApplicableGroupNode(
      HierarchyPath inheritorNodePath, String group, Set<String> exceptGroups) {
    HierarchyTree groupTree = _trees.get(group);
    if (groupTree == null) {
      return null;
    }
    if (exceptGroups.contains(group)) {
      return null;
    }
    return groupTree.findFirstMatchPathNode(inheritorNodePath);
  }

  private boolean isClusterGroup(String group) {
    return group.equals("node0") || group.equals("node1");
  }

  private static class IsHostnameStatement extends FlatJuniperParserBaseListener {

    private boolean _isHostname;

    @Override
    public void enterSy_host_name(Sy_host_nameContext ctx) {
      _isHostname = true;
    }

    private static boolean isHostnameStatement(Set_lineContext ctx) {
      IsHostnameStatement listener = new IsHostnameStatement();
      // Use simple ParseTreeWalker since exception should not occur during walk
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(listener, ctx);
      return listener._isHostname;
    }
  }

  private static class TokenInputMarker extends FlatJuniperParserBaseListener {

    private final String _input;

    private final Map<Token, String> _tokenInputs;

    private TokenInputMarker(String input, Map<Token, String> tokenInputs) {
      _input = input;
      _tokenInputs = tokenInputs;
    }

    @Override
    public void visitTerminal(TerminalNode node) {
      _tokenInputs.put(node.getSymbol(), _input);
    }
  }

  static class HierarchyTree {

    /**
     * Return the node in this tree that matches the given path of literals, using the first
     * matching node in this tree at each depth.
     *
     * <p>TODO: Fix logic here and/or at caller to fix x-failed test {@code
     * FlatJuniperGrammarTest.testApplyGroupsWildcardNestingExtraction}.
     */
    private @Nullable HierarchyNode findFirstMatchPathNode(HierarchyPath literalPath) {
      HierarchyNode current = _root;
      for (HierarchyChildNode toMatchNext : literalPath._nodes) {
        current = current.getFirstMatchingChildNode(toMatchNext);
        if (current == null) {
          return null;
        }
      }
      return current;
    }

    private enum AddPathResult {
      EXCLUDED,
      MODIFIED,
      UNMODIFIED
    }

    // package-private for use by IsListPathTree
    abstract static class HierarchyChildNode extends HierarchyNode {

      /** Not copied by {@link #copy()} */
      private boolean _deactivated;

      private Set_lineContext _line;
      protected int _lineNumber;
      protected final @Nonnull String _text;
      protected final @Nonnull String _unquotedText;

      private HierarchyChildNode(String text, int lineNumber) {
        _text = text;
        _unquotedText = unquote(text).orElse(text);
        _lineNumber = lineNumber;
      }

      /**
       * Create a copy of this node. Does not copy non-transitive node attributes: {@link
       * #_deactivated}.
       */
      protected abstract HierarchyChildNode copy();

      @Override
      protected void dumpParseTrees(ImmutableList.Builder<ParseTree> builder) {
        if (_line != null) {
          builder.add(_line);
        }
        builder.addAll(_errorNodes);
        dumpChildParseTrees(builder);
      }

      @Override
      protected int countParseTrees() {
        return (_line != null ? 1 : 0) + _errorNodes.size() + countChildParseTrees();
      }

      protected abstract boolean isMatchedBy(HierarchyLiteralNode node);

      protected abstract boolean isMatchedBy(HierarchyWildcardNode node);

      protected abstract boolean matches(HierarchyChildNode node);

      private boolean getDeactivated() {
        return _deactivated;
      }

      private void setDeactivated(boolean deactivated) {
        _deactivated = deactivated;
      }
    }

    private static final class HierarchyLiteralNode extends HierarchyChildNode {

      private HierarchyLiteralNode(String text, int lineNumber) {
        super(text, lineNumber);
      }

      @Override
      protected HierarchyChildNode copy() {
        return new HierarchyLiteralNode(_text, _lineNumber);
      }

      @Override
      protected boolean isMatchedBy(HierarchyLiteralNode node) {
        return _unquotedText.equals(node._unquotedText);
      }

      @Override
      protected boolean isMatchedBy(HierarchyWildcardNode node) {
        return node.matches(_unquotedText);
      }

      @Override
      protected boolean matches(HierarchyChildNode node) {
        return node.isMatchedBy(this);
      }

      @Override
      public String toString() {
        return String.format("Literal(%s)", _text);
      }
    }

    abstract static class HierarchyNode {
      private final @Nonnull Set<String> _exceptGroups;
      private final @Nonnull Set<String> _appliedGroups;

      // Invariant: children == literal + wildcard, keys are disjoint in the latter two.
      private @Nonnull LinkedHashMap<String, HierarchyChildNode> _children;

      private @Nonnull LinkedHashMap<String, HierarchyLiteralNode> _literalChildren;

      private @Nonnull LinkedHashMap<String, HierarchyWildcardNode> _wildcardChildren;

      @Nonnull List<ErrorNode> _errorNodes;

      protected final @Nonnull List<String> prependPrioritizedGroups(
          List<String> ancestralAppliedGroups) {
        if (_appliedGroups.isEmpty()) {
          return ancestralAppliedGroups;
        }
        return ImmutableList.<String>builder()
            .addAll(_appliedGroups)
            .addAll(ancestralAppliedGroups)
            .build();
      }

      /** Dump parse trees recursively into {@code builder}. */
      protected abstract void dumpParseTrees(ImmutableList.Builder<ParseTree> builder);

      /**
       * Returns the number of parse trees that would be returned by {@link
       * #dumpParseTrees(Builder)}.
       */
      protected abstract int countParseTrees();

      /**
       * Dump child node parse trees, using prioritized list of groups where priority decreases with
       * increasing index
       */
      protected final void dumpChildParseTrees(ImmutableList.Builder<ParseTree> builder) {
        for (HierarchyChildNode child : _children.values()) {
          child.dumpParseTrees(builder);
        }
      }

      /**
       * Returns the number of parse tress that would be returned by {@link
       * #dumpChildParseTrees(Builder)}.
       */
      protected final int countChildParseTrees() {
        int count = 0;
        for (HierarchyChildNode child : _children.values()) {
          count += child.countParseTrees();
        }
        return count;
      }

      /**
       * Add a set line to {@code output} prefixed by {@code prefix} for each path from this node to
       * a leaf.
       */
      protected final void appendSetLines(@Nonnull String prefix, @Nonnull StringBuilder output) {
        if (_children.isEmpty()) {
          // leaf, so append set line
          output.append(prefix).append("\n");
          _errorNodes.forEach(errorNode -> output.append(errorNode.getText().trim()).append("\n"));
        }
        // append set lines for every path from child to leaf
        for (HierarchyChildNode child : _children.values()) {
          // NB: do not use _children.keys() / child._unquotedText in order to preserve quotes
          child.appendSetLines(String.format("%s %s", prefix, child._text), output);
        }
      }

      private HierarchyNode() {
        _children = new LinkedHashMap<>();
        _literalChildren = new LinkedHashMap<>();
        _wildcardChildren = new LinkedHashMap<>();
        _appliedGroups = new LinkedHashSet<>();
        _exceptGroups = new HashSet<>();
        _errorNodes = ImmutableList.of();
      }

      void resetChildren() {
        _children = new LinkedHashMap<>();
        _literalChildren = new LinkedHashMap<>();
        _wildcardChildren = new LinkedHashMap<>();
      }

      void addGroup(String groupName) {
        _appliedGroups.add(groupName);
      }

      void addExceptGroup(String groupName) {
        _exceptGroups.add(groupName);
      }

      private void addChildNode(HierarchyChildNode node) {
        HierarchyChildNode replaced = _children.put(node._unquotedText, node);
        addConsistently(node);
        if (replaced != null && replaced.getClass() != node.getClass()) {
          // weird case where the removed node was of a different type than the added node.
          // let's remove it from the wrong list.
          removeConsistently(replaced);
        }
      }

      private HierarchyChildNode getChildNode(String text) {
        return _children.get(text);
      }

      private Map<String, HierarchyChildNode> getChildren() {
        return Collections.unmodifiableMap(_children);
      }

      /**
       * Return the first child of this node that matches the given node, or {@code null} if no
       * child matches.
       */
      private @Nullable HierarchyChildNode getFirstMatchingChildNode(HierarchyChildNode node) {
        if (node instanceof HierarchyWildcardNode) {
          // A wildcard node can't be matched by a literal, and can only be matched by a wildcard of
          // identical text. See: {@link HierarchyWildcardNode#isMatchedBy}
          return _wildcardChildren.get(node._unquotedText);
        }
        assert node instanceof HierarchyLiteralNode;
        HierarchyLiteralNode literal = _literalChildren.get(node._unquotedText);
        if (literal != null) {
          // We found the literal that matches it.
          return literal;
        }

        // See if there's a wildcard that matches it.
        for (HierarchyWildcardNode child : _wildcardChildren.values()) {
          if (child.matches(node)) {
            return child;
          }
        }
        return null;
      }

      boolean isWildcard() {
        return false;
      }

      @Nullable
      HierarchyChildNode putLast(@Nonnull HierarchyChildNode node) {
        HierarchyChildNode replaced = _children.remove(node._unquotedText);
        removeConsistently(replaced);
        _children.put(node._unquotedText, node);
        addConsistently(node);
        return replaced;
      }

      /**
       * When a node is added to the {@link #_children} array, also update {@link #_literalChildren}
       * and {@link #_wildcardChildren}.
       */
      private void addConsistently(@Nullable HierarchyChildNode addedChild) {
        if (addedChild instanceof HierarchyLiteralNode) {
          _literalChildren.put(addedChild._unquotedText, (HierarchyLiteralNode) addedChild);
        } else if (addedChild instanceof HierarchyWildcardNode) {
          _wildcardChildren.put(addedChild._unquotedText, (HierarchyWildcardNode) addedChild);
        }
      }

      /**
       * When a node is removed from the {@link #_children} array, also update {@link
       * #_literalChildren} and {@link #_wildcardChildren}.
       */
      private void removeConsistently(@Nullable HierarchyChildNode removedChild) {
        if (removedChild instanceof HierarchyLiteralNode) {
          HierarchyLiteralNode alsoRemoved = _literalChildren.remove(removedChild._unquotedText);
          assert alsoRemoved != null;
        } else if (removedChild instanceof HierarchyWildcardNode) {
          HierarchyWildcardNode alsoRemoved = _wildcardChildren.remove(removedChild._unquotedText);
          assert alsoRemoved != null;
        }
      }
    }

    static final class HierarchyPath {

      private final List<HierarchyChildNode> _nodes;
      private StatementContext _statement;

      HierarchyPath() {
        _nodes = new ArrayList<>();
      }

      void addNode(String text, int lineNumber) {
        HierarchyChildNode newNode = new HierarchyLiteralNode(text, lineNumber);
        _nodes.add(newNode);
      }

      void addWildcardNode(String text, int lineNumber) {
        HierarchyChildNode newNode = new HierarchyWildcardNode(text, lineNumber);
        _nodes.add(newNode);
      }

      @Nonnull
      String pathString() {
        return _nodes.stream().map(node -> node._text).collect(Collectors.joining(" "));
      }

      void setStatement(StatementContext statement) {
        _statement = statement;
      }

      @Override
      public String toString() {
        return "Path(Statement:" + _statement + "," + _nodes + ")";
      }
    }

    private static final class HierarchyRootNode extends HierarchyNode {

      @Override
      protected void dumpParseTrees(ImmutableList.Builder<ParseTree> builder) {
        builder.addAll(_errorNodes);
        dumpChildParseTrees(builder);
      }

      @Override
      protected int countParseTrees() {
        return countChildParseTrees() + _errorNodes.size();
      }
    }

    private static final class HierarchyWildcardNode extends HierarchyChildNode {

      private final String _wildcard;
      private final Pattern _wildcardPattern;

      private HierarchyWildcardNode(String text, int lineNumber) {
        super(text, lineNumber);
        if (_unquotedText.charAt(0) != '<'
            || _unquotedText.charAt(_unquotedText.length() - 1) != '>') {
          throw new BatfishException("Improperly-formatted wildcard: " + text);
        }
        _wildcard = _unquotedText.substring(1, _unquotedText.length() - 1);
        _wildcardPattern = PatternProvider.fromString(GroupWildcard.toJavaRegex(_wildcard));
      }

      @Override
      protected HierarchyChildNode copy() {
        return new HierarchyWildcardNode(_text, _lineNumber);
      }

      @Override
      protected boolean isMatchedBy(HierarchyLiteralNode node) {
        return false;
      }

      @Override
      protected boolean isMatchedBy(HierarchyWildcardNode node) {
        // TODO: check whether this is the only way to match two wildcards
        return _unquotedText.equals(node._unquotedText);
      }

      @Override
      protected boolean isWildcard() {
        return true;
      }

      @Override
      protected boolean matches(HierarchyChildNode node) {
        return node.isMatchedBy(this);
      }

      public boolean matches(String text) {
        return !text.equals("apply-groups")
            && !text.equals("apply-path")
            && _wildcardPattern.matcher(text).matches();
      }

      @Override
      public String toString() {
        return "Wildcard(" + _text + ")";
      }
    }

    private static GrammarSettings parserSettings() {
      return new GrammarSettings() {
        @Override
        public boolean getDisableUnrecognized() {
          return false;
        }

        @Override
        public int getMaxParserContextLines() {
          return 0;
        }

        @Override
        public int getMaxParserContextTokens() {
          return 0;
        }

        @Override
        public int getMaxParseTreePrintLength() {
          return 0;
        }

        @Override
        public boolean getPrintParseTree() {
          return false;
        }

        @Override
        public boolean getPrintParseTreeLineNums() {
          return false;
        }

        @Override
        public boolean getThrowOnLexerError() {
          return true;
        }

        @Override
        public boolean getThrowOnParserError() {
          return true;
        }

        @Override
        public void setDisableUnrecognized(boolean disableUnrecognized) {}

        @Override
        public void setPrintParseTree(boolean printParseTree) {}

        @Override
        public void setPrintParseTreeLineNums(boolean printParseTreeLineNums) {}

        @Override
        public void setThrowOnLexerError(boolean throwOnLexerError) {}

        @Override
        public void setThrowOnParserError(boolean throwOnParserError) {}
      };
    }

    private final HierarchyRootNode _root;

    private HierarchyTree() {
      _root = new HierarchyRootNode();
    }

    /**
     * Add the given {@code path} to this tree, update the {@code setLineContext} at the terminal
     * node, and append the {@code errorNode} to the list of error nodes at the terminal node.
     */
    @Nonnull
    AddPathResult addPath(
        HierarchyPath path,
        @Nullable Set_lineContext setLineContext,
        @Nullable ErrorNode errorNode) {
      AddPathResult result = AddPathResult.UNMODIFIED;
      HierarchyNode currentNodeInThisTree = _root;
      HierarchyChildNode matchNode = null;
      for (HierarchyChildNode currentPathNode : path._nodes) {
        matchNode = currentNodeInThisTree.getChildNode(currentPathNode._unquotedText);
        if (matchNode == null) {
          result = AddPathResult.MODIFIED;
          matchNode = currentPathNode.copy();
          currentNodeInThisTree.addChildNode(matchNode);
        }
        currentNodeInThisTree = matchNode;
      }
      assert matchNode != null;
      matchNode._line = setLineContext;
      if (errorNode != null) {
        matchNode._errorNodes =
            ImmutableList.<ErrorNode>builder().addAll(matchNode._errorNodes).add(errorNode).build();
      }
      return result;
    }

    private boolean isSubPathDeactivated(HierarchyPath path) {
      HierarchyNode currentNode = _root;
      HierarchyChildNode matchNode = null;
      for (HierarchyChildNode currentPathNode : path._nodes) {
        matchNode = currentNode.getChildNode(currentPathNode._unquotedText);
        if (matchNode == null) {
          return false;
        }
        if (matchNode.getDeactivated()) {
          return true;
        }
        currentNode = matchNode;
      }
      return false;
    }

    private @Nonnull HierarchyChildNode findExactPathMatchNode(HierarchyPath path) {
      HierarchyNode currentGroupNode = _root;
      HierarchyChildNode matchNode = null;
      checkArgument(!path._nodes.isEmpty(), "Path must be non-empty", path);
      for (HierarchyChildNode currentPathNode : path._nodes) {
        matchNode = currentGroupNode.getChildNode(currentPathNode._unquotedText);
        checkArgument(matchNode != null, "Path does not exist in tree: %s", path);
        currentGroupNode = matchNode;
      }
      return matchNode;
    }

    /**
     * Populate the specified setLine's children with the supplied path and return the generated set
     * statement
     */
    private static StatementContext setLineHelper(
        Set_lineContext setLine,
        HierarchyPath path,
        int overrideLine,
        boolean markWildcards,
        Map<Token, String> tokenInputs) {
      StringBuilder sb = new StringBuilder();
      for (HierarchyChildNode pathNode : path._nodes) {
        sb.append(pathNode._text);
        sb.append(" ");
      }
      String newStatementText = sb.toString();
      // Get rid of last " ", which matters for tokens where whitespace is not ignored
      newStatementText =
          "set " + newStatementText.substring(0, newStatementText.length() - 1) + "\n";
      TerminalNode set = new TerminalNodeImpl(new CommonToken(FlatJuniperLexer.SET, "set"));
      Set_line_tailContext setLineTail = new Set_line_tailContext(setLine, -1);
      TerminalNode newline = new TerminalNodeImpl(new CommonToken(FlatJuniperLexer.NEWLINE, "\n"));
      setLine.children = new ArrayList<>();
      setLine.children.add(set);
      setLine.children.add(setLineTail);
      setLine.children.add(newline);
      FlatJuniperCombinedParser parser =
          new FlatJuniperCombinedParser(newStatementText, parserSettings());
      // Use the supplied line number for the constructed nodes
      parser.getLexer().setOverrideTokenStartLine(overrideLine);
      if (markWildcards) {
        parser.setMarkWildcards(true);
      }
      Flat_juniper_configurationContext newConfiguration =
          parser.getParser().flat_juniper_configuration();
      markTokenInputs(newConfiguration, newStatementText, tokenInputs, parser);
      if (markWildcards) {
        parser.setMarkWildcards(false);
      }
      StatementContext newStatement = newConfiguration.set_line(0).set_line_tail().statement();
      newStatement.parent = setLineTail;

      setLineTail.children = new ArrayList<>();
      setLineTail.children.add(newStatement);
      return newStatement;
    }

    private Set_lineContext generateSetLine(
        HierarchyPath path,
        Flat_juniper_configurationContext configurationContext,
        int overrideLine,
        Map<Token, String> tokenInputs) {
      Set_lineContext setLine = new Set_lineContext(configurationContext, -1);
      setLineHelper(setLine, path, overrideLine, false, tokenInputs);
      return setLine;
    }

    private static void markTokenInputs(
        Flat_juniper_configurationContext newConfiguration,
        String newStatementText,
        Map<Token, String> tokenInputs,
        FlatJuniperCombinedParser parser) {
      TokenInputMarker listener = new TokenInputMarker(newStatementText, tokenInputs);
      ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
      walker.walk(listener, newConfiguration);
    }

    private @Nonnull List<ParseTree> getApplyPathLines(
        HierarchyPath basePath,
        HierarchyPath applyPathPath,
        Flat_juniper_configurationContext configurationContext,
        Map<Token, String> tokenInputs) {
      List<ParseTree> lines = new ArrayList<>();
      List<HierarchyChildNode> candidateNodes = getApplyPathPrefixes(applyPathPath);
      for (HierarchyChildNode candidateNode : candidateNodes) {
        String concreteText = candidateNode._text;
        int candidateLineNumber = candidateNode._lineNumber;
        String finalPrefixStr;
        if (isIpOrIp6(concreteText)) {
          // not a prefix, so need to append slash and prefix-length
          finalPrefixStr =
              String.format(
                  "%s/%d",
                  concreteText,
                  concreteText.contains(":")
                      ? Prefix6.MAX_PREFIX_LENGTH
                      : Prefix.MAX_PREFIX_LENGTH);
        } else if (isPrefixOrPrefix6(concreteText)) {
          finalPrefixStr = concreteText;
        } else {
          continue;
        }
        basePath.addNode(finalPrefixStr, candidateLineNumber);
        Set_lineContext setLine =
            generateSetLine(basePath, configurationContext, candidateLineNumber, tokenInputs);
        lines.add(setLine);
        basePath._nodes.remove(basePath._nodes.size() - 1);
      }
      return lines;
    }

    private @Nonnull List<HierarchyChildNode> getApplyPathPrefixes(HierarchyPath path) {
      List<HierarchyChildNode> prefixes = new ArrayList<>();
      getApplyPathPrefixes(path, _root, 0, prefixes);
      return prefixes;
    }

    private void getApplyPathPrefixes(
        HierarchyPath path,
        HierarchyNode currentNode,
        int currentDepth,
        List<HierarchyChildNode> prefixes) {
      if (currentDepth == path._nodes.size() - 1) {
        HierarchyChildNode currentPathNode = path._nodes.get(currentDepth);
        for (HierarchyChildNode currentChild : currentNode.getChildren().values()) {
          if (!currentChild.isWildcard() && currentPathNode.matches(currentChild)) {
            prefixes.add(currentChild);
          }
        }
      } else {
        HierarchyChildNode currentPathNode = path._nodes.get(currentDepth);
        for (HierarchyChildNode currentChild : currentNode.getChildren().values()) {
          if (currentPathNode.matches(currentChild)) {
            getApplyPathPrefixes(path, currentChild, currentDepth + 1, prefixes);
          }
        }
      }
    }

    /** Mark a group as being applied at given path in this tree. */
    private void markApplyGroups(HierarchyPath path, String groupName) {
      if (path._nodes.isEmpty()) {
        _root.addGroup(groupName);
      } else {
        HierarchyChildNode node = findExactPathMatchNode(path);
        node.addGroup(groupName);
      }
    }

    /** Mark a group as being excluded from inheritance at a given path in this tree. */
    private void markApplyGroupsExcept(HierarchyPath path, String groupName) {
      if (path._nodes.isEmpty()) {
        _root.addExceptGroup(groupName);
      } else {
        HierarchyChildNode node = findExactPathMatchNode(path);
        node.addExceptGroup(groupName);
      }
    }

    /**
     * Returns a string consisting of newline-separated set lines corresponding to this tree. One
     * set line is produced for each path from the root to a leaf.
     */
    private @Nonnull String toSetLines(@Nonnull String header) {
      StringBuilder output = new StringBuilder(header);
      _root._errorNodes.forEach(
          errorNode -> output.append(errorNode.getText().trim()).append("\n"));
      _root.appendSetLines("set", output);
      return output.toString();
    }
  }

  private final HierarchyTree _deactivateTree;

  private final HierarchyTree _masterTree;

  private final Map<String, HierarchyTree> _trees;

  private final Map<Token, String> _tokenInputs;

  Hierarchy() {
    _trees = new HashMap<>();
    _masterTree = new HierarchyTree();
    _deactivateTree = new HierarchyTree();
    _tokenInputs = new HashMap<>();
  }

  /** Add an error node to the root that would be printed first by {@link #toSetLines}. */
  void addMasterRootErrorNode(ErrorNode node) {
    _masterTree._root._errorNodes =
        ImmutableList.<ErrorNode>builder().addAll(_masterTree._root._errorNodes).add(node).build();
  }

  void addDeactivatePath(HierarchyPath path, Deactivate_lineContext ctx) {
    _deactivateTree.addPath(path, null, null);
    _deactivateTree.findExactPathMatchNode(path).setDeactivated(true);
  }

  void removeDeactivatePath(HierarchyPath path, Activate_lineContext ctx) {
    _deactivateTree.addPath(path, null, null);
    _deactivateTree.findExactPathMatchNode(path).setDeactivated(false);
  }

  void addMasterPath(
      HierarchyPath path, @Nullable Set_lineContext ctx, @Nullable ErrorNode errorNode) {
    if (path._nodes.isEmpty()) {
      assert ctx == null;
      assert errorNode == null;
    } else {
      _masterTree.addPath(path, ctx, errorNode);
    }
  }

  @Nonnull
  List<ParseTree> getApplyPathLines(
      HierarchyPath basePath,
      HierarchyPath applyPathPath,
      Flat_juniper_configurationContext configurationContext) {
    return _masterTree.getApplyPathLines(
        basePath, applyPathPath, configurationContext, _tokenInputs);
  }

  HierarchyTree getTree(String groupName) {
    return _trees.get(groupName);
  }

  boolean isDeactivated(HierarchyPath path) {
    return _deactivateTree.isSubPathDeactivated(path);
  }

  HierarchyTree newTree(String groupName) {
    HierarchyTree newTree = new HierarchyTree();
    _trees.put(groupName, newTree);
    return newTree;
  }

  /** Mark a group as being applied at given path in the main tree. */
  void markApplyGroups(HierarchyPath path, String groupName) {
    _masterTree.markApplyGroups(path, groupName);
  }

  /** Mark a group as being excluded from inheritance at a given path in the main tree. */
  void markApplyGroupsExcept(HierarchyPath path, String groupName) {
    _masterTree.markApplyGroupsExcept(path, groupName);
  }

  @Nonnull
  Map<Token, String> getTokenInputs() {
    return _tokenInputs;
  }

  /**
   * Returns a string consisting of newline-separated flat Juniper set lines corresponding to the
   * master tree, i.e. all the set lines in the configuration from which this {@link Hierarchy} was
   * produced.
   */
  @Nonnull
  String toSetLines(@Nonnull String header) {
    return _masterTree.toSetLines(header);
  }

  private static boolean isIpOrIp6(String text) {
    return Ip.tryParse(text).isPresent() || Ip6.tryParse(text).isPresent();
  }

  private static boolean isPrefixOrPrefix6(String text) {
    return Prefix.tryParse(text).isPresent() || Prefix6.tryParse(text).isPresent();
  }

  /**
   * A structure used for determining whether a path ends in a list node. Used for merging
   * apply-groups lines into the main hierarchy.
   */
  private static final class IsListPathTree {

    /** A node of the {@link IsListPathTree} */
    private static final class IsListPathNode {
      private IsListPathNode(boolean isListNode) {
        _children = new HashMap<>();
        _isListNode = isListNode;
      }

      /**
       * Return the wildcard child if present, else the child keyed by {@code text} if present, else
       * {@code null}.
       */
      private @Nullable IsListPathNode getMatchingChildNode(String text) {
        IsListPathNode wildcard = _children.get("<*>");
        if (wildcard != null) {
          return wildcard;
        }
        return _children.get(text);
      }

      private final @Nonnull Map<String, IsListPathNode> _children;

      /**
       * Whether this node corresponds to list node in the Juniper grammar.
       *
       * <p>A node corresponding to the last word of an item in {@link
       * Hierarchy#buildIsListPathTree} should have this set to {@code true}.
       */
      private final boolean _isListNode;
    }

    /**
     * Returns {@code true} iff the {@code path} corresponds to a node whose children are an ordered
     * list in the Juniper grammar, according to the rules identifying such nodes that have been
     * instantiated in this tree.
     */
    private boolean isListPath(HierarchyPath path) {
      IsListPathNode currentIsListPathNode = _root;
      for (int i = 0; i < path._nodes.size(); i++) {
        HierarchyChildNode currentSubpathNode = path._nodes.get(i);
        currentIsListPathNode =
            currentIsListPathNode.getMatchingChildNode(currentSubpathNode._unquotedText);
        if (currentIsListPathNode == null) {
          return false;
        }
      }
      return currentIsListPathNode._isListNode;
    }

    /**
     * Add a rule identifying list nodes in the Juniper grammar.
     *
     * <p>The rule must be a space-separated list of strings, each of which is either literal text
     * or a wildcard, and which each represent a matcher for a successively deeper segment of a path
     * in the Juniper hierarchy.
     *
     * <p>For instance, adding a rule {@code foo bar <*> baz} means that the {@code baz} in the
     * Juniper line {@code set groups somegroupname foo bar someusertext baz} should be treated as a
     * node whose children are an ordered list; and therefore children from group nodes whose path
     * matches {@code baz} should be appended after non-inherited children of {@code baz}, and also
     * after such children inherited from higher-priority groups.
     */
    private void addListPathRule(String listPathRule) {
      String[] components = listPathRule.split(" ");
      checkArgument(
          components.length > 0,
          "Expected list path with at least one word, but list path was: %s",
          listPathRule);
      IsListPathNode currentNode = _root;
      for (int i = 0; i < components.length; i++) {
        boolean markListPath = i == components.length - 1;
        currentNode =
            currentNode._children.computeIfAbsent(
                components[i], c -> new IsListPathNode(markListPath));
      }
    }

    private IsListPathTree() {
      _root = new IsListPathNode(false);
    }

    private final @Nonnull IsListPathNode _root;
  }

  /**
   * Returns {@code true} iff the {@code path} corresponds to a node whose children are an ordered
   * list in the Juniper grammar.
   */
  @VisibleForTesting
  static boolean isListPath(HierarchyPath path) {
    return IS_LIST_PATH_TREE.isListPath(path);
  }

  private static final IsListPathTree IS_LIST_PATH_TREE = buildIsListPathTree();

  /**
   * Build the tree used for determining whether a hierarchy path ends in a node whose children are
   * an ordered list in the Juniper grammar.
   */
  private static synchronized @Nonnull IsListPathTree buildIsListPathTree() {
    IsListPathTree tree = new IsListPathTree();
    for (String listPath : getJuniperListPaths()) {
      tree.addListPathRule(listPath);
    }
    return tree;
  }
}
