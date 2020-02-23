package net.kaedenn.debugtoy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kaedenn.debugtoy.util.Logf;

import org.jetbrains.annotations.NotNull;

/** Controller for the primary debug text box and the command box below it.
 *
 * This class manages registering mCommands entered in the command box
 * with their actions and help text.
 *
 * If the help text is omitted, then the default resource string
 * {@code cmd_help_default} is used.
 */
final class DebugPageController {
    private final MainActivity mActivity;
    private final HashMap<String, Command> mCommands = new HashMap<>();

    private static final String LOG_TAG = "debugPage";

    DebugPageController() {
        mActivity = MainActivity.getInstance();
        Log.i(LOG_TAG, "DebugPageController created");
    }

    /** Register a named command.
     *
     * The help text defaults to the {@code cmd_help_default} resource string.
     *
     * @param action Command to execute
     */
    void register(@NotNull Command action) {
        String cmd = action.getCommand();
        mCommands.put(cmd, action);
        Logf.d(LOG_TAG, "Registered command %s (%s)", cmd, action);
    }

    /** Removes a command entirely.
     *
     * @param cmd The command to remove
     */
    @SuppressWarnings("unused")
    void unregister(String cmd) {
        mCommands.remove(cmd);
    }

    /** Get all declared mCommands, except for the default (if present).
     *
     * @return A collection of mCommands, without the default command.
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    Collection<String> getCommands() {
        return mCommands.keySet();
    }

    /** Execute the command string.
     *
     * By default, the empty string is passed as the command's arguments string.
     *
     * If the command contains a space, then the command is set to the first
     * word (up to but not including the space) and the arguments are set to
     * everything after the first space.
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
        } else if (mCommands.containsKey(cmd)) {
            /* Handle a generic command */
            Command action = Objects.requireNonNull(mCommands.get(cmd));
            action.bindArgument(args);
            action.execute();
        }
    }

    /** Return whether or not the command is registered.
     *
     * If a command contains a space, then only the first word (up to but not
     * including the space) is used. Otherwise, the entire command is used.
     *
     * The {@code "help"} command is always registered.
     *
     * @param cmd The named command to examine
     * @return true if the command is bound, false otherwise
     */
    boolean isRegistered(String cmd) {
        if (cmd.equals("help")) {
            return true;
        } else if (cmd.contains(" ")) {
            String c = cmd.split(" ", 2)[0];
            return mCommands.containsKey(c);
        } else {
            return mCommands.containsKey(cmd);
        }
    }

    /** Get the content of the debugActionText widget.
     *
     * @return Current content of the debugActionText widget, as a string
     */
    String getDebugCommand() {
        TextView t = mActivity.findViewById(R.id.debugCommand);
        return t.getText().toString();
    }

    /** Clear the debugActionText widget's text.
     *
     * This function clears the small input box's content. This is called
     * automatically when a command is submitted.
     */
    void clearDebugCommand() {
        TextView t = mActivity.findViewById(R.id.debugCommand);
        t.setText("");
    }

    /** Scroll to the bottom of the containing scroll view.
     *
     * Scrolling takes place after the current main loop iteration to allow for
     * queued {@code debug.debug} calls to complete.
     */
    private void scrollToBottom() {
        ScrollView sv = mActivity.findViewById(R.id.debugTextScroll);
        TextView tv = mActivity.findViewById(R.id.debugText);
        tv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
    }

    /** Append a line to the debug text box.
     *
     * @param text The text to append
     */
    void debug(CharSequence text) {
        TextView t = mActivity.findViewById(R.id.debugText);
        t.append(text);
        t.append("\n");
        scrollToBottom();
    }

    /** Append a line to the debug text box.
     *
     * @param words The words to append
     */
    void debug(CharSequence[] words) {
        debug(String.join(" ", words));
    }

    /** Append a formatted message to the debug text box.
     *
     * @param format The format string
     * @param arguments The arguments to pass with the format string
     */
    void debugf(CharSequence format, Object... arguments) {
        debug(String.format(format.toString(), arguments));
    }

    /** Clear the debug text box.
     *
     * This clears the main debug text box of its content.
     */
    void clearDebug() {
        TextView t = mActivity.findViewById(R.id.debugText);
        t.setText("");
    }

    /** Executes the special command "help".
     *
     * This command lists the registered commands and their help strings.
     */
    private void executeHelpCommand() {
        for (String cmd : getCommands()) {
            String help = Objects.requireNonNull(mCommands.get(cmd)).getHelpText();
            if (help == null || help.isEmpty()) {
                /* Provide default help string for mCommands without help text */
                help = "This command has no help text.";
            }
            debug(String.format("%-8s - %s", cmd, help));
        }
    }

}
