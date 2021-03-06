package net.kaedenn.debugtoy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.system.Os;
import android.text.Html;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import net.kaedenn.debugtoy.annotation.Callback;
import net.kaedenn.debugtoy.util.Logf;
import net.kaedenn.debugtoy.util.Res;
import net.kaedenn.debugtoy.util.Str;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/** Main activity for the {@code net.kaedenn.debugtoy} application. */
@SuppressLint("DefaultLocale")
public class MainActivity extends Activity {

    /* Logging setup */
    private static final String LOG_TAG = "main";
    static {
        Logf.getInstance().add(MainActivity.class, LOG_TAG);
    }

    /* Native code is disabled until some actual use is found.
    static {
        Runtime.getRuntime().loadLibrary("particle-native");
    }
    */

    /* Provide public access to this MainActivity */
    private static WeakReference<MainActivity> mActivity;
    public static MainActivity getInstance() {
        return mActivity.get();
    }

    /* Controller for the title bar's text and scrolling effect */
    private TitleController mTitleController = null;

    /* References to each page (and the current page) */
    private View page1 = null;
    private View page2 = null;
    private View page3 = null;
    private View mCurrentPage = null;

    /* Types of navigation animation for switching the active page */
    private static final int PAGE_NO_ANIMATION = 0;
    private static final int PAGE_FADE_ANIMATION = 1;
    private static final int PAGE_SLIDE_ANIMATION = 2;
    private int mPageAnimationType = PAGE_SLIDE_ANIMATION;

    /* Controller for the first page. Public for other pages to use */
    public DebugPageController debug = null;

    /** Create the activity.
     *
     * This is the entry point to the application. This method contains the code
     * for setting up the individual pages (where necessary) and for setting up
     * the scrolling titlebar widget.
     *
     * @param savedInstanceState Saved application information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = new WeakReference<>(this);
        setContentView(R.layout.activity_main);

        page1 = requireViewById(R.id.page1);
        page2 = requireViewById(R.id.page2);
        page3 = requireViewById(R.id.page3);

        /* TODO: Figure out a good reason to replace page1, page2, page3 with a hash map
        ViewGroup pages = requireViewById(R.id.pagesLayout);
        for (int i = 0; i < pages.getChildCount(); ++i) {
            View page = pages.getChildAt(i);
            mPageMap.put(page.getId(), page);
        }
        */

        /* Title bar setup */
        mTitleController = new TitleController();
        for (String s : getResources().getStringArray(R.array.tbMessages)) {
            mTitleController.getTicker().addMessage(Html.fromHtml(s, 0));
        }
        mTitleController.getTicker().startAnimation();

        /* Select page1 directly */
        page1.setVisibility(View.VISIBLE);
        page2.setVisibility(View.GONE);
        page3.setVisibility(View.GONE);
        mCurrentPage = page1;

        /* TODO: Allow swiping between pages and remove the page buttons entirely */
        /* https://github.com/codepath/android_guides/wiki/Gestures-and-Touch-Events */

        /* Begin setup for page 1 */

        /* Create the debug text controller */
        debug = new DebugPageController();

        debug.register("env", arg -> {
            Context context = getApplicationContext();

            /* System.getProperties */
            for (String propKey : System.getProperties().stringPropertyNames()) {
                debug.debug(Str.kvToHtml("prop", propKey, System.getProperty(propKey)));
            }
            /* System.getenv */
            for (String envKey : System.getenv().keySet()) {
                debug.debug(Str.kvToHtml("env", envKey, System.getenv(envKey)));
            }
            /* Directories */
            debug.debug(Str.kvToHtml("cache", context.getCacheDir().getAbsolutePath()));
            debug.debug(Str.kvToHtml("code cache", context.getCodeCacheDir().getAbsolutePath()));
            debug.debug(Str.kvToHtml("data", context.getDataDir().getAbsolutePath()));
            debug.debug(Str.kvToHtml("files", context.getFilesDir().getAbsolutePath()));
            debug.debug(Str.kvToHtml("obb", context.getObbDir().getAbsolutePath()));
            if (context.getExternalCacheDir() != null) {
                debug.debug(Str.kvToHtml("external cache", context.getExternalCacheDir().getAbsolutePath()));
            }
            if (Debug.isDebuggerConnected()) {
                debug.debug("Debugger is connected");
            }
        }, "display information about the environment");

        debug.register("!", arg -> {
            debug.debug("Executing system command \"%s\"", arg);
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
            } catch (IOException | InterruptedException e) {
                debug.debug(e.toString());
            } catch (Exception e) {
                debug.debug("Unhandled exception: " + e.toString());
                throw e;
            }
        }, "execute a system command");

        debug.register("id", arg -> {
            debug.debug("pid: %d, ppid: %d", Os.getpid(), Os.getppid());
            debug.debug("uid: %d, euid: %d", Os.getuid(), Os.geteuid());
            debug.debug("gid: %d, egid: %d", Os.getgid(), Os.getegid());
            debug.debug("tid: %d", Os.gettid());
        }, "get user/group ID information");

        debug.register("title", arg -> {
            mTitleController.getTicker().addMessage(arg);
        }, "add new title message");

        debug.register("html-title", arg -> {
            mTitleController.getTicker().addMessage(Html.fromHtml(arg, 0));
        }, "add new HTML title message");

        debug.register("page-anim", arg -> {
            Integer animMode = Str.tryParseInteger(arg);
            if (animMode == null) {
                debug.debug("Failed to parse argument \"%s\" as an integer", arg);
            } else if (animMode == PAGE_NO_ANIMATION || animMode == PAGE_FADE_ANIMATION || animMode == PAGE_SLIDE_ANIMATION) {
                mPageAnimationType = animMode;
                toast("Set page animation type to mode %d", animMode);
            } else {
                debug.debug("Invalid animation index %d", animMode);
                toast("Invalid animation type %d", animMode);
            }
        }, "change the page animation type");

        /* Begin setup for page 2 */

        /* Begin setup for page 3 */

    }

    /** Intercepts a touch event before any of the children views see it.
     *
     * @param event The motion event to process.
     * @return True if the event should be consumed, false otherwise.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        /*
        Rect r = mTitleController.getAbsoluteCoordinates();
        Logf.dc("Received touch event %s", event.toString());
        if (event.getY() >= r.top && event.getY() <= r.bottom) {
            Logf.dc("Touch event was on top of the titlebar");
            if (mTitleController.processTouchEvent(event)) {
                return true;
            }
        }
        */
        /* Continue propagating the event */
        return super.dispatchTouchEvent(event);
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
        if (mCurrentPage != null && targetPage != null && mCurrentPage != targetPage) {
            /* Transition between mCurrentPage and targetPage */
            animatePageTransition(mCurrentPage, targetPage);
            /* Handle code unique to each page */
        }
        mCurrentPage = targetPage;
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
                ObjectAnimator fromAnim = ObjectAnimator.ofFloat(pageFrom, "translationX", 0f, -screenSize.x);
                fromAnim.setDuration(animDuration);
                fromAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pageFrom.setVisibility(View.GONE);
                        super.onAnimationEnd(animation);
                    }
                });
                fromAnim.start();
                /* Scroll pageTo from screenSize.x to 0 */
                pageTo.setVisibility(View.VISIBLE);
                pageTo.setAlpha(1f);
                ObjectAnimator toAnim = ObjectAnimator.ofFloat(pageTo, "translationX", screenSize.x, 0f);
                toAnim.setDuration(animDuration);
                toAnim.start();
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
     * @param text The text to show.
     */
    private void showSnack(@NotNull CharSequence text) {
        showSnack(requireViewById(R.id.top), text);
    }

    /** Show a "Snack Bar" message for the given view
     *
     * @param view The view to pass to {@link Snackbar#make}.
     * @param text The text to show.
     */
    private void showSnack(@NotNull View view, @NotNull CharSequence text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    /** Show a toast message with a short duration.
     *
     * @param text The toast message to show.
     * @see Toast
     */
    private void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    /** Show a formatted toast message with a short duration.
     *
     * @param format The format string for the toast message.
     * @param args The format arguments.
     */
    private void toast(String format, Object... args) {
        toast(String.format(format, args));
    }

    /** Handle button clicks.
     *
     * This function is called when any of the app's buttons are pressed. The
     * case statements are organized by page, with the top-level button cases
     * first.
     *
     * @param button Reference to the button clicked.
     */
    @Callback
    public void onButtonClick(@NotNull View button) {
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
            case R.id.btDebug: {
                String cmd = debug.getDebugCommand();
                if (debug.isRegistered(cmd)) {
                    debug.execute(cmd);
                } else {
                    showSnack(String.format("Failed to execute command \"%s\": no such command", cmd));
                }
            } break;
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
                showSnack(String.format("Unknown button with ID %d", button.getId()));
                break;
        }
    }

    /** Handle button toggle.
     *
     * This method is called when a registered toggle button changes states.
     *
     * @param button Reference to the button that changed states.
     */
    @Callback
    public void onButtonToggle(@NotNull View button) {
        CompoundButton b;
        try {
            b = (CompoundButton) button;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("View %s is not a compound button (error %s)", button.toString(), e.toString()));
        }
        int bid = b.getId();
        boolean on = b.isChecked();
        switch (bid) {
            case R.id.radioAnimNone:
                if (on) mPageAnimationType = PAGE_NO_ANIMATION;
                break;
            case R.id.radioAnimFade:
                if (on) mPageAnimationType = PAGE_FADE_ANIMATION;
                break;
            case R.id.radioAnimSlide:
                if (on) mPageAnimationType = PAGE_SLIDE_ANIMATION;
                break;
            case R.id.titleSpeedFast:
                if (on) mTitleController.setSpeedFast();
                break;
            case R.id.titleSpeedMedium:
                if (on) mTitleController.setSpeedNormal();
                break;
            case R.id.titleSpeedSlow:
                if (on) mTitleController.setSpeedSlow();
                break;
            case R.id.titleDirection:
                if (on) {
                    mTitleController.setLeftToRight();
                } else {
                    mTitleController.setRightToLeft();
                }
                break;
            default:
                showSnack(String.format("Unknown button with ID %d: %s", bid, b.toString()));
                break;
        }
    }

    /** Default callback for interactive views without callbacks.
     *
     * @param view The view that was clicked/touched.
     */
    @SuppressWarnings("unused")
    @Callback
    public void onViewClickDebugHandler(@NotNull View view) {
        debug.debug("Default click handler called on %s", view.toString());
    }

    /** Get the device's screen size in pixels.
     *
     * @return The screen size where x is width and y is height.
     */
    public Point getScreenSize() {
        Display d = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        return size;
    }

    /** Get the device's screen width in pixels.
     *
     * @return The width of the screen in pixels.
     */
    public int getScreenWidth() {
        Point size = getScreenSize();
        return size.x;
    }

    public Rect getAbsoluteLocation(@NotNull View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        int x = xy[0];
        int y = xy[1];
        int w = x + v.getWidth();
        int h = y + v.getHeight();
        return new Rect(x, y, w, h);
    }
}

