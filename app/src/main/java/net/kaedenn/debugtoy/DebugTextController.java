package net.kaedenn.debugtoy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import android.view.*;
import android.widget.*;

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
    private MainActivity main;
    private HashMap<String, Runnable> commands = new HashMap<>();
    private HashMap<String, String> helpTexts = new HashMap<>();

    private static final String COMMAND_DEFAULT = "";

    DebugTextController(@NotNull MainActivity mainActivity) {
        main = mainActivity;
    }

    /** Register a named command
     *
     * The help text defaults to the "cmd_help_default" resource string.
     *
     * @param cmd Named command to register
     * @param action Runnable to execute
     */
    public void register(@NotNull String cmd, @NotNull Runnable action) {
        String helpText = main.getResources().getString(R.string.cmd_help_default);
        register(cmd, action, String.format("%8s - %s", cmd, helpText));
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

    /** Get all declared commands, except for the default (if present)
     *
     * @return A collection of commands, without the default command.
     */
    public Collection<String> getCommands() {
        Set<String> cmdSet = commands.keySet();
        if (cmdSet.contains(COMMAND_DEFAULT)) {
            cmdSet.remove(COMMAND_DEFAULT);
        }
        return cmdSet;
    }

    public Runnable getAction(String cmd) {
        if (commands.containsKey(cmd)) {
            return commands.get(cmd);
        }
        return null;
    }

    public String getHelp(String cmd) {
        if (helpTexts.containsKey(cmd)) {
            return helpTexts.get(cmd);
        }
        return null;
    }

    public void unregister(String cmd) {
        commands.remove(cmd);
    }

    public void unregisterDefault() {
        commands.remove(COMMAND_DEFAULT);
    }

    public boolean execute(String cmd) {
        if (commands.containsKey(cmd)) {
            Runnable action = commands.get(cmd);
            action.run();
            return true;
        }
        return false;
    }

    public boolean isRegistered(String cmd) {
        return commands.containsKey(cmd);
    }

    public String getDebugCommand() {
        TextView t = main.findViewById(R.id.debugActionText);
        return t.getText().toString();
    }

    public void clearDebugCommand() {
        TextView t = main.findViewById(R.id.debugActionText);
        t.setText(COMMAND_DEFAULT);
    }

    public void debug(CharSequence text) {
        TextView t = main.findViewById(R.id.debugText);
        t.append(text);
        t.append("\n");
    }

    public void debugView(View view) {
        /* TODO: add more attributes */
        this.debug(view.getClass().getTypeName());
        this.debug(view.toString());
    }

    public void debug(Object obj) {
        debug(obj.toString());
    }

    public void clearDebug() {
        TextView t = main.findViewById(R.id.debugText);
        t.setText(COMMAND_DEFAULT);
    }

}
