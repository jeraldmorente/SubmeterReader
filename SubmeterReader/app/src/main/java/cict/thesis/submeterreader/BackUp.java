package cict.thesis.submeterreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BackUp extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int READ_REQUEST_CODE = 42;
    Button exp;
    TextView outp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_REQUEST_STORAGE);
            }
        }


        exp = findViewById(R.id.explore);
        outp = findViewById(R.id.output);

        exp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFiles();

            }
        });
    }

    private String readText(String input){
        File file = new File(Environment.getExternalStorageDirectory(),input);
        StringBuilder text = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null){
                text.append(line);
                text.append("\n");
            }
            br.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        return text.toString();
    }


    private void searchFiles(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode==RESULT_OK){
            switch(requestCode){
                case READ_REQUEST_CODE:
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    path = path.substring(path.indexOf(":")+1);
                    if(path.contains("emulated")){
                        path = path.substring(path.indexOf("0")+1);
                    }
                    outp.setText(readText(path));
                    break;
            }
        }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }
}
