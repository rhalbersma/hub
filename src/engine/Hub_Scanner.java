
package engine;

import draughts.Bad_Input;
import util.Pair;

public class Hub_Scanner {

   private final String p_string;
   private int p_pos;

   public Hub_Scanner(String s) {
      p_string = s;
      p_pos = 0;
   }

   public String get_command() throws Bad_Input {
      return get_name();
   }

   public Pair get_pair() throws Bad_Input {

      String name = get_name(); // <name>

      String value = "";

      skip_blank();

      if (peek_char() == '=') { // = <value>
         skip_char();
         value = get_value();
      }

      return Pair.make(name, value);
   }

   public String get_name() throws Bad_Input {

      String name = "";

      skip_blank();

      while (is_id(peek_char())) {
         name += get_char();
      }

      if (name.isEmpty()) throw new Bad_Input(); // not a name

      return name;
   }

   public String get_value() throws Bad_Input {

      String value = "";

      skip_blank();

      if (peek_char() == '"') { // "<value>"

         skip_char();

         while (peek_char() != '"') {
            if (is_end_low()) throw new Bad_Input(); // missing closing quote
            value += get_char();
         }

         skip_char();

      } else { // <value>

         value = get_name();
      }

      return value;
   }

   public boolean is_end() {
      skip_blank();
      return is_end_low();
   }

   private void skip_blank() {

      while (is_blank(peek_char())) {
         skip_char();
      }
   }

   private void skip_char() {
      assert !is_end_low();
      p_pos++;
   }

   private char get_char() {
      assert !is_end_low();
      return p_string.charAt(p_pos++);
   }

   private char peek_char() {
      return is_end_low() ? '\0' : p_string.charAt(p_pos); // HACK but makes parsing easier
   }

   private boolean is_end_low() {
      return p_pos == p_string.length();
   }

   private static boolean is_blank(char c) {
      return Character.isWhitespace(c);
   }

   private static boolean is_id(char c) {
      return !Character.isISOControl(c) && !is_blank(c) && c != '=' && c != '"'; // excludes '\0'
   }
}

