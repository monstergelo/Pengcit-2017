package com.cxxxv.historigram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.RunnableFuture;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class ActivityOne extends AppCompatActivity {
    public class Feature{
        public int startX;
        public int startY;
        public String nama;
        public int centroidX;
        public int centroidY;
        public List<Integer> chainCode;
        public List<Integer> border;
        public int intColor = 0;
        int panjang = 0;
        int lebar = 0;

        public void add(int input){
            if(border == null){
                border = new ArrayList<Integer>();
            }

            border.add(input);
        }

        public void addDir(int input){
            if(chainCode == null){
                chainCode = new ArrayList<Integer>();
            }

            chainCode.add(input);
        }

        /*
          7  0  1
          6     2
          5  4  3
        */
        private int DirtoX(int dir){
            if( (dir == 1) || (dir == 2) || (dir == 3) ){
                return 1;
            }
            else if( (dir == 5) || (dir == 6) || (dir == 7) ){
                return -1;
            }
            if( (dir == 4) || (dir == 0) ){
                return 0;
            }

            return 0;
        }

        private int DirtoY(int dir){
            if( (dir == 3) || (dir == 4) || (dir == 5) ){
                return 1;
            }
            else if( (dir == 7) || (dir == 0) || (dir == 1) ){
                return -1;
            }
            if( (dir == 6) || (dir == 2) ){
                return 0;
            }

            return 0;
        }

        public void Analyze(int panjang_foto, int lebar_foto){
            int maxx = 0;
            int maxy = 0;
            int minx = 0;
            int miny = 0;

            int x = 0;
            int y = 0;
            for(Integer i:chainCode){
                x += DirtoX(i);
                y += DirtoY(i);

                if(maxx < x){
                    maxx = x;
                }

                if(maxy < y){
                    maxy = y;
                }

                if(minx > x){
                    minx = x;
                }

                if(miny > y){
                    miny = y;
                }
            }

            panjang = maxx - minx;
            lebar = maxy - miny;
            centroidX = startX + minx + panjang/2;
            centroidY = startY + miny + lebar/2;

            double ratio = (double)panjang/ (double)lebar;
            if((ratio > 2) && (ratio < 6) && (centroidY < lebar_foto/2)){
                if(centroidX > panjang_foto/2){
                    nama = "mata-kanan";
                }
                else{
                    nama = "mata-kiri";
                }

            }
            else if((ratio >= 3.5) && (centroidY > lebar_foto/2)){
                nama = "mulut";
            }
            else{
                nama = "unknown";
            }
        }

        @Override
        public String toString() {
            String color = Integer.toHexString(intColor & 0x00FFFFFF);

            double ratio = (double)panjang/ (double)lebar;

            return new StringBuilder()
                    .append("<font color=#"+color+">")
                    .append(nama!=null?"{"+nama:"feature")
                    .append("("+startX+","+startY+")")
                    .append("("+ratio+")")
                    .append(chainCode.toString())
                    .append("</font>")
                    .append("}").toString();
        }
    }

    //##############################################################################################
    public class Wajah{
        public List<Feature> fitur;
        public int width;
        public int length;
        public Feature mataKiri;
        public Feature mataKanan;
        public Feature alisKiri;
        public Feature alisKanan;

        //ciri-ciri wajah
        public double jarak_mata;
        public double puncakAlisKananX;
        public double puncakAlisKananY;
        public double puncakAlisKiriX;
        public double puncakAlisKiriY;

        public void add(Feature in){
            if(fitur == null){
                fitur = new ArrayList<>();
            }

            fitur.add(in);
        }

        public void AnalyzeFeature(){
            int mata_kiri = 0;
            int mata_kanan = 0;
            for(Feature f:fitur){
                f.Analyze(length, width);

                if(f.nama == "mata-kiri"){
                    mata_kiri = f.centroidX;

                    if(mataKiri == null){
                        mataKiri = f;
                    }
                    else{
                        alisKiri = mataKiri;
                        mataKiri = f;
                        alisKiri.nama = "alis-kiri";
                    }
                }
                else if(f.nama == "mata-kanan"){
                    mata_kanan = f.centroidX;

                    if(mataKanan == null){
                        mataKanan = f;
                    }
                    else{
                        alisKanan = mataKanan;
                        mataKanan = f;
                        alisKanan.nama = "alis-kanan";
                    }
                }
            }

            jarak_mata = mata_kanan-mata_kiri;

            puncakAlisKananX = alisKanan!=null?alisKanan.startX:0;
            puncakAlisKananY = alisKanan!=null?alisKanan.startY:0;
            puncakAlisKiriX = alisKiri!=null?alisKiri.startX:0;
            puncakAlisKiriY = alisKiri!=null?alisKiri.startY:0;

            jarak_mata = jarak_mata/length;
            puncakAlisKananX = puncakAlisKananX/length;
            puncakAlisKananY = puncakAlisKananY/length;
            puncakAlisKiriX = puncakAlisKiriX/length;
            puncakAlisKiriY = puncakAlisKiriY/length;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("jarak_mata: "+jarak_mata+"\n")
                    .append("alis kanan: "+puncakAlisKananX+"|"+puncakAlisKananY+"\n")
                    .append("alis kiri: "+puncakAlisKiriX+"|"+puncakAlisKiriY+"\n")
                    //.append("ukuran mata: "+ukuran_relatif_mata)
                    .toString();
        }
    }
    //##############################################################################################
    private Bitmap originalBitmaps;
    private Bitmap originalBitmaps1;
    private Bitmap originalBitmaps2;
    private Bitmap originalBitmaps3;
    private Bitmap filteredBitmaps;
    private ImageView foto1;
    private ImageView foto2;
    private ImageView foto_compare;
    private TextView shapeCount;
    private TextView chainCodeView;
    private TextView foto1_data;
    private TextView foto2_data;
    private TextView foto_compare_data;
    private TextView result_compare;
    private ProgressBar loading;
    private Button next;
    private Button prev;
    private Button compare;
    private Button start_compare;
    private Button process_button;
    private int stateFoto;
    private int stateProcess;
    private PhotoManager myManager;
    SeekBar horizontal1;
    SeekBar horizontal2;
    SeekBar horizontal3;
    final int black = 10;
    final int white = 250;
    final int borderColorRed = 10;
    final int borderColorGreen = 90;
    final int borderColorBlue = 40;
    private int maxx;
    private int maxy;
    private Queue<Integer> tracingQueue;
    Wajah allBorder;
    Wajah wajah1;
    Wajah wajah2;
    Wajah wajah_compare;
    private boolean[][] justBorder;
    private int[] histogramRed;
    private int[] histogramBlue;
    private int[] histogramGreen;

    final int RESULT_LOAD_IMG_1 = 1;
    final int RESULT_LOAD_IMG_2 = 2;
    final int RESULT_LOAD_IMG_3 = 3;
//==================================================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("reqCode", Integer.toString(requestCode));
        Log.d("resultCode", Integer.toString(resultCode));
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                double width = 300;
//                double height = 400;
                Bitmap resized = Bitmap.createScaledBitmap(selectedImage, (int)(selectedImage.getWidth()*0.1), (int)(selectedImage.getHeight()*0.1), true);
//
//                originalBitmaps = selectedImage;
//                filteredBitmaps = originalBitmaps.copy(originalBitmaps.getConfig(), true);

                if(requestCode == RESULT_LOAD_IMG_1){
                    Log.d("masuk", "1");
                    foto1.setImageBitmap(resized);
                    originalBitmaps1 = resized;
                }
                else if(requestCode == RESULT_LOAD_IMG_2){
                    Log.d("masuk", "2");
                    foto2.setImageBitmap(resized);
                    originalBitmaps2 = resized;
                }
                else{ //reqCode == RESULT_LOAD_IMG_3
                    Log.d("masuk", "3");
                    foto_compare.setImageBitmap(resized);
                    originalBitmaps3 = resized;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }

        }else {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalBitmaps = BitmapFactory.decodeResource(getResources(), R.drawable.wajah02);
        filteredBitmaps = originalBitmaps.copy(originalBitmaps.getConfig(), true);
        foto1 = (ImageView) findViewById(R.id.imageView);
        foto_compare = (ImageView) findViewById(R.id.imageView2);
        originalBitmaps3 = ((BitmapDrawable) foto_compare.getDrawable()).getBitmap();
        loading = (ProgressBar) findViewById(R.id.progressBar2);
        compare = (Button) findViewById(R.id.button);
        start_compare = (Button) findViewById(R.id.button2);
        loading.bringToFront();
        myManager = new PhotoManager();
        stateFoto = 1;
        stateProcess = 0;
        //===============================================================================================
        process_button = (Button) findViewById(R.id.button3);
        foto_compare_data = (TextView) findViewById(R.id.textView5);
        final TextView horizontalT = (TextView) findViewById(R.id.textView5);
        //===============================================================================================
        process_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stateProcess == 0){
                    new PhotoManager().execute();
                    stateProcess++;
                }
                else if(stateProcess == 1){
                    new PhotoManager2().execute();
                    stateProcess++;
                }
                else {
                    foto_compare.setImageBitmap(originalBitmaps);
                    stateProcess = 0;
                }
            }
        });
        //===============================================================================================
        compare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                Log.d("panggil", "RESULT_LOAD_IMG_3");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG_3);
            }
        });

        start_compare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CompareFoto();
            }
        });
    }
    //==============================================================================================
    private SeekBar.OnSeekBarChangeListener ConfigSeekbar(final TextView text, final Integer foto_index){
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                text.setText(String.valueOf(i));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new PhotoManager().execute(text.getText().toString(), foto_index.toString());
            }
        };
    }
    //==============================================================================================
    private void setFoto(){
//        filtered = (ImageView) findViewById(R.id.imageView4);
//        int currentFoto = R.drawable.wajah01;
//
//        if(stateFoto == 1){
//            currentFoto = R.drawable.wajah01;
//        }
//        else if(stateFoto == 2){
//            currentFoto = R.drawable.wajah02;
//        }
//        else if(stateFoto == 3){
//            currentFoto = R.drawable.wajah03;
//        }
//        else if(stateFoto == 4){
//            currentFoto = R.drawable.wajah04;
//        }
//        else if(stateFoto == 5){
//            currentFoto = R.drawable.wajah05;
//        }
//        else if(stateFoto == 6){
//            currentFoto = R.drawable.wajah06;
//        }
//        else if(stateFoto == 7){
//            currentFoto = R.drawable.wajah07;
//        }
//        else if(stateFoto == 8){
//            currentFoto = R.drawable.wajah08;
//        }
//        else if(stateFoto == 9){
//            currentFoto = R.drawable.wajah09;
//        }
//        else if(stateFoto == 10){
//            currentFoto = R.drawable.wajah10;
//        }
//        else if(stateFoto == 11){
//            currentFoto = R.drawable.wajah11;
//        }
//        else if(stateFoto == 12){
//            currentFoto = R.drawable.wajah12;
//        }
//
//        filtered.setImageResource(currentFoto);
//
//        originalBitmaps = BitmapFactory.decodeResource(getResources(), currentFoto);
//        filteredBitmaps = originalBitmaps.copy(originalBitmaps.getConfig(), true);

    }
    //==============================================================================================
    public void CompareFoto(){
        Log.d("wajah1", wajah1.toString());
        Log.d("wajah2", wajah2.toString());
        Log.d("wajah_compare", wajah_compare.toString());

        //compare jarak_mata
        double jarak_mata1 = abs(wajah1.jarak_mata - wajah_compare.jarak_mata);
        double jarak_mata2 = abs(wajah2.jarak_mata - wajah_compare.jarak_mata);

        //compare alis 1
        double x1 = wajah1.puncakAlisKiriX;
        double y1 = wajah1.puncakAlisKiriY;
        double x2 = wajah_compare.puncakAlisKiriX;
        double y2 = wajah_compare.puncakAlisKiriY;
        double selisih1_alis_kiri = jarakTitik(x1, y1, x2, y2);

        x1 = wajah1.puncakAlisKananX;
        y1 = wajah1.puncakAlisKananY;
        x2 = wajah_compare.puncakAlisKananX;
        y2 = wajah_compare.puncakAlisKananY;
        double selisih1_alis_kanan = jarakTitik(x1, y1, x2, y2);

        //compare alis 2
        x1 = wajah2.puncakAlisKiriX;
        y1 = wajah2.puncakAlisKiriY;
        x2 = wajah_compare.puncakAlisKiriX;
        y2 = wajah_compare.puncakAlisKiriY;
        double selisih2_alis_kiri = jarakTitik(x1, y1, x2, y2);

        x1 = wajah2.puncakAlisKananX;
        y1 = wajah2.puncakAlisKananY;
        x2 = wajah_compare.puncakAlisKananX;
        y2 = wajah_compare.puncakAlisKananY;
        double selisih2_alis_kanan = jarakTitik(x1, y1, x2, y2);

        double selisih_alis_kiri = abs(selisih1_alis_kiri - selisih2_alis_kiri);
        double selisih_alis_kanan = abs(selisih1_alis_kanan - selisih2_alis_kanan);

        Log.d("jarak mata1", Double.toString(jarak_mata1));
        Log.d("jarak mata2", Double.toString(jarak_mata2));

        Log.d("selisih1_alis_kiri", Double.toString(selisih1_alis_kiri));
        Log.d("selisih2_alis_kiri", Double.toString(selisih2_alis_kiri));

        Log.d("selisih1_alis_kanan", Double.toString(selisih1_alis_kanan));
        Log.d("selisih2_alis_kanan", Double.toString(selisih2_alis_kanan));

        //===
        double score1 = 100 - jarak_mata1 - selisih1_alis_kiri - selisih1_alis_kanan;
        double score2 = 100 - jarak_mata2 - selisih2_alis_kiri - selisih2_alis_kanan;

        if(score1 > score2){
            result_compare.setText("foto1");
        }
        else
        {
            result_compare.setText("foto2");
        }
    }
    //==============================================================================================
    private double jarakTitik(double x1, double y1, double x2, double y2){
        return Math.sqrt( Math.pow(abs(x1-x2),2) + Math.pow((abs(y1-y2)),2));
    }
    //==============================================================================================
    private class PhotoManager extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            ChangePhoto();
            return null;
        }
    }
    private class PhotoManager2 extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            ChangePhoto2();
            return null;
        }
    }
    //==============================================================================================
    public boolean isAxisValid(int i, int j){
        if(i < 0 || i >= maxx || j < 0 || j>= maxy){
            return false;
        }

        return true;
    }
    //==============================================================================================
    public boolean isOutside(int i, int j){
        if(i < 0 || i >= maxx || j < 0 || j>= maxy){
            return true;
        }

        int pixel = filteredBitmaps.getPixel(i,j);
        Integer combined = (Color.red(pixel)+Color.blue(pixel)+Color.green(pixel)) / 3;
        if(combined == white){
            return true;
        }

        return false;
    }
    //==============================================================================================
    public boolean isOutsideWhite(int i, int j){
        if(i < 0 || i >= maxx || j < 0 || j>= maxy){
            return true;
        }

        int pixel = filteredBitmaps.getPixel(i,j);
        Integer combined = (Color.red(pixel)+Color.blue(pixel)+Color.green(pixel)) / 3;
        int borderColor = (borderColorRed + borderColorBlue + borderColorGreen) / 3;
        if(combined != white && combined != borderColor){
            return true;
        }

        return false;
    }
    //==============================================================================================
    public boolean isBlack(int i, int j){
        if(i < 0 || i >= maxx || j < 0 || j>= maxy){
            return false;
        }

        int pixel = filteredBitmaps.getPixel(i,j);
        int combined = (Color.red(pixel)+Color.blue(pixel)+Color.green(pixel)) / 3;
        int borderColor = (borderColorRed + borderColorBlue + borderColorGreen) / 3;

        if(combined != white && combined != borderColor){
            return true;
        }
        else{
            return false;
        }
    }
    //==============================================================================================
    public boolean isWhite(int i, int j){
        if(i < 0 || i >= maxx || j < 0 || j>= maxy){
            return false;
        }

        int pixel = filteredBitmaps.getPixel(i,j);
        int combined = (Color.red(pixel)+Color.blue(pixel)+Color.green(pixel)) / 3;
        int borderColor = (borderColorRed + borderColorBlue + borderColorGreen) / 3;

        if(combined != black && combined != borderColor && combined==white){
            return true;
        }
        else{
            return false;
        }
    }
    //==============================================================================================
    public boolean isNeighborExist(int i, int j, int d){
        if(!isAxisValid(i, j)){
            return false;
        }

        if(d < 1){
            return false;
        }
        if(isBlack(i-1,j+1)){
            Log.d("is Neighbor", Integer.toString(i-1)+'|'+Integer.toString(j+1));
            return true;
        }
        //W
        else if(isBlack(i-1,j)){
            Log.d("is Neighbor", Integer.toString(i-1)+'|'+Integer.toString(j));
            return true;
        }
        //NW
        else if(isBlack(i-1,j-1)){
            Log.d("is Neighbor", Integer.toString(i-1)+'|'+Integer.toString(j-1));
            return true;
        }

        return false;
    }
    //==============================================================================================
    public boolean isBorder(int i, int j){
        if(!isAxisValid(i,j)){
            return false;
        }

        if((isOutside(i+1,j)) || (isOutside(i-1,j)) || (isOutside(i,j+1)) || (isOutside(i,j-1))){
            if(isBlack(i, j)){
                return true;
            }

        }

        return false;
    }
    //==============================================================================================
    public boolean isBorderWhite(int i, int j){
        if(!isAxisValid(i,j)){
            return false;
        }

        if((isOutsideWhite(i+1,j)) || (isOutsideWhite(i-1,j)) || (isOutsideWhite(i,j+1)) || (isOutsideWhite(i,j-1))){
            if(isWhite(i, j)){
                return true;
            }

        }

        return false;
    }
    //==============================================================================================
    public int nextTraceI(int i, int direction) {
        direction = formatDirection(direction);
        int result = -999;
        if(direction%8 == 0) {
            result = i;
        }
        else if(direction%8 == 1) {
            result = i+1;
        }
        else if(direction%8 == 2) {
            result = i+1;
        }
        else if(direction%8 == 3) {
            result = i+1;
        }
        else if(direction%8 == 4) {
            result = i;
        }
        else if(direction%8 == 5) {
            result = i-1;
        }
        else if(direction%8 == 6) {
            result = i-1;
        }
        else if(direction%8 == 7) {
            result = i-1;
        }

        return result;
    }
    //==============================================================================================
    public int nextTraceJ(int j, int direction) {
        direction = formatDirection(direction);
        int result = -999;
        if(direction%8 == 0) {
            result = j-1;
        }
        else if(direction%8 == 1) {
            result = j-1;
        }
        else if(direction%8 == 2) {
            result = j;
        }
        else if(direction%8 == 3) {
            result = j+1;
        }
        else if(direction%8 == 4) {
            result = j+1;
        }
        else if(direction%8 == 5) {
            result = j+1;
        }
        else if(direction%8 == 6) {
            result = j;
        }
        else if(direction%8 == 7) {
            result = j-1;
        }

        return result;
    }
    //==============================================================================================
    public int formatDirection(int direction){
        if(direction >= 8){
            return formatDirection(direction - 8);
        }
        else if(direction < 0)
        {
            return formatDirection(direction + 8);
        }

        return direction;
    }
    //==============================================================================================
    public int getCurrentDirection(int oldDirection, int trial){
        int newDirection = (oldDirection + trial);

        if(newDirection%2 == 0){
            return formatDirection(newDirection);
        }
        else{ //newDirection ganjil
            return formatDirection(newDirection-1);
        }
    }
    //==============================================================================================
    public int[] checkNeighbor(int i, int j, int direction){
        int currentDirection = formatDirection(direction-1);

        for(int d=0; d<8; d++){
            if(isBorderWhite(nextTraceI(i, currentDirection+d), nextTraceJ(j, currentDirection+d))){
                int[] result = new int[4];
                result[0] = nextTraceI(i, currentDirection+d);
                result[1] = nextTraceJ(j, currentDirection+d);
                result[2] = getCurrentDirection(currentDirection, d);
                result[3] = formatDirection(currentDirection+d);
                return  result;
            }
        }

        int[] result = new int[4];
        result[0] = -999;
        result[1] = -999;
        result[2] = -999;
        result[3] = -999;
        return result;
    }
    //==============================================================================================
    public int TraceShape(int i, int j){
        /*
          7  0  1
          6     2
          5  4  3
        */
        Feature border = new Feature();
        border.startX = i;
        border.startY = j;
        int x = i;
        int y = j;
        int direction = 2;
        int[] neighbor;
        int pixelCount = 0;
        boolean end = false;
        while(!end) {
            filteredBitmaps.setPixel(x, y, Color.argb(1, borderColorRed, borderColorGreen, borderColorBlue));
            border.add(x);
            border.add(y);
            justBorder[x][y] = true;
            pixelCount++;
            Log.d("border", Integer.toString(x)+"|"+Integer.toString(y));
            neighbor = checkNeighbor(x, y, direction);
            direction = neighbor[2];
            if(direction != -999){
                border.addDir(neighbor[3]);
                x = neighbor[0];
                y = neighbor[1];
            }
            else{
                end = true;
            }
        }

        if(pixelCount > 280){
            allBorder.add(border);

            return 1;
        }
        else {
            boolean axis = false;
            for (int pixel : border.border) {
                if (axis) {
                    //now y
                    y = pixel;
                    justBorder[x][y] = false;

                } else {
                    //now x
                    x = pixel;
                }
                axis = !axis;
            }
            return 0;
        }
    }
    //==============================================================================================
    public int CountShape(){
        int count = 0;
        allBorder.length = maxx;
        allBorder.width = maxy;

        maxy = filteredBitmaps.getHeight();
        maxx = filteredBitmaps.getWidth();
        justBorder = new boolean[maxx][maxy];

        Log.d("te", "start counting");
        for(int j=10; j<maxy-10; j++) {
            for (int i = 10; i < maxx-10; i++) {
                int pixel = filteredBitmaps.getPixel(i,j);
                Integer combined = (Color.red(pixel)+Color.blue(pixel)+Color.green(pixel)) / 3;

                if(combined == white){
                    if(isBorderWhite(i, j)){
                        count += TraceShape(i, j);

                    }

                    if(count > 1000){
                        return count;
                    }
                }
                else { //combined == black

                }
            }
        }

        return count;
    }
    //==============================================================================================
    public void setThickPixel(Bitmap bm, int thick, int i, int j){
        int xstart = i-thick;
        int xend = i+thick;
        int ystart = j-thick;
        int yend = j+thick;

        filteredBitmaps.setPixel(i, j, Color.argb(1, borderColorRed, borderColorGreen, borderColorBlue));
        for(int x=xstart; x<xend; x++){
            for(int y=ystart; y<yend; y++){
                if(x < 0 || x >= maxx || y < 0 || y>= maxy){
                    continue;
                }

                filteredBitmaps.setPixel(x, y, Color.argb(1, borderColorRed, borderColorGreen, borderColorBlue));
            }
        }
    }
    //==============================================================================================
    public void drawThickPixel(Bitmap bm, int thick, int i, int j, int c){
        int xstart = i-thick;
        int xend = i+thick;
        int ystart = j-thick;
        int yend = j+thick;

        filteredBitmaps.setPixel(i, j, c);
        for(int x=xstart; x<xend; x++){
            for(int y=ystart; y<yend; y++){
                if(x < 0 || x >= maxx || y < 0 || y>= maxy){
                    continue;
                }

                filteredBitmaps.setPixel(x, y, c);
            }
        }
    }
    //==============================================================================================
    public Bitmap Filter(Integer[][] filter_matrix, Integer factor, Integer bias, Bitmap original){
        int size = filter_matrix.length;
        int filter_offset = size/2;
        Bitmap filtered = original.copy(original.getConfig(), true);
        int image_width = filtered.getWidth();
        int image_height = filtered.getHeight();

        //iterate semua pixel
        for(int i=0; i<image_width; i++){
            for(int j=0; j<image_height; j++){
                //apply filter
                int pixel_i = i - filter_offset;
                int pixel_j = j - filter_offset;
                double red = 0;
                double green = 0;
                double blue = 0;
                for(int fi=0; fi<size; fi++) {
                    for (int fj = 0; fj < size; fj++) {
                        int current_pixel;
                        //check pixel valid
                        if(pixel_i+fi>0 && pixel_i+fi<image_width && pixel_j+fj>0 && pixel_j+fj<image_height){
                            current_pixel = original.getPixel(pixel_i+fi, pixel_j+fj);
                        }
                        else{
                            current_pixel = Color.argb(1, 0, 0, 0);
                        }

                        red += ((double)Color.red(current_pixel) * (double)filter_matrix[fi][fj] / (double)factor);
                        green += ((double)Color.green(current_pixel) * (double)filter_matrix[fi][fj] / (double)factor);
                        blue += ((double)Color.blue(current_pixel) * (double)filter_matrix[fi][fj] / (double)factor);
                    }
                }

                red += bias;
                green += bias;
                blue += bias;
                //truncate result
                if(red > 255) red = 255;
                if(green > 255) green = 255;
                if(blue > 255) blue = 255;
                if(red < 0) red = 0;
                if(green < 0) green = 0;
                if(blue < 0) blue = 0;

                filtered.setPixel(i, j, Color.argb(1, (int)red, (int)green, (int)blue));
            }
        }

        return filtered;
    }
    //==============================================================================================
    public void FindFaces(){
        Log.d("width", Integer.toString(originalBitmaps.getWidth()));
        Log.d("height", Integer.toString(filteredBitmaps.getHeight()));
        Log.d("te", "start converting");
        for (int i = 0; i < maxx; i++) {
            for (int j = 0; j < maxy; j++) {
                int pixel = filteredBitmaps.getPixel(i, j);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                List<Boolean> skin_rule = new ArrayList<>();
                skin_rule.add(red > 95);
                skin_rule.add(green > 40);
                skin_rule.add(blue > 20);
                skin_rule.add(red > green);
                skin_rule.add(red > blue);
                skin_rule.add(abs(red-green) > 15);
                Boolean all_rule = true;
                for (boolean rule:skin_rule) {
                    all_rule = all_rule && rule;
                }

                if(all_rule){
                    filteredBitmaps.setPixel(i, j, Color.argb(1, 250, 250, 250));
                }

//                Integer combined = Color.blue(pixel) + Color.red(pixel) + Color.green(pixel);
//                combined = combined / 3;
//                if (combined > threshold) {
//                    //drawThickPixel(filteredBitmaps, 3, i, j, Color.argb(1, 250, 250, 250));
//                    filteredBitmaps.setPixel(i, j, Color.argb(1, 250, 250, 250));
//                } else {
//                    //drawThickPixel(filteredBitmaps, 3, i, j, Color.argb(1, 10, 10, 10));
//                    filteredBitmaps.setPixel(i, j, Color.argb(1, 10, 10, 10));
//                }
            }
        }
    }
    //==============================================================================================
    public void HighlightFaces(){
        final int count;
        final StringBuilder text = new StringBuilder();
        if ((allBorder != null) && (allBorder.fitur != null)) {
            count = allBorder.fitur.size();


            //redrawing
            Log.d("te", "start just shape");

            ///whiting
//            for (int i = 0; i < maxx; i++) {
//                for (int j = 0; j < maxy; j++) {
//                    filteredBitmaps.setPixel(i, j, Color.argb(1, white, white, white));
//                }
//            }

            //highlighting
            for (Feature shape : allBorder.fitur) {
                int l = 0;
                int i = shape.startX;
                int j = shape.startY;

                setThickPixel(filteredBitmaps, 0, i, j);
                for (int dir : shape.chainCode) {
                    if (dir == 0) {
                        i = i;
                        j = j - 1;
                    } else if (dir == 1) {
                        i = i + 1;
                        j = j - 1;
                    } else if (dir == 2) {
                        i = i + 1;
                        j = j;
                    } else if (dir == 3) {
                        i = i + 1;
                        j = j + 1;
                    } else if (dir == 4) {
                        i = i;
                        j = j + 1;
                    } else if (dir == 5) {
                        i = i - 1;
                        j = j + 1;
                    } else if (dir == 6) {
                        i = i - 1;
                        j = j;
                    } else if (dir == 7) {
                        i = i - 1;
                        j = j - 1;
                    }

                    setThickPixel(filteredBitmaps, 0, i, j);
                    l++;
                    //                if (l>50){
                    //                    break;
                    //                }
                }
            }

            int[] all_left = new int[allBorder.fitur.size()];
            int[] all_right = new int[allBorder.fitur.size()];
            int[] all_upper = new int[allBorder.fitur.size()];
            int[] all_bottom = new int[allBorder.fitur.size()];
            int index = 0;
            for (Feature shape : allBorder.fitur) {
                int left = 9999;
                int upper = 9999;
                int right = -9999;
                int bottom = -9999;

                boolean axis = false;
                for (Integer pixel : shape.border) {
                    if (axis) {
                        //now y
                        if (pixel < upper) {
                            upper = pixel;
                        } else if (pixel > bottom) {
                            bottom = pixel;
                        }
                    } else {
                        //now x
                        if (pixel < left) {
                            left = pixel;
                        } else if (pixel > right) {
                            right = pixel;
                        }
                    }

                    axis = !axis;
                }

                Log.e("upper", Integer.toString(upper));
                Log.e("bottom", Integer.toString(bottom));
                Log.e("left", Integer.toString(left));
                Log.e("right", Integer.toString(right));

                all_left[index] = left;
                all_right[index] = right;
                all_upper[index] = upper;
                all_bottom[index] = bottom;
                index++;
            }
            //=========================================================
            //Filter
            //=========================================================
            Integer[][] matrix_filter_5 = new Integer[5][5];
            matrix_filter_5[0][0] = 1; matrix_filter_5[1][0] = 1; matrix_filter_5[2][0] = 1; matrix_filter_5[3][0] = 1; matrix_filter_5[4][0] = 1;
            matrix_filter_5[0][1] = 1; matrix_filter_5[1][1] = 1; matrix_filter_5[2][1] = 1; matrix_filter_5[3][1] = 1; matrix_filter_5[4][1] = 1;
            matrix_filter_5[0][2] = 1; matrix_filter_5[1][2] = 1; matrix_filter_5[2][2] = 1; matrix_filter_5[3][2] = 1; matrix_filter_5[4][2] = 1;
            matrix_filter_5[0][3] = 1; matrix_filter_5[1][3] = 1; matrix_filter_5[2][3] = 1; matrix_filter_5[3][3] = 1; matrix_filter_5[4][3] = 1;
            matrix_filter_5[0][4] = 1; matrix_filter_5[1][4] = 1; matrix_filter_5[2][4] = 1; matrix_filter_5[3][4] = 1; matrix_filter_5[4][4] = 1;

            Integer[][] matrix_filter_3 = new Integer[3][3];
            matrix_filter_3[0][0] = 1; matrix_filter_3[1][0] = 0; matrix_filter_3[2][0] = -1;
            matrix_filter_3[0][1] = 2; matrix_filter_3[1][1] = 0; matrix_filter_3[2][1] = -2;
            matrix_filter_3[0][2] = 1; matrix_filter_3[1][2] = 0; matrix_filter_3[2][2] = -1;

            filteredBitmaps = Filter(matrix_filter_3, 1, 0, originalBitmaps);

            //draw boundary box
            for(int ind=0; ind<all_left.length; ind++){
                Random rnd = new Random();
                int red = rnd.nextInt(256);
                int green = rnd.nextInt(256);
                int blue = rnd.nextInt(256);

                for (int i = all_left[ind]; i < all_right[ind]; i++) {
                    drawThickPixel(filteredBitmaps, 3, i, all_upper[ind], Color.argb(1, red, green, blue));
                    drawThickPixel(filteredBitmaps, 3, i, all_bottom[ind], Color.argb(1, red, green, blue));
                }

                for (int j = all_upper[ind]; j < all_bottom[ind]; j++) {
                    drawThickPixel(filteredBitmaps, 3, all_left[ind], j, Color.argb(1, red, green, blue));
                    drawThickPixel(filteredBitmaps, 3, all_right[ind], j, Color.argb(1, red, green, blue));
                }
            }


        }
        else{
            count = 0;
        }
    }

    public void ChangePhoto() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.VISIBLE);
                process_button.setEnabled(false);
            }
        });

        final ImageView filtered = foto_compare;
        originalBitmaps = originalBitmaps3;
        wajah_compare = new Wajah();
        allBorder = wajah_compare;

        filteredBitmaps = originalBitmaps.copy(originalBitmaps.getConfig(), true);

        maxy = originalBitmaps.getHeight();
        maxx = originalBitmaps.getWidth();

        FindFaces();

        runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  filtered.setImageBitmap(filteredBitmaps);
                  loading.setVisibility(View.INVISIBLE);
                  process_button.setEnabled(true);
              }
        });
    }

    public void ChangePhoto2() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.VISIBLE);
                process_button.setEnabled(false);
            }
        });

        final ImageView filtered = foto_compare;

        maxy = originalBitmaps.getHeight();
        maxx = originalBitmaps.getWidth();

        CountShape();

        HighlightFaces();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filtered.setImageBitmap(filteredBitmaps);
                loading.setVisibility(View.INVISIBLE);
                process_button.setEnabled(true);
            }
        });
    }
}
