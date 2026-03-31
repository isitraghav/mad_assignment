package com.raghav.currency_converter;

import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {

    public static final String[] CURRENCIES = {"INR", "USD", "JPY", "EUR"};

    private static final Map<String, Map<String, Double>> RATES = new HashMap<>();

    static {
        double[] usdRates = {92.72, 1.00, 149.50, 0.92};

        for (int i = 0; i < CURRENCIES.length; i++) {
            Map<String, Double> ratesFromThisCurrency = new HashMap<>();

            for (int j = 0; j < CURRENCIES.length; j++) {
                double rate = usdRates[j] / usdRates[i];
                ratesFromThisCurrency.put(CURRENCIES[j], rate);
            }

            RATES.put(CURRENCIES[i], ratesFromThisCurrency);
        }
    }

    public static double convert(String from, String to, double amount) {
        if (from.equals(to)) return amount;

        double rate = RATES.get(from).get(to);
        return amount * rate;
    }
}
