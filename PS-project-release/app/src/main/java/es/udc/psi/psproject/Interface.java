package es.udc.psi.psproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Interface extends AppCompatActivity {

    EditText phonenumber,message;
    Button send;
    String TAG = "_TAG";
    private static final int REQUEST_SEND_SMS_PERMISSION = 0;
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;
    private static final int REQUEST_CONTACT = 1;
    String[] permissions= new String[]{
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS};


    // Declaración de los elementos de la interfaz
    Button send_location;
    Button get_contact;
    Button add_favourite;
    TextView show_selected_name, show_selected_number;

    //MediaPlayer
    MediaPlayer mediaPlayer;

    //Location
    int requestCodeLocation = 43;
    FusedLocationProviderClient fusedLocationProviderClient;
    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();


    // RecyclerView
    RecyclerView recyclerView;
    private FavouritesAdapter mAdapter;

    // Método para inicializar los elementos de la interfaz
    protected void init_elements(){
        send_location = findViewById(R.id.but_send_location);
        get_contact = findViewById(R.id.get_contact);
        add_favourite = findViewById(R.id.add_favourite);
        show_selected_name = findViewById(R.id.show_contact_selected);
        show_selected_name.setText(R.string.no_name);
        show_selected_number = findViewById(R.id.show_number_selected);
        show_selected_number.setText(R.string.no_number);
        // RecyclerView
        recyclerView = findViewById(R.id.categories_rv);
        initRecycler();
    }

    private void initRecycler(){
        mAdapter = new FavouritesAdapter(new ArrayList<>());
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setClickListener(new FavouritesAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Favourites fav = mAdapter.getFavourite(position);
                show_selected_name.setText(fav.getName());
                show_selected_number.setText(fav.getNumber());
            }
        });
    }

    private void addFavourite(String name, String number){
        mAdapter.addFavourite(new Favourites(name, number));
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
    }

    // Método para solicitar permisos
    private void requestContactsPermission()
    {
        if (!hasContactsPermission())
        {
            ActivityCompat.requestPermissions(this,permissions, REQUEST_READ_CONTACTS_PERMISSION);
        }
    }

    public void updateButton(boolean enable)
    {
        get_contact.setEnabled(enable);
    }

    private boolean hasContactsPermission()
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION && grantResults.length > 0)
        {
            updateButton(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);

        //Attach sound
        mediaPlayer=MediaPlayer.create(this, R.raw.sound);

        //Initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        init_elements();

        get_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        add_favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = show_selected_name.getText().toString();
                String number = show_selected_number.getText().toString();
                if(!name.isEmpty() && !number.isEmpty()){
                    addFavourite(name, number);
                }else{
                    Log.d("TAG", "empty fields");
                }
            }
        });

        send_location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Location
                getLocation();

                //Start sound
                mediaPlayer.start();

                Log.d(TAG, " Entering onClick");
                //Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            }
        });

        requestContactsPermission();
        updateButton(hasContactsPermission());
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Interface.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCodeLocation);
            String hola="Requesting permissions ...";
            Log.d("_TAG", hola);
        }  else {
            // Get Current Location
            Task<Location> currentLocationTask = fusedLocationProviderClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            );
            currentLocationTask.addOnCompleteListener((new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Task completed successfully
                        Location location = task.getResult();
                        sendLocation(location);
                    } else {
                        // Task failed with an exception
                        Exception exception = task.getException();
                        Log.e("_TAG", "Exception thrown: " + exception);
                    }
                }
            }));
        }
    }

    public void sendLocation(Location location){
        String locationSTR="LOCATION_ERROR";
        String locationURL="Enviado vía RedBell\nhttps://maps.google.com/?q=";
        try {
            locationURL = locationURL+location.getLatitude()+","+location.getLongitude();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
            locationSTR=locationSTR+" long: " + location.getLongitude() + " lat: " + location.getLatitude()
                    + "  location time: " + location.getTime() + " : "
                    + formatter.format(location.getTime());

            //Initialize geoCoder
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            //Initialize address list
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1
            );
            //Set latitude on TextView
            locationSTR=locationSTR+Html.fromHtml(
                    "<font color='#6200EE'><b>Latitude :</b><br></font>"
                            + addresses.get(0).getLatitude()
            );

            //Set longitude
            locationSTR=locationSTR+Html.fromHtml(
                    "<font color='#6200EE'><b>Longitude :</b><br></font>"
                            + addresses.get(0).getLongitude()
            );

            //Set country name
            locationSTR=locationSTR+Html.fromHtml(
                    "<font color='#6200EE'><b>Country Name :</b><br></font>"
                            + addresses.get(0).getCountryName()
            );

            //Set locality
            locationSTR=locationSTR+Html.fromHtml(
                    "<font color='#6200EE'><b>Locality :</b><br></font>"
                            + addresses.get(0).getLocality()
            );

            //Set address
            locationSTR=locationSTR+Html.fromHtml(
                    "<font color='#6200EE'><b>Address :</b><br></font>"
                            + addresses.get(0).getAddressLine(0)
            );

        } catch (IOException e) {
            e.printStackTrace();
        }

        //SEND SMS
        String number= show_selected_number.getText().toString();
        Log.d(TAG, number);
        try {
            Log.d(TAG, " Trying to Send");
            SmsManager smsManager=SmsManager.getDefault();
            smsManager.sendTextMessage(number,null,locationURL,null,null);
            Toast.makeText(getApplicationContext(),"Message Sent",Toast.LENGTH_LONG).show();
            Log.d(TAG, " Sent: "+locationURL);
        }catch (Exception e)
        {
            Log.d(TAG, String.valueOf(e));
            Toast.makeText(getApplicationContext(),"Mistakes were made",Toast.LENGTH_LONG).show();
        }
    }

   @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // Si el resultCode no es el ok, se sale del método
            if (resultCode != Activity.RESULT_OK) return;

            // Si el requestCode es el de la llamada a obtener contactos
            if (requestCode == REQUEST_CONTACT && data != null) {
                Uri contactUri = data.getData();

                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(contactUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {

                    // Se obtiene el id, el nombre y se comprueba si tiene numero de telefono
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    int hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    // Se actualiza el text view del nombre del contacto seleccionado
                    show_selected_name.setText(name);

                    // Si tiene numero de telefono se obtiene
                    String phone;
                    if (hasPhone > 0) {

                        // Se hace una query sobre la tabla de los numeros de telefono con el id del contacto
                        Cursor cp = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (cp != null && cp.moveToFirst()) {
                            phone = cp.getString(cp.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            cp.close();

                            // Se actualiza el text view del numero del contacto seleccionado
                            show_selected_number.setText(phone);
                        }
                    }else {

                        // Si no hay numero se actualiza el text view indicando que no hay numero
                        show_selected_number.setText(R.string.no_number);
                    }

                    // clean up cursor
                    cursor.close();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}