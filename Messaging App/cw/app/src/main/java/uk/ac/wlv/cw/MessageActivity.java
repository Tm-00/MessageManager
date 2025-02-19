package uk.ac.wlv.cw;
import androidx.appcompat.app.AppCompatActivity;

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



public class MessageActivity extends AppCompatActivity {

    DBHelper dbHelper;
    EditText etMessage;
    Button btnSubmit, btnLoadImage;
    ImageView imageView;
    Bitmap selectedImageBitmap;
    private static final int PICK_IMAGE = 1;

    // convert bitmap to byte array to store in database
    public byte[] convertToBArray(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DBHelper(this);

        etMessage = findViewById(R.id.Message);
        btnSubmit = findViewById(R.id.btnSubmit);
        imageView = findViewById(R.id.imageView);
        btnLoadImage  = findViewById(R.id.btnLoadImage);
        Button backButton = findViewById(R.id.backButton);

        // load img
        btnLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the file picker to select an image
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        // submit to save msg
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = etMessage.getText().toString();

                if (message.isEmpty()){
                    Toast.makeText(MessageActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check if an image is selected
                if(selectedImageBitmap != null){
                    //convert the bitmap to byte[]
                    byte[] imageBytes = convertToBArray(selectedImageBitmap);

                    // add data to database
                    boolean result = dbHelper.insertData(message, imageBytes);

                    if (result){
                        Toast.makeText(MessageActivity.this, "Data inserted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MessageActivity.this, "Data insert failed", Toast.LENGTH_SHORT).show();
                    }
                    // takes the user to the main menu
                    Intent intent = new Intent(MessageActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                // Open an input stream to get the image content
                InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}