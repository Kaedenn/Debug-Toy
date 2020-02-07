package net.kaedenn.debugtoy;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/** Main activity for the {@code net.kaedenn.debugtoy} application.
 *
 */
public class MainActivity extends AppCompatActivity {

    /* Controller for the first tab's objects */
    private DebugTextController debug;

    /* Indexes for the three tabs (TODO: REMOVE) */
    private static final int TAB1_INDEX = 0;
    private static final int TAB2_INDEX = 1;
    private static final int TAB3_INDEX = 2;

    /** Create the activity.
     *
     * This function also registers the primary commands that the
     * {@link DebugTextController} will handle.
     *
     * @param savedInstanceState Saved application information
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debug = new DebugTextController(this);
        selectTab(TAB1_INDEX);

        /* TODO: Allow swiping between tabs and remove the tab buttons entirely */

        /* Setup for tab 1 */

        /* Register the "env" command */
        debug.register(new Command("env", arg -> {
            Properties p = System.getProperties();
            debug.debug(String.format(getResources().getString(R.string.cmd_env_prop_text_f), p.size()));
            for (Object propKey : p.keySet()) {
                debug.debug(String.format("\"%s\" - \"%s\"", propKey, p.get(propKey)));
            }
            debug.debug(getResources().getString(R.string.cmd_env_var_text));
            TreeMap<String, String> env = new TreeMap<>(System.getenv());
            env.forEach((k, v) -> debug.debug(String.format("$%s = \"%s\"", k, v)));
        }, getResources().getString(R.string.cmd_env_help)));

        /* Register the "!" command */
        debug.register(new Command("!", arg -> {
            debug.debug(String.format(getResources().getString(R.string.cmd_run_running_f), arg));
            try {
                Process p = Runtime.getRuntime().exec(arg);
                BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                /* Do not allow program to execute for longer than 60 seconds */
                p.waitFor(60, TimeUnit.SECONDS);
                String s;
                while ((s = stdout.readLine()) != null) {
                    debug.debug(">> " + s);
                }
                while ((s = stderr.readLine()) != null) {
                    debug.debug("!! " + s);
                }
            }
            catch (IOException | InterruptedException e) {
                debug.debug(e.toString());
            } catch (Exception e) {
                debug.debug("Unhandled exception: " + e.toString());
                throw e;
            }
        }, getResources().getString(R.string.cmd_run_help)));

        /* TODO: Setup for tab 2
         *
         * Need function running periodically. Separate thread maybe? Can the
         * surface be modified from a different thread?
         */

        /* TODO: Setup for tab 3 */

    }

    /** Change the active tab by its numerical index.
     *
     * Tab indexes are not tab IDs. The index-to-ID association is as follows:
     *   {@link MainActivity#TAB1_INDEX} maps to {@code R.id.tabItem1}
     *   {@link MainActivity#TAB2_INDEX} maps to {@code R.id.tabItem2}
     *   {@link MainActivity#TAB3_INDEX} maps to {@code R.id.tabItem3}
     *
     * @param idx The tab index (a TABn_INDEX numeric constant)
     */
    public void selectTab(int idx) {
        int[] tabIDs = {R.id.tabItem1, R.id.tabItem2, R.id.tabItem3};
        int currentTab = getCurrentTab();
        if (idx != currentTab && idx >= TAB1_INDEX && idx <= TAB3_INDEX) {
            /* TODO: Show slide transition */
            /* TODO: Make the new slide visible */
            findViewById(tabIDs[idx]).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.tabItem1).setVisibility(idx == TAB1_INDEX ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.tabItem2).setVisibility(idx == TAB2_INDEX ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.tabItem3).setVisibility(idx == TAB3_INDEX ? View.VISIBLE : View.INVISIBLE);
    }

    /** Get the current tab index.
     *
     * @return The index for the current tab
     */
    public int getCurrentTab() {
        /* TODO: Remove references to tab indexes */
        if (findViewById(R.id.tabItem1).getVisibility() == View.VISIBLE) return TAB1_INDEX;
        if (findViewById(R.id.tabItem2).getVisibility() == View.VISIBLE) return TAB2_INDEX;
        if (findViewById(R.id.tabItem3).getVisibility() == View.VISIBLE) return TAB3_INDEX;
        return -1;
    }

    /** Show a "Snack Bar" message.
     *
     * The "Snack Bar" uses the {@code R.id.top} (top-level) view.
     *
     * @param text The text to show
     */
    private void showSnack(@NotNull CharSequence text) {
        showSnack(findViewById(R.id.top), text);
    }

    /** Show a "Snack Bar" message for the given view
     *
     * @param view The view to pass to {@link Snackbar#make}
     * @param text The text to show
     */
    private void showSnack(@NotNull View view, @NotNull CharSequence text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    /** Handle clicking of one of the first tab's buttons.
     *
     * This function is called when either the "Debug" or "Clear" buttons are
     * clicked by the user.
     *
     * @param button Reference to the button that was clicked
     */
    public void onButtonClick(@NotNull View button) {
        String cmd = debug.getDebugCommand();
        switch (button.getId()) {
            case R.id.btDebug:
                if (debug.isRegistered(cmd)) {
                    debug.execute(cmd);
                } else {
                    String err_f = getResources().getString(R.string.err_no_cmd_f);
                    showSnack(String.format(err_f, cmd));
                }
                break;
            case R.id.btClear:
                debug.clearDebug();
                break;
            case R.id.btClearAll:
                debug.clearDebug();
                debug.clearDebugCommand();
                break;
            default:
                String err_f = getResources().getString(R.string.err_invalid_button_f);
                showSnack(String.format(err_f, button.getId()));
                break;
        }
    }

    /** Handle clicking of the "Change Tab" buttons.
     *
     * This function is called when one of the "Tab" buttons are clicked by the
     * user.
     *
     * @param button Reference to the button that was clicked
     */
    public void onTabButtonClick(@NotNull View button) {
        switch (button.getId()) {
            case R.id.btTab1:
                selectTab(TAB1_INDEX);
                break;
            case R.id.btTab2:
                selectTab(TAB2_INDEX);
                break;
            case R.id.btTab3:
                selectTab(TAB3_INDEX);
                break;
            default:
                String err_f = getResources().getString(R.string.err_invalid_button_f);
                showSnack(String.format(err_f, button.getId()));
                break;
        }
    }
}

