
package util;

public class Number_Scanner { // group digits

   private final String p_string;
   private int p_pos;

   public Number_Scanner(String s) {
      p_string = s;
      p_pos = 0;
   }

   public boolean is_end() {
      return p_pos == p_string.length();
   }

   public String get_token() {

      if (is_end()) return "";

      String s = "";

      char c = p_string.charAt(p_pos++);
      s += c;

      if (Character.isDigit(c)) {

         while (!is_end()) {
            c = p_string.charAt(p_pos);
            if (!Character.isDigit(c)) break;
            s += c;
            p_pos++;
         }
      }

      return s;
   }

   public void unget_char() {
      assert p_pos > 0;
      p_pos--;
   }
}

