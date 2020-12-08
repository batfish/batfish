package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.StatementContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_host_nameContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import org.batfish.representation.juniper.GroupWildcard;

public final class Hierarchy {

  private static class IsHostnameStatement extends FlatJuniperParserBaseListener {

    private boolean _isHostname;

    @Override
    public void enterSy_host_name(Sy_host_nameContext ctx) {
      _isHostname = true;
    }

    private static boolean isHostnameStatement(StatementContext ctx) {
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

    public TokenInputMarker(String input, Map<Token, String> tokenInputs) {
      _input = input;
      _tokenInputs = tokenInputs;
    }

    @Override
    public void visitTerminal(TerminalNode node) {
      _tokenInputs.put(node.getSymbol(), _input);
    }
  }

  public static class HierarchyTree {

    private enum AddPathResult {
      BLACKLISTED,
      MODIFIED,
      UNMODIFIED
    }

    private abstract static class HierarchyChildNode extends HierarchyNode {

      private Set_lineContext _line;
      protected int _lineNumber;
      protected String _sourceGroup;
      public List<String> _sourceWildcards;
      protected String _text;

      private HierarchyChildNode(String text, int lineNumber) {
        _text = text;
        _lineNumber = lineNumber;
      }

      public abstract HierarchyChildNode copy();

      public abstract boolean isMatchedBy(HierarchyLiteralNode node);

      public abstract boolean isMatchedBy(HierarchyWildcardNode node);

      public abstract boolean matches(HierarchyChildNode node);
    }

    private static final class HierarchyLiteralNode extends HierarchyChildNode {

      private HierarchyLiteralNode(String text, int lineNumber) {
        super(text, lineNumber);
      }

      @Override
      public HierarchyChildNode copy() {
        return new HierarchyLiteralNode(_text, _lineNumber);
      }

      @Override
      public boolean isMatchedBy(HierarchyLiteralNode node) {
        return _text.equals(node._text);
      }

      @Override
      public boolean isMatchedBy(HierarchyWildcardNode node) {
        return matchWithJuniperRegex(_text, node._wildcard);
      }

      @Override
      public boolean matches(HierarchyChildNode node) {
        return node.isMatchedBy(this);
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Literal(" + _text);
        if (_sourceGroup != null) {
          sb.append(", srcGroup:\"" + _sourceGroup + "\"");
        }
        if (_sourceWildcards != null) {
          sb.append(", srcWildcard:\"" + _sourceWildcards + "\"");
        }
        sb.append(")");
        return sb.toString();
      }
    }

    private abstract static class HierarchyNode {

      protected Set<String> _blacklistedGroups;
      private final Map<String, HierarchyChildNode> _children;

      /**
       * Add a set line to {@code output} prefixed by {@code prefix} for each path from this node to
       * a leaf.
       */
      protected final void appendSetLines(@Nonnull String prefix, @Nonnull StringBuilder output) {
        if (_children.isEmpty()) {
          // leaf, so append set line
          output.append(prefix).append("\n");
        }
        _children.forEach(
            (childText, child) -> {
              // append set lines for every path from child to leaf
              child.appendSetLines(String.format("%s %s", prefix, childText), output);
            });
      }

      public HierarchyNode() {
        _children = new LinkedHashMap<>();
        _blacklistedGroups = new HashSet<>();
      }

      public void addBlacklistedGroup(String groupName) {
        _blacklistedGroups.add(groupName);
      }

      public void addChildNode(HierarchyChildNode node) {
        _children.put(node._text, node);
      }

      public HierarchyChildNode getChildNode(String text) {
        return _children.get(text);
      }

      public Map<String, HierarchyChildNode> getChildren() {
        return _children;
      }

      @Nullable
      public HierarchyChildNode getFirstMatchingChildNode(HierarchyChildNode node) {
        for (HierarchyChildNode child : _children.values()) {
          if (child.matches(node)) {
            return child;
          }
        }
        return null;
      }

      public boolean isWildcard() {
        return false;
      }
    }

    public static final class HierarchyPath {

      private boolean _containsWildcard;
      private final List<HierarchyChildNode> _nodes;
      private StatementContext _statement;

      public HierarchyPath() {
        _nodes = new ArrayList<>();
      }

      public void addNode(String text, int lineNumber) {
        HierarchyChildNode newNode = new HierarchyLiteralNode(text, lineNumber);
        _nodes.add(newNode);
      }

      public void addWildcardNode(String text, int lineNumber) {
        _containsWildcard = true;
        HierarchyChildNode newNode = new HierarchyWildcardNode(text, lineNumber);
        _nodes.add(newNode);
      }

      public boolean containsWildcard() {
        return _containsWildcard;
      }

      public String pathString() {
        return _nodes.stream().map(node -> node._text).collect(Collectors.joining(" "));
      }

      public void setStatement(StatementContext statement) {
        _statement = statement;
      }

      @Override
      public String toString() {
        return "Path(Statement:" + _statement + "," + _nodes + ")";
      }
    }

    private static final class HierarchyRootNode extends HierarchyNode {}

    private static final class HierarchyWildcardNode extends HierarchyChildNode {

      private final String _wildcard;

      private HierarchyWildcardNode(String text, int lineNumber) {
        super(text, lineNumber);
        if (text.charAt(0) != '<' || text.charAt(text.length() - 1) != '>') {
          throw new BatfishException("Improperly-formatted wildcard: " + text);
        }
        _wildcard = text.substring(1, text.length() - 1);
      }

      @Override
      public HierarchyChildNode copy() {
        return new HierarchyWildcardNode(_text, _lineNumber);
      }

      @Override
      public boolean isMatchedBy(HierarchyLiteralNode node) {
        return false;
      }

      @Override
      public boolean isMatchedBy(HierarchyWildcardNode node) {
        // TODO: check whether this is the only way to match two wildcards
        return _text.equals(node._text);
      }

      @Override
      public boolean isWildcard() {
        return true;
      }

      @Override
      public boolean matches(HierarchyChildNode node) {
        return node.isMatchedBy(this);
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

    private final @Nullable String _groupName;

    private final HierarchyRootNode _root;

    private HierarchyTree(@Nullable String groupName) {
      _groupName = groupName;
      _root = new HierarchyRootNode();
    }

    private void addGroupPaths(
        @Nullable Set_lineContext groupLine,
        Collection<HierarchyChildNode> currentGroupChildren,
        HierarchyTree masterTree,
        HierarchyPath path,
        List<ParseTree> lines,
        Flat_juniper_configurationContext configurationContext,
        boolean clusterGroup,
        Map<Token, String> tokenInputs) {
      if (groupLine != null) {
        int overrideLine = groupLine.getStart().getLine();
        Set_lineContext setLine = new Set_lineContext(configurationContext, -1);
        if (masterTree.addPath(path, setLine, _groupName) == AddPathResult.BLACKLISTED) {
          return;
        }
        StatementContext newStatement =
            setLineHelper(setLine, path, overrideLine, true, tokenInputs);
        if (!(clusterGroup && IsHostnameStatement.isHostnameStatement(newStatement))) {
          lines.add(setLine);
        }
      }
      for (HierarchyChildNode childNode : currentGroupChildren) {
        HierarchyChildNode newPathNode = childNode.copy();
        path._nodes.add(newPathNode);
        addGroupPaths(
            childNode._line,
            childNode.getChildren().values(),
            masterTree,
            path,
            lines,
            configurationContext,
            clusterGroup,
            tokenInputs);
        path._nodes.remove(path._nodes.size() - 1);
      }
    }

    public AddPathResult addPath(
        HierarchyPath path, @Nullable Set_lineContext ctx, @Nullable String group) {
      AddPathResult result = AddPathResult.UNMODIFIED;
      HierarchyNode currentNode = _root;
      HierarchyChildNode matchNode = null;
      for (HierarchyChildNode currentPathNode : path._nodes) {
        matchNode = currentNode.getChildNode(currentPathNode._text);
        if (matchNode == null) {
          result = AddPathResult.MODIFIED;
          matchNode = currentPathNode.copy();
          currentNode.addChildNode(matchNode);
        }
        if (matchNode._blacklistedGroups.contains(group)) {
          return AddPathResult.BLACKLISTED;
        }
        currentNode = matchNode;
      }
      assert matchNode != null;
      matchNode._line = ctx;
      matchNode._sourceGroup = group;
      return result;
    }

    public List<ParseTree> applyWildcardPath(
        HierarchyPath path,
        Flat_juniper_configurationContext configurationContext,
        Map<Token, String> tokenInputs) {
      HierarchyChildNode wildcardNode = findExactPathMatchNode(path);
      String sourceGroup = wildcardNode._sourceGroup;
      int remainingWildcards = 0;
      int lineNumber = -1;
      for (HierarchyChildNode node : path._nodes) {
        if (node.isWildcard()) {
          remainingWildcards++;
          lineNumber = node._lineNumber;
        }
      }
      List<String> appliedWildcards = new ArrayList<>();
      HierarchyPath newPath = new HierarchyPath();
      List<ParseTree> lines = new ArrayList<>();
      applyWildcardPath(
          path,
          configurationContext,
          sourceGroup,
          _root,
          0,
          remainingWildcards,
          appliedWildcards,
          newPath,
          lines,
          lineNumber,
          tokenInputs);
      return lines;
    }

    private void applyWildcardPath(
        HierarchyPath path,
        Flat_juniper_configurationContext configurationContext,
        String sourceGroup,
        HierarchyNode destinationTreeRoot,
        int startingIndex,
        int remainingWildcards,
        List<String> appliedWildcards,
        HierarchyPath newPath,
        List<ParseTree> lines,
        int overrideLineNumber,
        Map<Token, String> tokenInputs) {
      if (destinationTreeRoot._blacklistedGroups.contains(sourceGroup)) {
        return;
      }
      HierarchyChildNode currentPathNode = path._nodes.get(startingIndex);
      if (!currentPathNode.isWildcard()) {
        String currentPathNodeText = currentPathNode._text;
        HierarchyChildNode newDestinationTreeRoot =
            destinationTreeRoot.getChildNode(currentPathNodeText);
        if (newDestinationTreeRoot == null) {
          // If literal node does not exist, but there are still more
          // wildcards to match, we abort.
          // Else, we create node and continue recursing
          if (remainingWildcards > 0) {
            return;
          }
          newDestinationTreeRoot = currentPathNode.copy();
          destinationTreeRoot._children.put(newDestinationTreeRoot._text, newDestinationTreeRoot);
        }
        newPath._nodes.add(newDestinationTreeRoot);
        if (startingIndex == path._nodes.size() - 1) {
          newDestinationTreeRoot._sourceWildcards = new ArrayList<>();
          newDestinationTreeRoot._sourceWildcards.addAll(appliedWildcards);
          newDestinationTreeRoot._line =
              generateSetLine(newPath, configurationContext, overrideLineNumber, tokenInputs);
          lines.add(newDestinationTreeRoot._line);
        } else {
          applyWildcardPath(
              path,
              configurationContext,
              sourceGroup,
              newDestinationTreeRoot,
              startingIndex + 1,
              remainingWildcards,
              appliedWildcards,
              newPath,
              lines,
              overrideLineNumber,
              tokenInputs);
        }
        newPath._nodes.remove(newPath._nodes.size() - 1);
      } else {
        appliedWildcards.add(currentPathNode._text);
        if (startingIndex < path._nodes.size() - 1) {
          for (HierarchyChildNode destinationTreeNode : destinationTreeRoot._children.values()) {
            // if there are no matching children, then we recurse no
            // further
            if (!destinationTreeNode.isWildcard() && currentPathNode.matches(destinationTreeNode)) {
              newPath._nodes.add(destinationTreeNode);
              applyWildcardPath(
                  path,
                  configurationContext,
                  sourceGroup,
                  destinationTreeNode,
                  startingIndex + 1,
                  remainingWildcards - 1,
                  appliedWildcards,
                  newPath,
                  lines,
                  overrideLineNumber,
                  tokenInputs);
              newPath._nodes.remove(newPath._nodes.size() - 1);
            }
          }
        }
        appliedWildcards.remove(appliedWildcards.size() - 1);
      }
    }

    public boolean containsPathPrefixOf(HierarchyPath path) {
      Map<String, HierarchyChildNode> currentChildren = _root.getChildren();
      List<HierarchyChildNode> pathNodes = path._nodes;
      for (HierarchyChildNode currentPathNode : pathNodes) {
        HierarchyChildNode treeMatchNode = currentChildren.get(currentPathNode._text);
        if (treeMatchNode != null) {
          if (treeMatchNode.getChildren().size() == 0) {
            return true;
          } else {
            currentChildren = treeMatchNode.getChildren();
          }
        } else {
          break;
        }
      }
      return false;
    }

    private HierarchyChildNode findExactPathMatchNode(HierarchyPath path) {
      HierarchyNode currentGroupNode = _root;
      HierarchyChildNode matchNode = null;
      for (HierarchyChildNode currentPathNode : path._nodes) {
        matchNode = currentGroupNode.getChildNode(currentPathNode._text);
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

    public List<ParseTree> getApplyGroupsLines(
        HierarchyPath path,
        Flat_juniper_configurationContext configurationContext,
        HierarchyTree masterTree,
        boolean clusterGroup,
        Map<Token, String> tokenInputs) {
      List<ParseTree> lines = new ArrayList<>();
      HierarchyNode currentGroupNode = _root;
      HierarchyChildNode matchNode = null;
      HierarchyPath partialMatch = new HierarchyPath();
      if (path._nodes.isEmpty()) {
        addGroupPaths(
            null,
            _root.getChildren().values(),
            masterTree,
            path,
            lines,
            configurationContext,
            clusterGroup,
            tokenInputs);
      } else {
        for (HierarchyChildNode currentPathNode : path._nodes) {
          matchNode = currentGroupNode.getFirstMatchingChildNode(currentPathNode);
          if (matchNode == null) {
            String message = "No matching path";
            if (!partialMatch._nodes.isEmpty()) {
              message +=
                  ": Partial path match within applied group: \""
                      + partialMatch.pathString()
                      + "\"";
            }
            throw new PartialGroupMatchException(message);
          }
          partialMatch._nodes.add(matchNode);
          currentGroupNode = matchNode;
        }

        // at this point, matchNode is the node in the group tree whose
        // children must be added to the main tree with substitutions
        // applied
        // according to the supplied path
        addGroupPaths(
            matchNode._line,
            matchNode.getChildren().values(),
            masterTree,
            path,
            lines,
            configurationContext,
            clusterGroup,
            tokenInputs);
      }
      return lines;
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

    public List<ParseTree> getApplyPathLines(
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
        if (!concreteText.contains("/")) {
          // not a prefix, so need to append slash and prefix-length
          finalPrefixStr =
              String.format(
                  "%s/%d",
                  concreteText,
                  concreteText.contains(":")
                      ? Prefix6.MAX_PREFIX_LENGTH
                      : Prefix.MAX_PREFIX_LENGTH);
        } else {
          finalPrefixStr = concreteText;
        }
        basePath.addNode(finalPrefixStr, candidateLineNumber);
        Set_lineContext setLine =
            generateSetLine(basePath, configurationContext, candidateLineNumber, tokenInputs);
        lines.add(setLine);
        basePath._nodes.remove(basePath._nodes.size() - 1);
      }
      return lines;
    }

    private List<HierarchyChildNode> getApplyPathPrefixes(HierarchyPath path) {
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

    public String getGroupName() {
      return _groupName;
    }

    public void pruneAfterPath(HierarchyPath path) {
      HierarchyChildNode pathEnd = findExactPathMatchNode(path);
      pathEnd.getChildren().clear();
    }

    public void setApplyGroupsExcept(HierarchyPath path, String groupName) {
      HierarchyChildNode node = findExactPathMatchNode(path);
      node.addBlacklistedGroup(groupName);
    }

    /**
     * Returns a string consisting of newline-separated set lines corresponding to this tree. One
     * set line is produced for each path from the root to a leaf.
     */
    private @Nonnull String toSetLines(@Nonnull String header) {
      StringBuilder output = new StringBuilder(header);
      _root.appendSetLines("set", output);
      return output.toString();
    }
  }

  private final HierarchyTree _deactivateTree;

  private final HierarchyTree _masterTree;

  private final Map<String, HierarchyTree> _trees;

  private final Map<Token, String> _tokenInputs;

  public Hierarchy() {
    _trees = new HashMap<>();
    _masterTree = new HierarchyTree(null);
    _deactivateTree = new HierarchyTree(null);
    _tokenInputs = new HashMap<>();
  }

  public void addDeactivatePath(HierarchyPath path, Deactivate_lineContext ctx) {
    if (isDeactivated(path)) {
      return;
    }
    _deactivateTree.addPath(path, null, null);
    _deactivateTree.pruneAfterPath(path);
  }

  public void addMasterPath(HierarchyPath path, @Nullable Set_lineContext ctx) {
    _masterTree.addPath(path, ctx, null);
  }

  public List<ParseTree> getApplyGroupsLines(
      String groupName,
      HierarchyPath path,
      Flat_juniper_configurationContext configurationContext,
      boolean clusterGroup) {
    HierarchyTree tree = _trees.get(groupName);
    if (tree == null) {
      throw new UndefinedGroupBatfishException("No such group: \"" + groupName + "\"");
    }
    return tree.getApplyGroupsLines(
        path, configurationContext, _masterTree, clusterGroup, _tokenInputs);
  }

  public List<ParseTree> getApplyPathLines(
      HierarchyPath basePath,
      HierarchyPath applyPathPath,
      Flat_juniper_configurationContext configurationContext) {
    return _masterTree.getApplyPathLines(
        basePath, applyPathPath, configurationContext, _tokenInputs);
  }

  public HierarchyTree getMasterTree() {
    return _masterTree;
  }

  public HierarchyTree getTree(String groupName) {
    return _trees.get(groupName);
  }

  public boolean isDeactivated(HierarchyPath path) {
    return _deactivateTree.containsPathPrefixOf(path);
  }

  public HierarchyTree newTree(String groupName) {
    HierarchyTree newTree = new HierarchyTree(groupName);
    _trees.put(groupName, newTree);
    return newTree;
  }

  public void setApplyGroupsExcept(HierarchyPath path, String groupName) {
    _masterTree.setApplyGroupsExcept(path, groupName);
  }

  static boolean matchWithJuniperRegex(String candidate, String juniperRegex) {
    String regex = GroupWildcard.toJavaRegex(juniperRegex);
    return candidate.matches(regex);
  }

  public Map<Token, String> getTokenInputs() {
    return _tokenInputs;
  }

  /**
   * Returns a string consisting of newline-separated flat Juniper set lines corresponding to the
   * master tree, i.e. all the set lines in the configuration from which this {@link Hierarchy} was
   * produced.
   */
  public @Nonnull String toSetLines(@Nonnull String header) {
    return _masterTree.toSetLines(header);
  }
}
