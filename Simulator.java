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
    private int population;
    private static double OPENNESS = 1.0; //1.0 signifies a fully open state
    private static int EVENT_CAP = 50; //max people at one high risk event
    private int daysToSim;
    private static double quarantineRate = 0.5; //proportion of total infected days where the infected person is quarantined
    private static final int DIFFUSAL_FACTOR = 20; //in determineEvents, how many people per event the needed transmission is 
                                                   //diffused into.  less people per event --> more events
                                                   //more events -->> higher concentration of cases among low-adherence people
    
    private ArrayList<Event> events = new ArrayList<Event>();
    
    /* note: initial cases in constructor are different than in determineEvents.  These initial cases are actual initial cases
     * before starting simulation.  InitCases in determineEvents is cases in previous days used when determining events
     */
    public Simulator(int population, int initialCases, int days) 
    {
        state = new State(population);
        Iterator<Person> itr = state.getPeople().iterator();
        daysToSim = days;
        this.population = population;
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
    
    /* could definitely add more parameters to vary the features of the events generated.  Fine for now */
    public void determineEvents(int initCases, int finalCases) //populates inst var with events that would be needed per week to
                                                               //cause the given rise in cases
    {
        double effIFR = ((double)initCases) / ((double) population) * (1.0-quarantineRate); //effective ifr at the start 
        double ifrPrime = ((double)finalCases) / ((double)population); //final ifr
        double t = 1 - Math.exp((Math.log(1 - ifrPrime)) / (effIFR * DIFFUSAL_FACTOR)); /* transmission rate in order to 
        have pop/diffusal number of events all with the same t-rate in order to get the population to ifrPrime.  Equation
        derivation is in my notes*/
        
        for(int i = 0; i < population / DIFFUSAL_FACTOR; i++)
        {
            events.add(new Event(DIFFUSAL_FACTOR, t, false));
        }
        
    }
    
    public void run()
    {
        
    }
    
    public static void main(String [] args)
    {
        System.out.print("\f");
        Simulator s = new Simulator(100, 10, 20);
        s.determineEvents(10, 5);
    }
    
}
