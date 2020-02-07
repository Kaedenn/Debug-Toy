package net.kaedenn.debugtoy;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/** Simplistic command object.
 *
 * The command consists of a command, a function, and help text.
 *
 * See {@link Command#Command(String, Consumer, String)}.
 *
 * Commands are {@link java.util.function.Consumer} instances accepting a single
 * {@code String} parameter with the arguments bound to the command. This string
 * may be empty (or even {@code null}) if no parameters are bound.
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
class Command {
    private String cmd;
    private Consumer<String> action;
    private String helpText;
    private String arguments;

    /** Construct a command.
     *
     * @param command The command string to register
     * @param function A {@code Consumer<String>} instance
     * @param help A help string to display when the user wants help text
     */
    Command(String command, @NotNull Consumer<String> function, String help) {
        cmd = command;
        action = function;
        helpText = help;
        arguments = "";
    }

    /** Obtain the command's name.
     *
     * @return Command name
     */
    String getCommand() {
        return cmd;
    }

    /** Obtain the command's help text.
     *
     * @return Command help text
     */
    String getHelpText() {
        return helpText;
    }

    /** Add an argument to pass to the consumer.
     *
     * The arguments are passed as {@code String} to the consumer function in
     * the same order as they're bound. Note that consumer functions may be
     * called with an empty array if no arguments have been bound.
     *
     * @param arg The argument to add to the list of arguments.
     */
    void bindArgument(String arg) {
        arguments = arg;
    }

    /** Execute the consumer function.
     *
     * This function calls the consumer's {@code apply} method with the bound
     * arguments as an array. If no arguments have been bound, then an empty
     * array is passed instead. It is up to the function to determine what to do
     * with the arguments.
     */
    void execute() {
        action.accept(arguments == null ? "" : arguments);
    }
}

