package net.kaedenn.debugtoy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

import android.view.View;
import android.widget.ScrollView;
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
final class DebugPageController {
    private final MainActivity main;
    private final HashMap<String, Command> commands = new HashMap<>();

    DebugPageController(@NotNull MainActivity mainActivity) {
        main = mainActivity;
    }

    /** Register a named command.
     *
     * The help text defaults to the {@code cmd_help_default} resource string.
     *
     * @param action Command to execute
     */
    void register(@NotNull Command action) {
        String cmd = action.getCommand();
        commands.put(cmd, action);
    }

    /** Removes a command entirely.
     *
     * @param cmd The command to remove
     */
    @SuppressWarnings("unused")
    void unregister(String cmd) {
        commands.remove(cmd);
    }

    /** Get all declared commands, except for the default (if present).
     *
     * @return A collection of commands, without the default command.
     */
    Collection<String> getCommands() {
        return commands.keySet();
    }

    /** Execute the command string.
     *
     * @param command The command string to execute
     */
    void execute(String command) {
        if (command == null || command.length() == 0) {
            /* Prevent meaningless execution */
            return;
        }
        /* Split the first word (command) with the rest (arguments) */
        String[] words = command.split(" ", 2);
        String cmd = words[0];
        String args = (words.length == 1) ? "" : words[1];
        if (cmd.equals("help")) {
            /* Handle the special help command */
            executeHelpCommand();
        } else if (commands.containsKey(cmd)) {
            /* Handle a generic command */
            Command action = Objects.requireNonNull(commands.get(cmd));
            action.bindArgument(args);
            action.execute();
        }
        scrollToBottom();
    }

    /** Return whether or not the command is registered.
     *
     * @param cmd The named command to examine
     * @return true if the command is bound, false otherwise
     */
    boolean isRegistered(String cmd) {
        if (cmd.equals("help")) {
            return true;
        } else if (cmd.contains(" ")) {
            String c = cmd.split(" ", 2)[0];
            return commands.containsKey(c);
        } else {
            return commands.containsKey(cmd);
        }
    }

    /** Get the content of the debugActionText widget.
     *
     * @return Current content of the debugActionText widget, as a string
     */
    String getDebugCommand() {
        TextView t = main.findViewById(R.id.debugCommand);
        return t.getText().toString();
    }

    /** Clear the debugActionText widget's text.
     *
     */
    void clearDebugCommand() {
        TextView t = main.findViewById(R.id.debugCommand);
        t.setText("");
    }

    /** Scroll to the bottom of the containing scroll view.
     *
     */
    private void scrollToBottom() {
        ScrollView sv = main.findViewById(R.id.debugTextScroll);
        TextView tv = main.findViewById(R.id.debugText);
        tv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
    }

    /** Append a line to the debug text box.
     *
     * @param text The text to append
     */
    void debug(CharSequence text) {
        TextView t = main.findViewById(R.id.debugText);
        t.append(text);
        t.append("\n");
        scrollToBottom();
    }

    /** Clear the debug text box.
     *
     */
    void clearDebug() {
        TextView t = main.findViewById(R.id.debugText);
        t.setText("");
    }

    /** Executes the special command "help".
     *
     */
    private void executeHelpCommand() {
        for (String cmd : getCommands()) {
            String help = Objects.requireNonNull(commands.get(cmd)).getHelpText();
            if (help == null || help.isEmpty()) {
                /* Provide default help string for commands without help text */
                help = "Show this message";
            }
            debug(String.format("%8s - %s", cmd, help));
        }
    }

}
