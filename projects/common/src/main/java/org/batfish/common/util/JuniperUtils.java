package org.batfish.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;

/**
 * This class is a derivative work based on the perl module Crypt-Juniper by Kevin Brintnall.
 *
 * <p>Permission to release this derived code under the Apache 2.0 License was granted by Kevin
 * Brintnall on 2015/12/04. @see <a
 * href=http://search.cpan.org/~kbrint/Crypt-Juniper/lib/Crypt/Juniper.pm></a>
 */
public final class JuniperUtils {

  private static final JuniperUtils INSTANCE = new JuniperUtils();

  public static String decryptAndHashJuniper9CipherText(String key) {
    String privateSecret = decryptJuniper9CipherText(key);
    String saltedSecret = privateSecret + CommonUtil.salt();
    String sha256Digest = CommonUtil.sha256Digest(saltedSecret);
    return sha256Digest;
  }

  public static String decryptJuniper9CipherText(String key) {
    return INSTANCE.decrypt(key);
  }

  public static boolean isJuniper9CipherText(String text) {
    return INSTANCE._validationRegex.matcher(text).matches();
  }

  private final char[] _allCharacters;

  private final Map<Character, Integer> _allCharactersIndexMap;

  private final String[] _characterFamilies;

  private final Map<Character, Integer> _characterFamilyReverseIndexMap;

  private final List<List<Integer>> _codeMatrix;

  private final Pattern _validationRegex;

  private JuniperUtils() {
    _codeMatrix = initCodeMatrix();
    _characterFamilies =
        new String[] {
          "QzF3n6/9CAtpu0O", "B1IREhcSyrleKvMW8LXx", "7N-dVbwsY2g4oaJZGUDj", "iHkq.mPf5T"
        };
    _characterFamilyReverseIndexMap = initCharacterFamilyReverseIndexMap();
    _allCharacters = initAllCharacters();
    _allCharactersIndexMap = initAllCharactersIndexMap();
    _validationRegex = initValidationRegex();
  }

  private String decrypt(String key) {
    if (!isJuniper9CipherText(key)) {
      throw new BatfishException("Invalid Juniper $9$ ciphertext: \"" + key + "\"");
    }
    String[] chars = new String[] {key.substring("$9$".length())};
    char first = nibble(chars, 1).charAt(0);
    nibble(chars, _characterFamilyReverseIndexMap.get(first));
    char prev = first;
    StringBuilder decrypted = new StringBuilder();

    while (!chars[0].isEmpty()) {
      List<Integer> decode = _codeMatrix.get(decrypted.length() % _codeMatrix.size());
      int len = decode.size();
      char[] nibbleChars = new char[len];
      nibble(chars, len).getChars(0, len, nibbleChars, 0);
      List<Integer> gaps = new ArrayList<>();
      for (int i = 0; i < len; i++) {
        char nibbleChar = nibbleChars[i];
        int g = gap(prev, nibbleChar);
        prev = nibbleChar;
        gaps.add(g);
      }
      char newChar = gapDecode(gaps, decode);
      decrypted.append(newChar);
    }
    return decrypted.toString();
  }

  private int gap(char a, char b) {
    int diff = _allCharactersIndexMap.get(b) - _allCharactersIndexMap.get(a);
    int positiveDiff = diff + _allCharacters.length;
    int ret = positiveDiff % _allCharacters.length - 1;
    return ret;
  }

  private char gapDecode(List<Integer> gaps, List<Integer> codeRow) {
    int num = 0;
    if (gaps.size() != codeRow.size()) {
      throw new BatfishException("Gaps size does not match codeRow size");
    }
    for (int i = 0; i < gaps.size(); i++) {
      num += gaps.get(i) * codeRow.get(i);
    }
    int val = num % 256;
    char ret = (char) val;
    return ret;
  }

  private char[] initAllCharacters() {
    String allCharacters = String.join("", _characterFamilies);
    int len = allCharacters.length();
    char[] numAlpha = new char[len];
    allCharacters.getChars(0, len, numAlpha, 0);
    return numAlpha;
  }

  private Map<Character, Integer> initAllCharactersIndexMap() {
    Map<Character, Integer> alphaNum = new HashMap<>();
    for (int i = 0; i < _allCharacters.length; i++) {
      alphaNum.put(_allCharacters[i], i);
    }
    return alphaNum;
  }

  private Map<Character, Integer> initCharacterFamilyReverseIndexMap() {
    Map<Character, Integer> extra = new HashMap<>();
    for (int characterFamilyIndex = 0;
        characterFamilyIndex < _characterFamilies.length;
        characterFamilyIndex++) {
      String characterFamily = _characterFamilies[characterFamilyIndex];
      for (int i = 0; i < characterFamily.length(); i++) {
        char c = characterFamily.charAt(i);
        extra.put(c, 3 - characterFamilyIndex);
      }
    }
    return extra;
  }

  private List<List<Integer>> initCodeMatrix() {
    List<List<Integer>> codeMatrix = new ArrayList<>();
    codeMatrix.add(Arrays.asList(1, 4, 32));
    codeMatrix.add(Arrays.asList(1, 16, 32));
    codeMatrix.add(Arrays.asList(1, 8, 32));
    codeMatrix.add(Arrays.asList(1, 64));
    codeMatrix.add(Arrays.asList(1, 32));
    codeMatrix.add(Arrays.asList(1, 4, 16, 128));
    codeMatrix.add(Arrays.asList(1, 32, 64));
    return codeMatrix;
  }

  private Pattern initValidationRegex() {
    String allCharacters = String.join("", _characterFamilies);
    String allCharactersAdjusted = allCharacters.replaceAll("-", "") + "-";
    String regexText = "^\\$9\\$[" + allCharactersAdjusted + "]{4,}$";
    return Pattern.compile(regexText);
  }

  private String nibble(String[] chars, int length) {
    String nib = chars[0].substring(0, length);
    chars[0] = chars[0].substring(length);
    return nib;
  }
}
