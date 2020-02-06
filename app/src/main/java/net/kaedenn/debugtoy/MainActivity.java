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

    /* Indexes for the three tabs */
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

        debug.register(new Command("env", arg -> {
            Properties p = System.getProperties();
            debug.debug("Properties: " + p.size());
            for (Object propKey : p.keySet()) {
                debug.debug(String.format("\"%s\" - \"%s\"", propKey, p.get(propKey)));
            }
            debug.debug("Environment variables:");
            TreeMap<String, String> env = new TreeMap<>(System.getenv());
            env.forEach((k, v) -> debug.debug(String.format("$%s = \"%s\"", k, v)));
        }, "get environment"));

        debug.register(new Command("!", arg -> {
            debug.debug(String.format("Executing system command \"%s\"", arg));
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
            catch (IOException e) {
                debug.debug("Unhandled IOException: " + e.toString());
            }
            catch (InterruptedException e) {
                debug.debug("Unhandled InterruptedException: " + e.toString());
            }
            catch (Exception e) {
               debug.debug("Unhandled exception!! " + e.toString());
            }
        }, "execute a system command"));
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
    private void selectTab(int idx) {
        findViewById(R.id.tabItem1).setVisibility(idx == TAB1_INDEX ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.tabItem2).setVisibility(idx == TAB2_INDEX ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.tabItem3).setVisibility(idx == TAB3_INDEX ? View.VISIBLE : View.INVISIBLE);
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
