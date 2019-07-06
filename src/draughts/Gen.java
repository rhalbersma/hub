
package draughts;

import java.io.*;

public class Gen {

   public static List gen_moves(Pos pos) {

      List list = new List();

      add_captures(list, pos);
      if (list.size() == 0) add_quiets(list, pos);

      return list;
   }

   private static void add_quiets(List list, Pos pos) {

      int atk = pos.turn();
      long empty = pos.empty();

      // man moves

      for (int dir = 0; dir < 2; dir++) {

         int inc = Square.side_dir_inc[atk][dir];

         for (long froms = pos.piece_side(Piece.Man, atk) & Bit.shift(empty, -inc); froms != 0; froms = Bit.rest(froms)) {
            int from = Bit.first(froms);
            list.add_move(from, from + inc);
         }
      }

      // king moves

      for (long froms = pos.piece_side(Piece.King, atk); froms != 0; froms = Bit.rest(froms)) {

         int from = Bit.first(froms);

         for (long tos = Bit.king_moves(from) & empty; tos != 0; tos = Bit.rest(tos)) {
            int to = Bit.first(tos);
            if (Bit.is_incl(Bit.between(from, to), empty)) list.add_move(from, to);
         }
      }
   }

   private static void add_captures(List list, Pos pos) {

      int atk = pos.turn();
      int def = Side.opp(atk);

      long opp = pos.side(def);
      long empty = pos.empty();

      // man captures

      for (int dir = 0; dir < Square.dir_size; dir++) {

         int inc = Square.dir_inc[dir];

         for (long froms = pos.piece_side(Piece.Man, atk) & Bit.shift(opp, -inc) & Bit.shift(empty, -inc * 2); froms != 0; froms = Bit.rest(froms)) {
            int from = Bit.first(froms);
            add_man_captures(list, from, Bit.bit(from + inc), Bit.clear(opp, from + inc), Bit.set(empty, from), from + inc * 2);
         }
      }

      // king captures

      for (long froms = pos.piece_side(Piece.King, atk); froms != 0; froms = Bit.rest(froms)) {
         int from = Bit.first(froms);
         add_king_captures(list, from, 0, opp, Bit.set(empty, from), from);
      }
   }

   private static void add_man_captures(List list, int start, long caps, long opp, long empty, int from) {

      assert(Bit.has(empty, from));

      for (int dir = 0; dir < Square.dir_size; dir++) {

         int inc = Square.dir_inc[dir];

         if (Bit.has(Bit.shift(opp, -inc) & Bit.shift(empty, -inc * 2), from)) {
            add_man_captures(list, start, Bit.set(caps, from + inc), Bit.clear(opp, from + inc), empty, from + inc * 2);
         }
      }

      list.add_capture(start, from, caps);
   }

   private static void add_king_captures(List list, int start, long caps, long opp, long empty, int from) {

      assert(Bit.has(empty, from));

      for (long targets = Bit.king_captures(from) & opp; targets != 0; targets = Bit.rest(targets)) {

         int cap = Bit.first(targets);

         if (Bit.is_incl(Bit.between(from, cap), empty)) {

            int inc = Bit.inc(from, cap);

            for (int to = cap + inc; Square.is_valid(to) && Bit.has(empty, to); to += inc) {
               add_king_captures(list, start, Bit.set(caps, cap), Bit.clear(opp, cap), empty, to);
            }
         }
      }

      if (caps != 0) list.add_capture(start, from, caps);
   }
}

