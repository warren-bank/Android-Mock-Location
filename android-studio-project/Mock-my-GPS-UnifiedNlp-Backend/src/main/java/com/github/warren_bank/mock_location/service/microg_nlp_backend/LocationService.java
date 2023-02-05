package com.github.warren_bank.mock_location.service.microg_nlp_backend;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class LocationService extends Service {
    private class LocationServiceBinder extends ILocationService.Stub {
        @Override
        public void update(double lat, double lon) throws RemoteException {
            BackendService.update(lat, lon);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocationServiceBinder();
    }
}
