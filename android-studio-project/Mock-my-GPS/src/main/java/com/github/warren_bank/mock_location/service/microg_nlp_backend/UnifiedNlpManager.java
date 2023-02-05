package com.github.warren_bank.mock_location.service.microg_nlp_backend;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class UnifiedNlpManager {
    private ILocationService locationService;

    public UnifiedNlpManager(Context context) {
        connectToService(context);
    }

    public void update(double lat, double lon) {
        if (locationService != null) {
            try {
                locationService.update(lat, lon);
            }
            catch (RemoteException e) {}
        }
    }

    /*
     * return: boolean.
     *         TRUE indicates: system can find service, and client has permission to bind to it.
     */
    private boolean connectToService(Context context) {
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                locationService = ILocationService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                locationService = null;
            }
        };

        boolean canBindService = context.bindService(
            ServiceIntentBuilder.getLocationServiceBindIntent(),
            connection,
            Context.BIND_AUTO_CREATE
        );

        return canBindService;
    }
}
