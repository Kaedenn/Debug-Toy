package net.kaedenn.debugtoy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
 *
 * @see java.lang.Runnable
 */
public final class DebugTextController {
    private final MainActivity main;
    private HashMap<String, Runnable> commands = new HashMap<>();
    private HashMap<String, String> helpTexts = new HashMap<>();

    private static final String COMMAND_DEFAULT = "";

    DebugTextController(@NotNull MainActivity mainActivity) {
        main = mainActivity;
    }

    /** Register a named command
     *
     * The help text defaults to the {@code cmd_help_default} resource string.
     *
     * @param cmd Named command to register
     * @param action Runnable to execute
     */
    public void register(@NotNull String cmd, @NotNull Runnable action) {
        String helpText = main.getResources().getString(R.string.cmd_help_default);
        register(cmd, action, String.format("%8s - %s", cmd, helpText));
    }

    /** Register a command by resource ID
     *
     * The help text defaults to the {@code cmd_help_default} resource string.
     *
     * @param cmdResId String resource ID for the command to register
     * @param action Runnable to execute
     */
    public void register(int cmdResId, @NotNull Runnable action) {
        String cmd = main.getResources().getString(cmdResId);
        register(cmd, action);
    }

    /** Register a named command with help text
     *
     * @param cmd Named command to register
     * @param action Runnable to execute
     * @param helpText Help text for this action
     */
    public void register(@NotNull String cmd, @NotNull Runnable action, String helpText) {
        commands.put(cmd, action);
        helpTexts.put(cmd, helpText);
    }

    /** Register a command by resource ID with help text
     *
     * @param cmdResId String resource ID for the command to register
     * @param action Runnable to execute
     * @param helpResId String resource ID for the command's help text
     */
    public void register(int cmdResId, @NotNull Runnable action, int helpResId) {
        String cmd = main.getResources().getString(cmdResId);
        String help = main.getResources().getString(helpResId);
        register(cmd, action, help);
    }

    /** Register a default command (i.e. the empty string)
     *
     * @param action Runnable to execute
     */
    public void registerDefault(@NotNull Runnable action) {
        registerDefault(action, main.getResources().getString(R.string.cmd_help_default));
    }

    /** Register a default command (i.e. the empty string)
     *
     * @param action Runnable to execute
     * @param helpText Help text for this default action
     */
    public void registerDefault(@NotNull Runnable action, String helpText) {
        commands.put(COMMAND_DEFAULT, action);
        helpTexts.put(COMMAND_DEFAULT, helpText);
    }

    /** Register the default command with the help text defined by a resource ID
     *
     * @param action Runnable to execute
     * @param helpResId String resource ID for the command's help text
     */
    public void registerDefault(@NotNull Runnable action, int helpResId) {
        String help = main.getResources().getString(helpResId);
        registerDefault(action, help);
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
     * @return The Runnable action bound to the command
     */
    public Runnable getAction(String cmd) {
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
        if (helpTexts.containsKey(cmd)) {
            return helpTexts.get(cmd);
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
     * @return true if the command was executed, false otherwise
     */
    public boolean execute(String command) {
        String cmd = command;
        ArrayList<String> args = new ArrayList<>();
        if (cmd.startsWith("+")) {
            String[] tokens = cmd.substring(1).split(" ");
            if (tokens.length > 0) {
                cmd = tokens[0];
                for (int i = 1; i < tokens.length; ++i) {
                    args.add(tokens[i]);
                }
            }
            debug("Executing special command " + cmd);
            debug("With args " + String.join(", ", args));
        }
        if (commands.containsKey(cmd)) {
            Runnable action = commands.get(cmd);
            Objects.requireNonNull(action).run();
            return true;
        }
        return false;
    }

    /** Return whether or not the command is registered
     *
     * @param cmd The named command to examine
     * @return true if the command is bound, false otherwise
     */
    public boolean isRegistered(String cmd) {
        return commands.containsKey(cmd);
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

}
