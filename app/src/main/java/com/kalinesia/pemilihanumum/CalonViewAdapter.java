package com.kalinesia.pemilihanumum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class CalonViewAdapter extends ArrayAdapter<Calon> {
    private List<Calon> calonList; // deklarasi object list calon

    //the context object
    private Context mCtx;
    //here we are getting the calonlist and context
    //so while creating the object of this adapter class we need to give herolist and context
    public CalonViewAdapter(List<Calon> calonList, Context mCtx) {
        super(mCtx, R.layout.list_calon, calonList);
        this.calonList = calonList; // set object calonList
        this.mCtx = mCtx; // set object mCtx
    }


    //this method will return the list item
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = LayoutInflater.from(mCtx);

        //creating a view with our xml layout
        View listViewItem = inflater.inflate(R.layout.list_calon, null, true);

        //getting inisialisasi text views
        TextView textViewNomor = listViewItem.findViewById(R.id.textViewNomor);
        TextView textViewName = listViewItem.findViewById(R.id.textViewName);
        ImageView ImageUrl = listViewItem.findViewById(R.id.gambarCalon);
        TextView textVisiMisi = listViewItem.findViewById(R.id.textViewVisiMisi);

        //Getting the calon for the specified position
        Calon calon = calonList.get(position);

        //setting calon values to textviews
        textViewNomor.setText(calon.getNomorUrut());
        textViewName.setText(calon.getName());
        new DownLoadImageTask(ImageUrl).execute(calon.getBaseUrl()+"/upload/"+calon.getImageUrl());
        textVisiMisi.setText(calon.getVisiMisi());

        //returning the listitem
        return listViewItem;
    }
    private class DownLoadImageTask extends AsyncTask<String,Void,Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){ // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
        }
    }
}
