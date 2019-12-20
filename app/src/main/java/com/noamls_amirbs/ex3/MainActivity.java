package com.noamls_amirbs.ex3;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private ListView listView;
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> arrayAdapter;
    private Button insert_btn,search_btn;
    private EditText nameField,phoneField;
    private SQLiteDatabase contactsDB = null;
    public static final String MY_DB_NAME = "contacts.db";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.lstViewID);
        insert_btn = (Button)findViewById(R.id.insert_id);
        search_btn = (Button)findViewById(R.id.search_id);
        nameField = (EditText)findViewById(R.id.name_id);
        phoneField = (EditText)findViewById(R.id.phone_id);

        insert_btn.setOnClickListener(this);
        search_btn.setOnClickListener(this);



        arrayList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);
        createDB();
        showContacts();



    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.insert_id:

                addContact();

                break;
        }
    }
    public void createDB()
    {
        try
        {
            contactsDB = openOrCreateDatabase(MY_DB_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS contacts (id integer primary key, name VARCHAR, phone VARCHAR);";
            contactsDB.execSQL(sql);

        }
        catch (Exception e)
        {
            Log.d("debug", "Error Creating Database");
        }

    }

    public void addContact()
    {
        String contactName = nameField.getText().toString();
        String contactPhone = phoneField.getText().toString();
        if(contactName.matches("") && contactPhone.matches(""))
        {
            Toast.makeText(this, "missing name & phone", Toast.LENGTH_SHORT).show();
            return;
        }

        String sql = "INSERT INTO contacts (name, phone) VALUES ('" + contactName + "', '" + contactPhone + "');";
        contactsDB.execSQL(sql);
        arrayList.add(contactName + "  " + contactPhone);
        arrayAdapter.notifyDataSetChanged();
        Toast.makeText(this, contactName + "one contact added", Toast.LENGTH_SHORT).show();
    }

    public void showContacts()
    {
        // A Cursor provides read and write access to database results
        String sql = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sql, null);

        // Get the index for the column name provided
        int idColumn = cursor.getColumnIndex("id");
        int nameColumn = cursor.getColumnIndex("name");
        int phoneColumn = cursor.getColumnIndex("phone");

        String contactList = "";

        // Move to the first row of results & Verify that we have results
        if (cursor.moveToFirst()) {
            do {
                contactList = "";
                // Get the results and store them in a String
                String id = cursor.getString(idColumn);
                String name = cursor.getString(nameColumn);
                String phone = cursor.getString(phoneColumn);

                contactList = contactList  + name  + phone;

                arrayList.add(contactList);
                arrayAdapter.notifyDataSetChanged();

                // Keep getting results as long as they exist
            } while (cursor.moveToNext());



        } else {

            Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show();
        }

    }

}
