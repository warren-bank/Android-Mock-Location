package com.github.warren_bank.mock_location.service.microg_nlp_backend;

import android.content.Intent;

public class ServiceIntentBuilder {
    public static Intent getBackendServiceBindIntent() {
        Intent intent = new Intent();
        intent.setAction("com.github.warren_bank.mock_location.service.microg_nlp_backend.BackendService.BIND");
        intent.setClassName("com.github.warren_bank.mock_location.service.microg_nlp_backend", "com.github.warren_bank.mock_location.service.microg_nlp_backend.BackendService");
        return intent;
    }

    public static Intent getLocationServiceBindIntent() {
        Intent intent = new Intent();
        intent.setAction("com.github.warren_bank.mock_location.service.microg_nlp_backend.LocationService.BIND");
        intent.setClassName("com.github.warren_bank.mock_location.service.microg_nlp_backend", "com.github.warren_bank.mock_location.service.microg_nlp_backend.LocationService");
        return intent;
    }
}
