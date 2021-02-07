import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Random;
import java.util.*;
import java.lang.Math;
/**
 * Write a description of class State here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class State
{
     private final static double AVG_POD_SIZE = 5.0; //average number of primary contacts per person
     private final static double POD_DEV = 1.0; //st dev of pod sizes
     private HashSet<Person> people; //maps ID to person, just public for testing purposes
     private int population;
     private int dayCounter;
     private boolean maskMandate;
     private final double MASK_EFF = 0.65; //transmission reduction with mask
     private int deaths;
     private int totalCases;
     private static double PERCEIVED_IFR = 0.01; //perceived infection rate
     /**
      * First, initialize edges that connect primary contacts(pods) with a default size 
      * around which a random number is generated.  Until all nodes in the set population size
      * are colored, color the one node, make its connections, color its connections, and 
      * move on to another non-colored node.  Add edge weights centered around a constant
      * likelihood of transmission
      * 
      * Then, initialize edges that connect secondary contacts(random encounters).  Choose
      * a person/node, generate its connections(more than primary contacts but with lower 
      * edge weights).  Color the initial node but not its connections.  Repeat for all 
      * nodes in the population.  Take into account that as you go on, the nodes will have
      * more and more connections before they are actually initialized whereas the first 
      * node picked will have to generate all its connections itself
      * 
      * Then initialize restriction status which universally changes edge weights aka 
      * transmission rates and testing capacity, which reduces ripple effect of transmission
      * 
      * Takes about 30 seconds to generate a state with population = 10,000
      */
    public State(int pop)
    {
        population = pop;
        dayCounter = 0;
        people = new HashSet<Person>();
        generatePrimaryContacts();
        maskMandate = false;
        deaths = 0;
    }
    
     /**
     * 
     * 
     */
    public void generatePrimaryContacts()
    {
       int count = 0;
       while(count < this.population)
       {
           Person p = new Person(this);
           int podSize; 
           Random rand = new Random();
           do
           {
               podSize = (int) (rand.nextGaussian()*POD_DEV + AVG_POD_SIZE);
           } while(podSize < 0);

           people.add(p);
           count++;
           for(int i = 0; i < podSize; i++)
           {
               Person x = new Person(this);
               p.addPrimary(x);
               x.addPrimary(p);
               people.add(x);
               count++;
           }
       }
       population = people.size();
    }
    
    public HashSet<Person> getPeople()
    {
        return people;
    }
    
    public void advanceDay() //takes 15 sec/day with population = one million
    {
        Iterator<Person> itr = people.iterator();
        while(itr.hasNext())
        {
            Person p = itr.next();
            p.advance();
            
        }
        dayCounter++;
    }
    
    public int numInfected() //maybe preprocess this list as an instance variable?
    {
        int ctr = 0;
        Iterator<Person> itr = people.iterator();
        while(itr.hasNext())
        {
            if(itr.next().isInfected())
                ctr++;  
        }
        return ctr;
    }
    
    public void updateDeaths()
    {
        deaths++;
        population--;
    }
    
    public int numDeaths()
    {
        return deaths;
    }
    
    public void updateCases()
    {
        totalCases++;
    }
    
    public int numTotalCases()
    {
        return totalCases;
    }
    
    public HashSet<Person> groupEvent(int size, double danger) 
    {
        HashSet<Person> lst = new HashSet<Person>();
        ArrayList<Person> ordered = orderByEventPropensity(people); //reverse of what we want
        int i = ordered.size() - 1;
        double maxAdh = (100 - (10 * (Math.log(danger * 100)))) / 100;
        double minPropensity = 1/maxAdh; //no propensity higher than this will attend event
        while(lst.size() < size && i >= 0)
        {
            if(ordered.get(i).getEventPropensity() < minPropensity)
                break;
            lst.add(ordered.get(i));
            i--;
        }
        return lst;
    }
    
    private ArrayList<Person> orderByEventPropensity(HashSet<Person> original)
    {
        Iterator<Person> iter = original.iterator();
        ArrayList<Person> lst = new ArrayList<Person>();
        while(iter.hasNext())
        {
            lst.add(iter.next());
        }
        Collections.sort(lst, new Person(this)); //sorts it in ascending order
        return lst;
    }
    
    public void shuffleEventPropensity()
    {
        Iterator<Person> iter = people.iterator();
        while(iter.hasNext())
        {
            iter.next().generateEventPropensity();
        }
    }
    
    public int executeEvent(int maxSize, double intensity) //intensity is multiplier on regular transmission rate. baseline is
                                                         //secondary contact transmission rate
    {
        shuffleEventPropensity();
        Random rand = new Random();
        double n;
        double r = Person.getAvgTransmission() * intensity;
        if(maskMandate)
            r = r * MASK_EFF;
        double danger = (1 - Math.pow(1-(r*PERCEIVED_IFR),maxSize - 1)); //perceived chance of getting covid at this event
        HashSet<Person> peopleList = groupEvent(maxSize, danger);
        Iterator<Person> iter1 = peopleList.iterator();
        while(iter1.hasNext()) 
        {
            Person p1 = iter1.next();
            Iterator<Person> iter2 = peopleList.iterator();
            while(iter2.hasNext())
            {
                Person p2 = iter2.next();
                if(p1.isContagious() && !p2.isInfected())
                {
                    n = rand.nextDouble();
                    double t = p1.getTransmissionRate() * intensity;
                    if(n < t)
                        p2.infect((t-n) / n);
                }
                if(p2.isContagious() && !p1.isInfected())
                {
                    n = rand.nextDouble();
                    double t = p2.getTransmissionRate() * intensity;
                    if(n < t)
                        p1.infect((t-n) / n);
                }
            }
        }
        return peopleList.size(); //returns the actual size of the event so that future events can dynamically
                                  //change expected size based on actual attendance, since max size pretty much
                                  //serves as expected size too, factoring into expected danger.
    } 
    
    public void maskMandate() //only affects infectiousness, not mortality (simplification probably).  also only affects 2ndary contacts and events
    {
        if(!maskMandate)
        {
            maskMandate = true;
            Iterator<Person> itr = people.iterator();
            while(itr.hasNext())
            {
                itr.next().implementMaskMandate(MASK_EFF);
            }
        }
    }
    
    public void repealMaskMandate()
    {
        if(maskMandate)
        {
            maskMandate = false;
            Iterator<Person> itr = people.iterator();
            while(itr.hasNext())
            {
                itr.next().undoMaskMandate(MASK_EFF);
            }
        }
    }
     
    public void adjustAllAdherences(double change)
    {
        Iterator<Person> itr = people.iterator();
        while(itr.hasNext())
        {
            itr.next().adjustAdherence(change);
        }
    }
    
    public int getPopulation()
    {
        return population;
    }
}
