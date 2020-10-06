import java.util.ArrayList;
/**
 * Write a description of class Contact here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Contact
{
    private Person a;
    private Person b;
    private double transmission;

    /**
     * Constructor for objects of class Contact
     */
    public Contact(Person a, Person b, double transmission)
    {
        this.a = a;
        this.b = b;
        this.transmission = transmission;
    }
    public ArrayList<Person> getPair()
    {
        ArrayList<Person> people = new ArrayList<Person>();
        people.add(a);
        people.add(b);
        return people;
    }
    public double getTransmission()
    {
        return transmission;
    }
    public Person getOther(Person p) //not entirely sure on this
    {
        if(p == a)
            return b;
        if(p == b)
            return a;
        else
            return null;
           
    }
}
