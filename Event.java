
/**
 * Holds the two parameters that define an event: maxSize and intensity
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Event
{
    private int maxSize;
    private double intensity; //multiplier on AVG_T_S
    private boolean slack;
    public Event(int ms, double i, boolean s)
    {
        maxSize = ms;
        intensity = i;
        slack = s;
    }
    
    public int getMaxSize()
    {
        return maxSize;
    }
    
    public double getIntensity()
    {
        return intensity;
    }
    
    public boolean allowsSlack()
    {
        return slack;
    }
}
