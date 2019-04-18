package com.ismailhakkiaydin.mobileapp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.ListView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressDialog progressDialog;
    private List<Model> modelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        new HaberServisiAsyncTask().execute("https://www.wired.com/feed/");

    }

    class HaberServisiAsyncTask extends AsyncTask<String, String, List<Model>>{

        @Override
        protected List<Model> doInBackground(String... strings) {

            modelList = new ArrayList<Model>();
            HttpURLConnection httpURLConnection = null;
            try {
                URL url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                int baglantiDurumu = httpURLConnection.getResponseCode();

                if (baglantiDurumu == HttpURLConnection.HTTP_OK){

                    BufferedInputStream bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    publishProgress("Yazilar Okunuyor...");
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document document = documentBuilder.parse(bufferedInputStream);

                    NodeList yaziNodeList = document.getElementsByTagName("item");
                    for (int i = 0; i < yaziNodeList.getLength(); i++){
                        Element element = (Element) yaziNodeList.item(i);
                        NodeList nodeListTitle = element.getElementsByTagName("title");
                        NodeList nodeListLink = element.getElementsByTagName("link");
                        NodeList nodeListDate = element.getElementsByTagName("pubDate");
                        NodeList nodeListCreator = element.getElementsByTagName("dc:creator");
                        NodeList nodeListDescription = element.getElementsByTagName("description");

                        String title = nodeListTitle.item(0).getFirstChild().getNodeValue();
                        String link = nodeListLink.item(0).getFirstChild().getNodeValue();
                        String date = nodeListDate.item(0).getFirstChild().getNodeValue();
                        String creator = nodeListCreator.item(0).getFirstChild().getNodeValue();
                        String description = nodeListDescription.item(0).getFirstChild().getNodeValue();

                        Model model = new Model();
                        model.setTitle(title);
                        model.setLink(link);
                        model.setDate(date);
                        model.setCreator(creator);

                        Pattern p = Pattern.compile(".*<img[^>]*src=\"([^\"]*)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(description);
                        String photoUrl = null;

                        while (m.find()) {
                            photoUrl = m.group(1);
                            Bitmap bitmap = null;

                            try {
                                URL  urlResim = new URL(photoUrl.toString().trim());
                                InputStream is = urlResim.openConnection().getInputStream();
                                bitmap = BitmapFactory.decodeStream(is);

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            model.setResim(bitmap);
                        }
                        modelList.add(model);
                        publishProgress("Liste Güncelleniyor...");
                    }

                }else{

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
            }

            return modelList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Lütfen Bekleyiniz...", "İşlem Devam Ediyor...", true);
        }

        @Override
        protected void onPostExecute(List<Model> models) {
            super.onPostExecute(models);
            CustomAdapter customAdapter = new CustomAdapter(MainActivity.this, models);
            listView.setAdapter(customAdapter);
            progressDialog.cancel();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage(values[0]);
        }
    }

}
