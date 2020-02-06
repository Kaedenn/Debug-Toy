package net.kaedenn.debugtoy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

/** Controller for the primary debug text box and the command box below it.
 *
 * This class manages registering commands entered in the command box
 * with their actions and help text.
 *
 * If the help text is omitted, then the default resource string
 * {@code cmd_help_default} is used.
 */
public final class DebugTextController {
    private final MainActivity main;
    private final HashMap<String, Command> commands = new HashMap<>();

    public static final String COMMAND_DEFAULT = "";
    public static final String EXTENDED_COMMAND_CHR = "!";

    DebugTextController(@NotNull MainActivity mainActivity) {
        main = mainActivity;
    }

    /** Register a named command
     *
     * The help text defaults to the {@code cmd_help_default} resource string.
     *
     * @param action Command to execute
     */
    public void register(@NotNull Command action) {
        String cmd = action.getCommand();
        commands.put(cmd, action);
    }

    /** Get all declared commands, except for the default (if present)
     *
     * @return A collection of commands, without the default command.
     */
    public Collection<String> getCommands() {
        Set<String> cmdSet = commands.keySet();
        cmdSet.remove(COMMAND_DEFAULT);
        return cmdSet;
    }

    /** Get the action defined for the command
     *
     * @param cmd Named command to look up
     * @return The Command action bound to the command
     */
    public Command getAction(String cmd) {
        if (commands.containsKey(cmd)) {
            return commands.get(cmd);
        }
        return null;
    }

    /** Get the help text defined for the command
     *
     * @param cmd Named command to look up
     * @return The help text issued when the command was registered
     */
    public String getHelp(String cmd) {
        if (commands.containsKey(cmd)) {
            Command c = commands.get(cmd);
            return Objects.requireNonNull(c).getHelpText();
        }
        return null;
    }

    /** Removes a command entirely
     *
     * @param cmd The command to remove
     */
    public void unregister(String cmd) {
        commands.remove(cmd);
    }

    /** Removes the default command */
    public void unregisterDefault() {
        commands.remove(COMMAND_DEFAULT);
    }

    /** Execute the command string
     *
     * @param command The command string to execute
     * @return The object returned by the command, or null if no command was ran
     */
    public void execute(String command) {
        String cmd = command;
        ArrayList<String> args = new ArrayList<>();
        debug("Requested to run '" + command + "'");
        if (cmd.startsWith(EXTENDED_COMMAND_CHR)) {
            String[] tokens = cmd.split(" ");
            if (tokens.length > 0) {
                cmd = tokens[0];
                args.addAll(Arrays.asList(tokens).subList(1, tokens.length));
            }
        }
        debug("Running command " + cmd);
        if (cmd.equals(main.getResources().getString(R.string.cmd_help))) {
            executeHelpCommand();
        } else if (commands.containsKey(cmd)) {
            Command action = Objects.requireNonNull(commands.get(cmd));
            for (Object arg : args) {
                action.bindArgument(arg);
            }
            action.execute();
        }
    }

    /** Return whether or not the command is registered
     *
     * @param cmd The named command to examine
     * @return true if the command is bound, false otherwise
     */
    public boolean isRegistered(String cmd) {
        if (cmd.equals(main.getResources().getString(R.string.cmd_help))) {
            return true;
        } else if (cmd.startsWith(EXTENDED_COMMAND_CHR)) {
            String[] tokens = cmd.split(" ");
            return commands.containsKey(tokens[0]);
        } else {
            return commands.containsKey(cmd);
        }
    }

    /** Get the content of the debugActionText widget
     *
     * @return Current content of the debugActionText widget, as a string
     */
    public String getDebugCommand() {
        TextView t = main.findViewById(R.id.debugCommand);
        return t.getText().toString();
    }

    /** Clear the debugActionText widget's text */
    public void clearDebugCommand() {
        TextView t = main.findViewById(R.id.debugCommand);
        t.setText(COMMAND_DEFAULT);
    }

    /** Append a line to the debug text box
     *
     * @param text The text to append
     */
    public void debug(CharSequence text) {
        TextView t = main.findViewById(R.id.debugText);
        t.append(text);
        t.append("\n");
    }

    /** Append a dump of the given view to the debug text box
     *
     * @param view The view to inspect
     */
    public void debugView(View view) {
        /* TODO: add more attributes */
        this.debug(view.getClass().getTypeName());
        this.debug(view.toString());
    }

    /** Dump an arbitrary object to the debug text box
     *
     * @param obj The object to inspect
     */
    public void debug(Object obj) {
        debug(obj.toString());
    }

    /** Clear the debug text box */
    public void clearDebug() {
        TextView t = main.findViewById(R.id.debugText);
        t.setText(COMMAND_DEFAULT);
    }

    /** Executes the special command "help"
     *
     */
    private void executeHelpCommand() {
        for (String cmd : commands.keySet()) {
            String help = Objects.requireNonNull(commands.get(cmd)).getHelpText();
            if (help == null) {
                help = main.getResources().getString(R.string.cmd_help_default);
            }
            if (cmd.equals(EXTENDED_COMMAND_CHR)) {
                help = main.getResources().getString(R.string.cmd_extended) + ": " + help;
            }
            debug(String.format("%8s - %s", cmd, help));
        }
    }

}
