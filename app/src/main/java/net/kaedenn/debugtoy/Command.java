package net.kaedenn.debugtoy;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/** Simplistic command object.
 *
 * The command consists of a command, a function, and help text.
 *
 * See {@link Command#Command(String, Consumer, String)}.
 *
 * Commands are {@link java.util.function.Consumer} instances accepting a single
 * {@code String} parameter with the {@code mArgs} bound to the command. This
 * string may be empty (or even {@code null}) if no parameters are bound.
 *
 * Commands take a single argument: a {@code java.lang.String}. This argument
 * is bound via the {@link Command#bindArgument} method.
 *
 * No special handling is provided for specific command strings or command
 * strings starting with special characters. Such things are the responsibility
 * of the handling code.
 *
 * @see java.util.function.Consumer
 */
@SuppressWarnings("unused")
class Command {
    @NonNull
    private final String mCommand;
    @NonNull
    private final Consumer<String> mAction;
    private String mHelpText;
    private String mArgs;

    /** Construct a command without help text.
     *
     * This constructor should not be used. It is provided for convenience in
     * creating quick testing commands.
     *
     * @param command The command string to register
     * @param function A {@code Consumer<String>} instance
     */
    Command(String command, @NotNull Consumer<String> function) {
        this(command, function, "This function has no help text.");
    }

    /** Construct a command.
     *
     * @param command The command string to register
     * @param function A {@code Consumer<String>} instance
     * @param help A help string to display when the user wants help text
     */
    Command(@NonNull String command, @NotNull Consumer<String> function, String help) {
        mCommand = command;
        mAction = function;
        mHelpText = help;
        mArgs = "";
    }

    /** Obtain the command's name.
     *
     * @return Command name
     */
    String getCommand() {
        return mCommand;
    }

    /** Obtain the command's help text.
     *
     * @return Command help text
     */
    String getHelpText() {
        return mHelpText;
    }

    /** Change the command's help text.
     *
     * @param helpText The new help text to use
     */
    void setHelpText(String helpText) { mHelpText = helpText; }

    /** Add an argument to pass to the consumer.
     *
     * The {@code mArgs} are passed as {@code String} to the consumer function
     * in the same order as they're bound. Note that consumer functions may be
     * called with an empty array if no {@code mArgs} have been bound.
     *
     * @param arg The argument to add to the list of {@code mArgs}.
     */
    void bindArgument(String arg) {
        mArgs = arg;
    }

    /** Execute the consumer function.
     *
     * This function calls the consumer's {@code apply} method with the bound
     * {@code mArgs} as an array. If no {@code mArgs} have been bound, then an
     * empty array is passed instead. It is up to the function to determine what
     * to do with the {@code mArgs}.
     */
    void execute() {
        mAction.accept(mArgs == null ? "" : mArgs);
    }
}

