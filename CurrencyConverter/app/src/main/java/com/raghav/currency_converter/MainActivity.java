package com.raghav.currency_converter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView actvFromCurrency;
    private AutoCompleteTextView actvToCurrency;
    private TextInputEditText etFromAmount;
    private TextInputEditText etToAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFromAmount = findViewById(R.id.etFromAmount);
        etToAmount = findViewById(R.id.etToAmount);
        actvFromCurrency = findViewById(R.id.actvFromCurrency);
        actvToCurrency = findViewById(R.id.actvToCurrency);

        setupCurrencyDropdowns();
        setupConversionListeners();
        setupSwapButton();
    }

    private void setupCurrencyDropdowns() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, CurrencyConverter.CURRENCIES) {

            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = CurrencyConverter.CURRENCIES;
                        results.count = CurrencyConverter.CURRENCIES.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };

        actvFromCurrency.setAdapter(adapter);
        actvToCurrency.setAdapter(adapter);

        actvFromCurrency.setText(CurrencyConverter.CURRENCIES[1], false);
        actvToCurrency.setText(CurrencyConverter.CURRENCIES[0], false);
    }

    private void setupConversionListeners() {
        etFromAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performConversion();
            }
        });

        actvFromCurrency.setOnItemClickListener((parent, view, position, id) -> performConversion());
        actvToCurrency.setOnItemClickListener((parent, view, position, id) -> performConversion());
    }

    private void setupSwapButton() {
        findViewById(R.id.btnSwap).setOnClickListener(v -> {
            String from = actvFromCurrency.getText().toString();
            String to = actvToCurrency.getText().toString();

            actvFromCurrency.setText(to, false);
            actvToCurrency.setText(from, false);

            performConversion();
        });
    }

    private void performConversion() {
        String amountText = etFromAmount.getText() != null ? etFromAmount.getText().toString() : "";

        if (amountText.isEmpty()) {
            etToAmount.setText("");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            etToAmount.setText("");
            return;
        }

        String from = actvFromCurrency.getText().toString();
        String to = actvToCurrency.getText().toString();

        if (from.isEmpty() || to.isEmpty()) return;

        double result = CurrencyConverter.convert(from, to, amount);
        etToAmount.setText(String.format("%.2f", result));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
