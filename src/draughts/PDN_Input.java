
package draughts;

import java.io.*;

import hub.*;

public class PDN_Input {

   public static Game load_game(String file_name) throws Bad_Input {

      Game game = new Game();

      FileReader file;

      try {
         file = new FileReader(file_name);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
         return game;
      }

      PDN_Scanner scan = new PDN_Scanner(file);

      Token t = scan.get_token();

      // skip tags

      while (t.lexeme().equals("[")) {

         t = scan.get_token();
         if (t.token() != Token.Class.ID) {
            System.err.println(t.lexeme());
            System.err.println("PDN syntax error: expected identifier");
            throw new Bad_Input();
         }
         String name = t.lexeme();

         t = scan.get_token();
         if (t.token() != Token.Class.String) {
            System.err.println(t.lexeme());
            System.err.println("PDN syntax error: expected string");
            throw new Bad_Input();
         }
         String value = t.lexeme();

         t = scan.get_token();
         if (!t.lexeme().equals("]")) {
            System.err.println(t.lexeme());
            System.err.println("PDN syntax error: expected ']'");
            throw new Bad_Input();
         }

         if (name.equals("FEN")) {
            game = new Game(value);
         }

         t = scan.get_token();
      }

      // parse moves

      while (t.token() != Token.Class.EOF
          && t.token() != Token.Class.Result
          && !t.lexeme().equals("[")) {

         if (t.token() == Token.Class.Move_Number) {

            // skip

         } else if (t.token() == Token.Class.Move) {

            Pos pos = game.pos();
            long mv = Move.from_string(t.lexeme(), pos);

            if (mv == Move.None || mv == Move.Amb) {
               pos.disp();
               System.out.println("PDN illegal move: " + t.lexeme());
               System.out.println();
               throw new Bad_Input();
            }

            game.add_move(mv);

         } else {

            System.err.println(t.lexeme());
            System.err.println("PDN syntax error: expected move");
            throw new Bad_Input();
         }

         t = scan.get_token();
      }

      return game;
   }
}

class PDN_Scanner {

   private FileReader p_file;
   private boolean p_undo;
   private int p_char;

   PDN_Scanner(FileReader file) {
      p_file = file;
      p_undo = false;
   }

   Token get_token() throws Bad_Input {

      // skip blanks

      while (true) {

         read_char();

         if (p_char < 0) { // EOF

            return new Token("", Token.Class.EOF);

         } else if (Character.isWhitespace(p_char)) {

            // no-op

         } else if (p_char == '{') {

            while (true) {

               read_char();
               if (p_char < 0) {
                  System.err.println("PDN syntax error: end-of-file in comment");
                  throw new Bad_Input();
               } else if (p_char == '}') {
                  break;
               }
            }

         } else {

            break;
         }
      }

      // scan token

      String lexeme = "";

      if (Character.isDigit(p_char)) {

         lexeme += (char) p_char;
         lexeme += read_digits();

         read_char();

         if (p_char == '.') {

            while (true) {
               read_char();
               if (p_char != '.') break;
            }

            unread_char();

            return new Token(lexeme, Token.Class.Move_Number);

         } else if (p_char == '-' || p_char == 'x') {

            while (p_char == '-' || p_char == 'x') {

               lexeme += (char) p_char;

               String digits = read_digits();
               if (digits.equals("")) {
                  System.err.println("PDN syntax error: expected number in move");
                  throw new Bad_Input();
               }

               lexeme += digits;

               read_char();
            }

            unread_char();

            if (lexeme.equals("2-0") || lexeme.equals("1-1") || lexeme.equals("0-2")) {
               return new Token(lexeme, Token.Class.Result);
            } else {
               return new Token(lexeme, Token.Class.Move);
            }

         } else { // including EOF

            unread_char();

            return new Token(lexeme, Token.Class.Number);
         }

      } else if (Character.isLetter(p_char)) {

         lexeme += (char) p_char;

         while (true) {

            read_char();
            if (p_char < 0 || !(Character.isLetterOrDigit(p_char) || p_char == '_')) {
               unread_char();
               break;
            }

            lexeme += (char) p_char;
         }

         return new Token(lexeme, Token.Class.ID);

      } else if (p_char == '"') {

         while (true) {

            read_char();
            if (p_char < 0) {
               System.err.println("PDN syntax error: end-of-file in string");
               throw new Bad_Input();
            } else if (p_char == '"') {
               break;
            }

            lexeme += (char) p_char;
         }

         return new Token(lexeme, Token.Class.String);

      } else if (p_char == '*') {

         lexeme += (char) p_char;

         return new Token(lexeme, Token.Class.Result);

      } else {

         lexeme += (char) p_char;

         return new Token(lexeme, Token.Class.Other);
      }
   }

   private String read_digits() {

      String lexeme = "";

      while (true) {

         read_char();
         if (p_char < 0 || !Character.isDigit(p_char)) {
            unread_char();
            break;
         }

         lexeme += (char) p_char;
      }

      return lexeme;
   }

   private void read_char() {

      if (p_undo) {

         p_undo = false;

      } else {

         try {
            p_char = p_file.read();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private void unread_char() {
      assert !p_undo;
      p_undo = true;
   }
}

class Token {

   enum Class { ID, String, Number, Move, Move_Number, Result, Other, EOF };

   private String p_lexeme;
   private Class p_token;

   Token(String lexeme, Class token) {

      if (Hub.vars.get_bool("pdn-trace")) {
         System.out.println(lexeme);
      }

      p_lexeme = lexeme;
      p_token = token;
   }

   String lexeme() {
      return p_lexeme;
   }

   Class token() {
      return p_token;
   }
}

