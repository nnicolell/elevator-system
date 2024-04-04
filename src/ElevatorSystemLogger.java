import java.util.logging.*;

public class ElevatorSystemLogger extends Logger {

    public ElevatorSystemLogger(String name) {
        super(name, null);
        try {
            FileHandler fileHandler = new FileHandler(name + ".log");
            addHandler(fileHandler);

            // Remove the default console handler to prevent duplicate log messages
            setUseParentHandlers(false);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            addHandler(consoleHandler);
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
            severe("Error occured while creating log file for elevator: " + name);
        }
    }

}
