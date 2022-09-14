package cict.thesis.submeterreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainMenu extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int WRITE_EXTERNAL_STORAGE = 1;
    private static final int RESULT_PICK_CONTACT = 1 ;
    Button sendBtn, selectNumber, calculate;
    EditText txtphoneNo;
    EditText present_read, previous_read, rate_times;
    TextView billingTimes, billPayment, usage;
    String phoneNo, billType, previousD, presentD, dueD, mText;
    String message, present, previous, timesRate, totalBill;
    Spinner spinner;
    private DatePickerDialog datePickerDialog, previousDate, presentDate;
    private Button dateButton, previousRdg, presentRdg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        initDatePicker();
        initDatePickerPrev();
        initDatePickerPres();
        dateButton = findViewById(R.id.datePickerButton);
        previousRdg = findViewById(R.id.previous_date);
        usage = findViewById(R.id.cosumption);
        presentRdg = findViewById(R.id.present_date);
        present_read = findViewById(R.id.presentrdg_field);
        previous_read = findViewById(R.id.previousrdg_field);

        present_read.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8,1)});
        previous_read.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8,1)});
        rate_times = findViewById(R.id.billing_times);
        calculate = findViewById(R.id.calculate);
        billPayment = findViewById(R.id.billpayment);
        dateButton.setText(setDueDate());
        presentRdg.setText(getTodaysDate());
        previousRdg.setText(getPreviousMonth());
        sendBtn = findViewById(R.id.send_button);
        txtphoneNo = findViewById(R.id.contact_field);
        selectNumber = findViewById(R.id.pickcontact);
        spinner = findViewById(R.id.bill_type);
        billingTimes =  findViewById(R.id.bill_times);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                computeBill();
                sendSMSMessage();
        }
        });
        calculate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                computeBill();
            }
        });

        selectNumber.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, RESULT_PICK_CONTACT); } });

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Water Bill");
        arrayList.add("Electricity Bill");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = parent.getItemAtPosition(position).toString();

                        if(selected.equals("Water Bill")){
                            billingTimes.setText("Rate per m\u00b3");
                        }
                        else if(selected.equals("Electricity Bill")){
                            billingTimes.setText("Rate per kWh");
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

    }

    private void computeBill() {
        billType =  spinner.getSelectedItem().toString();
        String previousDF = previous_read.getText().toString();
        String presentDF = present_read.getText().toString();
        String rateF = rate_times.getText().toString();
        if(TextUtils.isEmpty(previousDF)){
            previous_read.setError("This field cannot be empty!");
        }
        else if(TextUtils.isEmpty(presentDF)){
            present_read.setError("This field cannot be empty!");
        }
        else if(TextUtils.isEmpty(rateF)){
            rate_times.setError("This field cannot be empty!");
        }
        else{
            DecimalFormat df = new DecimalFormat("0.00");
            double present = Double.valueOf(String.valueOf(present_read.getText()));
            double previous = Double.valueOf(String.valueOf(previous_read.getText()));
            if (present < previous) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
                builder.setMessage("Previous read cannot be larger than Present read")
                        .setTitle("Note:")
                        .setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            else{
                int timesRate = Integer.parseInt(String.valueOf(rate_times.getText()));
                double difference = present - previous;
                String finaldiff = df.format(difference);
                double totalBill = difference * timesRate;
                String finalbill = df.format(totalBill);
                String type = "";
                if(billType.equals("Water Bill")){
                    type = " m\u00b3";
                }
                else if(billType.equals("Electricity Bill")){
                    type = " kWh";
                }
                billPayment.setText("\u20B1 "+finalbill);
                usage.setText(""+finaldiff+type);
            }

        }
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private String getPreviousMonth() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month - 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private String setDueDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        day = day + 5;
        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month = month + 1;
                String date = makeDateString(day, month, year);
                dateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);

    }

    private void initDatePickerPrev() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month = month + 1;
                String date = makeDateString(day, month, year);
                previousRdg.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;
        previousDate = new DatePickerDialog(this, style, dateSetListener, year, month, day);

    }

    private void initDatePickerPres() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month = month + 1;
                String date = makeDateString(day, month, year);
                presentRdg.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int style = AlertDialog.THEME_HOLO_LIGHT;
        presentDate = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + ", " + year;
    }

    private String getMonthFormat(int month) {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "DEC";

        return "JAN";
    }

    public void openDatePicker(View view)
    {
        datePickerDialog.show();
    }

    public void setPreviousDate(View view)
    {
        previousDate.show();
    }

    public void setPresentDate(View view)
    {
        presentDate.show();
    }


    protected void sendSMSMessage() {
        DecimalFormat df = new DecimalFormat("0.00");
        DecimalFormat df2 = new DecimalFormat("0.0");
        phoneNo = txtphoneNo.getText().toString();
        previousD = previousRdg.getText().toString();
        presentD = presentRdg.getText().toString();
        dueD = dateButton.getText().toString();
        billType =  spinner.getSelectedItem().toString();

        present = present_read.getText().toString();
        previous = previous_read.getText().toString();
        timesRate = rate_times.getText().toString();
        totalBill = billPayment.getText().toString();

        if(TextUtils.isEmpty(previous)){
            previous_read.setError("This field cannot be empty!");
        }
        else if(TextUtils.isEmpty(present)){
            present_read.setError("This field cannot be empty!");
        }
        else if(TextUtils.isEmpty(timesRate)){
            rate_times.setError("This field cannot be empty!");
        }
        else if(TextUtils.isEmpty(phoneNo)){
            txtphoneNo.setError("Select or input from Contact!");
        }

        else{
            double presentN = Double.valueOf(String.valueOf(present_read.getText()));
            double previousN =Double.valueOf(String.valueOf(previous_read.getText()));
            int timesRateN = Integer.parseInt(String.valueOf(rate_times.getText()));
            double difference = presentN - previousN;
            String differenceN = df2.format(difference);
            double totalBillN = difference * timesRateN;
            String finalbill = df.format(totalBillN);

            String type = "";
            if(billType.equals("Water Bill")){
                type = " cubic";

            }
            else if(billType.equals("Electricity Bill")){
                type = " kWh";
            }

            message = ""+ billType + " Notice!\n\n" +
                    "From " + previousD + " to "+ presentD +"\n"+
                    "Prev Rdg: "+previous+"\n" +
                    "Pres Rdg: "+present+"\n" +
                    "Usage: " + differenceN+ ""+ type + "\n" +
                    "Per"+type+": "+timesRate+"\n\n" +
                    "Amount: P"+finalbill+"\n" +
                    "Due: "+dueD;

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
            else {
                SendTextMsg();
            }
        }
    }

    private void saveTextFile(String mText) {
        phoneNo = txtphoneNo.getText().toString();
        presentD = presentRdg.getText().toString();
        billType =  spinner.getSelectedItem().toString();

        String filenam = billType + " - "+ presentD;
        try{
            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path+"/Download/Bill_Records/"+phoneNo+"/");
            dir.mkdirs();
            String fileName = filenam +".txt";
            File file = new File(dir, fileName);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(mText);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filen() {
        DecimalFormat df = new DecimalFormat("0.00");
        DecimalFormat df2 = new DecimalFormat("0.0");
        phoneNo = txtphoneNo.getText().toString();
        previousD = previousRdg.getText().toString();
        presentD = presentRdg.getText().toString();
        dueD = dateButton.getText().toString();
        billType =  spinner.getSelectedItem().toString();

        present = present_read.getText().toString();
        previous = previous_read.getText().toString();

        timesRate = rate_times.getText().toString();
        totalBill = billPayment.getText().toString();

        if(TextUtils.isEmpty(previous)){
            previous_read.setError("This field cannot be empty!");
        }
        else if(TextUtils.isEmpty(present)){
            present_read.setError("This field cannot be empty!");
        }
        else if(TextUtils.isEmpty(timesRate)){
            rate_times.setError("This field cannot be empty!");
        }
        else if(TextUtils.isEmpty(phoneNo)){
            txtphoneNo.setError("Select or input from Contact!");
        }

        else{
            double presentN = Double.valueOf(String.valueOf(present_read.getText()));
            double previousN =Double.valueOf(String.valueOf(previous_read.getText()));
            int timesRateN = Integer.parseInt(String.valueOf(rate_times.getText()));
            double difference = presentN - previousN;
            String differenceN = df2.format(difference);
            double totalBillN = difference * timesRateN;
            String finalbill = df.format(totalBillN);

            String type = "";
            if(billType.equals("Water Bill")){
                type = " cubic";

            }
            else if(billType.equals("Electricity Bill")){
                type = " kWh";
            }

            mText = ""+ billType + " Record!\n\n" +
                    "From " + previousD + " to "+ presentD +"\n"+
                    "Prev Rdg: "+previous+"\n" +
                    "Pres Rdg: "+present+"\n" +
                    "Usage: " + differenceN+ ""+ type + "\n" +
                    "Per"+type+": "+timesRate+"\n\n" +
                    "Amount: P"+finalbill;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ==PackageManager.PERMISSION_GRANTED){
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, WRITE_EXTERNAL_STORAGE);
                }
                else {
                    saveTextFile(mText);
                }
            }
            else{
                saveTextFile(mText);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SendTextMsg();
                } else { Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();

                }

            }
            case WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveTextFile(mText);
                } else { Toast.makeText(getApplicationContext(), "Failed saving file.", Toast.LENGTH_LONG).show();

                }

            }


        }
    }

    private void SendTextMsg(){
        billType =  spinner.getSelectedItem().toString();
        phoneNo = txtphoneNo.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
        builder.setMessage("Allow this application to send "+ billType+ " notice to "+phoneNo+"?");
        builder.setTitle("Alert:");
        builder.setCancelable(false);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, message, null, null);
                Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                filen();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
        builder.setMessage("Close this application?");
        builder.setTitle("Alert!");
        builder.setCancelable(false);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainMenu.this.finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode==RESULT_OK){
            switch(requestCode){
                case RESULT_PICK_CONTACT:
                    contactPicked (data);
                    break;
            }
        }
        else{
            Toast.makeText(this,"Failed to select Contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void contactPicked(Intent data){
        Cursor cursor = null;
        try {
            String phoneNo = null;
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null,null,null, null);
            cursor.moveToFirst();
            int  phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            phoneNo = cursor.getString(phoneIndex);
            txtphoneNo.setText(phoneNo);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemId = item.getItemId();
        switch(itemId) {
            case R.id.abt:
                if(item.isChecked()) {
                    item.setChecked(false);
                }else {
                    item.setChecked(true);
                }
                Intent a = new Intent(getApplicationContext(),About.class);
                startActivity(a);
                return true;

            case R.id.bckup:
                if(item.isChecked()) {
                    item.setChecked(false);
                }else {
                    item.setChecked(true);
                }
                Intent b = new Intent(getApplicationContext(),BackUp.class);
                startActivity(b);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class DecimalDigitsInputFilter implements InputFilter {
        private Pattern mPattern;
        DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
    return null;
        }
    }
}