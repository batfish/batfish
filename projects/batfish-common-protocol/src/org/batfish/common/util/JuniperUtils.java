package org.batfish.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.batfish.common.BatfishException;

/**
 * This class is a derivative work based on the perl module Crypt-Juniper
 * <http://search.cpan.org/~kbrint/Crypt-Juniper/lib/Crypt/Juniper.pm> by Kevin
 * Brintnall
 *
 * Permission to release this derived code under the Apache 2.0 License was
 * granted by Kevin Brintnall on 2015/12/04
 */
public final class JuniperUtils {

   private static final JuniperUtils INSTANCE = new JuniperUtils();

   public static final String SALT = "'rn5;/Jq_g,ROS-ObQ.l+h)*+(Amq?]Wn75zq3eN6kI}VZon03etYA>}{&{[~|)/}l}QXAgpG_wnv[w~JG?|(LfWe88#omyZv.PcOSDU5j2_9C5p9kZ7`UiHry67%<;mHO*Mz=Mn/G-R%(=Onc6A^ps^28%4xAU*i5&/Y<3L/v-dzI0dtjw=[p1=[pn[Z_8W~uW5K{9{HOCKwi6@6DfrG?Ds*HZ!ai,a$+LzpC/,bvp2:}t4<Ol3sE|I&|6'<NNZ}SE#voQd9*[yZjrnP~?1R^+Un%oOee!D#7}<=QEJ<@*&IRpC,5LH+OKVg,?^M4IAq9x8tvcRC]g7KG:U?Q@PvD[Od's|.`sH1#/IEzMoDu+k`1DnW+XsAt3?*n1=z:W&O!VQ--:U&wF]&Bj4O(51NsSU4{I0%3$}bi3_[_[y<A}Zbil%Rd^Q{)4F_lIoNl.AVyEM)O<d%pD+Q~X&:P0CyTcAfav[|zE;Q$x%ib^oS_epZ20)l.w&ZaBJHTE1D^dM$/w~Zs6[ED2K++PRF1z/ENxaL3^?}tvYWoFRez~dY2E~uk[Y=~7T7E&ME)$LA/%0g'W{oA3j9Q-'gV1YB*)'.fNdIPr'krar)}k[md:V5x]rhze(8?1jEWJJBICH!At<U-:{XSpRCSa_S7d<Pv(oW_Zu^&#UuI6BjydC>ZkDK8_W;zbBJ9T'<:`<!~JL|z4b2o4t'.M??:nU/QF'F0U0H,KO2L8xPL|z`%8w-v,,js/8`Bkg8TR6S<x)z3x,du31n=_0-|xU$0;5/A8zjdJ19hYYOw-g8c_L.9bh^/Ekv-6tHo{x=x!f0P/(P+~j0c]|LQ1kbU-VBs~PiJJXH1h^Bo{3It[Itar%g4b=Jd'YnKA_}okjW72Hy_I_#KnI^784F}0XD}8{W4*#Ceo#l!5qB`ozr._=/$}iEULC?$eB|1hlvZta-*[R31Njig~_c#FQS1N(Y5k{wgC.U1,y-+rx[~HN`Om_}-#vKeOIU'0T0/Y>glh/Fz<rc^cV2Lpi.%T.";

   public static String decryptAndHashJuniper9CipherText(String key) {
      String privateSecret = decryptJuniper9CipherText(key);
      String saltedSecret = privateSecret + SALT;
      String sha256Digest = CommonUtil.sha256Digest(saltedSecret);
      return sha256Digest;
   }

   public static String decryptJuniper9CipherText(String key) {
      return INSTANCE.decrypt(key);
   }

   private final char[] _allCharacters;

   private final Map<Character, Integer> _allCharactersIndexMap;

   private final String[] _characterFamilies;

   private final Map<Character, Integer> _characterFamilyReverseIndexMap;

   private final List<List<Integer>> _codeMatrix;

   private final Pattern _validationRegex;

   private JuniperUtils() {
      _codeMatrix = initCodeMatrix();
      _characterFamilies = new String[] { "QzF3n6/9CAtpu0O",
            "B1IREhcSyrleKvMW8LXx", "7N-dVbwsY2g4oaJZGUDj", "iHkq.mPf5T" };
      _characterFamilyReverseIndexMap = initCharacterFamilyReverseIndexMap();
      _allCharacters = initAllCharacters();
      _allCharactersIndexMap = initAllCharactersIndexMap();
      _validationRegex = initValidationRegex();
   }

   private String decrypt(String key) {
      boolean valid = _validationRegex.matcher(key).matches();
      if (!valid) {
         throw new BatfishException(
               "Invalid Juniper $9$ ciphertext: \"" + key + "\"");
      }
      String[] chars = new String[] { key.substring("$9$".length()) };
      char first = nibble(chars, 1).charAt(0);
      nibble(chars, _characterFamilyReverseIndexMap.get(first));
      char prev = first;
      String decrypted = "";

      while (chars[0].length() > 0) {
         List<Integer> decode = _codeMatrix
               .get(decrypted.length() % _codeMatrix.size());
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
         decrypted += newChar;
      }
      return decrypted;
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
      for (int characterFamilyIndex = 0; characterFamilyIndex < _characterFamilies.length; characterFamilyIndex++) {
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
      codeMatrix.add(Arrays.asList(new Integer[] { 1, 4, 32 }));
      codeMatrix.add(Arrays.asList(new Integer[] { 1, 16, 32 }));
      codeMatrix.add(Arrays.asList(new Integer[] { 1, 8, 32 }));
      codeMatrix.add(Arrays.asList(new Integer[] { 1, 64 }));
      codeMatrix.add(Arrays.asList(new Integer[] { 1, 32 }));
      codeMatrix.add(Arrays.asList(new Integer[] { 1, 4, 16, 128 }));
      codeMatrix.add(Arrays.asList(new Integer[] { 1, 32, 64 }));
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
