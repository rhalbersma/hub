
package engine;

public interface Engine_User {

   void engine_ready(String name);
   void engine_quit();

   void engine_search_info(String info);
   void engine_move(long mv, long expecting);

   void engine_pong();
}

