package batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.Flat_juniper_configurationContext;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.Set_lineContext;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.Set_line_tailContext;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.StatementContext;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import batfish.main.BatfishException;
import batfish.main.PedanticBatfishException;

public class Hierarchy {

   public static class HierarchyTree {

      private static abstract class HierarchyChildNode extends HierarchyNode {

         private LinkedHashMap<StatementContext, List<Token>> _statements;
         protected String _text;

         private HierarchyChildNode(String text) {
            _text = text;
            _statements = new LinkedHashMap<StatementContext, List<Token>>();
         }

         public void addStatement(StatementContext statement, List<Token> tokens) {
            _statements.put(statement, tokens);
         }

         public abstract HierarchyChildNode copy();

         public abstract boolean isMatchedBy(HierarchyLiteralNode node);

         public abstract boolean isMatchedBy(HierarchyWildcardNode node);

         public abstract boolean matches(HierarchyChildNode node);

      }

      private static final class HierarchyLiteralNode extends
            HierarchyChildNode {

         private HierarchyLiteralNode(String text) {
            super(text);
         }

         @Override
         public HierarchyChildNode copy() {
            return new HierarchyLiteralNode(_text);
         }

         public boolean isMatchedBy(HierarchyLiteralNode node) {
            return _text.equals(node._text);
         }

         @Override
         public boolean isMatchedBy(HierarchyWildcardNode node) {
            String regex = node._wildcard.replaceAll("\\*", ".*");
            return _text.matches(regex);
         }

         @Override
         public boolean matches(HierarchyChildNode node) {
            return node.isMatchedBy(this);
         }

         @Override
         public String toString() {
            return "Literal(" + _text + ")";
         }

      }

      private static abstract class HierarchyNode {

         private Map<String, HierarchyChildNode> _children;

         public HierarchyNode() {
            _children = new LinkedHashMap<String, HierarchyChildNode>();
         }

         public void addChildNode(HierarchyChildNode node) {
            _children.put(node._text, node);
         }

         public HierarchyChildNode getChildNode(String text) {
            return _children.get(text);
         }

         public HierarchyChildNode getFirstMatchingChildNode(
               HierarchyChildNode node) {
            for (HierarchyChildNode child : _children.values()) {
               if (child.matches(node)) {
                  return child;
               }
            }
            return null;
         }

      }

      public static final class HierarchyPath {

         private List<HierarchyChildNode> _nodes;
         private StatementContext _statement;

         public HierarchyPath() {
            _nodes = new ArrayList<HierarchyChildNode>();
         }

         public void addNode(String text) {
            HierarchyChildNode newNode = new HierarchyLiteralNode(text);
            _nodes.add(newNode);
         }

         public void addWildcardNode(String text) {
            HierarchyChildNode newNode = new HierarchyWildcardNode(text);
            _nodes.add(newNode);
         }

         public void setStatement(StatementContext statement) {
            _statement = statement;
         }

         @Override
         public String toString() {
            return "Path(Statement:" + _statement + "," + _nodes + ")";
         }

      }

      private static final class HierarchyRootNode extends HierarchyNode {
      }

      private static final class HierarchyWildcardNode extends
            HierarchyChildNode {

         private String _wildcard;

         private HierarchyWildcardNode(String text) {
            super(text);
            if (text.charAt(0) != '<' || text.charAt(text.length() - 1) != '>') {
               throw new BatfishException("Improperly-formatted wildcard");
            }
            _wildcard = text.substring(1, text.length() - 1);
         }

         @Override
         public HierarchyChildNode copy() {
            return new HierarchyWildcardNode(_text);
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
         public boolean matches(HierarchyChildNode node) {
            return node.isMatchedBy(this);
         }

         @Override
         public String toString() {
            return "Wildcard(" + _text + ")";
         }

      }

      private String _groupName;
      private HierarchyRootNode _root;

      private HierarchyTree(String groupName) {
         _groupName = groupName;
         _root = new HierarchyRootNode();
      }

      public void addPath(HierarchyPath path, List<Token> tokens) {
         HierarchyNode currentGroupNode = _root;
         for (HierarchyChildNode currentPathNode : path._nodes) {
            HierarchyChildNode matchNode = currentGroupNode
                  .getChildNode(currentPathNode._text);
            if (matchNode == null) {
               matchNode = currentPathNode.copy();
               currentGroupNode.addChildNode(matchNode);
            }
            matchNode.addStatement(path._statement, tokens);
            currentGroupNode = matchNode;
         }
      }

      public List<ParseTree> getApplyGroupsLines(HierarchyPath path, Flat_juniper_configurationContext configurationContext) {
         List<ParseTree> lines = new ArrayList<ParseTree>();
         HierarchyNode currentGroupNode = _root;
         HierarchyChildNode matchNode = null;
         for (HierarchyChildNode currentPathNode : path._nodes) {
            matchNode = currentGroupNode
                  .getFirstMatchingChildNode(currentPathNode);
            if (matchNode == null) {
               throw new PedanticBatfishException(
                     "Apply-groups invocation without matching path");
            }
            currentGroupNode = matchNode;
         }
         for (Entry<StatementContext, List<Token>> e : matchNode._statements.entrySet()) {
            StatementContext statement = e.getKey();
            List<Token> tokens = e.getValue();
            ParseTree modifiedGroupLine = applyPath(path, statement, configurationContext, tokens);
            lines.add(modifiedGroupLine);
         }
         return lines;
      }

      public String getGroupName() {
         return _groupName;
      }

   }

   public static Set_lineContext applyPath(HierarchyPath path,
         StatementContext statement, Flat_juniper_configurationContext configurationContext, List<Token> tokens) {
      Set_lineContext setLine = new Set_lineContext(configurationContext, -1);
      TerminalNode set = new TerminalNodeImpl(new CommonToken(FlatJuniperGrammarLexer.SET, "set"));
      Set_line_tailContext setLineTail = new Set_line_tailContext(setLine, -1);
      TerminalNode newline = new TerminalNodeImpl(new CommonToken(FlatJuniperGrammarLexer.NEWLINE, "\n"));
      setLine.children = new ArrayList<ParseTree>();
      setLine.children.add(set);
      setLine.children.add(setLineTail);
      setLine.children.add(newline);

      String[] components = new String[tokens.size()];
      for (int i = 0; i < tokens.size(); i++) {
         components[i] = tokens.get(i).getText();
      }
      for (int i = 0; i < path._nodes.size(); i++) {
         components[i] = path._nodes.get(i)._text;
      }
      StringBuilder sb = new StringBuilder();
      for (String component : components) {
         sb.append(component + " ");
      }
      String newStatementText = sb.toString();
      FlatJuniperGrammarCombinedParser parser = new FlatJuniperGrammarCombinedParser(newStatementText, true, true);
      StatementContext newStatement = parser.getParser().statement();
      newStatement.parent = setLineTail;

      setLineTail.children = new ArrayList<ParseTree>();
      setLineTail.children.add(newStatement);
      return setLine;
   }

   private Map<String, HierarchyTree> _trees;

   public Hierarchy() {
      _trees = new HashMap<String, HierarchyTree>();
   }

   public List<ParseTree> getApplyGroupsLines(String groupName,
         HierarchyPath path, Flat_juniper_configurationContext configurationContext) {
      HierarchyTree tree = _trees.get(groupName);
      return tree.getApplyGroupsLines(path, configurationContext);
   }

   public HierarchyTree getTree(String groupName) {
      return _trees.get(groupName);
   }

   public HierarchyTree newTree(String groupName) {
      HierarchyTree newTree = new HierarchyTree(groupName);
      _trees.put(groupName, newTree);
      return newTree;
   }

}
