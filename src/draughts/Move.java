
package draughts;

import java.io.*;

import util.*;

public class Move {

   public static final long None = 0;
   public static final long Amb  = 1;

   static long make(int from, int to) {
      return make(from, to, 0);
   }

   static long make(int from, int to, long caps) {

      assert Square.is_valid(from);
      assert Square.is_valid(to);
      assert Bit.is_valid(caps);

      assert !Bit.has(caps, from);
      assert !Bit.has(caps, to);

      return Bit.bit(from) | Bit.bit(to) | caps;
   }

   public static long bit(long mv) { // for GUI
      return mv;
   }

   public static boolean is_legal(long mv, Pos pos) {
      List list = Gen.gen_moves(pos);
      return list.has(mv);
   }

   static boolean is_conversion(long mv, Pos pos) {
      return is_capture(mv, pos) || is_man(mv, pos);
   }

   static boolean is_capture(long mv, Pos pos) {
      long caps = mv & pos.side(Side.opp(pos.turn()));
      return caps != 0;
   }

   static boolean is_man(long mv, Pos pos) {
      long froms = mv & pos.side(pos.turn());
      return Bit.has_common(froms, pos.piece(Piece.Man));
   }

   public static String to_string(long mv, Pos pos) {
      return to_string(mv, pos, false);
   }

   public static String to_hub(long mv, Pos pos) {
      return to_string(mv, pos, true);
   }

   private static String to_string(long mv, Pos pos, boolean add_caps) {

      assert is_legal(mv, pos);

      long froms = mv & pos.side(pos.turn());
      long tos   = mv & pos.empty();
      long caps  = mv & pos.side(Side.opp(pos.turn()));

      if (tos == 0) tos = froms; // to = from

      int from = Bit.first(froms);
      int to   = Bit.first(tos);

      String s = "";

      s += Square.to_string(from);
      s += (caps != 0) ? "x" : "-";
      s += Square.to_string(to);

      if (add_caps) {
         for (long b = caps; b != 0; b = Bit.rest(b)) {
            int sq = Bit.first(b);
            s += "x" + Square.to_string(sq);
         }
      }

      return s;
   }

   public static long from_string(String s, Pos pos) throws Bad_Input {

      Number_Scanner scan = new Number_Scanner(s);

      int from = Square.from_string(scan.get_token());

      String sep = scan.get_token();
      if (!sep.equals("-") && !sep.equals("x")) throw new Bad_Input();

      int to = Square.from_string(scan.get_token());

      if (!scan.is_end()) throw new Bad_Input();

      List list = Gen.gen_moves(pos);
      return list.find(Bit.bit(from) | Bit.bit(to));
   }

   public static long from_hub(String s) throws Bad_Input {

      Number_Scanner scan = new Number_Scanner(s);

      int from = Square.from_string(scan.get_token());

      String sep = scan.get_token();
      if (!sep.equals("-") && !sep.equals("x")) throw new Bad_Input();

      int to = Square.from_string(scan.get_token());

      long caps = 0;

      while (!scan.is_end()) {

         String cap = scan.get_token();
         if (!cap.equals("x")) throw new Bad_Input();

         int sq = Square.from_string(scan.get_token());
         caps = Bit.set(caps, sq);
      }

      return make(from, to, caps);
   }
}

