package org.batfish.grammar;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.ParseTreeSentences;

@ParametersAreNonnullByDefault
public class ParseTreePrettyPrinter implements ParseTreeListener {

  private final BatfishCombinedParser<?, ?> _combinedParser;
  private final ParserRuleContext _ctx;

  /** The current indentation level, based on the depth of the parse tree. */
  private int _indent;

  /**
   * If present, the (lowercase) names of rules for which methods are considered to be implemented.
   */
  private final @Nullable Set<String> _implementedRuleNames;

  private final @Nonnull ParseTreeSentences _ptSentences;
  private final boolean _printLineNumbers;
  private final @Nonnull List<String> _ruleNames;
  private final @Nonnull Vocabulary _vocabulary;

  private ParseTreePrettyPrinter(
      ParserRuleContext ctx,
      BatfishCombinedParser<?, ?> combinedParser,
      boolean printLineNumbers,
      @Nullable Set<String> implementedRuleNames) {
    Parser grammar = combinedParser.getParser();
    List<String> ruleNames = Arrays.asList(grammar.getRuleNames());
    _vocabulary = grammar.getVocabulary();
    _combinedParser = combinedParser;
    _ruleNames = ruleNames;
    _ctx = ctx;
    _ptSentences = new ParseTreeSentences();
    _printLineNumbers = printLineNumbers;
    _implementedRuleNames =
        implementedRuleNames == null ? null : ImmutableSet.copyOf(implementedRuleNames);
    _indent = 0;
  }

  public static ParseTreeSentences getParseTreeSentences(
      ParserRuleContext ctx, BatfishCombinedParser<?, ?> combinedParser, boolean printLineNumbers) {
    return getParseTreeSentences(ctx, combinedParser, printLineNumbers, null);
  }

  public static <L extends ParseTreeListener> ParseTreeSentences getParseTreeSentences(
      ParserRuleContext ctx,
      BatfishCombinedParser<?, ?> combinedParser,
      boolean printLineNumbers,
      @Nullable Set<String> implementedRuleNames) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(combinedParser);
    ParseTreePrettyPrinter printer =
        new ParseTreePrettyPrinter(ctx, combinedParser, printLineNumbers, implementedRuleNames);
    walker.walk(printer, ctx);
    return printer._ptSentences;
  }

  // Visible for testing
  static String printWithCharacterLimit(List<String> strings, int maxStringLength) {
    StringBuilder sb = new StringBuilder();

    // A limit of <= 0 is treated as effectively no limit
    int effectiveMaxStringLength = maxStringLength <= 0 ? Integer.MAX_VALUE : maxStringLength;

    ListIterator<String> iter = strings.listIterator();
    while (effectiveMaxStringLength > sb.length() && iter.hasNext()) {
      String string = iter.next();

      // Assume we're okay adding the whole string even if it pushes us over the maxStringLength
      sb.append(string);
      if (iter.hasNext()) {
        sb.append("\n");
      }
    }

    if (iter.hasNext()) {
      sb.append("and ");
      sb.append(strings.size() - iter.nextIndex());
      sb.append(" more line(s)");
    }
    return sb.toString();
  }

  public static String print(ParserRuleContext ctx, BatfishCombinedParser<?, ?> combinedParser) {
    return print(ctx, combinedParser, false);
  }

  public static String print(
      ParserRuleContext ctx, BatfishCombinedParser<?, ?> combinedParser, boolean printLineNumbers) {
    int maxStringLength = combinedParser.getSettings().getMaxParseTreePrintLength();
    List<String> strings =
        getParseTreeSentences(ctx, combinedParser, printLineNumbers).getSentences();
    return printWithCharacterLimit(strings, maxStringLength);
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    if (ctx != _ctx) {
      _ptSentences.addSentence(" ".repeat(2 * _indent));
    } else {
      _ptSentences.appendToLastSentence(" ".repeat(2 * _indent));
    }
    String ruleName = _ruleNames.get(ctx.getRuleIndex());

    if (ctx.getParent() != null) {
      for (Field f : ctx.getParent().getClass().getFields()) {
        try {
          if (!f.getName().equals(ruleName) && f.get(ctx.getParent()) == ctx) {
            _ptSentences.appendToLastSentence(f.getName() + " = ");
          }
        } catch (Throwable t) {
          // Ignore the error and continue.
        }
      }
    }
    _ptSentences.appendToLastSentence("(" + ruleName);
    if (_implementedRuleNames != null && !_implementedRuleNames.contains(ruleName.toLowerCase())) {
      _ptSentences.appendToLastSentence("*");
    }
    _indent++;
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    _indent--;
    _ptSentences.appendToLastSentence(")");
  }

  @Override
  public void visitErrorNode(ErrorNode ctx) {
    String nodeText = BatfishCombinedParser.escape(ctx.getText());
    _ptSentences.addSentence(" ".repeat(2 * _indent));
    int tokenType = ctx.getSymbol().getType();
    String tokenName;
    if (tokenType == Lexer.EOF) {
      tokenName = "EOF";
      _ptSentences.appendToLastSentence(tokenName + ":" + nodeText);
    } else if (tokenType == BatfishLexer.UNRECOGNIZED_LINE_TOKEN) {
      _ptSentences.appendToLastSentence("<UnrecognizedLine>:'" + nodeText + "'");
    } else {
      tokenName = _vocabulary.getSymbolicName(tokenType);
      _ptSentences.appendToLastSentence("<ErrorNode>:" + tokenName + ":'" + nodeText + "'");
    }
  }

  @Override
  public void visitTerminal(TerminalNode ctx) {
    String nodeText = BatfishCombinedParser.escape(ctx.getText());
    _ptSentences.addSentence(" ".repeat(2 * _indent));
    Token t = ctx.getSymbol();
    int tokenType = t.getType();
    int modeAsInt = _combinedParser.getTokenMode(t);
    String mode;
    if (modeAsInt == -1) {
      mode = "<MANUAL/UNKNOWN>";
    } else {
      mode = _combinedParser.getLexer().getModeNames()[modeAsInt];
    }

    String tokenName = (tokenType == Token.EOF) ? "EOF" : _vocabulary.getSymbolicName(tokenType);

    // If the parent context has a named field pointing to the token, it is because the user
    // has a defined name. Add it to the output message.
    for (Field f : ctx.getParent().getClass().getFields()) {
      if (f.getName().equals("start")
          || f.getName().equals("stop")
          || f.getName().startsWith("_t")
          || f.getName().equals(tokenName)) {
        continue;
      }
      try {
        if (f.get(ctx.getParent()) == ctx.getSymbol()) {
          _ptSentences.appendToLastSentence(f.getName() + " = ");
        }
      } catch (Throwable thrown) {
        // Ignore the error and continue.
      }
    }

    if (tokenType == Token.EOF) {
      _ptSentences.appendToLastSentence(tokenName + ":" + nodeText);
    } else {
      _ptSentences.appendToLastSentence(tokenName + ":'" + nodeText + "'");
    }
    if (!mode.equals("DEFAULT_MODE")) {
      _ptSentences.appendToLastSentence("  <== mode:" + mode);
    }

    if (_printLineNumbers) {
      _ptSentences.appendToLastSentence(String.format(" line:%s", _combinedParser.getLine(t)));
    }
  }
}
