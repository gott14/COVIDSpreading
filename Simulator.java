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
    private static double quarantineRate = 0.35; //proportion of total infected days where the infected person is quarantined
    private static int incubation = 3; //used in setup as incubation period
    private static int sicknessLen = 10; //days youre sick with covid (assumption for setup function), starting from infection
    private static final int DIFFUSAL_FACTOR = 7500; //in determineEvents, how many people per event the needed transmission is 
                                                   //diffused into.  less people per event --> more events
                                                   //more events -->> higher concentration of cases among low-adherence people
                                                   //program performance is very sensitive with respect to diffusal factor
    
    //for Maine with STATEPOP=1300000, POP=200000, correct DIFFUSAL_FACTOR is b/w 3000 and 4000, probably closer to 3000
    //more tests should probably be done since this is a very important parameter -->backtest
    private static ArrayList<Event> events = new ArrayList<Event>();
    private ArrayList<Integer> pastCases = new ArrayList<Integer>();
    private static int POP = 500000; 
    //DIFFUSAL_FACTOR should be increased proportionally to POP to maintain similar execution
    private static int STATEPOP = 1300000; //should input this, days to sim, and maybe mortality through the csv file
    private static int SCALED_BY = STATEPOP / POP;
    private static int EVENTSPERDAY = POP / DIFFUSAL_FACTOR;
    public Simulator(ArrayList<Integer> pastCases, int days)
    {
        daysToSim = days;
        this.pastCases = pastCases;
        setup();
        this.state = new State(POP); 
        int cases = 0;
        for(int k = pastCases.size()-sicknessLen; k < pastCases.size(); k++) //make sure this is the correct direction to traverse
        {
           cases += pastCases.get(k);
        }
        int [] caseDist = new int[sicknessLen];
        int adjCases = cases / SCALED_BY;
        int ctr = 0;
        while(adjCases > 0)
        {
            caseDist[ctr]++;
            adjCases--;
            ctr++;
            if(ctr >= caseDist.length)
                ctr = 0;
        }
        for(int i = 0; i < caseDist.length; i++)
        {
            state.batchInfect(caseDist[i]);
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
    //should add a way so that initial events can be determined given that there is already a mask mandate in place
    private void determineEvents(int initCases, int finalCases, boolean mask) //populates inst var with events that would be needed per week to
                                                               //cause the given rise in cases
    {
        double effIFR = ((double)initCases) / ((double) POP) * (1.0-quarantineRate); //effective ifr at the start 
        double ifrPrime = ((double)finalCases) / ((double)POP); //final ifr
        /**double t = 1 - Math.exp((Math.log(1 - ifrPrime)) / (effIFR * DIFFUSAL_FACTOR)); **/ /* transmission rate in order to 
        have pop/diffusal number of events all with the same t-rate in order to get the population to ifrPrime.  Equation
        derivation is in my notes*/
        
        double presInf = effIFR * (double)DIFFUSAL_FACTOR; //presumed infected at each event
        //new way to calculate t, seems like it works about the same
        double t = (double)finalCases / (((double)DIFFUSAL_FACTOR - presInf) * presInf * (double)EVENTSPERDAY); 
        if(mask)
            t = t / state.getMaskEff(); //if already a mask mandate, adjust t to account for turning on the mandate
                                        //at the beginning of the sim
        
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
            determineEvents(cases, cases + pastCases.get(i), false);
        }
    }
    
    public static void run(String filename, int days, boolean mask) throws Exception
    {
        Scanner sc = new Scanner(new File(filename)); //most recent are at the top
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
        if(mask)
            state.maskMandate();
        for(int i = 0; i < daysToSim; i++)
        {
            double effIFR = ((double)state.getCurrentCases()) / ((double) POP) * (1.0-quarantineRate);
            double predCases = 0;
            for(int k = 0; k < 1; k++) //if k < EVENTSPERDAY is changed to k < 1, program runs as it should
            {
            Event e = events.get(index);
            predCases += (effIFR * e.getMaxSize()) * (e.getIntensity() * Person.getAvgTransmission() * e.getMaxSize());
            state.executeEvent(e.getMaxSize(), e.getIntensity(), e.allowsSlack());
            index++;
            if(index >= events.size())
                index = 0;
            }
            
            System.out.print("Total cases: " + state.numTotalCases() * SCALED_BY);
            System.out.print(" Current cases: " + state.getCurrentCases() * SCALED_BY);
            //System.out.print(" Pred new cases: " + predCases * SCALED_BY);
            int dcases = state.advanceDay();
            System.out.print(" Daily Cases: " + dcases * SCALED_BY + "\n");
        }
    }
    
    public static void main(String [] args) throws Exception
    {
        String file = args[0];
        int DAYS = 60;
        run(file, DAYS, false);
    }
    
}
