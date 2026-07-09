package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Converts a Juniper AS Path regex to a Java regex.
 *
 * <p>This is a hand-written recursive-descent parser. It replaces an earlier implementation built
 * on the parboiled library; the produced Java regex strings are unchanged, except that a bare
 * {@code {n}} exact-repetition operator is now applied to the term (the parboiled grammar silently
 * dropped it).
 *
 * @see <a
 *     href="https://www.juniper.net/documentation/en_US/junos/topics/usage-guidelines/policy-configuring-as-path-regular-expressions-to-use-as-routing-policy-match-conditions.html">Juniper
 *     docs</a>
 */
public final class AsPathRegex {
  private static final LoadingCache<String, String> CONVERT_REGEX_CACHE =
      Caffeine.newBuilder()
          .maximumSize(1 << 10) // 1K instances
          .build(AsPathRegex::convertToJavaRegexInternal);

  /** Matches either the start of the AS path string or a separating space. */
  private static final String MAYBE_SEPARATOR = "(^| )";

  private final String _input;
  private int _pos;

  private AsPathRegex(String input) {
    _input = input;
    _pos = 0;
  }

  /** Converts the given Juniper AS Path regular expression to a Java regular expression. */
  public static @Nonnull String convertToJavaRegex(String regex) {
    return CONVERT_REGEX_CACHE.get(regex);
  }

  private static @Nonnull String convertToJavaRegexInternal(String regex) {
    AsPathRegex parser = new AsPathRegex(regex);
    String result = parser.parseTopLevel();
    if (result == null) {
      throw new IllegalArgumentException("Unhandled input: " + regex);
    }
    return result;
  }

  // Grammar (mirrors the former parboiled grammar):
  //   TopLevel   := '^'? (NullAsPath | AsPath) '$'? EOI
  //   NullAsPath := "()"
  //   AsPath     := Term (space* Term)*
  //   Term       := BareAsnRange | BareOr | (T_TopLevel Operator?)
  //   T_TopLevel := T_Dot | AsnRange | ASN | T_Group | T_Or
  //   Operator   := '*' | '+' | '?' | '{' Number ('}' | ',' ('}' | Number '}'))
  // The leading '^' and trailing '$' are consumed but ignored: every produced
  // Java regex is already anchored.

  private @Nullable String parseTopLevel() {
    consumeChar('^');
    String result = parseNullAsPath();
    if (result == null) {
      result = parseAsPath();
    }
    if (result == null) {
      return null;
    }
    consumeChar('$');
    if (_pos != _input.length()) {
      return null;
    }
    return result;
  }

  private @Nullable String parseNullAsPath() {
    int start = _pos;
    if (consumeString("()")) {
      return "^$";
    }
    _pos = start;
    return null;
  }

  private @Nullable String parseAsPath() {
    String first = parseTerm();
    if (first == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder(first);
    while (true) {
      int save = _pos;
      ignoreSpace();
      String term = parseTerm();
      if (term == null) {
        _pos = save;
        break;
      }
      sb.append(term);
    }
    return "^" + sb + "$";
  }

  private @Nullable String parseTerm() {
    int start = _pos;

    String range = parseBareAsnRange();
    if (range != null) {
      return range;
    }
    _pos = start;

    String or = parseBareOr();
    if (or != null) {
      return or;
    }
    _pos = start;

    String top = parseTTopLevel();
    if (top == null) {
      _pos = start;
      return null;
    }
    int afterTop = _pos;
    String withOp = parseOperator(top);
    if (withOp != null) {
      return withOp;
    }
    _pos = afterTop;
    return top;
  }

  private @Nullable String parseBareAsnRange() {
    int start = _pos;
    String low = parseNumber();
    if (low == null || !consumeChar('-')) {
      _pos = start;
      return null;
    }
    String high = parseNumber();
    if (high == null) {
      _pos = start;
      return null;
    }
    return MAYBE_SEPARATOR + rangeToOr(low, high);
  }

  private @Nullable String parseBareOr() {
    String first = parseTTopLevel();
    if (first == null) {
      return null;
    }
    StringBuilder acc = new StringBuilder(first);
    int matched = 0;
    while (true) {
      int save = _pos;
      ignoreSpace();
      if (!consumeChar('|')) {
        _pos = save;
        break;
      }
      ignoreSpace();
      String next = parseTTopLevel();
      if (next == null) {
        _pos = save;
        break;
      }
      acc.append('|').append(next);
      matched++;
    }
    if (matched == 0) {
      return null;
    }
    return "(" + acc + ")";
  }

  private @Nullable String parseTTopLevel() {
    String dot = parseTDot();
    if (dot != null) {
      return dot;
    }
    String asnRange = parseAsnRange();
    if (asnRange != null) {
      return asnRange;
    }
    String asn = parseAsn();
    if (asn != null) {
      return asn;
    }
    String group = parseTGroup();
    if (group != null) {
      return group;
    }
    return parseTOr();
  }

  private @Nullable String parseTDot() {
    if (consumeChar('.')) {
      return MAYBE_SEPARATOR + "\\d+";
    }
    return null;
  }

  private @Nullable String parseAsnRange() {
    int start = _pos;
    if (!consumeChar('[')) {
      return null;
    }
    String range = parseBareAsnRange();
    if (range == null || !consumeChar(']')) {
      _pos = start;
      return null;
    }
    return range;
  }

  private @Nullable String parseAsn() {
    String number = parseNumber();
    if (number == null) {
      return null;
    }
    return MAYBE_SEPARATOR + number;
  }

  private @Nullable String parseTGroup() {
    int start = _pos;
    if (!consumeChar('(')) {
      return null;
    }
    ignoreSpace();
    String first = parseTerm();
    if (first == null) {
      _pos = start;
      return null;
    }
    StringBuilder acc = new StringBuilder(first);
    while (true) {
      int save = _pos;
      ignoreSpace();
      String term = parseTerm();
      if (term == null) {
        _pos = save;
        break;
      }
      acc.append(term);
    }
    ignoreSpace();
    if (!consumeChar(')')) {
      _pos = start;
      return null;
    }
    return acc.toString();
  }

  private @Nullable String parseTOr() {
    int start = _pos;
    if (!consumeChar('(')) {
      return null;
    }
    String or = parseBareOr();
    if (or == null || !consumeChar(')')) {
      _pos = start;
      return null;
    }
    return or;
  }

  /**
   * Applies an optional repetition operator to {@code term}. Returns null (consuming nothing) if no
   * operator is present.
   */
  private @Nullable String parseOperator(String term) {
    if (consumeChar('*')) {
      return String.format("(%s)*", term);
    }
    if (consumeChar('+')) {
      return String.format("(%s)+", term);
    }
    if (consumeChar('?')) {
      return String.format("(%s)?", term);
    }
    int start = _pos;
    if (!consumeChar('{')) {
      return null;
    }
    String low = parseNumber();
    if (low == null) {
      _pos = start;
      return null;
    }
    if (consumeChar('}')) {
      // {n} exact repetition.
      return String.format("(%s){%s}", term, low);
    }
    if (!consumeChar(',')) {
      _pos = start;
      return null;
    }
    if (consumeChar('}')) {
      // {n,} n-or-more repetition.
      return String.format("(%s){%s,}", term, low);
    }
    String high = parseNumber();
    if (high == null || !consumeChar('}')) {
      _pos = start;
      return null;
    }
    // {n,m} range repetition.
    return String.format("(%s){%s,%s}", term, low, high);
  }

  /** Matches one or more digits and returns them. */
  private @Nullable String parseNumber() {
    int start = _pos;
    while (_pos < _input.length() && Character.isDigit(_input.charAt(_pos))) {
      _pos++;
    }
    if (_pos == start) {
      return null;
    }
    return _input.substring(start, _pos);
  }

  private void ignoreSpace() {
    while (_pos < _input.length() && _input.charAt(_pos) == ' ') {
      _pos++;
    }
  }

  private boolean consumeChar(char c) {
    if (_pos < _input.length() && _input.charAt(_pos) == c) {
      _pos++;
      return true;
    }
    return false;
  }

  private boolean consumeString(String s) {
    if (_input.startsWith(s, _pos)) {
      _pos += s.length();
      return true;
    }
    return false;
  }

  static String rangeToOr(String lowAsn, String highAsn) {
    long start = Long.parseLong(lowAsn);
    long end = Long.parseLong(highAsn);
    checkArgument(start <= end, "Invalid range %s-%s", start, end);
    String bigOr =
        LongStream.range(start, end + 1).mapToObj(Long::toString).collect(Collectors.joining("|"));
    return String.format("(%s)", bigOr);
  }
}
