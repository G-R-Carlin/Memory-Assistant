package com.memory_athlete.memoryassistant.mySpace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.memory_athlete.memoryassistant.Helper;
import com.memory_athlete.memoryassistant.R;
import com.memory_athlete.memoryassistant.reminders.ReminderUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import timber.log.Timber;

public class WriteFile extends AppCompatActivity {
    private boolean name = false;
    String path;
    String oldName = null;
    boolean deleted = false;
    EditText searchEditText;
    EditText mySpaceEditText;
    FloatingActionButton searchFAB;
    int searchIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Helper.theme(this, WriteFile.this);
        setContentView(R.layout.activity_write_file);
        String header = intent.getStringExtra("mHeader");
        if (header == null) header = "New";
        else oldName = header;
        //header = header.substring(0, header.length() - 4);
        setTitle(header);

        mySpaceEditText = findViewById(R.id.my_space_editText);
        searchEditText = findViewById(R.id.search_edit_text);
        searchFAB = findViewById((R.id.search_mySpace_FAB));

        path = intent.getStringExtra("fileName");
        if (intent.getBooleanExtra("name", true)) {
            ((EditText) findViewById(R.id.f_name)).setText(getTitle().toString());
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(
                        path + File.separator + getTitle().toString() + ".txt")));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                mySpaceEditText.setText(text);
                //findViewById(R.id.saveFAB).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //intent.getStringExtra()

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    searchIndex = 0;
                    search("" + s);
                    // TODO searchFAB.setImageDrawable();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                Timber.d(getTitle().toString());
                File file = new File(path + File.separator + getTitle().toString() + ".txt");
                deleted = true;
                finish();
                return !file.exists() || file.delete();
            case R.id.dont_save:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case android.R.id.home:
                if (save()) NavUtils.navigateUpFromSameTask(this);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // hide searchEditText if it is visible
        if (searchEditText.getVisibility() == View.VISIBLE) {
            searchEditText.setVisibility(View.GONE);
            return;
        }
        // save() returns false if save was rejected to notify the user at most once.
        if (save()) super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!deleted) save();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean save() {
        String string = mySpaceEditText.getText().toString();
        String fname = ((EditText) findViewById(R.id.f_name)).getText().toString();
        if (fname.length() == 0) {
            if (!name) {
                ((EditText) findViewById(R.id.f_name)).setError("please enter a name");
                findViewById(R.id.f_name).requestFocus();
                //Toast.makeText(this, "please enter a name", Toast.LENGTH_SHORT).show();
                name = true;
                return false;
            }
            Toast.makeText(this, "Didn't save nameless file", Toast.LENGTH_SHORT).show();
            return true;
        }
        String dirPath = path;
        if (fname.length() > 250) {
            if (name) return true;

            Toast.makeText(this, "Try again with a shorter name", Toast.LENGTH_SHORT).show();
            name = true;
            return false;
        }

        if (oldName != null && !fname.equals(oldName)) {
            File from = new File(path + File.separator + oldName + ".txt");
            if (from.exists()) {
                File to = new File(path + File.separator + fname + ".txt");
                from.renameTo(to);
            }
        }

        fname = path + File.separator + fname + ".txt";
        Timber.v("fname = %s", fname);
        if (!Helper.mayAccessStorage(this)) {
            if (name) {
                Toast.makeText(this, "Permission to access storage is needed",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            name = true;
            return false;
        }
        if (Helper.externalStorageNotWritable()) {
            Toast.makeText(this, "Please check the storage", Toast.LENGTH_SHORT).show();
            if (name) return true;

            name = true;
            return false;
        }
        if (Helper.makeDirectory(dirPath)) {
            try {
                FileOutputStream outputStream = new FileOutputStream(new File(fname));
                outputStream.write(string.getBytes());
                outputStream.close();

                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(this).edit();
                editor.putLong(fname, System.currentTimeMillis());
                Timber.v(fname + "made at " + System.currentTimeMillis());
                editor.apply();
                ReminderUtils.mySpaceReminder(this, fname);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
            }
        }
        Timber.v("fileName = %s", path);
        return true;
    }

    private void search(String stringToSearch) {
        String fullText = mySpaceEditText.getText().toString();
        boolean hasText = fullText.contains(stringToSearch);
        if (hasText) {
            searchIndex = fullText.indexOf(stringToSearch, searchIndex);
            if (searchIndex == -1) searchIndex = 0;

            int lineNumber = mySpaceEditText.getLayout().getLineForOffset(searchIndex);
            int totalLines = mySpaceEditText.getLayout().getLineCount();
            int editTextViewBottom = findViewById(R.id.my_space_editText).getBottom();
            findViewById(R.id.my_space_scroll_view).scrollTo(0, editTextViewBottom * lineNumber / totalLines);
            searchIndex++;
            return;
        }
        Toast.makeText(getApplicationContext(), R.string.not_found, Toast.LENGTH_SHORT).show();
    }

    public void search(View view) {
        String stringToSearch = searchEditText.getText().toString();
        searchIndex++;
        search(stringToSearch);
        searchEditText.setVisibility(View.VISIBLE);
    }
}
