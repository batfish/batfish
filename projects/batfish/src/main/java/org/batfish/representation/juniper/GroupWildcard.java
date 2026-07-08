package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import javax.annotation.Nullable;

/**
 * Utility to convert a JunOS group wildcard into a Java regex.
 *
 * <p>The conversion is a hand-written recursive-descent parser. It replaces an earlier
 * implementation built on the parboiled library; the produced Java regex strings are unchanged.
 *
 * @see <a
 *     href="https://www.juniper.net/documentation/en_US/junos/topics/topic-map/configuration-groups-usage.html#id-using-wildcards-with-configuration-groups">Juniper
 *     docs</a>
 */
public final class GroupWildcard {
  private static final LoadingCache<String, String> CACHE =
      Caffeine.newBuilder().maximumSize(1_000_000).build(GroupWildcard::toJavaRegexInternal);

  private final String _input;
  private int _pos;

  private GroupWildcard(String input) {
    _input = input;
    _pos = 0;
  }

  public static String toJavaRegex(String wildcard) {
    return CACHE.get(wildcard);
  }

  private static String toJavaRegexInternal(String wildcard) {
    if (wildcard.isEmpty()) {
      return "";
    }
    GroupWildcard parser = new GroupWildcard(wildcard);
    String result = parser.parseTopLevel();
    checkArgument(result != null, "Unhandled input: <%s>", wildcard);
    return result;
  }

  // Grammar (mirrors the former parboiled grammar):
  //   TopLevel     := Element+ EOI
  //   Element      := '*' | '?' | CharacterClass | AllLiterals | '[' | ']'
  //   CharacterClass := '[' ('!' | '^')? ClassLiterals ']'
  //   ClassLiterals  := [a-zA-Z0-9\-_:/,]+
  //   AllLiterals    := (ClassLiterals | '.')+
  // '*' -> ".*", '?' -> "\w", '.' -> "\.", '[' -> "\[", ']' -> "\]",
  // leading '!'/'^' in a class -> "^".

  private @Nullable String parseTopLevel() {
    StringBuilder sb = new StringBuilder();
    String first = parseElement();
    if (first == null) {
      return null;
    }
    sb.append(first);
    while (_pos < _input.length()) {
      String element = parseElement();
      if (element == null) {
        return null;
      }
      sb.append(element);
    }
    return sb.toString();
  }

  private @Nullable String parseElement() {
    if (consumeChar('*')) {
      return ".*";
    }
    if (consumeChar('?')) {
      return "\\w";
    }
    String characterClass = parseCharacterClass();
    if (characterClass != null) {
      return characterClass;
    }
    String allLiterals = parseAllLiterals();
    if (allLiterals != null) {
      return allLiterals;
    }
    // Unmatched brackets fall through to literal escapes.
    if (consumeChar('[')) {
      return "\\[";
    }
    if (consumeChar(']')) {
      return "\\]";
    }
    return null;
  }

  private @Nullable String parseCharacterClass() {
    int start = _pos;
    if (!consumeChar('[')) {
      return null;
    }
    String negation = "";
    if (consumeChar('!') || consumeChar('^')) {
      negation = "^";
    }
    String literals = parseClassLiterals();
    if (literals == null || !consumeChar(']')) {
      _pos = start;
      return null;
    }
    return "[" + negation + literals + "]";
  }

  /** Characters that can be used inside a character class. */
  private @Nullable String parseClassLiterals() {
    int start = _pos;
    while (_pos < _input.length() && isClassLiteral(_input.charAt(_pos))) {
      _pos++;
    }
    if (_pos == start) {
      return null;
    }
    return _input.substring(start, _pos);
  }

  /** Characters that can be used outside a character class; a superset of the class literals. */
  private @Nullable String parseAllLiterals() {
    StringBuilder sb = new StringBuilder();
    boolean matched = false;
    while (_pos < _input.length()) {
      char c = _input.charAt(_pos);
      if (isClassLiteral(c)) {
        sb.append(c);
        _pos++;
        matched = true;
      } else if (c == '.') {
        sb.append("\\.");
        _pos++;
        matched = true;
      } else {
        break;
      }
    }
    return matched ? sb.toString() : null;
  }

  private static boolean isClassLiteral(char c) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || (c >= '0' && c <= '9')
        || c == '-'
        || c == '_'
        || c == ':'
        || c == '/'
        || c == ',';
  }

  private boolean consumeChar(char c) {
    if (_pos < _input.length() && _input.charAt(_pos) == c) {
      _pos++;
      return true;
    }
    return false;
  }
}
