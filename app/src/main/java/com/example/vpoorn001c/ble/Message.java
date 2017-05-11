package com.example.vpoorn001c.ble;



import android.content.Context;
import android.widget.Toast;

/**
 * Created by Vamshi .
 */
public class  Message {

    public  static void message(Context context, String message)
    {
        Toast.makeText(context, message,Toast.LENGTH_LONG).show();
    }
}
