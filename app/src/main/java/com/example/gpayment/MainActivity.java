package com.example.gpayment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText nametxt, upiIdtxt, msgtxt, amttxt, transIdtxt, refIdtxt;
    Button paybtn;
    final int PAY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nametxt = findViewById(R.id.edtname);
        upiIdtxt = findViewById(R.id.edtupiid);
        msgtxt = findViewById(R.id.edtmsg);
        amttxt = findViewById(R.id.edtamt);
        transIdtxt = findViewById(R.id.edttnid);
        refIdtxt = findViewById(R.id.edtrefid);

        paybtn = findViewById(R.id.btnpay);
        paybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nametxt.getText().toString();
                String upiId = upiIdtxt.getText().toString();
                String amt = amttxt.getText().toString();
                String msg = msgtxt.getText().toString();
                String tnid = String.valueOf(System.currentTimeMillis());
                String refId = refIdtxt.getText().toString();

                if (name.isEmpty() || upiId.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Name and UPI Id are required", Toast.LENGTH_SHORT).show();
                } else {
                    PayUsingUpi(name, upiId, amt, msg, tnid, refId);
                }
            }
        });
    }

    private void PayUsingUpi(String name, String upiId, String amt, String msg, String trnId, String refId) {
        Uri uri = new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", msg)
                .appendQueryParameter("am", amt)
                .appendQueryParameter("tid", trnId)
                .appendQueryParameter("tr", refId)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiIntent = new Intent(Intent.ACTION_VIEW);
        upiIntent.setData(uri);
        Intent chooser = Intent.createChooser(upiIntent, "Pay with UPI");
        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, PAY_REQUEST);
        } else {
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAY_REQUEST) {
            if (isInternetAvailable(MainActivity.this)) {
                if (data == null) {
                    Toast.makeText(this, "Transaction not complete", Toast.LENGTH_SHORT).show();
                } else {
                    String text = data.getStringExtra("response");
                    if (text == null) {
                        text = "discard";
                    }
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(text);
                    upiPaymentCheck(text);
                }
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void upiPaymentCheck(String data) {
        String paymentCancel = "";
        String status = "";
        String[] response = data.split("&");

        for (String res : response) {
            String[] equalStr = res.split("=");
            if (equalStr.length >= 2) {
                if (equalStr[0].equalsIgnoreCase("Status")) {
                    status = equalStr[1].toLowerCase();
                }
            } else {
                paymentCancel = "Payment cancelled";
            }
        }

        if (status.equals("success")) {
            Toast.makeText(this, "Transaction Successful", Toast.LENGTH_SHORT).show();
        } else if (paymentCancel.equals("Payment cancelled")) {
            Toast.makeText(this, "Payment cancelled by user", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Transaction failed: " + status, Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable();
        }
        return false;
    }
}
