package com.tunibus.tunibusdriver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;


import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class TrackerService extends Service {
    protected DocumentReference ligneReference=null;
    protected FirebaseFirestore database=FirebaseFirestore.getInstance();
    private static final String TAG = TrackerService.class.getSimpleName();

    private FirebaseAuth mAuth;
    private DocumentReference busRefernce=database.collection("buses").document();
    protected FusedLocationProviderClient client;
    protected LocationCallback mLocationCallBack= new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            Location location = locationResult.getLastLocation();
            if (location != null) {
                GeoPoint geoPoint =new GeoPoint(location.getLatitude(),location.getLongitude());

                Map<String,Object> bus=new HashMap<>();
                bus.put("ligne",ligneReference);
                bus.put("location",geoPoint);
                busRefernce.set(bus, SetOptions.merge());
                Log.d("LocationUpdate","failure");


            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            stopSelf();
        }
        buildNotification();
        final String path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id);



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String id = intent.getStringExtra("id");


        if (id!=null){
            Log.d("onStart",id);
          ligneReference =database.collection("lignes").document(id);
            requestLocationUpdates();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void buildNotification() {
        String stop = "stop";
        String incident ="incident";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        registerReceiver(sendIncident,new IntentFilter(incident));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_tracker);
        startForeground(1, builder.build());
    }
    protected BroadcastReceiver sendIncident =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String incident=intent.getStringExtra("incident");
            if(incident!=null){
                Map<String,Object> bus=new HashMap<>();
                bus.put("incident",incident);
                busRefernce.set(bus,SetOptions.merge());
            }

        }
    };

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            client.removeLocationUpdates(mLocationCallBack);
            busRefernce.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    sendBroadcast(new Intent("stopped"));
                    unregisterReceiver(stopReceiver);
                    stopSelf();

                }
            });

            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped

        }
    };



    private void requestLocationUpdates() {
        // Functionality coming next step
        LocationRequest request = new LocationRequest();

        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
         client = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request,mLocationCallBack, null);
        }
    }


}

