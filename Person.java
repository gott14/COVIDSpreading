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
    private final static double ADH_DEVIATION = 0.25; //default st dev for adherence distribution
    private final static double MOR_DEVIATION = 0.1; //default st dev for mortality distribution
    private final static double AVG_ADH = 0.5; //average amount of adherence to rules
    private final static double AVG_INF = 14.0; //average days infected
    private final static double DEV_INF = 1.0; //st dev of days infected
    private final static double AVG_LAG = 3.0; //avg days between infection and contagious
    private final static double DEV_LAG = 0.2; //stdev of lag
    private final static double AVG_T_P = 0.75/AVG_INF; //avg daily transmission rate for primary contact - based on cruise ship study
    private final static double DEV_T_P = 1.0; //st dev of transmission rate for primary contact
    private static double AVG_T_S = 0.1/AVG_INF; //avg daily transmission rate for 2ndary contact (meaning someone not in your pod). not final
                                            //because it can be affected by a mask mandate
    private final static double DEV_T_S = 1.0; //st dev of transmission rate of 2ndary contact
    private boolean infected; 
    private boolean contagious;
    private double eventPropensity;
    private double adherence; //between 0 and 1, 1 being full adherence to state policy, also representing propensity to go to events
    private double mortality; //mortality*avg mortality = this person's mortality
    private ArrayList<Contact> primary; //primary contacts
    private ArrayList<Contact> secondary; //secondary contacts
    private int lag; //days until the person is contagious, -1 if not infected
    private int daysInfected; //0 if not infected; if infected, days until not infected
    private boolean aware; //true if the person knows they have covid
    private boolean symptoms; //true if they will be symptomatic 
    private int symptomLag; //days between contagious and symptoms, -1 if asymptomatic
    private static double SYMP_LAG = 3.0; //average days between contagious and symptoms, if symptomatic case
    private static double SYMP_LAG_DEV = 1.0; //st dev of the above
    private int daysTillResults; //days until test results.  -1 if not waiting on results
    
    private static double TESTING_FREQ = 0.01; //percentage of asymptiomatic people who get tested on any given day of their having covid
    private static double TESTING_LAG = 3.0; //average days to get test result
    private static double T_LAG_DEV = 0.2; //st dev for time to get test results
    private static double ASYMP = 0.4; //percentage that are asymptomatic and won't get tested
    /** 
     * Initialize instance variables:
     * willingness to social distance--how much govt policy affects their edge weights
     * infection status
     * mortality risk
     */
    public Person()
    {
        infected = false;
        aware = false;
        Random rand = new Random();
        primary = new ArrayList<Contact>();
        secondary = new ArrayList<Contact>();
        daysInfected = 0;
        lag = -1;
        daysTillResults = -1;
        symptomLag = -1;
        do
        {
           adherence = rand.nextGaussian()*ADH_DEVIATION + AVG_ADH;
        } while(adherence < 0.0 || adherence > 1.0);
        eventPropensity = generateEventPropensity();
        do
        {
           mortality = rand.nextGaussian()*MOR_DEVIATION + 1;
        } while(mortality < 0.0);
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
    
    public void addSecondary(Person person)
    {
        Random rand = new Random();
        double transmission;
        do
        {
            transmission = rand.nextGaussian()*DEV_T_S + AVG_T_S;
        } while(transmission > 1.0 || transmission < 0.0);
        secondary.add(new Contact(this, person, transmission));
    }
    
    public ArrayList<Contact> getPrimary()
    {
        return primary;
    }
    
    public ArrayList<Contact> getSecondary()
    {
        return secondary;
    }
    
    public void infect()
     {
        Random rand = new Random();
        if(!infected)
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
        
        if(rand.nextDouble() >= ASYMP)
        {
            symptoms = true;
            do {
            symptomLag = (int) (rand.nextGaussian()*SYMP_LAG + SYMP_LAG_DEV); }  while(symptomLag < 0);
        }
       }
       }
     
    
    public boolean isInfected()
    {
        return infected;
    }
    
    public boolean isContagious()
    {
        return contagious;
    }
    
    public static double getTransmissionRate()
    {
        return AVG_T_S;
    }
    
    private double generateEventPropensity()
    {
       Random rand = new Random();
       return rand.nextGaussian() * ADH_DEVIATION + (1.0/adherence);
    }
    
    public double getEventPropensity()
    {
        return eventPropensity;
    }
    
    public void implementMaskMandate(double maskEff)
    {
        AVG_T_S = AVG_T_S * (1 - (maskEff * adherence));
    }
    
    public void undoMaskMandate(double maskEff)
    {
        AVG_T_S = AVG_T_S / (1 - (maskEff * adherence));
    }
    
    public void advance()
    {
        Random rand = new Random();
        if(infected)
        {
            daysInfected--;
            if(lag > 0)
                lag--;
            if(lag == 0)
                contagious  = true;
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
        if(daysInfected == 0)
        {
            infected = false;
            contagious = false;
            symptoms = false;
            lag = -1;
            daysTillResults = -1;
            symptomLag = -1;
            aware = false;
        }

        if(contagious && !aware)
        {
            for(int i = 0; i < primary.size(); i++)
            {
                double num = rand.nextDouble();
                if(num < primary.get(i).getTransmission())
                {
                    primary.get(i).getOther(this).infect();
                }
            }
            for(int i = 0; i < secondary.size(); i++)
            {
                double num = rand.nextDouble();
                if(num < secondary.get(i).getTransmission())
                {
                    secondary.get(i).getOther(this).infect();
                }
            }
        }
        
        eventPropensity = generateEventPropensity(); //need to add way to modify if they know they are infected
        
        if(aware)
            eventPropensity = 0;
    }
}

