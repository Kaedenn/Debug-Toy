package net.kaedenn.debugtoy;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
/*
import androidx.appcompat.widget.Toolbar;
 */

import android.view.*;
import android.widget.*;

import org.jetbrains.annotations.*;

public class MainActivity extends AppCompatActivity {

    private DebugTextController debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debug = new DebugTextController(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debug.debug("Debugging text");
                debug.debug(view);
                showSnack("Replace text with your own action");
            }
        });

        /* cmd_show_fab command */
        debug.register(getResources().getString(R.string.cmd_show_fab), new Runnable() {
            @Override
            public void run() {
                FloatingActionButton f = findViewById(R.id.fab);
                f.show();
            }
        }, getResources().getString(R.string.cmd_show_fab_help));
        /* cmd_hide_fab command */
        debug.register(getResources().getString(R.string.cmd_hide_fab), new Runnable() {
            @Override
            public void run() {
                FloatingActionButton f = findViewById(R.id.fab);
                f.hide();
            }
        }, getResources().getString(R.string.cmd_hide_fab_help));

        /* Default command */
        debug.register(getResources().getString(R.string.cmd_help), new Runnable() {
            @Override
            public void run() {
                debug.debug(getResources().getString(R.string.cmd_help_commands));
                for (String s : debug.getCommands()) {
                    String helpText = debug.getHelp(s);
                    debug.debug(String.format("%-8s %s", s, helpText));
                }
            }
        });
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
                break;
            default:
                String err_f = getResources().getString(R.string.err_invalid_button_f);
                showSnack(String.format(err_f, button.getId()));
                break;
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
