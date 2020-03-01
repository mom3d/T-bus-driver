package com.tunibus.tunibusdriver;
import android.Manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tunibus.api.LignesHoraires;
import com.tunibus.api.MyAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TrackerActivity extends AppCompatActivity{
    FirebaseFirestore database=FirebaseFirestore.getInstance();

    protected FirebaseAuth mAuth;
    protected ToggleButton button;
    protected Spinner spinner;
    protected Button submit;
    protected EditText editText;
    LignesHoraires selectedLigneHoraie;
    protected BroadcastReceiver serviceStopped=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            button.setChecked(false);
           spinner.setEnabled(true);
           editText.setEnabled(true);
           submit.setText("envoyer");
           submit.setEnabled(false);



        }
    };


    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        final List<LignesHoraires> lignesHoraires = new ArrayList<LignesHoraires>();
        spinner=findViewById(R.id.spinner);
        final MyAdapter adapter=new MyAdapter(getApplicationContext(),lignesHoraires);
        button=findViewById(R.id.toggleButton2);
        spinner.setAdapter(adapter);
        submit=findViewById(R.id.button);
        editText=findViewById(R.id.editText);
        button.setEnabled(false);
        submit.setEnabled(false);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.isEnabled()){
                    String incident=editText.getText().toString();

                    sendBroadcast(new Intent("incident").putExtra("incident",incident));
                    editText.setEnabled(false);
                    submit.setText("reset");
                }else{
                    sendBroadcast(new Intent("incident").putExtra("incident",""));
                    editText.setEnabled(true);
                    submit.setText("envoyer");
                }

            }
        });

        button.setEnabled(false);
        database.collection("lignes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot doc : task.getResult()){
                            lignesHoraires.add(new LignesHoraires(doc.get("nom").toString(),doc.get("arrets.0.heure_depart").toString(),doc.getId()));
                            adapter.notifyDataSetChanged();
                            Log.d("lignes",doc.getId()+"=>"+doc.get("nom"));
                        }

                    }
                });






       spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
              selectedLigneHoraie=(LignesHoraires) adapterView.getItemAtPosition(i);
              button.setEnabled(true);
              button.setChecked(false);


           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {

           }
       });




        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            startActivity(new Intent(this,LoginActivity.class));

        }

        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();

        }

        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if(isChecked){
                    Log.d("toggle","checked");
                    TrackerActivity.this.spinner.setEnabled(false);
                    startService(new Intent(TrackerActivity.this,
                            TrackerService.class)
                            .putExtra("id",selectedLigneHoraie.getId())

                    );
                    submit.setEnabled(true);


                }else{
                    Log.d("toggle"," not checked");

                    sendBroadcast(new Intent("stop"));
                    submit.setEnabled(false);

                }
            }
        });

        registerReceiver(serviceStopped,new IntentFilter("stopped"));


        checkLocationPermission();
    }

    private void checkLocationPermission() {
        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
          //  startTrackerService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    private void startTrackerService() {
        startService(new Intent(this, TrackerService.class));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            //startTrackerService();
        } else {
            Toast.makeText(this,"permission est nécessaire pour activer le service ",
                    Toast.LENGTH_LONG).show();
            finish();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if(item.getItemId()==R.id.logout){
           mAuth.signOut();

           startActivity(new Intent(TrackerActivity.this,LoginActivity.class));

       }
       return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }
}