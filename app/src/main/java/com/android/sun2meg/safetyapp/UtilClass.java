package com.android.sun2meg.safetyapp;
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

public class UtilClass {

    public static String URLDecode(String str) {
        // Implement your URL decoding logic here
        return str;
    }

    public static void downloadVideo(String urlString, String filePath) {
        int begin, end;
        String tmpstr = null;
        try {
            URL url = new URL(urlString);
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

        } catch (MalformedURLException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }

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
        try {
            URL u = new URL(result);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");

            c.setDoOutput(true);
            c.connect();

            FileOutputStream f = new FileOutputStream(new File(filePath));

            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int sz = 0;
            while ((sz = in.read(buffer)) > 0) {
                f.write(buffer, 0, sz);
            }
            f.close();
        } catch (MalformedURLException e) {
            new RuntimeException();
        } catch (IOException e) {
            new RuntimeException();
        }
    }
}
