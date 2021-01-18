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
    
    private static double LOW_RISK_MULT = 0.3; //multiplier for low risk events
    private static double MED_RISK_MULT = 1.0;
    private static double HIGH_RISK_MULT = 4.0;
    
    
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
    
    public void run()
    {
        for(int d = 0; d < daysToSim; d++)
        {
            for(int i = 0; i < 1; i++)
                state.executeEvent(state.getPopulation() / 2, LOW_RISK_MULT);
            for(int k = 0; k < 1; k++)
                state.executeEvent(state.getPopulation() / 10, MED_RISK_MULT);
            for(int j = 0; j < 1; j++)
                state.executeEvent(state.getPopulation() / 2, HIGH_RISK_MULT);
        }
        
        System.out.println("Currently infected: " + state.numInfected());
        System.out.println("Total infected: " + state.numTotalCases());
        System.out.println("Total deaths: " + state.numDeaths());
    }
    
    public static void main(String [] args)
    {
        System.out.print("\f");
        Simulator s = new Simulator(100, 10, 20);
        s.run();
    }
}
