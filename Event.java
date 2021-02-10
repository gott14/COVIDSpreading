
/**
 * Holds the two parameters that define an event: maxSize and intensity
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Event
{
    private int maxSize;
    private double intensity;
    
    public Event(int ms, double i)
    {
        maxSize = ms;
        intensity = i;
    }
    
    public int getMaxSize()
    {
        return maxSize;
    }
    
    public double getIntensity()
    {
        return intensity;
    }
}
