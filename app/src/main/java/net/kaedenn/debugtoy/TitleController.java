package net.kaedenn.debugtoy;

import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import net.kaedenn.debugtoy.util.Strings;

class TitleController {
    private static final String LOG_TAG = "title";

    private String text;
    private Animation anim = null;
    private TextView titleView;

    TitleController(TextView textView) {
        titleView = textView;
        text = "";
        Log.d(LOG_TAG, "TitleController constructed");
    }

    void setText(String newText) {
        text = newText;
        startAnimation();
        Log.d(LOG_TAG, String.format("Setting title to %s", Strings.escape(newText)));
    }

    private void startAnimation() {
        Point screenSize = MainActivity.getInstance().getScreenSize();
        Paint p = titleView.getPaint();
        int width = Math.round(p.measureText(text));
        int endWidth = width;

        String titleText = text;
        if (endWidth < screenSize.x) {
            /* Append spaces to make the title text long enough to scroll
             * cleanly off screen. */
            StringBuilder sb = new StringBuilder(text);
            double spaceWidth = p.measureText(" ");
            int numSpaces = (int)Math.ceil((screenSize.x - endWidth) / spaceWidth);
            for (int i = 0; i < numSpaces; ++i) { sb.append(" "); }
            titleText = text;
            endWidth = Math.round(p.measureText(titleText));
        }

        /* The final text used in the animation */
        final String finalText = titleText;

        /* Ensure the text box is large enough to store the string */
        ViewGroup.LayoutParams params = titleView.getLayoutParams();
        params.width = endWidth;
        titleView.setLayoutParams(params);

        /* Calculate the final starting and ending offsets */
        float margin = screenSize.x / 10.0f;
        float fromX = screenSize.x;
        float toX = -(width + margin);

        anim = new TranslateAnimation(fromX, toX, 0.0f, 0.0f);
        anim.setFillAfter(true);
        anim.setDuration(Math.round(3.5 * (width + screenSize.x + margin)));
        titleView.post(() -> {
            titleView.setText(finalText);
            titleView.startAnimation(anim);
        });
    }

}
