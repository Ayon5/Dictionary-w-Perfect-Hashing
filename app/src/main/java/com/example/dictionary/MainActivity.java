package com.example.dictionary;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

class slot {
    int a, b, n, m, p;
    ArrayList <String> keys, vals;
    private String[] map, mpkey;

    slot () {
        keys = new ArrayList<>();
        vals = new ArrayList<>();
    }

    private int hash(String key) {
        long res = 0;
        for(char ch : key.toCharArray()) {
            res = (res * a + (int) ch) % p;
        }
        res = (res * a + b) % p;
        return (int) (res % m);
    }

    boolean Collision() {
        map = new String[m];
        mpkey = new String[m];
        for (int i = 0; i < keys.size(); i++) {
            int index = hash(keys.get(i));
            if(map[index] != null) return true;
            map[index] = vals.get(i);
            mpkey[index] = keys.get(i);
        }
        return false;
    }

    String search (String key) {
        if(keys.isEmpty()) return null;
        int index = hash(key);
        if(mpkey[index] == null || !mpkey[index].equals(key)) return null;
        return map[index];
    }
}

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText txtWord;
    private TextView txtResult;
    private Button searchButton;

    private static int m = 100005, a, b, prime;
    private slot[] holes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtWord = findViewById(R.id.wordToSearch);
        txtResult = findViewById(R.id.wordResult);
        searchButton = findViewById(R.id.search);

        searchButton.setOnClickListener(this);
        holes = new slot[m];
        for (int i = 0; i < m; i++) holes[i] = new slot();
        prime = getRandPrime(m + 10, m + 1000);
        a = ThreadLocalRandom.current().nextInt(1, prime - 1);
        b = ThreadLocalRandom.current().nextInt(0, prime - 1);
        try {
            dicData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPrime(int p) {
        for (int i = 2; i * i <= p; i++) {
            if(p % i == 0) return false;
        }
        return true;
    }

    private int getRandPrime(int l, int r) {
        int prime;
        do {
            prime = ThreadLocalRandom.current().nextInt(l, r);
        } while (isPrime(prime));
        return prime;
    }

    private int priHash(String key) {
        long res = 0;
        for (char ch : key.toCharArray()) {
            res = res * a + (int) ch;
            res = res % prime;
        }
        res = (res * a + b) % prime;
        return (int)(res % m);
    }

    private void addWord(String eng, String ban) {
        int index = priHash(eng);
        holes[index].keys.add(eng);
        holes[index].vals.add(ban);
    }

    @SuppressLint("Assert")
    private void secHash() {
        for (slot S : holes) {
            if(S.keys.isEmpty()) continue;
            S.n = S.keys.size();
            S.m = S.n * S.n;
            int c = 0;
            do {
                S.p = getRandPrime(m + 500, m + 1000);
                S.a = ThreadLocalRandom.current().nextInt(1, S.p - 1);
                S.b = ThreadLocalRandom.current().nextInt(0, S.p - 1);
                c++;
            } while (S.Collision() && c < 500);
            if (c >= 500) {
                for(String str : S.keys) System.out.println(str);
                throw new AssertionError();
            }
        }
    }

    private void dicData() throws IOException {
        Context mContext = MainActivity.this;
        InputStream IS = mContext.getAssets().open("DicDB.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(IS));
        String line;
        while ((line = reader.readLine()) != null) {
            int separator = line.indexOf('|');
            String eng = line.substring(0, separator);
            String ban = line.substring(separator + 1);
            addWord(eng, ban);
        }
        IS.close();
        secHash();
    }

    private String wordSearch(String word) throws NullPointerException{
        int hash = priHash(word);
        String result = holes[hash].search(word);
        if(result == null) result = "Word Not Found";
        return result;
    }

    private void wordIO() {
        String word = txtWord.getText().toString().trim().toLowerCase();
        String result = wordSearch(word);
        txtResult.setText(result);
    }

    @Override
    public void onClick(View view) {
        if(view == searchButton) {
            wordIO();
        }
    }
}
