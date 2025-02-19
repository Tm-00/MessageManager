package uk.ac.wlv.cw;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import java.io.File;

public class EmailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    Uri selectedImageUri;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        EditText email = findViewById(R.id.EmailAddress);
        EditText subjectline = findViewById(R.id.Subject);
        EditText msg = findViewById(R.id.Message);
        Button btnSendEmail = findViewById(R.id.Sendbtn);
        Button btnLoadImage = findViewById(R.id.btnLoadImage);
        Button backButton = findViewById(R.id.backButton);
        Button btnShareSocialMedia = findViewById(R.id.btnShare);
        imageView = findViewById(R.id.imageView);

        btnLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the image picker
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = subjectline.getText().toString();
                String message = msg.getText().toString();
                String email_id = email.getText().toString();

                // Prepare the email intent
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822"); // Ensures only email apps respond

                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email_id}); // Use a string array for the email
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (selectedImageUri != null){
                    // attach image
                    intent.putExtra(Intent.EXTRA_STREAM, selectedImageUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant permission to access the image
                }

                try {
                    // Start email app activity
                    startActivity(Intent.createChooser(intent, "Send Email"));
                } catch (android.content.ActivityNotFoundException ex) {
                    // Handle the case where no email app is found
                    Toast.makeText(EmailActivity.this, "No email app installed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnShareSocialMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = subjectline.getText().toString();
                String message = msg.getText().toString();

                // Prepare the share intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                // Include the message text
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);

                if (selectedImageUri != null) {
                    // Open the file picker to select an image
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, selectedImageUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                try {
                    // find viable app
                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                } catch (ActivityNotFoundException ex) {
                   // if no viable apps found
                    Toast.makeText(EmailActivity.this, "No app available to share content", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData(); // Get the image URI
            try {
                // Open an input stream to get the image content
                InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}