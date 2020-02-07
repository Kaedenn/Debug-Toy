package net.kaedenn.debugtoy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

    /* Controller for the first page's objects */
    private DebugPageController debug;

    /* Controller for the second page's objects */
    private SurfacePageController surfaceController;

    /* The three pages and the current page (a reference to one of the three) */
    private View page1 = null;
    private View page2 = null;
    private View page3 = null;
    private View currentPage = null;

    /** Create the activity.
     *
     * This function also registers the primary commands that the
     * {@link DebugPageController} will handle.
     *
     * @param savedInstanceState Saved application information
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        page1 = findViewById(R.id.page1);
        page2 = findViewById(R.id.page2);
        page3 = findViewById(R.id.page3);

        /* Select page1 directly */
        forceSetPage(page1);

        /* TODO: Allow swiping between pages and remove the page buttons entirely */

        /* Setup for page 1 */

        /* Create the debug text controller */
        debug = new DebugPageController(this);

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

        /* Setup for page 2 */
        surfaceController = new SurfacePageController(this);

        /* TODO: Setup for page 3 */

    }

    /** Force the given page to be visible.
     *
     * The other pages will be set to GONE. No checking is done to ensure that
     * {@param page} is actually one of the main pages.
     *
     * @param page The view to show
     */
    private void forceSetPage(@NotNull View page) {
        page1.setVisibility(View.GONE);
        page2.setVisibility(View.GONE);
        page3.setVisibility(View.GONE);
        page.setVisibility(View.VISIBLE);
        currentPage = page;
    }

    /** Change the active page.
     *
     * @param targetPage The page to move to
     */
    public void selectPage(View targetPage) {
        View currentView = currentPage;
        if (currentView != null && targetPage != null && currentView != targetPage) {
            targetPage.setAlpha(0);
            targetPage.setVisibility(View.VISIBLE);
            targetPage.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);
            currentView.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            currentView.setVisibility(View.GONE);
                        }
                    });
            /* Handle code unique to each page */
            if (targetPage == page2) {
                surfaceController.doAppear();
            } else if (currentPage == page2) {
                surfaceController.doDisappear();
            }
        }
        currentPage = targetPage;
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

    /** Handle clicking of one of the first page's buttons.
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
            case R.id.btPage1:
                selectPage(page1);
                break;
            case R.id.btPage2:
                selectPage(page2);
                break;
            case R.id.btPage3:
                selectPage(page3);
                break;
            default:
                String err_f = getResources().getString(R.string.err_invalid_button_f);
                showSnack(String.format(err_f, button.getId()));
                break;
        }
    }
}

