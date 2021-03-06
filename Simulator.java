import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Random;
import java.util.*;
import java.io.*;
/**
 * Write a description of class Simulator here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Simulator
{
    private static State state;
    private String stateName;
    private static double OPENNESS = 1.0; //1.0 signifies a fully open state
    private static int EVENT_CAP = 50; //max people at one high risk event
    private static int daysToSim;
    private static final int SETUP_DAYS = 7; //days looked at in setup function
    private static double quarantineRate = 0.65; //proportion of total infected days where the infected person is quarantined
    private static int incubation = 3; //used in setup as incubation period
    private static int sicknessLen = 10; //days youre sick with covid (assumption for setup function), starting from infection
    private static final int DIFFUSAL_FACTOR = 1000; //in determineEvents, how many people per event the needed transmission is 
                                                   //diffused into.  less people per event --> more events
                                                   //more events -->> higher concentration of cases among low-adherence people
                                                 
    
    private static ArrayList<Event> events = new ArrayList<Event>();
    private ArrayList<Integer> pastCases = new ArrayList<Integer>();
    private static int POP = 100000; //should input this, days to sim, and maybe mortality through the csv file
    private static int EVENTSPERDAY = POP / DIFFUSAL_FACTOR;
    public Simulator(ArrayList<Integer> pastCases, int days)
    {
        daysToSim = days;
        this.pastCases = pastCases;
        setup();
        this.state = new State(POP); 
        int n = 0;
        for(int k = pastCases.size()-sicknessLen; k < pastCases.size(); k++)
        {
           n += pastCases.get(k);
           state.batchInfect(pastCases.get(k));
           state.advanceDay();
        }
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
    private void determineEvents(int initCases, int finalCases) //populates inst var with events that would be needed per week to
                                                               //cause the given rise in cases
    {
        double effIFR = ((double)initCases) / ((double) POP) * (1.0-quarantineRate); //effective ifr at the start 
        double ifrPrime = ((double)finalCases) / ((double)POP); //final ifr
        double t = 1 - Math.exp((Math.log(1 - ifrPrime)) / (effIFR * DIFFUSAL_FACTOR)); /* transmission rate in order to 
        have pop/diffusal number of events all with the same t-rate in order to get the population to ifrPrime.  Equation
        derivation is in my notes*/
        
        for(int i = 0; i < POP / DIFFUSAL_FACTOR; i++)
        {
            events.add(new Event(DIFFUSAL_FACTOR, t / Person.getAvgTransmission(), false));
        }
        
    }
    
    public void setup() //list of number of cases in each of past number of days
    {
        for(int i = pastCases.size() - SETUP_DAYS; i < pastCases.size(); i++)
        {
            int cases = 0;
            for(int j = incubation; j <= sicknessLen; j++)
            {
                cases += pastCases.get(i-j);
            }
            determineEvents(cases, cases + pastCases.get(i));
        }
    }
    
    public static void run(String filename, int days) throws Exception
    {
        Scanner sc = new Scanner(new File(filename));
        sc.useDelimiter(",");
        ArrayList<Integer> cases = new ArrayList<Integer>();
        while(sc.hasNextLine())
        {
            String s = sc.nextLine();
            double d = Double.parseDouble(s);
            int i = (int) d;
            cases.add(0, i); //add at beginning
        }
        Simulator s = new Simulator(cases, days);
        
        int index = 0;
        System.out.println("Starting");
        for(int i = 0; i < daysToSim; i++)
        {
            for(int k = 0; k < EVENTSPERDAY; k++)
            {
            Event e = events.get(index);
            state.executeEvent(e.getMaxSize(), e.getIntensity(), e.allowsSlack());
            index++;
            if(index >= events.size())
                index = 0;
            }
            System.out.print("Current cases: " + state.getCurrentCases());
            int dcases = state.advanceDay();
            System.out.print(" Daily Cases: " + dcases + "\n");
        }
    }
    
    public static void main(String [] args) throws Exception
    {
        String file = args[0];
        int DAYS = 30;
        run(file, DAYS);
    }
    
}
