
package draughts;

import java.io.*;

import io.*;

public class PDN_Output implements Output {

   Output p_out;
   String p_line;

   PDN_Output(Output out) {
      p_out = out;
      p_line = "";
   }

   void put_tag_pair(String name, String value) {

      assert p_line.length() == 0;

      String line = "";

      line += "[";

      line += name;

      line += " \"";
      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if (c == '\"' || c == '\\') line += '\\';
         line += c;
      }
      line += "\"";

      line += "]";

      p_out.put_line(line);
   }

   public void put_line(String line) {
      assert line.length() == 0;
      p_out.put_line(line);
   }

   public void put_word(String word) {

      assert word.length() != 0;

      assert p_line.length() < 80;

      if (p_line.length() == 0) {

         p_line += word;
         assert p_line.length() < 80;

      } else {

         int size = p_line.length() + 1 + word.length();

         if (size >= 80) {
            p_out.put_line(p_line);
            p_line = word;
         } else {
            p_line += " " + word;
         }

         assert p_line.length() < 80;
      }
   }

   public void flush() {
      if (p_line.length() != 0) {
         p_out.put_line(p_line);
         p_line = "";
      }
   }

   public void close() {
      flush();
      p_out.close();
   }

   public static void save_game(String file_name, Game game) {

      OutputStream os;

      try {
         os = new FileOutputStream(file_name);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
         return;
      }

      Stream_Output so = new Stream_Output(os);
      PDN_Output po = new PDN_Output(so);

      Pos pos = game.start_pos();

      // starting position

      String fen = FEN.to_fen(pos);
      if (!fen.equals(FEN.Start)) {
         po.put_tag_pair("Setup", "1");
         po.put_tag_pair("FEN", fen);
      }

      // moves

      if (game.size() != 0 && pos.turn() != Side.White) {
         po.put_word("1...");
      }

      int parity = (pos.turn() != Side.White) ? 1 : 0;

      for (int i = 0; i < game.size(); i++) {

         int ply = i + parity;

         if (ply % 2 == 0) {
            po.put_word(Integer.toString(ply / 2 + 1) + ".");
         }

         long mv = game.move(i);
         po.put_word(Move.to_string(mv, pos));
         pos = new Pos(pos, mv);
      }

      po.put_word("*");
      po.close();
   }
}

