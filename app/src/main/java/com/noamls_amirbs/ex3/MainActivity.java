package com.noamls_amirbs.ex3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    int images[] = {R.mipmap.gray_phone_icon,R.mipmap.green_phone_icon};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        displayContact();


    }
    public void displayContact()
    {
        Vector<String> mTitle = new Vector<>();
        mTitle = showContacts();
        MyAdapter adapter = new MyAdapter(this, mTitle, images);
        listView.setAdapter(adapter);
        Log.d("debug", "here!!!!");
        for (int i = 0; i < mTitle.size(); i++)
        {
            Log.d("debug","valu: "+ mTitle.get(i));
        }
    }
    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        Vector<String> rTitle;
        int rImgs[];

        MyAdapter (Context c, Vector<String> title, int imgs[]) {
            super(c, R.layout.row, R.id.textView1, title);
            this.context = c;
            this.rTitle = title;
            this.rImgs = imgs;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView images = row.findViewById(R.id.image);
            TextView myTitle = row.findViewById(R.id.textView1);


            // now set our resources on views
            images.setImageResource(rImgs[0]);
            myTitle.setText(rTitle.get(position));


            return row;
        }
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
        Toast.makeText(this, contactName + "one contact added", Toast.LENGTH_SHORT).show();

        displayContact();
    }

    public Vector<String> showContacts()
    {
        Vector <String> vec = new Vector<>();
        String sql = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sql, null);

        int idColumn = cursor.getColumnIndex("id");
        int nameColumn = cursor.getColumnIndex("name");
        int phoneColumn = cursor.getColumnIndex("phone");

        String contactList = "";
        if (cursor.moveToFirst()) {
            do {
                contactList = "";
                String id = cursor.getString(idColumn);
                String name = cursor.getString(nameColumn);
                String phone = cursor.getString(phoneColumn);

                contactList = contactList  + name  +"  "+ phone ;

                vec.addElement(contactList);

            } while (cursor.moveToNext());


        } else {

            Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show();
        }
        return vec;
    }

}