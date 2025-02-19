package uk.ac.wlv.cw;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import androidx.appcompat.widget.Toolbar;

public class UpdateMessageActivity extends AppCompatActivity {

    EditText message_input;
    ImageView image_input;
    Button update_Button, btnLoadImage, delete_button;

    String id, message;
    Bitmap selectedImageBitmap;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_message);

        message_input = findViewById(R.id.Message2);
        image_input = findViewById(R.id.imageView2);
        update_Button = findViewById(R.id.btnSubmit2);
        btnLoadImage = findViewById(R.id.btnLoadImage2);
        delete_button = findViewById(R.id.btndelete);
        Button backButton = findViewById(R.id.backButton);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getandSetIntentData();

        // loads image
        btnLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        // Listener event to process updating the database with new info the user inputs
        update_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity ma = new MainActivity();
                DBHelper dbh = new DBHelper(UpdateMessageActivity.this);
                message = message_input.getText().toString().trim();

                byte[] imageBytes = null;
                if(selectedImageBitmap != null){
                    imageBytes = convertToBArray(selectedImageBitmap);
                }
                dbh.updateData(id, message, imageBytes);
                ma.refreshData();
            }
        });


        // delete button calls confirm dialog method
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateMessageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    // gets the string values from the the start activity initiated in main method and sets the data in this activity
    void getandSetIntentData(){
        if(getIntent().hasExtra("ID") && getIntent().hasExtra("MESSAGE") && getIntent().hasExtra("IMAGE")){
            // gets the data from intent
            id = getIntent().getStringExtra("ID");
            message = getIntent().getStringExtra("MESSAGE");
            byte[] imageBytes = getIntent().getByteArrayExtra("IMAGE");

            // sets the intent data
            message_input.setText(message);

            if(imageBytes != null && imageBytes.length > 0){
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                image_input.setImageBitmap(bitmap);
            }

        }else{
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        }
    }

    // works esentially the same as the confirm dialog in main activity
    void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete" + message + " ?");
        builder.setMessage("Are you sure you want to delete" + message + " ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                MainActivity ma = new MainActivity();
                DBHelper dbh = new DBHelper(UpdateMessageActivity.this);
                dbh.deleteOneRow(id);
                ma.refreshData();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        builder.create().show();
    }


    // convert bitmap to byte array to store in database
    public byte[] convertToBArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        return outputStream.toByteArray();
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
                image_input.setImageBitmap(selectedImageBitmap);  // Show the selected image
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}