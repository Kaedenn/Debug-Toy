package net.kaedenn.debugtoy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.system.Os;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/** Main activity for the {@code net.kaedenn.debugtoy} application. */
public class MainActivity extends Activity {
    private static final String LOG_TAG = "main";

    private static WeakReference<MainActivity> mActivity;
    public static MainActivity getInstance() {
        return mActivity.get();
    }

    private TitleController titleController = null;

    private View page1 = null;
    private View page2 = null;
    private View page3 = null;
    private View currentPage = null;

    private DebugPageController debug = null;

    private SurfacePageController surfaceController = null;

    /* Application-specific files should be stored here */
    private File cacheDir = null;

    private int mTouchSlop = 0;

    /** Create the activity.
     *
     * This function also registers the primary commands that the
     * {@link DebugPageController} will handle.
     *
     * @param savedInstanceState Saved application information
     */
    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = new WeakReference<>(this);
        setContentView(R.layout.activity_main);
        cacheDir = getApplicationContext().getCacheDir();

        page1 = findViewById(R.id.page1);
        page2 = findViewById(R.id.page2);
        page3 = findViewById(R.id.page3);

        titleController = new TitleController(findViewById(R.id.titlebar));

        /* Title bar setup */
        String titlebarText = "testing";
        setTitleText(titlebarText);

        /* Select page1 directly */
        setPage(page1);

        /* TODO: Allow swiping between pages and remove the page buttons entirely */
        /* https://developer.android.com/training/gestures/viewgroup#intercept */
        /* https://developer.android.com/reference/android/view/ViewGroup */

        /* Setup for page 1 */

        /* Create the debug text controller */
        debug = new DebugPageController();

        /* Register the "env" command */
        debug.register(new Command("env", arg -> {
            /* System.getProperties */
            Properties p = System.getProperties();
            debug.debug(String.format("Properties: %s", p.size()));
            for (Object propKey : p.keySet()) {
                debug.debug(String.format("\"%s\" - \"%s\"", propKey, p.get(propKey)));
            }
            /* System.getenv */
            TreeMap<String, String> env = new TreeMap<>(System.getenv());
            env.forEach((k, v) -> debug.debug(String.format("$%s = \"%s\"", k, v)));
            /* Cache directory */
            debug.debug("cache: " + cacheDir.getAbsolutePath());
        }, "Display information about the environment"));

        /* Register the "id" command */
        debug.register(new Command("id", arg -> {
            debug.debug(String.format("pid: %d, ppid: %d", Os.getpid(), Os.getppid()));
            debug.debug(String.format("uid: %d, euid: %d", Os.getuid(), Os.geteuid()));
            debug.debug(String.format("gid: %d, egid: %d", Os.getgid(), Os.getegid()));
            debug.debug(String.format("tid: %d", Os.gettid()));
        }, "get user/group ID information"));

        /* Register the "title" command */
        debug.register(new Command("title", this::setTitleText, "set the title"));

        /* Register the "title" command */
        debug.register(new Command("qtitle",
                (arg) -> titleController.queueText(arg, true),
                "set the title"));

        /* Register the "!" command */
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
            catch (IOException | InterruptedException e) {
                debug.debug(e.toString());
            } catch (Exception e) {
                debug.debug("Unhandled exception: " + e.toString());
                throw e;
            }
        }, "Execute a system command"));

        debug.register(new Command("html-title", arg -> {
            TextView tv = findViewById(R.id.titlebar);
            Spanned text = Html.fromHtml(arg.isEmpty() ? "<b>Some text</b> with formatting <i>and stuff</i>" : arg, 0);
            tv.setText(text);
        }, "inspect an item"));

        /* Setup for page 2 */

        surfaceController = new SurfacePageController();

        /* Setup for page 3 */

    }

    @SuppressLint("DefaultLocale")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        debug.debug(String.format("onTouchEvent(%s, %d)", event.toString(), action));
        switch (action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(LOG_TAG,"Action was DOWN");
                debug.debug("Motion DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE) :
                Log.d(LOG_TAG,"Action was MOVE");
                debug.debug("Motion MOVE");
                return true;
            case (MotionEvent.ACTION_UP) :
                Log.d(LOG_TAG,"Action was UP");
                debug.debug("Motion UP");
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                Log.d(LOG_TAG,"Action was CANCEL");
                debug.debug("Motion CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(LOG_TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                debug.debug("Motion outside");
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    /** Force the given page to be visible.
     *
     * The other pages will be set to GONE. No checking is done to ensure that
     * {@param page} is actually one of the main pages.
     *
     * @param page The view to show
     */
    private void setPage(@NotNull View page) {
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
    private void selectPage(View targetPage) {
        View currentView = currentPage;
        if (currentView != null && targetPage != null && currentView != targetPage) {
            /* Transition between currentPage and targetPage */
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
    @SuppressWarnings("unused")
    private void showSnack(@NotNull CharSequence text) {
        showSnack(findViewById(R.id.top), text);
    }

    /** Show a "Snack Bar" message for the given view
     *
     * @param view The view to pass to {@link Snackbar#make}
     * @param text The text to show
     */
    @SuppressWarnings("unused")
    private void showSnack(@NotNull View view, @NotNull CharSequence text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    /** Show a toast message with a short duration.
     *
     * @param text The toast message to show
     * @see Toast
     */
    @SuppressWarnings("unused")
    private void shortToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    /** Show a toast message with a long duration.
     *
     * @param text The toast message to show
     * @see Toast
     */
    @SuppressWarnings("unused")
    private void longToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
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

    /** Get the device's screen size.
     *
     * @return The screen size where x is width and y is height.
     */
    public Point getScreenSize() {
        Display d = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        return size;
    }

    /** Set the title text and start the title text animation.
     *
     * @param text The text to use
     */
    private void setTitleText(String text) {
        titleController.setText(text, true);
    }
}

