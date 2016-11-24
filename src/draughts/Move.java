
package draughts;

import java.io.*;

import util.*;

public class Move {

   public static final int None = 0;
   public static final int Amb = 1;

   static long make(int from, int to) {
      return make(from, to, 0);
   }

   static long make(int from, int to, long caps) {
      assert Square.is_valid(from);
      assert Square.is_valid(to);
      assert (caps & 077) == 0;
      return (caps << 6) | (((long) from) << 6) | ((long) to);
   }

   static int from(long mv) {
      return (int) ((mv >> 6) & 077);
   }

   static int to(long mv) {
      return (int) (mv & 077);
   }

   static long caps(long mv) {
      return (mv >> 6) & ~077;
   }

   public static long bit(long mv) { // for GUI

      if (mv == None) return 0;

      int  from = Move.from(mv);
      int  to   = Move.to(mv);
      long caps = Move.caps(mv);

      long b = caps;
      b = Bit.set(b, from);
      if (to != from) b = Bit.set(b, to); // for debug

      return b;
   }

   public static boolean is_legal(long mv, Pos pos) {
      List list = Gen.gen_moves(pos);
      return list.has(mv);
   }

   public static boolean is_conversion(long mv, Pos pos) {

      int  from = Move.from(mv);
      long caps = Move.caps(mv);

      return caps != 0 || Piece.is_man(pos.square(from));
   }

   public static String to_string(long mv, Pos pos) {

      assert is_legal(mv, pos);

      int  from = Move.from(mv);
      int  to   = Move.to(mv);
      long caps = Move.caps(mv);

      String s = "";

      s += Square.to_string(from);
      s += (caps != 0) ? "x" : "-";
      s += Square.to_string(to);

      return s;
   }

   public static String to_hub(long mv) {

      int  from = Move.from(mv);
      int  to   = Move.to(mv);
      long caps = Move.caps(mv);

      String s = "";

      s += Square.to_string(from);
      s += (caps != 0) ? "x" : "-";
      s += Square.to_string(to);

      for (long b = caps; b != 0; b = Bit.rest(b)) {
         int sq = Bit.first(b);
         s += "x" + Square.to_string(sq);
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

