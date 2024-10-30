package gt.edu.umg.finalexamprogramming2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.os.Environment;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private ImageView imageView;
    private Button btnTakePhoto;
    private TextView tvName, tvLocation, tvTime, tvDate;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        tvName = findViewById(R.id.tvName);
        tvLocation = findViewById(R.id.tvLocation);
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar la base de datos
        PhotoDatabaseHelper dbHelper = new PhotoDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        // Obtener permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            getCurrentLocation();
        }

        btnTakePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    updateLocationTextView();
                }
            });
        }
    }

    private void updateLocationTextView() {
        if (currentLocation != null) {
            String locationText = currentLocation.getLatitude() + ", " + currentLocation.getLongitude();
            tvLocation.setText(locationText);
        } else {
            tvLocation.setText("Ubicación: Desconocida");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(imageBitmap);
            saveImageWithLocation(imageBitmap);

            // Actualizar la hora actual en el TextView correspondiente
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            tvTime.setText("Hora: " + currentTime);
        }
    }

    private void saveImageWithLocation(Bitmap bitmap) {
        Uri imageUri = saveImageToGallery(bitmap);
        String imageName = "IMG_" + System.currentTimeMillis() + ".jpg";
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String finalDate = dateFormat.format(currentDate);
        String hora = java.text.DateFormat.getTimeInstance().format(new java.util.Date());

        if (currentLocation != null && imageUri != null) {
            ContentValues values = new ContentValues();
            values.put("image_uri", imageUri.toString());
            values.put("latitude", currentLocation.getLatitude());
            values.put("longitude", currentLocation.getLongitude());
            values.put("name", imageName);
            values.put("date", finalDate);
            values.put("hora", hora);
            database.insert("photos", null, values);

            // Mostrar la información en los TextViews
            TextView tvName = findViewById(R.id.tvName);
            TextView tvLocation = findViewById(R.id.tvLocation);
            TextView tvTime = findViewById(R.id.tvTime);

            tvName.setText(imageName);
            tvLocation.setText(currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
            tvDate.setText(finalDate);
            tvTime.setText("Hora: " + hora);

            Toast.makeText(this, "Foto guardada con ubicación", Toast.LENGTH_SHORT).show();
        }
    }


    private Uri saveImageToGallery(Bitmap bitmap) {
        Uri imageUri = null;
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyAppImages");

            // Insertar los datos en MediaStore para crear el archivo
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUri;
    }
}
