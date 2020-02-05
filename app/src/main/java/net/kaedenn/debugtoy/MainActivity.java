package net.kaedenn.debugtoy;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private DebugTextController debug;

    private static final int TAB1_INDEX = 0;
    private static final int TAB2_INDEX = 1;
    private static final int TAB3_INDEX = 2;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debug = new DebugTextController(this);
        selectTab(TAB1_INDEX);

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> selectTab(TAB1_INDEX));

        debug.register(R.string.cmd_show_fab, () -> fab.show(), R.string.cmd_show_fab_help);

        debug.register(R.string.cmd_hide_fab, () -> fab.hide(), R.string.cmd_hide_fab_help);

        debug.register(getResources().getString(R.string.cmd_view), () -> debug.debugView(findViewById(R.id.btTab1)), getResources().getString(R.string.cmd_view_help));

        /* cmd_help command (and default) */
        Runnable helpCommand = () -> {
            debug.debug(getResources().getString(R.string.cmd_help_commands));
            for (String s : debug.getCommands()) {
                String helpText = debug.getHelp(s);
                debug.debug(String.format("%-8s %s", s, helpText));
            }
        };
        debug.register(R.string.cmd_help, helpCommand, R.string.cmd_help_help);
        debug.registerDefault(helpCommand, R.string.cmd_help_help);

    }

    private void selectTab(int idx) {
        findViewById(R.id.tabItem1).setVisibility(idx == TAB1_INDEX ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.tabItem2).setVisibility(idx == TAB2_INDEX ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.tabItem3).setVisibility(idx == TAB3_INDEX ? View.VISIBLE : View.INVISIBLE);
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
