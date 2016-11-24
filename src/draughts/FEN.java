
package draughts;

import java.io.*;

import util.*;

public class FEN {

   public static final String Start = "W:W31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50:B1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20";

   public static String to_fen(Pos pos) {

      String s = "";

      s += output_side(pos.turn());

      for (int sd = 0; sd < Side.Size; sd++) {
         s += ":";
         s += output_side(sd);
         s += output_pieces(pos.man(sd), pos.king(sd));
      }

      return s;
   }

   private static String output_side(int sd) {
      return (sd == Side.White) ? "W" : "B";
   }

   private static String output_pieces(long man, long king) {

      String s = "";

      for (long b = man | king; b != 0; b = Bit.rest(b)) {

         int sq = Bit.first(b);

         if (!s.equals("")) s += ",";
         if (Bit.is_set(king, sq)) s += "K";
         s += Square.to_string(sq);
      }

      return s;
   }

   public static Pos from_fen(String s) throws Bad_Input {

      Number_Scanner scan = new Number_Scanner(s);

      int turn = parse_side(scan);

      long[] man  = new long[Side.Size];
      long[] king = new long[Side.Size];

      while (!scan.is_end()) {

         String sep = scan.get_token();
         if (!sep.equals(":")) throw new Bad_Input();

         int sd = parse_side(scan);
         Pieces p = parse_pieces(scan);
         man[sd]  |= p.man();
         king[sd] |= p.king();
      }

      return new Pos(turn, man[Side.White], man[Side.Black], king[Side.White], king[Side.Black]);
   }

   private static int parse_side(Number_Scanner scan) throws Bad_Input {

      String side = scan.get_token();

      if (side.equals("W")) {
         return Side.White;
      } else if (side.equals("B")) {
         return Side.Black;
      } else {
         throw new Bad_Input();
      }
   }

   private static Pieces parse_pieces(Number_Scanner scan) throws Bad_Input {

      long man = 0;
      long king = 0;

      while (true) {

         boolean is_king = false;

         String t = scan.get_token();
         if (t.equals(",")) t = scan.get_token();

         if (t.equals("")) { // EOS
            break;
         } else if (t.equals(":")) {
            scan.unget_char(); // HACK: no unget_token
            break;
         } else if (t.equals("K")) {
            is_king = true;
            t = scan.get_token();
         }

         int from = parse_square(t);
         int to = from;

         if (!scan.is_end()) {
            if (scan.get_token().equals("-")) {
               to = parse_square(scan.get_token());
            } else {
               scan.unget_char(); // HACK: no unget_token
            }
         }

         if (from > to) throw new Bad_Input();

         for (int sq = from; sq <= to; sq++) {
            if (is_king) {
               king = Bit.set(king, Square.from_50(sq - 1));
            } else {
               man = Bit.set(man, Square.from_50(sq - 1));
            }
         }
      }

      return new Pieces(man, king);
   }

   private static int parse_square(String s) throws Bad_Input {
      int sq = Integer.parseInt(s);
      if (sq < 1 || sq > 50) throw new Bad_Input();
      return sq;
   }
}

class Pieces {

   private long p_man;
   private long p_king;

   Pieces(long man, long king) {
      p_man = man;
      p_king = king;
   }

   long man() {
      return p_man;
   }

   long king() {
      return p_king;
   }
}

