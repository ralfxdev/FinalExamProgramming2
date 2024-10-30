package gt.edu.umg.finalexamprogramming2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, PhotoDialogFragment.OnImageDeletedListener {

    private GoogleMap mMap;
    private SQLiteDatabase database;
    private ImageView imagePreview;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onImageDeleted() {
        displayPhotoMarkers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        imagePreview = findViewById(R.id.imagePreview);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        PhotoDatabaseHelper dbHelper = new PhotoDatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        displayCurrentLocation();
        displayPhotoMarkers();
    }

    private void displayCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos si no están concedidos
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
                            drawCurrentLocationCircle(currentLocation); // Llama al método para dibujar el círculo
                        }
                    }
                });
    }

    private void drawCurrentLocationCircle(LatLng currentLocation) {
        mMap.addCircle(new CircleOptions()
                .center(currentLocation)
                .radius(50)
                .strokeColor(0xFF0000FF)
                .fillColor(0x220000FF)
                .strokeWidth(2));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayCurrentLocation();
            }
        }
    }

    private void displayPhotoMarkers() {
        mMap.clear();
        Cursor cursor = database.query("photos", null, null, null, null, null, null);
        int offset = 0; // Desplazamiento inicial
        while (cursor.moveToNext()) {
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            String imageUriString = cursor.getString(cursor.getColumnIndex("image_uri"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String hora = cursor.getString(cursor.getColumnIndex("hora"));
            String id = cursor.getString(cursor.getColumnIndex("id"));
            String ubicacion = latitude + ", " + longitude; // Crear la cadena de ubicación
            Uri imageUri = Uri.parse(imageUriString);

            LatLng location = new LatLng(latitude + offset * 0.00011, longitude + offset * 0.00011);
            Bitmap bitmap = getBitmapFromUri(imageUri);
            if (bitmap != null) {
                Object[] markerData = {bitmap, name, date, hora, ubicacion, id, imageUriString};
                mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
                        .setTag(markerData);
            }

            offset++;
        }
        cursor.close();

        // Habilitar clic en los marcadores
        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() instanceof Object[]) {
                Object[] markerData = (Object[]) marker.getTag();
                Bitmap bitmap = (Bitmap) markerData[0];
                String name = (String) markerData[1];
                String date = (String) markerData[2];
                String hora = (String) markerData[3];
                String ubicacion = (String) markerData[4];
                String id = (String) markerData[5];
                String imageUriString = (String) markerData[6];

                PhotoDialogFragment dialog = PhotoDialogFragment.newInstance(bitmap, name, ubicacion, date, hora, id, imageUriString);
                dialog.show(getSupportFragmentManager(), "photoDialog");
            }
            return false; // Devolver false para que el evento continúe
        });
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
