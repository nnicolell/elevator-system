import java.util.logging.*;

/**
 * A class to log information regarding the ElevatorSystem.
 */
public class ElevatorSystemLogger extends Logger {

    /**
     * Initializes an ElevatorLogger.
     *
     * @param name A String representing the name of the log file.
     */
    public ElevatorSystemLogger(String name) {
        super(name, null);
        try {
            // create a FileHandler for logging to a file
            FileHandler fileHandler = new FileHandler(name + ".log");
            addHandler(fileHandler);

            // remove the default console handler to prevent duplicate log messages
            setUseParentHandlers(false);

            // create a ConsoleHandler for logging to the console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            addHandler(consoleHandler);

            // create a custom formatter to include second timing precision in the log messages
            SimpleFormatter formatter = new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord record) {
                    return String.format("[%1$tF %1$tT.%1$tL] [%2$s] %3$s%n",
                            new java.util.Date(record.getMillis()), record.getLoggerName(),
                            record.getMessage());
                }
            };
            fileHandler.setFormatter(formatter);
            consoleHandler.setFormatter(formatter);
        } catch (Exception e) {
            severe("Error occurred while creating log file " + name + ".");
        }
    }

}
