
/**
 * Write a description of class Test here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Test
{
   public static void main (String [] args)
   { 
       State maine = new State(100);
       for(int i = 0; i < 10; i++)
       {
           maine.getPeople().get(i).infect();
       }
       for(int i = 0; i < 100; i++)
       {
         System.out.println(maine.numInfected());
         for(int k = 0; k < 4; k++) {
         maine.executeEvent(100, 1.0); }
         maine.executeEvent(10, 10.0);
         maine.advanceDay();
       }
   }
}
