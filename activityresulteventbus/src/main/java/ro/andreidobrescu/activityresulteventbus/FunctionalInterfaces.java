package ro.andreidobrescu.activityresulteventbus;

public class FunctionalInterfaces
{
    @FunctionalInterface
    public interface Mapper<T, R>
    {
        R invoke(T arg);
    }

    @FunctionalInterface
    public interface Consumer<T>
    {
        void invoke(T arg);
    }

    @FunctionalInterface
    public interface Procedure
    {
        void invoke();
    }
}
