package com.noamls_amirbs.ex3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private ListView listView;
    private Button insert_btn,search_btn;
    private EditText nameField,phoneField;
    private SQLiteDatabase contactsDB = null;
    public static final String MY_DB_NAME = "contacts.db";
    Vector<Boolean> hasPhoneNum = new Vector<>();
    Vector<String> vec = new Vector<>();

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //=============== set the screen button/text view ================//
        listView = findViewById(R.id.lstViewID);
        insert_btn = (Button) findViewById(R.id.insert_id);
        search_btn = (Button) findViewById(R.id.search_id);
        nameField = (EditText) findViewById(R.id.name_id);
        phoneField = (EditText) findViewById(R.id.phone_id);
        insert_btn.setOnClickListener(this);
        search_btn.setOnClickListener(this);
        listView = findViewById(R.id.lstViewID);

        //==========create the data base and display it==========================//
        createDB();
        vec = showContacts();
        displayContact(vec);
        //=========dial the number once the caller mclick the contact==========//
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(getIdContact(position+1).matches(""))
                {
                    Toast.makeText(MainActivity.this, "the contact has no number", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                  dialPhoneNumber(getIdContact(position+1));
            }
        });
        //====================================================================//
    }
    //===== use to dail ==========//
    public void dialPhoneNumber(String phoneNumber)
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
    //===== control the search and the insert button ===//
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.insert_id:// get here when the user click the ADD button to add contact
                addContact();
                break;
            case R.id.search_id:// get here when the user decide to search a specific contact
                findSubString();
                break;

        }
    }

    //======= create instance of list adapter ==========//
    public void displayContact(Vector<String> mTitle)
    {
        MyAdapter adapter = new MyAdapter(this, mTitle);
        listView.setAdapter(adapter);
    }
    class MyAdapter extends ArrayAdapter<String>
    {
        Context context;
        Vector<String> contact_data;

        MyAdapter (Context c, Vector<String> contactData)
        {
            super(c, R.layout.row, R.id.single_contact, contactData);
            this.context = c;
            this.contact_data = contactData;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView images = row.findViewById(R.id.image);
            TextView singleContact = row.findViewById(R.id.single_contact);
            int image = 0;
            //== set the image color ===//
            if(!hasPhoneNum.isEmpty())
            {
                if(hasPhoneNum.get(position))
                    image = R.drawable.ic_call_green;

                else
                    image = R.drawable.ic_call_gray;
            }
            images.setImageResource(image);
            singleContact.setText(contact_data.get(position));
            return row;
        }
    }
    //== create the data file ==//
    public void createDB()
    {
        try
        {
            contactsDB = openOrCreateDatabase(MY_DB_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS contacts (id integer primary key, name VARCHAR, phone VARCHAR);";
            contactsDB.execSQL(sql);
        }
        catch (Exception e) { Log.d("debug", "Error Creating Database"); }
    }

    //===== add contact once the addCotact button was clicked ========//
    public void addContact()
    {
        String contactName = nameField.getText().toString();
        String contactPhone = phoneField.getText().toString();
        if(contactPhone.matches(""))
            hasPhoneNum.addElement(false);
        else
            hasPhoneNum.addElement(true);

        if(contactName.matches("") && contactPhone.matches(""))
        {
            Toast.makeText(this, "missing name & phone", Toast.LENGTH_SHORT).show();
            return;
        }
        if(contactName.matches(""))
        {
            Toast.makeText(this, "missing contact name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(checkAndUpdateDupContact(contactName,contactPhone))
        {
            Log.d("debug","the contact name already exist");
            return;
        }

        String sql = "INSERT INTO contacts (name, phone) VALUES ('" + contactName + "', '" + contactPhone + "');";
        contactsDB.execSQL(sql);
        Toast.makeText(this, contactName + " contact added", Toast.LENGTH_SHORT).show();
        nameField.setText("");
        phoneField.setText("");

        hasPhoneNum.removeAllElements();
        vec = showContacts();
        displayContact(vec);
    }
   //==== we get here to check if there is a duplicate name, and if there is when update the phone number
    public boolean checkAndUpdateDupContact(String newName,String newPhoneNum)
    {
        String sql = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sql, null);
        int nameColumn = cursor.getColumnIndex("name");
        if (cursor.moveToFirst()) {
            do {
                String cur_name = cursor.getString(nameColumn);
                if(newName.matches(cur_name))
                {
                    String strSQL = "UPDATE contacts "
                            + "SET phone = "+"'" + newPhoneNum + "'"
                            + "WHERE name = "+"'" + newName + "'";
                    Log.d("debug","value: "+strSQL);
                    contactsDB.execSQL(strSQL);
                    hasPhoneNum.removeAllElements();
                    vec = showContacts();
                    displayContact(vec);
                    nameField.setText("");
                    phoneField.setText("");

                    return true;
                }
            } while (cursor.moveToNext());
        } else { Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show(); }
        return false;
    }
    //====== use to search a sub contact ============//
    public void findSubString()
    {
        Vector <String> vec = new Vector<>();
        hasPhoneNum.removeAllElements();

        String sql = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sql, null);
        int nameColumn = cursor.getColumnIndex("name");
        int phoneColumn = cursor.getColumnIndex("phone");
        String contactList = "";
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(nameColumn);
                String phone = cursor.getString(phoneColumn);
                contactList =  name  +"\n\n"+ phone ;

                if(contactList.toLowerCase().contains(nameField.getText().toString().toLowerCase())&&contactList.toLowerCase().contains(phoneField.getText().toString().toLowerCase()))
                {
                    vec.addElement(contactList);
                    if(phone.matches(""))
                        hasPhoneNum.addElement(false);
                    else
                        hasPhoneNum.addElement(true);
                }
            } while (cursor.moveToNext());
        } else { Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show(); }
        displayContact(vec);
    }
    //use in check if there is a number in the specific contact //
    public String getIdContact(int curId)
    {
        String sql = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sql, null);

        int idColumn = cursor.getColumnIndex("id");
        int phoneColumn = cursor.getColumnIndex("phone");

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(idColumn);
                String phone = cursor.getString(phoneColumn);

                if(curId == Integer.parseInt(id))
                    return phone;

            } while (cursor.moveToNext());
        } else { Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show(); }
        return "";
    }
    //===== use in display function to dispaly the contact list ============//
    public Vector<String> showContacts()
    {
        Vector <String> vec = new Vector<>();
        String sql = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sql, null);
        int nameColumn = cursor.getColumnIndex("name");
        int phoneColumn = cursor.getColumnIndex("phone");

        String contactList = "";
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(nameColumn);
                String phone = cursor.getString(phoneColumn);

                contactList =  name  +"\n\n"+ phone ;
                if(phone.matches(""))
                    hasPhoneNum.addElement(false);
                else
                    hasPhoneNum.addElement(true);

                vec.addElement(contactList);

            } while (cursor.moveToNext());
        } else { Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show(); }
        return vec;
    }
}