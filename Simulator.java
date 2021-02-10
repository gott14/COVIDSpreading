import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Random;
import java.util.*;
/**
 * Write a description of class Simulator here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Simulator
{
    private State state;
    private String stateName;
    private static double OPENNESS = 1.0; //1.0 signifies a fully open state
    private static int EVENT_CAP = 50; //max people at one high risk event
    private int daysToSim;
    
    private ArrayList<Event> events;
    
    public Simulator(int population, int initialCases, int days)
    {
        state = new State(population);
        Iterator<Person> itr = state.getPeople().iterator();
        daysToSim = days;
        for(int i = 0; i < initialCases; i++) 
        {
            itr.next().infect(0.5);
        }
    }
    
    public Simulator(int days, String state)
    {
        daysToSim = days;
        
    }
    public static void setOpenness(double n)
    {
        OPENNESS = n;
    }
    
    public void setName(String n)
    {
        stateName = n;
    }
    
    public static void setEventCap(int n)
    {
        EVENT_CAP = n;
    }
    
    public State getState()
    {
        return state;
    }
    
    public void determineEvents(int initCases, int finalCases) //populates inst var with events that would be needed per week to
                                                               //cause the given rise in cases
    {
        
    }
    
    public void run()
    {
        
    }
    
    public static void main(String [] args)
    {
        System.out.print("\f");
        Simulator s = new Simulator(100, 10, 20);
        s.run();
    }
}
