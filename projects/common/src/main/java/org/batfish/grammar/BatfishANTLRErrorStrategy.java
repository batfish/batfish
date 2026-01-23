package org.batfish.grammar;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Intended as a replacement for default ANTLR parser error recovery strategy. The basic idea here
 * is to throw out any lines containing invalidities, and try to parse as if those lines were never
 * there. Meanwhile, each invalid line should show up in the parse tree as {@link ErrorNode} at an
 * appropriate scope. The advantage of this strategy is that we do not unnecessarily pop back up to
 * the top level as soon as an error occurs.
 */
public class BatfishANTLRErrorStrategy extends DefaultErrorStrategy {

  /**
   * Construct a factory for making instances of {@link BatfishANTLRErrorStrategy} with supplied
   * {@code separatorToken} and {@code minimumRequiredSeparatorText}.
   */
  public static class BatfishANTLRErrorStrategyFactory {

    private final String _minimumRequiredSeparatorText;

    private final int _separatorToken;

    public BatfishANTLRErrorStrategyFactory(
        int separatorToken, String minimumRequiredSeparatorText) {
      _minimumRequiredSeparatorText = minimumRequiredSeparatorText;
      _separatorToken = separatorToken;
    }

    public BatfishANTLRErrorStrategy build(String text) {
      return new BatfishANTLRErrorStrategy(_separatorToken, _minimumRequiredSeparatorText, text);
    }
  }

  /**
   * Generic {@link RecognitionException} used by {@link BatfishANTLRErrorStrategy} to be thrown in
   * situations not easily mappable to traditional ANTLR parser error conditions.
   */
  static class BatfishRecognitionException extends RecognitionException {

    public BatfishRecognitionException(
        Recognizer<?, ?> recognizer, IntStream input, ParserRuleContext ctx) {
      super(null, recognizer, input, ctx);
    }
  }

  private final String[] _lines;

  private boolean _recoveredAtEof;

  private int _separatorToken;

  private String _parserStateAtRecovery;

  /**
   * Construct a {@link BatfishANTLRErrorStrategy} that throws out invalid lines from as delimited
   * by {@code separatorToken}. The {@code minimumRequiredSeparatorText} is used to split {@code
   * text} into lines, which {@link BatfishANTLRErrorStrategy} uses when creating instances of
   * {@link ErrorNode} from discarded lines.
   *
   * @param separatorToken Token that delimits lines
   * @param minimumRequiredSeparatorText Minimal string representation of {@code separatorToken}
   * @param text text of file to split into lines
   */
  private BatfishANTLRErrorStrategy(
      int separatorToken, String minimumRequiredSeparatorText, String text) {
    _lines = text.split(Pattern.quote(minimumRequiredSeparatorText), -1);
    _separatorToken = separatorToken;
  }

  /**
   * Consume all tokens a whole line at a time until the next token is one expected by the current
   * rule. Each line (as delimited by supplied separator token) starting from the current line up to
   * the last line consumed is placed in an {@link ErrorNode} and inserted as a child of the current
   * rule.
   *
   * @param recognizer The {@link Parser} to whom to delegate creation of each {@link ErrorNode}
   */
  private void consumeBlocksUntilWanted(Parser recognizer) {
    IntervalSet expecting = recognizer.getExpectedTokens();
    IntervalSet whatFollowsLoopIterationOrRule = expecting.or(getErrorRecoverySet(recognizer));

    int nextToken;
    do {
      // Eat tokens until we are at the end of the line
      consumeUntilEndOfLine(recognizer);

      // Get the line number and separator text from the separator token
      Token separatorToken = recognizer.getCurrentToken();

      // Insert the current line as an {@link ErrorNode} as a child of the current rule
      createErrorNode(recognizer, recognizer.getContext(), separatorToken);

      // Eat the separator token
      recognizer.consume();

      nextToken = recognizer.getInputStream().LA(1);
    } while (!whatFollowsLoopIterationOrRule.contains(nextToken) && nextToken != Lexer.EOF);
  }

  private void consumeUntilEndOfLine(Parser parser) {
    consumeUntil(parser, IntervalSet.of(_separatorToken));
  }

  /**
   * Create an error node with the text of the current line and insert it into parse tree
   *
   * @param recognizer The recognizer with which to create the error node
   * @param separator The token that ends the unrecognized link. This is also used to determine the
   *     index of the line to return in error messages.
   * @return The token contained in the error node
   */
  private Token createErrorNode(Parser recognizer, ParserRuleContext ctx, Token separator) {
    if (_recoveredAtEof) {
      _recoveredAtEof = false;
      throw new BatfishRecognitionException(recognizer, recognizer.getInputStream(), ctx);
    }
    if (separator.getType() == Lexer.EOF) {
      _recoveredAtEof = true;
    }
    String lineText = _lines[separator.getLine() - 1] + separator.getText();
    Token lineToken =
        new UnrecognizedLineToken(lineText, separator.getLine(), _parserStateAtRecovery);
    ErrorNode errorNode = recognizer.createErrorNode(ctx, lineToken);
    ctx.addErrorNode(errorNode);
    return lineToken;
  }

  /**
   * Attempt to get {@code parser} into a state where parsing can continue by throwing away the
   * current line and abandoning the current rule if possible. If at root already, the current line
   * is placed into an {@link ErrorNode} at the root and parsing continues at the next line (first
   * base case). If in a child rule with a parent that A) has its own parent and B) started on the
   * same line; then an exception is thrown to defer cleanup to the parent (recursive case). In any
   * other case, this rule is removed from its parent, and the current line is inserted as an {@link
   * ErrorNode} in its place as a child of that parent (second base case). If no lines remain,
   * parsing stops.
   *
   * @param parser The {@link Parser} needing to perform recovery
   * @return If base case applies, returns a {@link Token} whose containing the text of the
   *     created @{link ErrorNode}.
   */
  private Token recover(Parser parser) {
    lastErrorIndex = parser.getInputStream().index();
    if (lastErrorStates == null) {
      lastErrorStates = new IntervalSet();
    }
    lastErrorStates.add(parser.getState());

    // Consume anything left on the line
    consumeUntilEndOfLine(parser);

    ParserRuleContext ctx = parser.getContext();
    ParserRuleContext parent = ctx.getParent();

    // Recursive case
    if (parent != null
        && parent.parent != null
        && (parent.getStart() == null || parent.getStart().getLine() == ctx.getStart().getLine())) {
      throw new BatfishRecognitionException(parser, parser.getInputStream(), parent);
    }

    // Get the line number and separator text from the separator token
    Token separatorToken = parser.getCurrentToken();

    if (parent == null) {
      // First base case
      parser.consume();
      Token errorNode = createErrorNode(parser, ctx, separatorToken);
      endErrorCondition(parser);
      return errorNode;
    } else {
      // Second base case
      List<ParseTree> parentChildren = parent.children;
      parentChildren.remove(parentChildren.size() - 1);
      // Copy error nodes to parent so we don't lose unrecognized lines.
      if (ctx.children != null) {
        ctx.children.stream()
            .filter(c -> c instanceof ErrorNode)
            .forEach(c -> parent.addErrorNode((ErrorNode) c));
      }
      parser.consume();
      Token errorNode = createErrorNode(parser, parent, separatorToken);
      endErrorCondition(parser);
      return errorNode;
    }
  }

  @Override
  public void recover(Parser recognizer, RecognitionException e) {
    beginErrorCondition(recognizer);
    recover(recognizer);
  }

  /**
   * Recover from adaptive prediction failure (when more than one token is needed for rule
   * prediction, and the first token by itself is insufficient to determine an error has occured) by
   * throwing away lines until adaptive prediction succeeds or there is nothing left to throw away.
   * Each discarded line is inserted as a child of the current rule as an {@link ErrorNode}.
   *
   * @param recognizer The {@link Parser} for whom adaptive prediction has failed
   */
  public void recoverInCurrentNode(Parser recognizer) {
    beginErrorCondition(recognizer);
    lastErrorIndex = recognizer.getInputStream().index();
    if (lastErrorStates == null) {
      lastErrorStates = new IntervalSet();
    }
    lastErrorStates.add(recognizer.getState());

    consumeUntilEndOfLine(recognizer);

    // Get the line number and separator text from the separator token
    Token separatorToken = recognizer.getCurrentToken();

    ParserRuleContext ctx = recognizer.getContext();
    ParserRuleContext parent = ctx.getParent();

    // Check if parent expects the separator after current child completes.
    // Different grammars structure rules differently:
    // - Junos style: parent owns separator (e.g., set_line: SET set_line_tail NEWLINE)
    // - NX-OS style: child owns separator (e.g., banner_exec: ... NEWLINE)
    // Heuristic: Only skip consuming separator if:
    // 1. Parent exists and started on the same line (single-line parent rule)
    // 2. Parent expects separator in its production
    // Note: For flattened configs (Junos, Palo Alto), line numbers refer to the flattened
    // output, not the original hierarchical config.
    boolean shouldConsumeSeparator = true;
    if (parent != null
        && parent.getStart() != null
        && parent.getStart().getLine() == separatorToken.getLine()) {
      IntervalSet expected = recognizer.getExpectedTokens();
      if (expected.contains(_separatorToken)) {
        // Parent started on same line and expects separator - don't consume it (Junos style)
        shouldConsumeSeparator = false;
      }
    }

    if (shouldConsumeSeparator) {
      recognizer.consume();
    }

    createErrorNode(recognizer, ctx, separatorToken);
    if (recognizer.getInputStream().LA(1) == Lexer.EOF) {
      recover(recognizer);
    } else {
      endErrorCondition(recognizer);
    }
  }

  @Override
  public Token recoverInline(Parser recognizer) throws RecognitionException {
    beginErrorCondition(recognizer);
    return recover(recognizer);
  }

  @Override
  public void reportError(Parser recognizer, RecognitionException e) {
    if (!(e instanceof BatfishRecognitionException)) {
      recognizer.notifyErrorListeners(e.getOffendingToken(), e.getMessage(), e);
    }
  }

  @Override
  public void sync(Parser recognizer) throws RecognitionException {
    /*
     * BEGIN: Copied from super
     */
    ATNState s = recognizer.getInterpreter().atn.states.get(recognizer.getState());
    if (inErrorRecoveryMode(recognizer)) {
      return;
    }
    TokenStream tokens = recognizer.getInputStream();
    int la = tokens.LA(1);
    IntervalSet nextTokens = recognizer.getATN().nextTokens(s);
    /*
     * If next token is unmatchable (i.e. from a lexer error), we need to hide the whole line before
     * returning so we don't unnecessarily pop out of the star or plus loop (if in one) afterwards.
     */
    int atnStateType = s.getStateType();
    boolean atLoopExitDecision =
        (atnStateType == ATNState.STAR_LOOP_BACK
            || atnStateType == ATNState.PLUS_LOOP_BACK
            || atnStateType == ATNState.STAR_LOOP_ENTRY);
    boolean lexerErrorAtLoopExitDecision =
        la == BatfishLexer.UNMATCHABLE_TOKEN && atLoopExitDecision;
    boolean lexerErrorAtStartOfLineAtLoopExitDecision =
        lexerErrorAtLoopExitDecision
            && ((BatfishParser) recognizer).getLastConsumedToken() == _separatorToken;
    if (!lexerErrorAtStartOfLineAtLoopExitDecision
        && (nextTokens.contains(Token.EPSILON) || nextTokens.contains(la))) {
      return;
    }
    /*
     * END: Copied from super
     */

    boolean topLevel = recognizer.getContext().parent == null;

    switch (atnStateType) {
      case ATNState.BLOCK_START:
      case ATNState.STAR_BLOCK_START:
      case ATNState.PLUS_BLOCK_START:
      case ATNState.STAR_LOOP_ENTRY:
      case ATNState.PLUS_LOOP_BACK:
      case ATNState.STAR_LOOP_BACK:
        if (topLevel || lexerErrorAtStartOfLineAtLoopExitDecision) {
          /*
           * When at top level, we cannot pop up. So consume every "line" until we have one that
           * starts with a token acceptable at the top level.
           *
           * We also don't want to pop out of star or plus loops whose elements start at the
           * beginning of a line, or else we'd lose the whole loop.
           */
          reportUnwantedToken(recognizer);
          consumeBlocksUntilWanted(recognizer);
          return;
        } else {
          /*
           * If not at the top level, error out to pop up a level. This may repeat until the next
           * token is acceptable at the given level.
           *
           * Note that this branch is also taken for errors occurring in start or plus loops in the
           * middle in the middle of a line; in that case we want to throw the whole loop (and its
           * containing context) away.
           */
          beginErrorCondition(recognizer);
          throw new InputMismatchException(recognizer);
        }

      default:
        return;
    }
  }

  @Override
  protected void beginErrorCondition(Parser parser) {
    if (inErrorRecoveryMode(parser)) {
      return;
    }
    _parserStateAtRecovery = parser.getRuleContext().toString(Arrays.asList(parser.getRuleNames()));
    super.beginErrorCondition(parser);
  }

  @Override
  protected void endErrorCondition(Parser recognizer) {
    _parserStateAtRecovery = null;
    super.endErrorCondition(recognizer);
  }
}
