package net.kaedenn.debugtoy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Command {
    protected String cmd;
    protected Consumer<Object[]> action;
    protected String helpText;
    protected List<Object> arguments;

    protected Command(String command, Consumer<Object[]> function) {
        this(command, function, null);
    }
    protected Command(String command, Consumer<Object[]> function, String help) {
        cmd = command;
        action = function;
        helpText = help;
    }
    public void setHelpText(String help) {
        helpText = help;
    }
    public String getCommand() {
        return cmd;
    }
    public Consumer<?> getAction() {
        return action;
    }
    public String getHelpText() {
        return helpText;
    }

    public void bindArgument(Object arg) {
        if (arguments == null) {
            arguments = new ArrayList<>();
        }
        arguments.add(arg);
    }

    public void bindArguments(List<Object> args) {
        for (Object arg : args) {
            bindArgument(arg);
        }
    }

    public void execute() {
        if (arguments != null) {
            action.accept(arguments.toArray());
        } else {
            action.accept(new Object[]{});
        }
    }
}
