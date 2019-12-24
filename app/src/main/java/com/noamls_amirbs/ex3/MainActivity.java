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
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> arrayAdapter;
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

        listView = findViewById(R.id.lstViewID);
        insert_btn = (Button) findViewById(R.id.insert_id);
        search_btn = (Button) findViewById(R.id.search_id);
        nameField = (EditText) findViewById(R.id.name_id);
        phoneField = (EditText) findViewById(R.id.phone_id);

        insert_btn.setOnClickListener(this);
        search_btn.setOnClickListener(this);
        listView = findViewById(R.id.lstViewID);

        createDB();

        vec = showContacts();
        displayContact(vec);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                dialPhoneNumber(getIdContact(position+1));
            }
        });
    }
    public void dialPhoneNumber(String phoneNumber)
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.insert_id:
                addContact();
                break;
            case R.id.search_id:
                findSubString();
                break;

        }
    }


    public void displayContact(Vector<String> mTitle)
    {
        MyAdapter adapter = new MyAdapter(this, mTitle);
        listView.setAdapter(adapter);
    }
    class MyAdapter extends ArrayAdapter<String>
    {
        Context context;
        Vector<String> rTitle;


        MyAdapter (Context c, Vector<String> title)
        {
            super(c, R.layout.row, R.id.textView1, title);
            this.context = c;
            this.rTitle = title;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView images = row.findViewById(R.id.image);
            TextView myTitle = row.findViewById(R.id.textView1);
            int image = 0;
            if(!hasPhoneNum.isEmpty())
            {
                if(hasPhoneNum.get(position))
                    image = R.mipmap.green_phone_icon;

                else
                    image = R.mipmap.gray_phone_icon;
            }
            images.setImageResource(image);
            myTitle.setText(rTitle.get(position));
            return row;
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
        catch (Exception e) { Log.d("debug", "Error Creating Database"); }
    }


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
        if(checkAndUpdateDupContact(contactName,contactPhone))
        {
            Log.d("debug","the contact name already exist");
            return;
        }


        String sql = "INSERT INTO contacts (name, phone) VALUES ('" + contactName + "', '" + contactPhone + "');";
        contactsDB.execSQL(sql);
        Toast.makeText(this, contactName + "one contact added", Toast.LENGTH_SHORT).show();
        nameField.setText("");
        phoneField.setText("");

        hasPhoneNum.removeAllElements();
        vec = showContacts();
        displayContact(vec);
    }

    public boolean checkAndUpdateDupContact(String newName,String newPhoneNum)
    {
        String sql = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sql, null);
        int nameColumn = cursor.getColumnIndex("name");
        String str = "ls";
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

                    return true;
                }

            } while (cursor.moveToNext());


        } else {

            Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void findSubString()
    {
        Vector <String> vec = new Vector<>();
        Vector <Boolean> phoneNameBool = new Vector<>();

        String sql = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sql, null);
        int nameColumn = cursor.getColumnIndex("name");
        int phoneColumn = cursor.getColumnIndex("phone");
        String contactList = "";
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(nameColumn);
                String phone = cursor.getString(phoneColumn);
                contactList =  name  +"  "+ phone ;

                if(contactList.toLowerCase().contains(nameField.getText().toString().toLowerCase())&&contactList.toLowerCase().contains(phoneField.getText().toString().toLowerCase()))
                    vec.addElement(contactList);

            } while (cursor.moveToNext());
        } else { Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show(); }
        hasPhoneNum.removeAllElements();
        showContacts();
        displayContact(vec);
    }
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


        } else {

            Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show();
        }
        return "";

    }

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

                contactList =  name  +"  "+ phone ;
                if(phone.matches(""))
                    hasPhoneNum.addElement(false);
                else
                    hasPhoneNum.addElement(true);

                vec.addElement(contactList);

            } while (cursor.moveToNext());


        } else {

            Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show();
        }
        return vec;
    }

}