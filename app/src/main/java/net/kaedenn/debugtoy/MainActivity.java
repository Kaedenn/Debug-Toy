package net.kaedenn.debugtoy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.system.Os;
import android.text.Html;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import net.kaedenn.debugtoy.util.Logf;
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

    /* Tag used for logging */
    private static final String LOG_TAG = "main";

    /* Load the native particle (Page 2) library */
    static {
        Runtime.getRuntime().loadLibrary("particle-native");
        Logf.getInstance().add(MainActivity.class, LOG_TAG);
    }

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

    /* Types of navigation animation for switching the active page */
    private static final int PAGE_NO_ANIMATION = 0;
    private static final int PAGE_FADE_ANIMATION = 1;
    private static final int PAGE_SLIDE_ANIMATION = 2;
    private int mPageAnimationType = PAGE_FADE_ANIMATION;

    /* Controller for the first page. Public for other pages to use */
    public DebugPageController debug = null;

    /* Controller for the second page */
    private SurfacePageController surfaceController = null;

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
        /* https://github.com/codepath/android_guides/wiki/Gestures-and-Touch-Events */
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

        /* Register the "anim" command */
        debug.register(new Command("anim", arg -> {
            try {
                int animMode = Integer.parseInt(arg);
                switch (animMode) {
                    case PAGE_NO_ANIMATION:
                    case PAGE_FADE_ANIMATION:
                    case PAGE_SLIDE_ANIMATION:
                        mPageAnimationType = animMode;
                        debug.debugf("Set animation type to %d", animMode);
                        break;
                    default:
                        debug.debugf("Invalid animation index %d", animMode);
                        break;
                }
            }
            catch (NumberFormatException e) {
                debug.debugf("Failed to parse argument \"%s\" as a number: %s", arg, e.toString());
            }
        }, "change the page animation type"));

        /* Begin setup for page 2 */

        /* Disable surface page entirely for now
        surfaceController = new SurfacePageController();
         */

        /* Begin setup for page 3 */

    }

    /** Process a touch event.
     *
     * @param event The motion event to process.
     * @return True when the event is consumed, false otherwise.
     */
    @SuppressLint("DefaultLocale")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        Logf.dc("Obtained final touch event %s", event.toString());
        int action = event.getActionMasked();
        switch (action) {
            case (MotionEvent.ACTION_DOWN) :
                Logf.dc("Action was DOWN");
                debug.debug("Motion DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE) :
                Logf.dc("Action was MOVE");
                debug.debug("Motion MOVE");
                return true;
            case (MotionEvent.ACTION_UP) :
                Logf.dc("Action was UP");
                debug.debug("Motion UP");
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                Logf.dc("Action was CANCEL");
                debug.debug("Motion CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Logf.dc("Movement occurred outside of screen bounds");
                debug.debug("Motion outside");
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    /** Intercepts a touch event.
     *
     * @param event The motion event to process.
     * @return True if the event should be consumed, false otherwise.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Logf.dc("Received touch event %s, dispatching", event.toString());
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

    /** Navigate to a different page with an animation.
     *
     * This method also executes any code necessary to load or unload a specific
     * page, such as calling any "entering page" or "leaving page" methods.
     *
     * Does nothing if the source and target pages are the same (or if one of
     * them are {@code null}).
     *
     * @param targetPage The page to navigate to.
     */
    private void selectPage(View targetPage) {
        if (currentPage != null && targetPage != null && currentPage != targetPage) {
            /* Transition between currentPage and targetPage */
            animatePageTransition(currentPage, targetPage);
            /* Handle code unique to each page */
            /* The page 2 animation is disabled at the moment.
            if (targetPage == page2) {
                surfaceController.doEnterPage();
            } else if (currentPage == page2) {
                surfaceController.doLeavePage();
            }
            */
        }
        currentPage = targetPage;
    }

    /** Handle the animation logic to transition from one page to another.
     *
     * Animations are set via {@code mPageAnimationType} and each animation type
     * has its own animation logic.
     *
     * @param pageFrom The page we're navigating from.
     * @param pageTo The page we're navigating to.
     */
    private void animatePageTransition(View pageFrom, View pageTo) {
        final int animDuration = Res.getInteger(R.integer.pageAnimationDuration);
        switch (mPageAnimationType) {
            case PAGE_NO_ANIMATION: {
                /* Simple case: pages just appear and disappear. No animation */
                pageTo.setVisibility(View.VISIBLE);
                pageFrom.setVisibility(View.GONE);
            } break;
            case PAGE_FADE_ANIMATION: {
                /* Fading animation: pages cross-fade */
                /* Set the destination page as visible (but transparent) */
                pageTo.setAlpha(0);
                pageTo.setVisibility(View.VISIBLE);
                /* Animate the destination page to visible */
                pageTo.animate()
                        .alpha(1f)
                        .setDuration(animDuration)
                        .setListener(null);
                /* Animate the source page to transparent */
                pageFrom.animate()
                        .alpha(0f)
                        .setDuration(animDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                pageFrom.setVisibility(View.GONE);
                                pageFrom.setAlpha(1f);
                            }
                        });
            } break;
            case PAGE_SLIDE_ANIMATION: {
                /* Sliding animation: pages slide in and out horizontally */
                Point screenSize = getScreenSize();
                /* Scroll pageFrom from 0 to -screenSize.x */
                ObjectAnimator fromAnim = ObjectAnimator.ofFloat(pageFrom, "translationX", -screenSize.x);
                fromAnim.setDuration(animDuration);
                fromAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Logf.d(LOG_TAG, "Sliding animation from %s to %s complete", pageFrom, pageTo);
                        pageFrom.setVisibility(View.GONE);
                        super.onAnimationEnd(animation);
                    }
                });
                fromAnim.start();
                /* Scroll pageTo from screenSize.x to 0 */
                pageTo.setTranslationX(screenSize.x);
                pageTo.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(pageTo, "translationX", 0f)
                        .setDuration(animDuration)
                        .start();
            } break;
            default:
                Logf.ec("Invalid page transition selection %d", mPageAnimationType);
                break;
        }
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
            /* Default */
            default:
                String err_f = Res.getString(R.string.err_invalid_button_f);
                showSnack(String.format(err_f, button.getId()));
                break;
        }
    }

    public void onSwitchToggle(@NotNull View switchView) {
        if (!(switchView instanceof Switch)) {
            throw new RuntimeException(String.format("View %s not a Switch", switchView.toString()));
        }
        boolean isOn = ((Switch)switchView).isChecked();
        switch (switchView.getId()) {
            case R.id.switchDebug:
                debug.debug("Debug toggle switch is " + (isOn ? "on" : "off"));
                shortToast("Debug toggle switch is " + (isOn ? "on" : "off"));
                break;
            case R.id.switchAnimation:
                debug.debug("Animation toggle switch is " + (isOn ? "on" : "off"));
                shortToast("Animation toggle switch is " + (isOn ? "on" : "off"));
                break;
            default:
                String err_f = Res.getString(R.string.err_invalid_button_f);
                showSnack(String.format(err_f, switchView.getId()));
                break;
        }
    }

    public void onPageAnimationSelection(@NotNull View radioButton) {
        if (!(radioButton instanceof RadioButton)) {
            throw new AssertionError("Page animation selection object must be a RadioButton; got: " + radioButton.toString());
        }
        switch (radioButton.getId()) {
            case R.id.radioAnimNone:
                mPageAnimationType = PAGE_NO_ANIMATION;
                break;
            case R.id.radioAnimFade:
                mPageAnimationType = PAGE_FADE_ANIMATION;
                break;
            case R.id.radioAnimSlide:
                mPageAnimationType = PAGE_SLIDE_ANIMATION;
                break;
            default:
                Logf.ec("Invalid radio button ID %d from %s", radioButton.getId(), radioButton.toString());
                break;
        }
    }

    public void onViewClickDefault(@NotNull View view) {
        debug.debugf("Clicked on view %s", view.toString());
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

