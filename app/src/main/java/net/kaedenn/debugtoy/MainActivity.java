package net.kaedenn.debugtoy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import net.kaedenn.debugtoy.util.Res;
import net.kaedenn.debugtoy.util.StringUtil;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Main activity for the {@code net.kaedenn.debugtoy} application. */
public class MainActivity extends Activity {

    /* Load the native particle (Page 2) library */
    static {
        Runtime.getRuntime().loadLibrary("particle-native");
    }

    /* Tag used for logging */
    private static final String LOG_TAG = "main";

    /* Provide public access to this MainActivity */
    private static WeakReference<MainActivity> mActivity;
    public static MainActivity getInstance() {
        return mActivity.get();
    }

    /* Controller for the title bar's text and scrolling effect */
    private TitleController titleController = null;

    /* References to each page (and the current page) */
    private View page1 = null;
    private View page2 = null;
    private View page3 = null;
    private View currentPage = null;

    /* Controller for the first page. Public for other pages to use.  */
    public DebugPageController debug = null;

    /* Controller for the second page */
    private SurfacePageController surfaceController = null;

    /* Application-specific files should be stored here */
    /** Create the activity.
     *
     * This function performs initial setup for the three pages:
     * Page 1: Register the commands for the {@link DebugPageController}.
     * Page 2: Construct the surface controller.
     * Page 3: Nothing yet.
     *
     * @param savedInstanceState Saved application information
     */
    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = new WeakReference<>(this);
        setContentView(R.layout.activity_main);

        page1 = findViewById(R.id.page1);
        page2 = findViewById(R.id.page2);
        page3 = findViewById(R.id.page3);

        /* Title bar setup */
        titleController = new TitleController();
        titleController.setTextColor(((TextView)findViewById(R.id.titlebar)).getCurrentTextColor());

        for (String s : getResources().getStringArray(R.array.title_messages)) {
            titleController.addMessage(Html.fromHtml(s, 0));
        }

        /* Select page1 directly */
        setPage(page1);

        /* TODO: Allow swiping between pages and remove the page buttons entirely */
        /* https://developer.android.com/training/gestures/viewgroup#intercept */
        /* https://developer.android.com/reference/android/view/ViewGroup */

        /* Begin setup for page 1 */

        /* Create the debug text controller */
        debug = new DebugPageController();

        /* Register the "env" command */
        debug.register(new Command("env", arg -> {
            Context context = getApplicationContext();

            /* System.getProperties */
            Properties p = System.getProperties();
            debug.debugf("Properties: %s", p.size());
            for (Object propKey : p.keySet()) {
                debug.debugf("\"%s\" - \"%s\"", propKey, p.get(propKey));
            }
            /* System.getenv */
            System.getenv().forEach((k,v) -> debug.debugf("$%s = %s", k, StringUtil.escape(v)));
            /* Directories */
            debug.debugf("%s %s", "cache", context.getCacheDir().getAbsolutePath());
            debug.debugf("%s %s", "code cache", context.getCodeCacheDir().getAbsolutePath());
            debug.debugf("%s %s", "data", context.getDataDir().getAbsolutePath());
            debug.debugf("%s %s", "files", context.getFilesDir().getAbsolutePath());
            debug.debugf("%s %s", "obb", context.getObbDir().getAbsolutePath());
            if (context.getExternalCacheDir() != null) {
                debug.debugf("%s %s", "external cache", context.getExternalCacheDir().getAbsolutePath());
            }

        }, "Display information about the environment"));

        /* Register the "!" command */
        debug.register(new Command("!", arg -> {
            debug.debugf("Executing system command \"%s\"", arg);
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

        /* Register the "id" command */
        debug.register(new Command("id", arg -> {
            debug.debugf("pid: %d, ppid: %d", Os.getpid(), Os.getppid());
            debug.debugf("uid: %d, euid: %d", Os.getuid(), Os.geteuid());
            debug.debugf("gid: %d, egid: %d", Os.getgid(), Os.getegid());
            debug.debugf("tid: %d", Os.gettid());
        }, "get user/group ID information"));

        /* Register the "title" command */
        debug.register(new Command("title", titleController::addMessage, "add a title message"));

        /* Register the "html-title" command */
        debug.register(new Command("html-title", arg -> titleController.addMessage(Html.fromHtml(arg, 0)), "add HTML title message"));

        /* Begin setup for page 2 */

        surfaceController = new SurfacePageController();

        /* Begin setup for page 3 */

    }

    /** Process a touch event.
     *
     * @param event The motion event to process.
     * @return True when the event is consumed, false otherwise.
     *
     * @see Activity#onTouchEvent(MotionEvent)
     */
    @SuppressLint("DefaultLocale")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getActionMasked();
        //debug.debugf("onTouchEvent(%s, %d)", event.toString(), action);
        switch (action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(LOG_TAG, "Action was DOWN");
                debug.debug("Motion DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE) :
                Log.d(LOG_TAG, "Action was MOVE");
                debug.debug("Motion MOVE");
                return true;
            case (MotionEvent.ACTION_UP) :
                Log.d(LOG_TAG, "Action was UP");
                debug.debug("Motion UP");
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                Log.d(LOG_TAG, "Action was CANCEL");
                debug.debug("Motion CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(LOG_TAG, "Movement occurred outside of screen bounds");
                debug.debug("Motion outside");
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    /** Process a touch event.
     *
     * @param event The motion event to process.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
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
                    .setDuration(Res.getInteger(R.integer.pageAnimationDuration))
                    .setListener(null);
            currentView.animate()
                    .alpha(0f)
                    .setDuration(Res.getInteger(R.integer.pageAnimationDuration))
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

    /** Handle button clicks.
     *
     * This function is called when any of the app's buttons are pressed. The
     * case statements are organized by page, with the top-level button cases
     * first.
     *
     * @param button Reference to the button clicked.
     */
    public void onButtonClick(@NotNull View button) {
        String cmd = debug.getDebugCommand();
        switch (button.getId()) {
            /* Page selection */
            case R.id.btPage1:
                selectPage(page1);
                break;
            case R.id.btPage2:
                selectPage(page2);
                break;
            case R.id.btPage3:
                selectPage(page3);
                break;
            /* Page 1 */
            case R.id.btDebug:
                if (debug.isRegistered(cmd)) {
                    debug.execute(cmd);
                } else {
                    String err_f = Res.getString(R.string.err_no_cmd_f);
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
            /* Page 2 */
            /* Page 3 */
            default:
                String err_f = Res.getString(R.string.err_invalid_button_f);
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
}

