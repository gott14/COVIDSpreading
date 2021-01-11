import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Random;
import java.util.*;
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
     private final static double AVG_S_CONTACTS = 50.0; //average number of secondary contacts per person
     private final static double SEC_DEV = 5.0; //st dev of secondary contacts
     public Hashtable<Integer, Person> people; //maps ID to person, just public for testing purposes
     private int population;
     private int dayCounter;
     private boolean maskMandate;
     private final static double MASK_EFF = 0.8; //transmission reduction with mask
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
        people = new Hashtable<Integer, Person>();
        generatePrimaryContacts();
        generateSecondaryContacts();
        maskMandate = false;
    }
    
     /**
     * Maybe abstract out the create of the population? right now the primary contacts have to be generated before
     * the secondary contacts
     */
    public void generatePrimaryContacts()
    {
       int count = 0;
       while(count < this.population)
       {
           Person p = new Person();
           int podSize; 
           Random rand = new Random();
           do
           {
               podSize = (int) (rand.nextGaussian()*POD_DEV + AVG_POD_SIZE);
           } while(podSize < 0);

           people.put(count, p);
           count++;
           for(int i = 0; i < podSize; i++)
           {
               Person x = new Person();
               p.addPrimary(x);
               x.addPrimary(p);
               people.put(count, x);
               count++;
           }
       }
    }
    
    public Hashtable<Integer, Person> getPeople()
    {
        return people;
    }
    
    /**
     * Some nodes with higher IDs may end up with more than their random number of secondary contacts, but it will
     * still be from a random process overall.
     */
    public void generateSecondaryContacts() 
    {
        for(int i = 0; i < this.population; i++)
        {
            Person p = people.get(i);
            int secSize;
            Random rand = new Random();
            do
            {
                secSize = (int) (rand.nextGaussian()*SEC_DEV + AVG_S_CONTACTS);
            } while(secSize < 0);
            
            while(p.getSecondary().size() < secSize)
            {
                int index;
                do
                {
                    index = rand.nextInt(population);
                } while(index != i); //makes sure its not the chosen person themselves
                Person x = people.get(index);
                p.addSecondary(x);
                x.addSecondary(p);
            }
            
        }
    }
    
    public void advanceDay()
    {
        for(int i = 0; i < people.size(); i++)
        {
            Person p = people.get(i);
            p.advance();
            
        }
        dayCounter++;
    }
    
    public int numInfected() //maybe preprocess this list as an instance variable?
    {
        int ctr = 0;
        for(int i = 0; i < people.size(); i++)
        {
            if(people.get(i).isInfected())
                ctr++;  
        }
        return ctr;
    }
    
    public HashSet<Person> groupEvent(int size) //need to test
    {
        HashSet<Person> lst = new HashSet<Person>();
        ArrayList<Person> ordered = orderByEventPropensity(people); //reverse of what we want
        int i = ordered.size() - 1;
        while(lst.size() < size && i >= 0)
        {
            lst.add(ordered.get(i));
            i--;
        }
        return lst;
    }
    
    private ArrayList<Person> orderByEventPropensity(Hashtable<Integer, Person> original)
    {
        Iterator<Person> iter = original.values().iterator();
        ArrayList<Person> lst = new ArrayList<Person>();
        while(iter.hasNext())
        {
            lst.add(iter.next());
        }
        Collections.sort(lst, new Person()); //sorts it in ascending order
        return lst;
    }
    
    public void executeEvent(int size, double intensity) //intensity is multiplier on regular transmission rate. baseline is
                                                         //secondary contact transmission rate
    {
        Random rand = new Random();
        double n;
        HashSet<Person> peopleList = groupEvent(size);
        Iterator<Person> iter1 = peopleList.iterator();
        Iterator<Person> iter2 = peopleList.iterator();
        while(iter1.hasNext()) 
        {
            Person p1 = iter1.next();
            while(iter2.hasNext())
            {
                Person p2 = iter2.next();
                if(p1.isContagious())
                {
                    n = rand.nextDouble();
                    if(n < Person.getTransmissionRate() * intensity)
                        p2.infect();
                }
                if(p2.isContagious())
                {
                    n = rand.nextDouble();
                    if(n < Person.getTransmissionRate() * intensity)
                        p1.infect();
                }
            }
        }
    } 
    
    public void maskMandate() //only affects infectiousness, not mortality (simplification probably).  also only affects 2ndary contacts and events
    {
        if(!maskMandate)
        {
            maskMandate = true;
            for(int i = 0; i < population; i++)
            {
                people.get(i).implementMaskMandate(MASK_EFF);
            }
        }
    }
    
    public void repealMaskMandate()
    {
        if(maskMandate)
        {
            maskMandate = false;
            for(int i = 0; i < population; i++)
            {
                people.get(i).undoMaskMandate(MASK_EFF);
            }
        }
    }
    
}
