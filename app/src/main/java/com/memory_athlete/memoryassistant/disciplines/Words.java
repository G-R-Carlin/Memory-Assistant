package com.memory_athlete.memoryassistant.disciplines;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.memory_athlete.memoryassistant.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import timber.log.Timber;


public class Words extends DisciplineFragment {
    private ArrayList<String> mDictionary = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ((EditText) rootView.findViewById(R.id.no_of_values)).setHint(getString(R.string.enter) + " " + getString(R.string.words_small));

        new DictionaryAsyncTask().execute();
        hasGroup = false;
        return rootView;
    }

    @Override
    protected void numbersVisibility(int visibility) {
        rootView.findViewById(R.id.practice_list_view).setVisibility(visibility);
    }

    @Override
    protected ArrayList backgroundArray() {
        StringBuilder stringBuilder = new StringBuilder();
        Random rand = new Random();
        ArrayList<String> arrayList = new ArrayList<>();
        short n;

        for (int i = 0; i < a.get(NO_OF_VALUES);) {
            n = (short) rand.nextInt(mDictionary.size());
            stringBuilder.append(mDictionary.get(n)).append("\n");
            if ((++i) % 20 == 0) {
                arrayList.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
            if (a.get(RUNNING) == FALSE) break;
        }
        arrayList.add(stringBuilder.toString());
        return arrayList;
    }

    @Override
    protected void generateRandom() {
        (new GenerateRandomArrayListAsyncTask()).execute(a);
    }

    private void createDictionary() {
        BufferedReader dict = null;

        try {
            dict = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.words)));
            String word;
            while ((word = dict.readLine()) != null) {
                mDictionary.add(word);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dict.close();
        } catch (IOException e) {
            Timber.e("File not closed");
        }
    }

    private class DictionaryAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... a) {
            createDictionary();
            return "";
        }
    }

}