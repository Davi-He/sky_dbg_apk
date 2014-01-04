
package com.xluo.readwritesysfile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.graphics.Color;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ReadWrite extends Activity {

    public EditText mEditText = null;
    public Button read = null;
    public Button write = null;
    public TextView result = null;

    public EditText enterCommand = null; //  ‰»Î√¸¡Ó ‰»ÎøÚ
    public Button sendCommentBtn = null; // ∑¢ÀÕ√¸¡Ó∞¥≈•
    private TextView showLog = null; // œ‘ ælog result

    private TextView showhelp = null;
    private TextView testview = null;
    private Button testbtn = null;

    public final String file = "/sys/skyworth/sky_debug/gpio_debug";
    private final String strShowHelp = "gpio debug";

    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            String str = (String) msg.obj;
            showLog.append(str + "\n\n\n\n\n");
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mEditText = (EditText) findViewById(R.id.edit);
        read = (Button) findViewById(R.id.read);
        write = (Button) findViewById(R.id.write);
        result = (TextView) findViewById(R.id.display);

        enterCommand = (EditText) findViewById(R.id.edit_command);
        sendCommentBtn = (Button) findViewById(R.id.send_btn);

        showLog = (TextView) findViewById(R.id.show);
        showhelp = (TextView) findViewById(R.id.showhelp);
        testview = (TextView) findViewById(R.id.testview);
        testbtn = (Button) findViewById(R.id.testbtn);

        showhelp.setText(strShowHelp);
        showhelp.setTextColor(Color.RED);

        testbtn.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                // TODO Auto-generated method stub
                testview.setText(strShowHelp);
            }
        });
        
        read.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                File mFile = new File(file);
                FileReader fr = null;
                BufferedReader buffer = null;
                try {
                    fr = new FileReader(mFile);
                    buffer = new BufferedReader(fr);
                    String str = null;
                    while ((str = buffer.readLine()) != null) {
                        result.setText(str);
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    try {
                        buffer.close();
                        fr.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        write.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                File mFile = new File(file);
                OutputStream output = null;
                OutputStreamWriter outputWrite = null;
                PrintWriter print = null;
                try {
                    output = new FileOutputStream(mFile);
                    outputWrite = new OutputStreamWriter(output);
                    print = new PrintWriter(outputWrite);

                    String str = mEditText.getText().toString();
                    if (str != null && !str.equals("")) {
                        print.print(str);
                        print.flush();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        print.close();
                        outputWrite.close();
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sendCommentBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                final String command = enterCommand.getText().toString();
                if ("".equals(command)) {
                    return;
                }

                new Thread(new Runnable() {

                    public void run() {
                        // TODO Auto-generated method stub
                        Socket socket = new Socket();
                        try {
                            Log.d("Bugreport", "Opening client socket - ");
                            socket.bind(null);
                            socket.connect((new InetSocketAddress("127.0.0.1", 6037)),
                                    5000);
                            InputStream inStream = socket.getInputStream();
                            OutputStream stream = socket.getOutputStream();
                            stream.write(command.getBytes());

                            byte[] buff = new byte[1024];

                            while (true && -1 != inStream.read(buff)) {
                                String str = new String(buff);
                                // Log.e("ReadWrite", str);
                                Message msg = handler.obtainMessage();
                                msg.obj = str;
                                handler.sendMessage(msg);
                                Thread.sleep(100);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } /*
                           * finally { try { if (null != socket) {
                           * socket.close(); socket = null; } } catch
                           * (IOException e) { // TODO Auto-generated catch
                           * block e.printStackTrace(); } }
                           */
                    }
                }).start();
            }
        });
    }
}
