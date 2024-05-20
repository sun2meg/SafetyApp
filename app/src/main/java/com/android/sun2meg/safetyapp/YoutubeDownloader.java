package com.android.sun2meg.safetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
public class YoutubeDownloader extends Activity {
    private static final int PERMISSIONS_REQUEST_CODE = 1001;

    private class ReceivingDataFromYoutube extends AsyncTask<String, Void, Void> {

        private ProgressDialog dialog = new ProgressDialog(YoutubeDownloader.this);
        private String result;

        protected void onPreExecute() {
            dialog.setMessage("Downloading...");
            dialog.show();
        }

        protected Void doInBackgroundx(String... arg0) {
            int begin, end;
            String tmpstr = null;
            try {
                URL url = new URL("http://www.youtube.com/watch?v=y12-1miZHLs&nomobile=1");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                InputStream stream = con.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream);
                StringBuffer buffer = new StringBuffer();
                char[] buf = new char[262144];
                int chars_read;
                while ((chars_read = reader.read(buf, 0, 262144)) != -1) {
                    buffer.append(buf, 0, chars_read);
                }
                tmpstr = buffer.toString();

                begin = tmpstr.indexOf("url_encoded_fmt_stream_map=");
                end = tmpstr.indexOf("&", begin + 27);
                if (end == -1) {
                    end = tmpstr.indexOf("\"", begin + 27);
                }
                tmpstr = UtilClass.URLDecode(tmpstr.substring(begin + 27, end));

                con.disconnect(); // Disconnect HttpURLConnection
            } catch (IOException e) {
                e.printStackTrace();
                // Handle network error, log or display to the user
            }

            // Parsing and file downloading logic
            if (tmpstr != null) {
                try {
                    Vector<String> url_encoded_fmt_stream_map = new Vector<>();
                    begin = 0;
                    end = tmpstr.indexOf(",");

                    while (end != -1) {
                        url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, end));
                        begin = end + 1;
                        end = tmpstr.indexOf(",", begin);
                    }

                    url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, tmpstr.length()));
                    String result = "";
                    Enumeration<String> url_encoded_fmt_stream_map_enum = url_encoded_fmt_stream_map.elements();
                    while (url_encoded_fmt_stream_map_enum.hasMoreElements()) {
                        tmpstr = url_encoded_fmt_stream_map_enum.nextElement();
                        begin = tmpstr.indexOf("itag=");
                        if (begin != -1) {
                            end = tmpstr.indexOf("&", begin + 5);

                            if (end == -1) {
                                end = tmpstr.length();
                            }

                            int fmt = Integer.parseInt(tmpstr.substring(begin + 5, end));

                            if (fmt == 35) {
                                begin = tmpstr.indexOf("url=");
                                if (begin != -1) {
                                    end = tmpstr.indexOf("&", begin + 4);
                                    if (end == -1) {
                                        end = tmpstr.length();
                                    }
                                    result = UtilClass.URLDecode(tmpstr.substring(begin + 4, end));
                                    break;
                                }
                            }
                        }
                    }

                    URL u = new URL(result);
                    HttpURLConnection c = (HttpURLConnection) u.openConnection();
                    c.setRequestMethod("GET");

                    c.setDoOutput(true);
                    c.connect();

                    String filename = "3.flv";
                    File file = new File(getFilesDir(), filename); // Internal storage directory
                    String filePath = file.getAbsolutePath();

                    try (InputStream in = c.getInputStream(); FileOutputStream f = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int sz;
                        while ((sz = in.read(buffer)) > 0) {
                            f.write(buffer, 0, sz);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle file writing error
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle error in parsing or file downloading logic
                }
            }

            return null;
        }

        @Override
        protected Void doInBackground(String... arg0) {
            int begin, end;
            String tmpstr = null;
            try {
                URL url = new URL("http://www.youtube.com/watch?v=y12-1miZHLs&nomobile=1");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                InputStream stream = con.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream);
                StringBuffer buffer = new StringBuffer();
                char[] buf = new char[262144];
                int chars_read;
                while ((chars_read = reader.read(buf, 0, 262144)) != -1) {
                    buffer.append(buf, 0, chars_read);
                }
                tmpstr = buffer.toString();

                begin = tmpstr.indexOf("url_encoded_fmt_stream_map=");
                end = tmpstr.indexOf("&", begin + 27);
                if (end == -1) {
                    end = tmpstr.indexOf("\"", begin + 27);
                }
                tmpstr = UtilClass.URLDecode(tmpstr.substring(begin + 27, end));

                con.disconnect(); // Disconnect HttpURLConnection

                // Parsing and file downloading logic
                Vector<String> url_encoded_fmt_stream_map = new Vector<>();
                begin = 0;
                end = tmpstr.indexOf(",");

                while (end != -1) {
                    url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, end));
                    begin = end + 1;
                    end = tmpstr.indexOf(",", begin);
                }

                url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, tmpstr.length()));
                String result = "";
                Enumeration<String> url_encoded_fmt_stream_map_enum = url_encoded_fmt_stream_map.elements();
                while (url_encoded_fmt_stream_map_enum.hasMoreElements()) {
                    tmpstr = url_encoded_fmt_stream_map_enum.nextElement();
                    begin = tmpstr.indexOf("itag=");
                    if (begin != -1) {
                        end = tmpstr.indexOf("&", begin + 5);

                        if (end == -1) {
                            end = tmpstr.length();
                        }

                        int fmt = Integer.parseInt(tmpstr.substring(begin + 5, end));

                        if (fmt == 35) {
                            begin = tmpstr.indexOf("url=");
                            if (begin != -1) {
                                end = tmpstr.indexOf("&", begin + 4);
                                if (end == -1) {
                                    end = tmpstr.length();
                                }
                                result = UtilClass.URLDecode(tmpstr.substring(begin + 4, end));
                                break;
                            }
                        }
                    }
                }

                URL u = new URL(result);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");

                c.setDoOutput(true);
                c.connect();

                File outputDir = getExternalFilesDir(null);
                FileOutputStream outputStream = new FileOutputStream(new File(outputDir, "VID_" + System.currentTimeMillis() + ".flv"));

                InputStream in = c.getInputStream();
                byte[] byteArray = new byte[1024];
                int byteCount = 0;
                while ((byteCount = in.read(byteArray)) > 0) {
                    outputStream.write(byteArray, 0, byteCount);
                }
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle network or file IO error
            } catch (NumberFormatException e) {
                e.printStackTrace();
                // Handle number format error
            }

            return null;
        }


//        @Override
//        protected Void doInBackground(String... arg0) {
//            int begin, end;
//            String tmpstr = null;
//            try {
//                URL url=new URL("http://www.youtube.com/watch?v=y12-1miZHLs&nomobile=1");
//                HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                con.setRequestMethod("GET");
//                InputStream stream=con.getInputStream();
//                InputStreamReader reader=new InputStreamReader(stream);
//                StringBuffer buffer=new StringBuffer();
//                char[] buf=new char[262144];
//                int chars_read;
//                while ((chars_read = reader.read(buf, 0, 262144)) != -1) {
//                    buffer.append(buf, 0, chars_read);
//                }
//                tmpstr = buffer.toString();
//
//                begin = tmpstr.indexOf("url_encoded_fmt_stream_map=");
//                end = tmpstr.indexOf("&", begin + 27);
//                if (end == -1) {
//                    end = tmpstr.indexOf("\"", begin + 27);
//                }
//                tmpstr = UtilClass.URLDecode(tmpstr.substring(begin + 27, end));
//
//                con.disconnect(); // Disconnect HttpURLConnection
//            } catch (IOException e) {
//                e.printStackTrace();
//                // Handle network error, log or display to the user
//            }
//
////
////                tmpstr=buffer.toString();
////
////                begin  = tmpstr.indexOf("url_encoded_fmt_stream_map=");
////                end = tmpstr.indexOf("&", begin + 27);
////                if (end == -1) {
////                    end = tmpstr.indexOf("\"", begin + 27);
////                }
////                tmpstr = UtilClass.URLDecode(tmpstr.substring(begin + 27, end));
////
////            } catch (MalformedURLException e) {
////                throw new RuntimeException();
////            } catch (IOException e) {
////                throw new RuntimeException();
////            }
//
//            Vector url_encoded_fmt_stream_map = new Vector();
//            begin = 0;
//            end   = tmpstr.indexOf(",");
//
//            while (end != -1) {
//                url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, end));
//                begin = end + 1;
//                end   = tmpstr.indexOf(",", begin);
//            }
//
//            url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, tmpstr.length()));
//            String result = "";
//            Enumeration url_encoded_fmt_stream_map_enum = url_encoded_fmt_stream_map.elements();
//            while (url_encoded_fmt_stream_map_enum.hasMoreElements()) {
//                tmpstr = (String)url_encoded_fmt_stream_map_enum.nextElement();
//                begin = tmpstr.indexOf("itag=");
//                if (begin != -1) {
//                    end = tmpstr.indexOf("&", begin + 5);
//
//                    if (end == -1) {
//                        end = tmpstr.length();
//                    }
//
//                    int fmt = Integer.parseInt(tmpstr.substring(begin + 5, end));
//
//                    if (fmt == 35) {
//                        begin = tmpstr.indexOf("url=");
//                        if (begin != -1) {
//                            end = tmpstr.indexOf("&", begin + 4);
//                            if (end == -1) {
//                                end = tmpstr.length();
//                            }
//                            result = UtilClass.URLDecode(tmpstr.substring(begin + 4, end));
//                            this.result=result;
//                            break;
//                        }
//                    }
//                }
//            }
//            try {
//                URL u = new URL(result);
//                HttpURLConnection c = (HttpURLConnection) u.openConnection();
//                c.setRequestMethod("GET");
///*              c.setRequestProperty("Youtubedl-no-compression", "True");
//              c.setRequestProperty("User-Agent", "YouTube");*/
//
//                c.setDoOutput(true);
//                c.connect();
//                new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
//                File outputDir = getExternalFilesDir(null);
////                File outputFile = new File(outputDir, "VID_" + System.currentTimeMillis() + ".mp4");
//                FileOutputStream f=new FileOutputStream(new File(outputDir, "VID_" + System.currentTimeMillis() + ".flv"));
//
////                FileOutputStream f=new FileOutputStream(new File("/sdcard/3.flv"));
//
//                InputStream in=c.getInputStream();
//                byte[] buffer=new byte[1024];
//                int sz = 0;
//                while ( (sz = in.read(buffer)) > 0 ) {
//                    f.write(buffer,0, sz);
//                }
//                f.close();
//            } catch (MalformedURLException e) {
//                new RuntimeException();
//            } catch (IOException e) {
//                new RuntimeException();
//            }
//            return null;
//        }

        protected void onPostExecute(Void unused) {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_downloader);
// Check and request permissions at runtime
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            // Permissions already granted, start AsyncTask
            new ReceivingDataFromYoutube().execute();
        }
    }

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start AsyncTask
                new ReceivingDataFromYoutube().execute();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }

}