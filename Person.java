import java.util.ArrayList;
import java.util.Random;
import java.util.*;
/**
 * Write a description of class Person here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Person implements Comparator<Person>
{
    private State state;
    
    private final static double ADH_DEVIATION = 0.2; //default st dev for adherence distribution
    private final static double MOR_DEVIATION = 0.002; //default st dev for mortality distribution
    private final static double AVG_MOR = 0.01; //mortality rate
    private static double AVG_ADH = 0.75; //average amount of adherence to rules
    private final static double AVG_INF = 14.0; //average days infected
    private final static double DEV_INF = 1.0; //st dev of days infected
    private final static double AVG_LAG = 3.0; //avg days between infection and contagious
    private final static double DEV_LAG = 0.2; //stdev of lag
    private final static double AVG_T_P = 0.85/AVG_INF; //avg daily transmission rate for primary contact - based on cruise ship study
    private final static double DEV_T_P = 0.05; //st dev of transmission rate for primary contact
    private static double AVG_T_S = 0.1; //avg daily transmission rate for 2ndary contact (meaning someone not in your pod, but 
                                            //someone you interact with at an event). not final because it can be affected by a mask mandate
                                            //baseline rate for events, can be higher with higher intensity (or lower)
    private final static double DEV_T_S = 0.05; //st dev of transmission rate of 2ndary contact
    private double transmission; //how easily this person sheds virus to secondary contacts
    private boolean infected; 
    private boolean contagious;
    private double eventPropensity;
    private double adherence; //between 0 and 1, 1 being full adherence to state policy, also representing propensity to go to events
    private double mortality; //person's mortality rate/probability given average severity
    private ArrayList<Contact> primary; //primary contacts
    private int lag; //days until the person is contagious, -1 if not infected
    private int daysInfected; //-1 if not infected; if infected, days until not infected
    private boolean aware; //true if the person knows they have covid
    private boolean symptoms; //true if they will be symptomatic 
    private int symptomLag; //days between contagious and symptoms, -1 if asymptomatic
    private static double SYMP_LAG = 3.0; //average days between contagious and symptoms, if symptomatic case
    private static double SYMP_LAG_DEV = 0.5; //st dev of the above
    private int daysTillResults; //days until test results.  -1 if not waiting on results
    
    private static double TESTING_FREQ = 0.01; //percentage of asymptiomatic people who get tested on any given day of their having covid
    private static double TESTING_LAG = 3.0; //average days to get test result
    private static double T_LAG_DEV = 0.2; //st dev for time to get test results
    private static double ASYMP = 0.4; //percentage that are asymptomatic and won't get tested
    
    private final static int IMMUNITY_LEN = 120; //days of immunity from covid
    private int immunity_ctr; //countdown to when immunity is over.  -1 when N/A
    private boolean immune; //can't spread or get covid
    
    private final static int VACCINE_LAG = 14; //days until vaccine takes effect
    private final static int VACCINE_IMM_LEN = 365; //how long immunity lasts from a vaccine
    private int vaccine_ctr; //days until vaccine takes effect, -1 if N/A
    
    private double severity_index; //how severe the illness is.  closer to 0 is less severe. Max value of 1.  value of -1.0 when N/A
    private boolean alive;
    /** 
     * Initialize instance variables:
     * willingness to social distance--how much govt policy affects their edge weights
     * infection status
     * mortality risk
     */
    public Person(State st)
    {
        infected = false;
        aware = false;
        Random rand = new Random();
        primary = new ArrayList<Contact>();
        daysInfected = -1;
        lag = -1;
        daysTillResults = -1;
        symptomLag = -1;
        immunity_ctr = -1;
        immune = false;
        vaccine_ctr = -1;
        severity_index = -1.0;
        state = st;
        alive = true;
        do
        {
           adherence = rand.nextGaussian()*ADH_DEVIATION + AVG_ADH;
        } while(adherence < 0.0 || adherence > 1.0);
        eventPropensity = generateEventPropensity();
        do
        {
           mortality = rand.nextGaussian()*MOR_DEVIATION + AVG_MOR;
        } while(mortality < 0.0);
        do
        {
            transmission = rand.nextGaussian()*DEV_T_S + AVG_T_S;
        } while(transmission < 0.0 || transmission >= 1.0);
    }
    /**
     * Compares by current event propensity.  Returns 1 if a is larger, -1 if b is larger, and 0 if equal
     * Explicit comparisons used to ensure there are no errors with double precision being compared to 0.
     */
    public int compare(Person a, Person b)
    {
        if(a.eventPropensity > b.eventPropensity)
            return 1;
        else if(a.eventPropensity < b.eventPropensity)
            return -1;
        return 0;
    }
    
    /**
     * Takes a contact from the State constructor and adds the contact to the list
     */
    public void addPrimary(Person person)
    {
        Random rand = new Random();
        double transmission;
        do
        {
        transmission = rand.nextGaussian()*DEV_T_P + AVG_T_P;
        } while(transmission > 1.0 || transmission < 0.0);
        primary.add(new Contact(this, person, transmission));
    }
    
    public ArrayList<Contact> getPrimary()
    {
        return primary;
    }
    
    public void infect(double sev)
     {
        Random rand = new Random();
        severity_index = sev;
        if(!infected && !immune && alive)
        {
        infected = true;
        do
        {
            daysInfected = (int) (rand.nextGaussian()*DEV_INF + AVG_INF);
        } while(daysInfected < 0);
        do
        {
            lag = (int) (rand.nextGaussian()*DEV_LAG + AVG_LAG);
        } while (lag <= 0);
        
        if(rand.nextDouble() <= (1-ASYMP)*(sev / 0.5)) //avg sev value is 0.5
        {
            symptoms = true;
            do {
            symptomLag = (int) (rand.nextGaussian()*SYMP_LAG + SYMP_LAG_DEV); }  while(symptomLag < 0);
        }
       }
       }
     
    public void die()
    {
        alive = false;
        for(int i = 0; i < primary.size(); i++)
        {
            Person c = primary.get(i).getOther(this);
            c.getPrimary().remove(this);
        }
        state.updateDeaths();
    }
    
    public boolean isInfected()
    {
        return infected;
    }
    
    public boolean isContagious()
    {
        return contagious;
    }
    
    public boolean isImmune()
    {
        return immune;
    }
    
    public boolean isAlive()
    {
        return alive;
    }
    
    public void vaccinate()
    {
        if(!infected)
            vaccine_ctr = VACCINE_LAG;
    }
    
    public double getTransmissionRate()
    {
        if(!immune)
            return transmission;
        else
            return 0.0;
    }
    
    public double generateEventPropensity()
    {
       if(aware)
       {
           return 0;
       }
       else
       {
       Random rand = new Random();
       return rand.nextGaussian() * ADH_DEVIATION + (1.0/adherence);
       }
    }
    
    public static double getAvgTransmission()
    {
        return AVG_T_S;
    }
    
    public static double getAsymp()
    {
        return ASYMP;
    }
    
    public static double getTestingFreq()
    {
        return TESTING_FREQ;
    }
    
    public double getEventPropensity()
    {
        return eventPropensity;
    }
    
    public void implementMaskMandate(double maskEff)
    {
        transmission = transmission * (1 - (maskEff * adherence));
    }
    
    public void undoMaskMandate(double maskEff)
    {
        transmission = transmission / (1 - (maskEff * adherence));
    }
    
    public void adjustAdherence(double change) //negative change means adherence will decrease
    {
        adherence = adherence + change;
        AVG_ADH = AVG_ADH + change;
    }
    
    public void advance()
    {
        Random rand = new Random();
        if(alive)
        {
        if(infected)
        {
            daysInfected--;
            if(lag > 0)
                lag--;
            if(lag == 0) {
                contagious  = true;
                state.updateCases(); 
                lag = -1;   }
            if(daysTillResults > 0)
                daysTillResults--;
            if(daysTillResults == 0)
                aware = true;
            if(symptomLag > 0 && symptoms)
                symptomLag--;
            if(symptomLag == 0 && symptoms)
                aware = true;
            if(rand.nextDouble() < TESTING_FREQ && daysTillResults < 0)
            {
                do {
                daysTillResults = (int) (rand.nextGaussian() * T_LAG_DEV + TESTING_LAG); }  while(daysTillResults < 0);
            }   
        }
        else
        {
            if(immunity_ctr == 0)
            {
                immunity_ctr = -1;
                immune = false;
            }
            if(immunity_ctr != -1)
                immunity_ctr--;
        }
        if(daysInfected == 0)
        {
            Random r = new Random();
            if(r.nextDouble() < mortality * (severity_index / 0.5))
            {
                die();
            }            
            daysInfected = -1;
            infected = false;
            contagious = false;
            symptoms = false;
            lag = -1;
            daysTillResults = -1;
            symptomLag = -1;
            aware = false;
            immunity_ctr = IMMUNITY_LEN;
            immune = true;
            state.decrementCurrentCases();
            
        }
        
        if(vaccine_ctr != -1)
        {
            vaccine_ctr--;
        }
        if(vaccine_ctr == 0)
        {
            vaccine_ctr = -1;
            immunity_ctr = VACCINE_IMM_LEN;
        }
        
        if(contagious && !aware && alive)
        {
            for(int i = 0; i < primary.size(); i++)
            {
                double num = rand.nextDouble();
                double t = primary.get(i).getTransmission();
                if(num < t)
                {
                    primary.get(i).getOther(this).infect((t-num) / t);
                }
            }
        }
        
        }
    }
}

