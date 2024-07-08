package org.batfish.representation.juniper.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.Nonnull;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SkipNode;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.common.StringBuilderSink;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.DebuggingValueStack;
import org.parboiled.support.ParsingResult;

/**
 * A class that converts a Juniper AS Path regex to a Java regex.
 *
 * @see <a
 *     href="https://www.juniper.net/documentation/en_US/junos/topics/usage-guidelines/policy-configuring-as-path-regular-expressions-to-use-as-routing-policy-match-conditions.html">Juniper
 *     docs</a>
 */
@BuildParseTree
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public class AsPathRegex extends BaseParser<String> {
  private static final LoadingCache<String, String> CONVERT_REGEX_CACHE =
      Caffeine.newBuilder()
          .maximumSize(1 << 10) // 1K instances
          .build(AsPathRegex::convertToJavaRegexInternal);
  private static final String MAYBE_SEPARATOR = "(^| )";

  Rule TopLevel() {
    return Sequence(Optional('^'), FirstOf(NullAsPath(), AsPath()), Optional('$'), EOI);
  }

  Rule NullAsPath() {
    return Sequence(String("()"), push("^$"));
  }

  Rule AsPath() {
    return Sequence(
        Term(), // first term, will be pop(1) below.
        ZeroOrMore(
            IgnoreSpace(),
            Term(), // pop()
            push(String.format("%s%s", pop(1), pop()))),
        push(String.format("^%s$", pop())));
  }

  Rule Term() {
    return FirstOf(
        BareAsnRange(), // AsnRange without [] around it cannot be followed by an operator
        BareOr(), // Or Without () around it cannot be followed by an operator
        Sequence(T_TopLevel(), Optional(Operator())));
  }

  Rule T_TopLevel() {
    return FirstOf(T_Dot(), AsnRange(), ASN(), T_Group(), T_Or());
  }

  @SkipNode
  Rule Operator() {
    return FirstOf(
        Op_Asterisk(), Op_Plus(), Op_QuestionMark(), Op_Exact(), Op_OrMore(), Op_Range());
  }

  @SuppressSubnodes
  Rule Op_Asterisk() {
    return Sequence(Ch('*'), push(String.format("(%s)*", pop())));
  }

  @SuppressSubnodes
  Rule Op_Plus() {
    return Sequence(Ch('+'), push(String.format("(%s)+", pop())));
  }

  @SuppressSubnodes
  Rule Op_QuestionMark() {
    return Sequence(Ch('?'), push(String.format("(%s)?", pop())));
  }

  @SuppressSubnodes
  Rule Op_Exact() {
    return Sequence('{', Number(), '}');
  }

  @SuppressSubnodes
  Rule Op_Range() {
    return Sequence(
        '{',
        Number(),
        ',',
        Number(),
        '}',
        push(String.format("(%s){%s,%s}", pop(2), pop(1), pop())));
  }

  @SuppressSubnodes
  Rule Op_OrMore() {
    return Sequence('{', Number(), ',', '}', push(String.format("(%s){%s,}", pop(1), pop())));
  }

  @SuppressSubnodes
  Rule T_Dot() {
    return Sequence(Ch('.'), push(MAYBE_SEPARATOR + "\\d+"));
  }

  // BareOr, with () around it. BareOr does all the stack manipulation.
  Rule T_Or() {
    return Sequence('(', BareOr(), ')');
  }

  Rule T_Group() {
    return Sequence(
        '(',
        IgnoreSpace(),
        Term(), // pop(1)
        ZeroOrMore(
            Sequence(
                IgnoreSpace(),
                Term(), // pop()
                push(String.format("%s%s", pop(1), pop())))),
        IgnoreSpace(),
        ')');
  }

  @SuppressSubnodes
  Rule Digit() {
    return CharRange('0', '9');
  }

  // Like Number(), but requires either start of string or preceding space.
  @SuppressSubnodes
  Rule ASN() {
    return Sequence(Number(), push(String.format("%s%s", MAYBE_SEPARATOR, pop())));
  }

  // A decimal number.
  @SuppressSubnodes
  Rule Number() {
    return Sequence(OneOrMore(Digit()), push(match()));
  }

  @SuppressSubnodes
  Rule BareAsnRange() {
    return Sequence(
        Sequence(Number(), '-', Number()),
        push(String.format("%s%s", MAYBE_SEPARATOR, rangeToOr(pop(1), pop()))));
  }

  Rule BareOr() {
    return Sequence(
        T_TopLevel(), // pop(1)
        OneOrMore(
            IgnoreSpace(),
            '|',
            IgnoreSpace(),
            T_TopLevel(), // pop()
            push(String.format("%s|%s", pop(1), pop()))),
        push(String.format("(%s)", pop())));
  }

  // BareAsnRange, with [] around it. BareAsnRange does all the stack manipulation.
  Rule AsnRange() {
    return Sequence('[', BareAsnRange(), ']');
  }

  @SuppressNode
  Rule IgnoreSpace() {
    return ZeroOrMore(' ');
  }

  // Must be package-private to be accessible to generated parser.
  static String rangeToOr(String lowAsn, String highAsn) {
    long start = Long.parseLong(lowAsn);
    long end = Long.parseLong(highAsn);
    checkArgument(start <= end, "Invalid range %s-%s", start, end);
    String bigOr =
        LongStream.range(start, end + 1).mapToObj(Long::toString).collect(Collectors.joining("|"));
    return String.format("(%s)", bigOr);
  }

  /** Converts the given Juniper AS Path regular expression to a Java regular expression. */
  public static @Nonnull String convertToJavaRegex(String regex) {
    return CONVERT_REGEX_CACHE.get(regex);
  }

  private static @Nonnull String convertToJavaRegexInternal(String regex) {
    AsPathRegex parser = Parboiled.createParser(AsPathRegex.class);
    BasicParseRunner<String> runner = new BasicParseRunner<>(parser.TopLevel());
    ParsingResult<String> result = runner.run(regex);
    if (!result.matched) {
      throw new IllegalArgumentException("Unhandled input: " + regex);
    }
    return result.resultValue;
  }

  /** Like {@link #convertToJavaRegex(String)}, but for debugging. */
  @SuppressWarnings("unused") // leaving here for future debugging.
  static @Nonnull String debugConvertToJavaRegex(String regex) {
    AsPathRegex parser = Parboiled.createParser(AsPathRegex.class);
    TracingParseRunner<String> runner =
        (TracingParseRunner<String>)
            new TracingParseRunner<String>(parser.TopLevel())
                .withLog(new StringBuilderSink())
                .withValueStack(new DebuggingValueStack<>(new StringBuilderSink()));
    DebuggingValueStack<String> stack = (DebuggingValueStack<String>) runner.getValueStack();
    ParsingResult<String> result = runner.run(regex);
    if (!result.matched) {
      throw new IllegalArgumentException(
          "Unhandled input: " + regex + "\n" + runner.getLog() + "\n" + stack.log);
    }
    return result.resultValue;
  }
}
