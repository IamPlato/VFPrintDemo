package com.verifone.smartpos.printdemo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vfi.smartpos.deviceservice.aidl.IPrinter;
import com.vfi.smartpos.deviceservice.aidl.PrinterListener;
import com.vfi.smartpos.vfservprint.VFServiceConnectListener;
import com.vfi.smartpos.vfservprint.VFServiceManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    IPrinter iPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VFServiceManager.bindService(this, new VFServiceConnectListener() {
            @Override
            public void onServiceConnected() {
                try {
                    iPrinter = VFServiceManager.getPrinter();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onServiceDisconnected() {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.feed_line:
                try {
                    iPrinter.feedLine(20);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case R.id.set_gray:
                try {
                    iPrinter.setGray(1);

                    Bundle format1 = new Bundle();
                    //iPrinter.setLineSpace(10);

                    //barcode
                    format1.putInt("align", 0);
                    format1.putInt("width", 192);
                    format1.putInt("height", 128);
                    iPrinter.addBarCode(format1, "13524044282");

                    //qrcode
                    Bundle format2 = new Bundle();
                    format2.putInt("offset", 50);//expectedHeight
                    format2.putInt("expectedHeight", 128);
                    iPrinter.addQrCode(format2, "www.13524044282.qq.com");

                    //img
                    Bundle format3 = new Bundle();
                    format3.putInt("offset", 50);
                    format3.putInt("width", 384);
                    format3.putInt("height", 128);
                    byte[] imgs = getBitmapByte(R.drawable.confused);
                    if (imgs.length > 0) {
                        iPrinter.addImage(format3, imgs);
                        iPrinter.startPrint(new MyPrinterListener());
                        iPrinter.feedLine(2);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case R.id.print_text:
                try {
                    Bundle format = new Bundle();
                    format.putInt("font", 0);
                    format.putInt("align", 0);
                    format.putBoolean("bold", false);
                    format.putBoolean("newline", true);
                    //format.putFloat("scale_w",(float)1.5);
                    //format.putFloat("scale_h",(float)1.5);
                    iPrinter.addText(format, "ABCDabcd1234测试打印إيرث");
                    startPrint();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;

            case R.id.print_text2:
                try {
                    Bundle format = new Bundle();
                    format.putInt("fontSize", 0);
                    format.putBoolean("bold", true);
                    format.putString("fontStyle", "Chinese");
                    String lstr = "床前明月光1234567890";
                    String mstr = "";
                    String rstr = "疑是地上霜1234567890";
                    int mode = 0;
                    iPrinter.addTextInLine(format, lstr, mstr, rstr, mode);
                    startPrint();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;

            case R.id.print_img:
                try {
                    Bundle format = new Bundle();
                    format.putInt("offset", 0);
                    format.putInt("width", 192);
                    format.putInt("height", 128);
                    format.putInt("gray", 100);
                    byte[] imgs = getBitmapByte(R.mipmap.ic_launcher);
                    iPrinter.addImage(format, imgs);

                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

                break;

            case R.id.print_barCode:
                try {
                    Bundle format = new Bundle();
                    format.putInt("align", 0);
                    format.putInt("height", 128);
                    format.putInt("barCodeType", 0);
                    iPrinter.addBarCode(format, "I am Verifone");
                    startPrint();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

                break;
            case R.id.print_qrCode:
                try {
                    Bundle format = new Bundle();
                    format.putInt("offset", 50);//expectedHeight
                    format.putInt("expectedHeight", 128);
                    iPrinter.addQrCode(format, "I am Verifone");
                    startPrint();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case R.id.print_screen_capture:
                try {
                    iPrinter.addScreenCapture(new Bundle());
                    startPrint();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case R.id.clean_cache:
                try {
                    int result = iPrinter.cleanCache();
                    Toast.makeText(this, result + "", Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }

    private void startPrint() {
        try {
            iPrinter.startPrint(new MyPrinterListener());
            //iPrinter.startPrintInEmv(new MyPrinterListener());
            //iPrinter.startSaveCachePrint(new MyPrinterListener());
            iPrinter.feedLine(5);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    static class MyPrinterListener extends PrinterListener.Stub {
        @Override
        public void onError(int error) throws RemoteException {
            Log.d(TAG, "onError");
        }

        @Override
        public void onFinish() throws RemoteException {
            Log.d(TAG, "onError");
        }
    }

    private byte[] getBitmapByte(int id) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BitmapFactory.decodeResource(getResources(), id).compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VFServiceManager.unBindService();
    }
}