
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
       maine.people.get(0).infect();
       while(true)
       {
         System.out.println(maine.numInfected());
         maine.advanceDay();
       }
   }
}
