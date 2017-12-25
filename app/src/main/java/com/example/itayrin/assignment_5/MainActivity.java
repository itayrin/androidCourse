package com.example.itayrin.assignment_5;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.itayrin.assignment_5.DownloadFileService.DOWNLOAD_FILE_ACTION;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "MainActivity";
    public static final String CURRENT = "CURRENT";
    /**
     * the adapter that will connect our SQL DB to the listview
     */
    private SQLAdapter myAdapter;

    /**
     * The model
     * */
    private DBOpenHelper db;

    /**
     * The receiver
     */
    private BroadcastReceiver receiver;
    private ProgressBar progressBar;

    /**
     * The Service
     */

    @Override
    protected void onStart() {
        super.onStart();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int progress = intent.getIntExtra(CURRENT,0);
                progressBar.setProgress(progress*10);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_FILE_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View
        ListView listView = (ListView) findViewById(R.id.studentList);
        Button add = (Button) findViewById(R.id.addButton);
        final EditText inputName = (EditText) findViewById(R.id.inputName);
        final EditText inputAge = (EditText) findViewById(R.id.inputAge);
        final EditText inputGrade = (EditText) findViewById(R.id.inputGrade);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(90);
        final Button startService = (Button) findViewById(R.id.startService);

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(),DownloadFileService.class);
                startService(in);
            }
        });

        //Model SQL Local DataBase
        db = new DBOpenHelper(getApplicationContext());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Here we handle a list view item click

                final long _id = id;
                makePopup(_id,db.getRowById(id));
            }
        });

        /**
         * Here is where we add new item to the SQL database
         */
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strName = inputName.getText().toString();
                if (strName.isEmpty() || inputAge.getText().toString().matches("") || inputGrade.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), "Please fill all of the fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                int intAge = Integer.parseInt(inputAge.getText().toString());
                int intGrade = Integer.parseInt(inputGrade.getText().toString());
                if(intGrade < 0 || intGrade > 100){
                    Toast.makeText(getApplicationContext(), "Please enter a grade in the range [0,100]", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(intAge < 0 || intAge > 150){
                    Toast.makeText(getApplicationContext(), "Please enter an age in the range [0,150]", Toast.LENGTH_SHORT).show();
                    return;
                }
                String url = "https://botlist.co/system/BotList/Bot/logos/000/002/895/medium/submission1570rao3Hr.JPG";

                //Create an object
                StudentObject obj = new StudentObject(strName,intGrade,intAge,url);

                //Clear the 'EditText' fields
                inputName.setText("");
                inputAge.setText("");
                inputGrade.setText("");

                //add the object to the SQLModel
                db.insertLine(obj);

                //Tell the adapter to refresh the view
                myAdapter.changeCursor(db.getAllRows());
            }
        });

        //Create the adapter
        myAdapter = new SQLAdapter(getApplicationContext(), db.getAllRows(), false);

        //connect the adapter to the ListView
        listView.setAdapter(myAdapter);
    }

    /*This method creates and shows a Change-Grade-Popup
    * @params: _id - the id that the adapter works with
    *          a cursor
    * */
    void makePopup(final long _id,Cursor cursor){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View myDialogView = inflater.inflate(R.layout.change_grade_dialog, null);

        //present the student's name
        final TextView studentName = (TextView) myDialogView.findViewById(R.id.studentName);
        String nameString = cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.KEY_NAME));
        studentName.setText(nameString);

        //Build the dialog
        final AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Change Grade");
        dialog.setView(myDialogView);
        dialog.setPositiveButton("Change Grade", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText newGrade = (EditText) myDialogView.findViewById(R.id.dialog_editText);

                //If no grade entered
                if(newGrade.getText().toString().matches("")){
                    //getBack(or stay) to dialog and enter a number
                    Toast.makeText(getApplicationContext(), "Please Enter a grade or cancel", Toast.LENGTH_SHORT).show();
                    return;
                }

                int newGradeNum = Integer.parseInt(newGrade.getText().toString());
                //Update the database with the new grade
                ContentValues cv = new ContentValues();
                cv.put(DBOpenHelper.KEY_GRADE,newGradeNum);
                db.getWritableDatabase().update(DBOpenHelper.TABLE_NAME,cv,DBOpenHelper.KEY_ID+"="+_id,null);
                myAdapter.changeCursor(db.getAllRows());//Refresh List view
                dialog.dismiss();

            }
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.create();
        dialog.show();
    }

        /**
     * Cursor adapter
     */
    private class SQLAdapter extends CursorAdapter {

        //Helper to create view
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());

        public SQLAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        /**
         * Here we create the view that each line will be look like
         *
         * @param context
         * @param cursor
         * @param parent
         * @return the view for each line
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.student_line, parent, false);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            TextView name = (TextView) view.findViewById(R.id.name);
            TextView age = (TextView) view.findViewById(R.id.age);
            TextView grade = (TextView) view.findViewById(R.id.grade);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);

            /**
             * Here we pull the info from single object stored in the DB
             */
            String nameString = cursor.getString(cursor.getColumnIndexOrThrow(DBOpenHelper.KEY_NAME));
            int anAge = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_AGE));
            int aGrade = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.KEY_GRADE));
            final String imageUrl = cursor.getString(cursor.getColumnIndex(DBOpenHelper.KEY_IMAGE));

            /**
             * Attach the model to the view
             */
            name.setText(nameString);
            age.setText("Age: " + Integer.toString(anAge));
            grade.setText("Grade: " + Integer.toString(aGrade));

            LinearLayout lin = (LinearLayout) view.findViewById(R.id.studentLine);
            if(aGrade < 60){
                lin.setBackgroundColor(0xFFFF0000);//red
            }
            else if(aGrade >=60 && aGrade < 80){
                lin.setBackgroundColor(0xFFffff00);//yellow
            }
            else if(aGrade >= 80 && aGrade < 100){
                lin.setBackgroundColor(0xFF0000ff);//blue
            }
            else{
                lin.setBackgroundColor(0xFF00ff00);//green
            }

            getImageFromNetwork(imageView, imageUrl);
        }
    }

    private void getImageFromNetwork(final ImageView imageView, final String imageUrl) {
        new Thread("ImageThread") {
            @Override
            public void run() {
                try {
                    URL url = new URL(imageUrl);
                    final Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(image);
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }.start();
    }
}

