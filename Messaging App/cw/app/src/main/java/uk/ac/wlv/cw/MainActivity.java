package uk.ac.wlv.cw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Build;
import android.provider.MediaStore;
import java.io.OutputStream;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;
    FloatingActionButton btnCreateMessage, btnCreateEmail, btnDeleteSelected, btnUpdateSelected, btnShareSelected;
    ImageView ndiv;
    TextView ndtv;
    DBHelper dbHelper;
    ArrayList<String> id, message;
    ArrayList<byte[]> image;
    CustomAdapter ca;
    //test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        rv = findViewById(R.id.rv);
        btnCreateMessage = findViewById(R.id.createMsg);
        btnCreateEmail = findViewById(R.id.goToEmailandSM);
        btnDeleteSelected = findViewById(R.id.deleteSelectedButton);
        btnUpdateSelected = findViewById(R.id.updateSelectedButton);
        btnShareSelected = findViewById(R.id.shareSelectedButton);
        ndiv = findViewById(R.id.ndiv);
        ndtv = findViewById(R.id.ndtv);

        dbHelper = new DBHelper(MainActivity.this);
        id = new ArrayList<>();
        message = new ArrayList<>();
        image = new ArrayList<>();

        // on app startup store all the database from the sql to the app
        storeAllData();

        ca = new CustomAdapter(MainActivity.this, this, id, message, image);

        // sets the adapter to custom adapter
        rv.setAdapter(ca);
        // sets the layout as linear singular list
        rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // When the create message button is pressed, go to that activity
        btnCreateMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                startActivity(intent);
            }
        });

        // When the Email button is pressed, go to that activity
        btnCreateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EmailActivity.class);
                startActivity(intent);
            }
        });

        // When the delete button is pressed, it deletes the messages that are selected
        btnDeleteSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finds the messages that are selected
                ArrayList<String> selectedIds = ca.getSelectedItems();
                // if no message selected tell the user
                if (selectedIds.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No items selected!", Toast.LENGTH_SHORT).show();
                } else {
                    // try to use the method inputting the message ids as a parameter so it knows which ones to delete
                    deletedSelectedMessages(selectedIds);
                }
            }
        });

        // When the update button is pressed, take the user to manage the selected message
        btnUpdateSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // similar to the delete method uses the getSelectedItems method from the custom adapted to identify the selected messages
                ArrayList<String> selectedIds = ca.getSelectedItems();
                // if no message selected tell the user
                if (selectedIds.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No items selected!", Toast.LENGTH_SHORT).show();
                    // app can only handle one message to update at a time so if multiple are selected then inform the user
                } else if (selectedIds.size() > 1) {
                    Toast.makeText(MainActivity.this, "Please select only one item to update!", Toast.LENGTH_SHORT).show();
                    // if there's one and only one message selected
                    // take the user to the update message activity
                    // whilst also getting the information of the message and feeding it to the class
                    // using intent.putExtra/getStringExtra as a getter and setter so that it knows what message is being managed
                } else {
                    int selectedPosition = id.indexOf(selectedIds.get(0));
                    if (selectedPosition != -1) {
                        Intent intent = new Intent(MainActivity.this, UpdateMessageActivity.class);
                        intent.putExtra("ID", id.get(selectedPosition));
                        intent.putExtra("MESSAGE", message.get(selectedPosition));
                        intent.putExtra("IMAGE", image.get(selectedPosition));
                        startActivityForResult(intent, 1);
                    }
                }
            }
        });

        // when the share button is pressed any messages that are selected are chosen to be shared on a social media app of the users choosing
        btnShareSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the id of selected messages
                ArrayList<String> selectedIds = ca.getSelectedItems();
                // if no messages selected tells the user
                if (selectedIds.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No items selected!", Toast.LENGTH_SHORT).show();
                } else {
                    StringBuilder messagesToShare = new StringBuilder();
                    ArrayList<byte[]> imagesToShare = new ArrayList<>();
                    // for each loop that gets every selected id in our array "selectedIDs"
                    for (String selectedId : selectedIds) {
                        int selectedPosition = id.indexOf(selectedId);
                        if (selectedPosition != -1) {
                            // gets the text from the message and then adds it to the string builder using "\n\n" to space out each message as all text will be in a single string.
                            messagesToShare.append(message.get(selectedPosition)).append("\n\n");
                            // adds the images from each message selected to the array
                            imagesToShare.add(image.get(selectedPosition));
                        }
                    }

                    // sets the intent and type
                    Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.setType("*/*");

                    // creates a uri array list
                    ArrayList<Uri> imageUris = new ArrayList<>();
                    // for each image in ImagesToShare convert them to a bitmap then add them to the imageUris array list
                    for (byte[] imageByteArray : imagesToShare) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                        Uri imageUri = saveImageToMediaStore(bitmap);
                        if (imageUri != null) {
                            imageUris.add(imageUri);
                        }
                    }

                    // sets the message as text for the next intent to get similar to before
                    shareIntent.putExtra(Intent.EXTRA_TEXT, messagesToShare.toString());

                    // take image from array add them to next intent
                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);

                    // permissions for URI
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (!imageUris.isEmpty()) {
                        startActivity(Intent.createChooser(shareIntent, null));
                    } else {
                        Toast.makeText(MainActivity.this, "No valid images to share!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Helper method to save the image using ContentResolver
    private Uri saveImageToMediaStore(Bitmap bitmap) {
        ContentResolver resolver = getContentResolver();
        Uri imageUri = null;

        // Define content values to insert into MediaStore
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "SharedImage_" + System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/");

        try {
            // Insert the image into MediaStore and get the Uri for the image
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (imageUri != null) {
                // Write the bitmap data to the Uri using the output stream
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUri;
    }

    // Method for deleting selected messages
    void deletedSelectedMessages(ArrayList<String> selectedIds) {
        // calls the database class to use the delete one row method that is implemented there
        DBHelper dbh = new DBHelper(MainActivity.this);
        // similar to other methods/listeners it uses a foreach loop to find the selected messages
        for (String id : selectedIds) {
            // method deletes one row however for each loop means it deletes each of the rows identified
            dbh.deleteOneRow(id);
            // calls a refresh data to display the updated database on the page without needed to close/restart the app
            refreshData();
        }
    }

    // method for storing all data
    void storeAllData() {
        // using a cursor to read/write the information in the database
        Cursor cursor = dbHelper.getAllData();
        // iterates through the database using a while loop adding the value of each column
        if (cursor != null && cursor.getCount() > 0) {
            // cursor only reads one row at a time so the while loop moves the cursor to the next row until it has read all
            while (cursor.moveToNext()) {
                id.add(cursor.getString(0));
                message.add(cursor.getString(1));
                image.add(cursor.getBlob(2));
            }
            //close the cursor to make it invalid and avoid memory leaks once all data has been handled
            cursor.close();
        }

        // if there is data identified by the "id" variable then it will hide the ui
        // if there is no data will show ui that indicates so
        if (id.isEmpty()) {
            ndiv.setVisibility(View.VISIBLE);
            ndtv.setVisibility(View.VISIBLE);
        } else {
            ndiv.setVisibility(View.GONE);
            ndtv.setVisibility(View.GONE);
        }
    }

    // method to refresh the data by first removing all data stored then calling the storeAllData() method
    void refreshData() {
        id.clear();
        message.clear();
        image.clear();
        storeAllData();
        ca.notifyDataSetChanged();
    }

    // used the main_menu xml file to populate the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // adds functionality to the action bar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // if the search button is selected then go to the search activity
        if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent(getBaseContext(), SearchActivity.class);
            startActivity(intent);
            return true;
        }
        // if delete all is pressed trigger the confirmdialog method.
        if (item.getItemId() == R.id.delete_all) {
            confirmDeleteAll();
        }
        return super.onOptionsItemSelected(item);
    }

    // purpose of this method is to ask the user to confirm if they want to delete all data
    // if user says yes then delete all data
    void confirmDeleteAll() {
        // dialog builder used to generate the pop up that appears when the delete all button is pressed
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all messages?");
        // On click Listener for if the "yes" button is clicked
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            // calls the delete all data method from the DBHelper class, the refreshes the data
            dbHelper.deleteAllData();
            refreshData();
            Toast.makeText(MainActivity.this, "Deleted all messages successfully", Toast.LENGTH_SHORT).show();
        });
        // if the user presses no then do nothing subsequently hides the dialog
        builder.setNegativeButton("No", null);
        builder.create().show();
    }
}
