
package draughts;

import util.*;

public class FEN {

   public static String to_fen(Pos pos) {

      String s = "";

      s += (pos.turn() == Side.White) ? "W" : "B";
      s += ":W" + output_pieces(pos, Side.White);
      s += ":B" + output_pieces(pos, Side.Black);

      return s;
   }

   private static String output_pieces(Pos pos, int sd) {

      String s = "";

      for (long b = pos.side(sd); b != 0; b = Bit.rest(b)) {

         int sq = Bit.first(b);

         if (!s.isEmpty()) s += ",";
         if (pos.is_piece(sq, Piece.King)) s += "K";
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

         String t = scan.get_token();
         if (!t.equals(":")) throw new Bad_Input();

         int sd = parse_side(scan);
         long[] piece = parse_pieces(scan);
         man[sd]  |= piece[Piece.Man];
         king[sd] |= piece[Piece.King];
      }

      return new Pos(turn,
                 man[Side.White],
                 man[Side.Black],
                 king[Side.White],
                 king[Side.Black]);
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

   private static long[] parse_pieces(Number_Scanner scan) throws Bad_Input {

      long[] piece = new long[Piece.Size];

      while (true) {

         int pc = Piece.Man;

         String t = scan.get_token();
         if (t.equals(",")) t = scan.get_token();

         if (t.isEmpty()) { // EOS
            break;
         } else if (t.equals(":")) {
            scan.unget_char(); // HACK: no unget_token
            break;
         } else if (t.equals("K")) {
            pc = Piece.King;
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
            piece[pc] = Bit.set(piece[pc], Square.from_std(sq));
         }
      }

      return piece;
   }

   private static int parse_square(String s) throws Bad_Input {
      int sq = Integer.parseInt(s);
      if (sq < 1 || sq > Square.Dense_Size) throw new Bad_Input();
      return sq;
   }
}

