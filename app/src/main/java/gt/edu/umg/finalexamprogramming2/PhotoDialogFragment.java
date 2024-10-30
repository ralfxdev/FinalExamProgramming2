package gt.edu.umg.finalexamprogramming2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class PhotoDialogFragment extends DialogFragment {

    private static final String ARG_BITMAP = "bitmap";
    private static final String ARG_NAME = "name";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_DATE = "date";
    private static final String ARG_TIME = "time";

    private static final String ARG_ID = "id";

    private static final String ARG_URI_IMG = "uri_img";

    private OnImageDeletedListener callback;

    public interface OnImageDeletedListener {
        void onImageDeleted();
    }

    public static PhotoDialogFragment newInstance(Bitmap bitmap, String name, String location, String date, String time, String id, String uri_img) {
        PhotoDialogFragment fragment = new PhotoDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BITMAP, bitmap);
        args.putString(ARG_NAME, name);
        args.putString(ARG_LOCATION, location);
        args.putString(ARG_DATE, date);
        args.putString(ARG_TIME, time);
        args.putString(ARG_ID, id);
        args.putString(ARG_URI_IMG, uri_img);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnImageDeletedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnImageDeletedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_dialog, container, false);
        ImageView imageView = view.findViewById(R.id.fullImageView);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvLocation = view.findViewById(R.id.tvLocation);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvTime = view.findViewById(R.id.tvTime);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        if (getArguments() != null) {
            Bitmap bitmap = getArguments().getParcelable(ARG_BITMAP);
            String name = getArguments().getString(ARG_NAME);
            String location = getArguments().getString(ARG_LOCATION);
            String date = getArguments().getString(ARG_DATE);
            String time = getArguments().getString(ARG_TIME);
            final String id = getArguments().getString(ARG_ID);
            String uri_img = getArguments().getString(ARG_URI_IMG);

            imageView.setImageBitmap(bitmap);
            tvName.setText(name);
            tvLocation.setText(location);
            tvDate.setText(date);
            tvTime.setText(time);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteImage(id, uri_img);
                }
            });
        }

        return view;
    }

    private void deleteImage(String id, String imageUri) {
        PhotoDatabaseHelper dbHelper = new PhotoDatabaseHelper(getContext());

        Uri uri = Uri.parse(imageUri);
        int rowsDeleted = getContext().getContentResolver().delete(uri, null, null);

        // Eliminar la entrada de la base de datos
        boolean isDeleted = dbHelper.deletePhotoById(id);

        if (rowsDeleted > 0 && isDeleted) {
            // Informar a la actividad que la imagen fue eliminada
            Toast.makeText(getContext(), "Imagen eliminada", Toast.LENGTH_SHORT).show();
            callback.onImageDeleted();
            dismiss();
        } else {
            Toast.makeText(getContext(), "Error al eliminar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

}

