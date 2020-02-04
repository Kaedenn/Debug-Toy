package net.kaedenn.debugtoy;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private DebugTextController debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setSupportActionBar((Toolbar)findViewById(R.id.actionBar));
        debug = new DebugTextController(this);
        selectTab(0);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTab(0);
            }
        });

        /* cmd_show_fab command */
        debug.register(R.string.cmd_show_fab, new Runnable() {
            @Override
            public void run() {
                FloatingActionButton f = findViewById(R.id.fab);
                f.show();
            }
        }, R.string.cmd_show_fab_help);

        /* cmd_hide_fab command */
        debug.register(R.string.cmd_hide_fab, new Runnable() {
            @Override
            public void run() {
                FloatingActionButton f = findViewById(R.id.fab);
                f.hide();
            }
        }, R.string.cmd_hide_fab_help);

        /* cmd_view command */
        debug.register(getResources().getString(R.string.cmd_view), new Runnable() {
            @Override
            public void run() {
                debug.debugView(findViewById(R.id.btTab1));
            }
        }, getResources().getString(R.string.cmd_view_help));

        /* cmd_help command (and default) */
        Runnable helpCommand = new Runnable() {
            @Override
            public void run() {
                debug.debug(getResources().getString(R.string.cmd_help_commands));
                for (String s : debug.getCommands()) {
                    String helpText = debug.getHelp(s);
                    debug.debug(String.format("%-8s %s", s, helpText));
                }
            }
        };
        debug.register(R.string.cmd_help, helpCommand, R.string.cmd_help_help);
        debug.registerDefault(helpCommand, R.string.cmd_help_help);
    }

    private void selectTab(int idx) {
        int[] visible = {View.INVISIBLE, View.INVISIBLE, View.INVISIBLE};
        if (idx == 0) visible[0] = View.VISIBLE;
        if (idx == 1) visible[1] = View.VISIBLE;
        if (idx == 2) visible[2] = View.VISIBLE;
        findViewById(R.id.tabItem1).setVisibility(visible[0]);
        findViewById(R.id.tabItem2).setVisibility(visible[1]);
        findViewById(R.id.tabItem3).setVisibility(visible[2]);
    }

    private void showSnack(@NotNull CharSequence text) {
        showSnack(findViewById(R.id.fab), text);
    }

    private void showSnack(@NotNull View view, @NotNull CharSequence text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void onButtonClick(@NotNull View button) {
        String cmd = debug.getDebugCommand();
        switch (button.getId()) {
            case R.id.btDebug:
                if (!debug.execute(cmd)) {
                    String err_f = getResources().getString(R.string.err_no_cmd_f);
                    showSnack(String.format(err_f, cmd));
                }
                break;
            case R.id.btClear:
                debug.clearDebug();
                debug.clearDebugCommand();
                break;
            default:
                String err_f = getResources().getString(R.string.err_invalid_button_f);
                showSnack(String.format(err_f, button.getId()));
                break;
        }
    }

    public void onTabButtonClick(@NotNull View button) {
        switch (button.getId()) {
            case R.id.btTab1:
                selectTab(0);
                break;
            case R.id.btTab2:
                selectTab(1);
                break;
            case R.id.btTab3:
                selectTab(2);
                break;
            default:
                String err_f = getResources().getString(R.string.err_invalid_button_f);
                showSnack(String.format(err_f, button.getId()));
                break;
        }
    }
}
