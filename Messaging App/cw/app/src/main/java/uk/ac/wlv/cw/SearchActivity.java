package uk.ac.wlv.cw;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SearchActivity extends AppCompatActivity {


    private TextView mTextView;
    private EditText mEditWordView;
    private DBHelper dbh;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mEditWordView = ((EditText) findViewById(R.id.search_word));
        mTextView = ((TextView) findViewById(R.id.search_result));
        backButton = ((Button) findViewById(R.id.backButton));
        dbh = new DBHelper(this);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }


    public void showResult(View v){
        String word = mEditWordView.getText().toString();
        mTextView.setText("Result for " + word + ":\n\n");

        // search for word in database
        Cursor cursor = dbh.search(word);

        // Only process a non-null cursor with rows.
        if (cursor!= null & cursor.getCount() > 0){
            // You must move the cursor to the first item.
            cursor.moveToFirst();
            int i;
            String result;
            // Iterate over the cursor, while there are entries.
            do {
                // Don't guess at the column index.
                // Get the index for the named column.
                i = cursor.getColumnIndex(DBHelper.COL_2);
                // Get the value from the column for the current cursor.
                result = cursor.getString(i);
                // Add result to what's already in the text view.
                mTextView.append(result + "\n");
            } while (cursor.moveToNext()); //returns true or false
                cursor.close();
        }
    }
}