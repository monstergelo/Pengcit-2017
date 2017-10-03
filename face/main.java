package com.cxxxv.historigram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

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
    //==============================================================================================
    private Bitmap originalBitmaps;
    private Bitmap filteredBitmaps;
    private ImageView filtered;
    private TextView shapeCount;
    private ProgressBar loading;
    private PhotoManager myManager;
    SeekBar horizontal;
    final int black = 10;
    final int white = 250;
    final int borderColorRed = 10;
    final int borderColorGreen = 90;
    final int borderColorBlue = 40;
    private int maxx;
    private int maxy;
    private Queue<Integer> tracingQueue;
    List<Integer> border;
    List<List<Integer>> allBorder;
    private boolean[][] justBorder;
    private int[] histogramRed;
    private int[] histogramBlue;
    private int[] histogramGreen;
//==================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalBitmaps = BitmapFactory.decodeResource(getResources(), R.drawable.fotowajah2);
        filteredBitmaps = originalBitmaps.copy(originalBitmaps.getConfig(), true);
        filtered = (ImageView) findViewById(R.id.imageView4);
        loading = (ProgressBar) findViewById(R.id.progressBar2);
        loading.bringToFront();
        myManager = new PhotoManager();
        //===============================================================================================
        horizontal = (SeekBar) findViewById(R.id.seekBar5);
        shapeCount = (TextView) findViewById(R.id.textView3);
        final TextView horizontalT = (TextView) findViewById(R.id.textView2);
        //===============================================================================================
        horizontal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                horizontalT.setText(String.valueOf(i));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new PhotoManager().execute(horizontalT.getText().toString());
            }
        });
    }
    //==============================================================================================
    private class PhotoManager extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            ChangePhoto(Integer.parseInt(strings[0]));
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

        if(combined != black && combined != borderColor){
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
            if((!isAxisValid(i+1,j)) || (!isAxisValid(i-1,j)) || (!isAxisValid(i,j+1)) || (!isAxisValid(i,j-1))){
                if(isWhite(i, j)){
                    return true;
                }
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
            if(isBorder(nextTraceI(i, currentDirection+d), nextTraceJ(j, currentDirection+d))){
                int[] result = new int[3];
                result[0] = nextTraceI(i, currentDirection+d);
                result[1] = nextTraceJ(j, currentDirection+d);
                result[2] = getCurrentDirection(currentDirection, d);
                return  result;
            }
        }

        int[] result = new int[3];
        result[0] = -999;
        result[1] = -999;
        result[2] = -999;
        return result;
    }
    //==============================================================================================
    public int TraceShape(int i, int j){
        /*
          7  0  1
          6     2
          5  4  3
        */
        border = new ArrayList<Integer>();
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
                x = neighbor[0];
                y = neighbor[1];
            }
            else{
                end = true;
            }
        }

        if(pixelCount > 70){
            List<Integer> temp = new ArrayList<Integer>(border);
            allBorder.add(temp);

            return 1;
        }
        else{
            boolean axis = false;
            for(int pixel:border){
                if(axis){
                    //now y
                    y = pixel;
                    justBorder[x][y] = false;

                }
                else{
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
        border = new ArrayList<Integer>();
        allBorder = new ArrayList<List<Integer>>();

        maxy = filteredBitmaps.getHeight();
        maxx = filteredBitmaps.getWidth();
        justBorder = new boolean[maxx][maxy];

        Log.d("te", "start counting");
        for(int j=0; j<maxy; j++) {
            for (int i = 0; i < maxx; i++) {
                int pixel = filteredBitmaps.getPixel(i,j);
                Integer combined = (Color.red(pixel)+Color.blue(pixel)+Color.green(pixel)) / 3;

                if(combined == white){
                    //nothing
                }
                else { //combined == black
                    if(isBorder(i, j)){
                        count += TraceShape(i, j);

                    }

                    if(count > 1000){
                        return count;
                    }
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
    public void ChangePhoto(Integer threshold){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.VISIBLE);
                horizontal.setEnabled(false);
            }
        });
        maxy = originalBitmaps.getHeight();
        maxx = originalBitmaps.getWidth();

        //=========================================================
        //EQUALIZATION
        //=========================================================
        Log.d("te", "start equalization");
        histogramRed = new int[256];
        histogramBlue = new int[256];
        histogramGreen = new int[256];

        //create histogram
        for(int i=0; i<maxx; i++){
            for(int j=0; j<maxy; j++){
                int pixel = originalBitmaps.getPixel(i, j);

                histogramRed[Color.red(pixel)]++;
                histogramGreen[Color.green(pixel)]++;
                histogramBlue[Color.blue(pixel)]++;
            }
        }

        //create equalization map
        for(int i=1; i<histogramRed.length; i++){
            histogramRed[i] = histogramRed[i] + histogramRed[i-1];
            histogramGreen[i] = histogramGreen[i] + histogramGreen[i-1];
            histogramBlue[i] = histogramBlue[i] + histogramBlue[i-1];
        }

        //normalize equalization map
        int maxHistogramRed = histogramRed[histogramRed.length-1];
        int maxHistogramGreen = histogramGreen[histogramGreen.length-1];
        int maxHistogramBlue = histogramBlue[histogramBlue.length-1];

        int normalize_factor_red = maxHistogramRed / 256;
        int normalize_factor_green = maxHistogramGreen / 256;
        int normalize_factor_blue = maxHistogramBlue / 256;

        for(int i=1; i<histogramRed.length; i++){
            histogramRed[i] = histogramRed[i] / normalize_factor_red;
            histogramGreen[i] = histogramGreen[i] + normalize_factor_green;
            histogramBlue[i] = histogramBlue[i] + normalize_factor_blue;
        }

        //create aggregate equalization map
        int aggregateSize = 60;
        int aggregateRed = 0;
        int aggregateGreen = 0;
        int aggregateBlue = 0;
        for(int i=0; i<histogramRed.length; i++){
            //find aggregate
            if(i+aggregateSize > 255){
                aggregateSize = 255 - i;
            }

            for(int j=i; j<i+aggregateSize; j++){
                aggregateRed += histogramRed[j] ;
                aggregateGreen += histogramGreen[j];
                aggregateBlue += histogramBlue[j];
            }

            //apply aggregate
            for(int j=i; j<i+aggregateSize; j++){
                histogramRed[j] = aggregateRed / aggregateSize;
                histogramGreen[j] = aggregateGreen / aggregateSize;
                histogramBlue[j] = aggregateBlue / aggregateSize;
            }

            i += aggregateSize;
        }

        //equalize image
        for(int i=0; i<maxx; i++){
            for(int j=0; j<maxy; j++){
                int pixel = originalBitmaps.getPixel(i, j);

                filteredBitmaps.setPixel(i, j, Color.argb(
                        1,
                        histogramRed[Color.red(pixel)],
                        histogramGreen[Color.green(pixel)],
                        histogramBlue[Color.blue(pixel)]
                        )
                );
            }
        }

        //=========================================================
        //BLACK and WHITE
        //=========================================================
        Log.d("te", "start converting");
        for(int i=0; i<maxx; i++){
            for(int j=0; j<maxy; j++){
                int pixel = filteredBitmaps.getPixel(i, j);

                Integer combined = 255 + Color.blue(pixel) - Color.red(pixel) - Color.green(pixel);
                combined = combined / 2;
                if(combined < threshold){
                    //drawThickPixel(filteredBitmaps, 3, i, j, Color.argb(1, 250, 250, 250));
                    filteredBitmaps.setPixel(i, j, Color.argb(1, 250, 250, 250));
                }
                else
                {
                    //drawThickPixel(filteredBitmaps, 3, i, j, Color.argb(1, 10, 10, 10));
                    filteredBitmaps.setPixel(i, j, Color.argb(1, 10, 10, 10));
                }
            }
        }

        CountShape();
        //hilangkan frame border
        allBorder.remove(0);
        final int count = allBorder.size();
        Log.d("all the shapes", allBorder.toString());
        
//        Log.d("shapeCOunt", Integer.toString(count));
//
//        Log.d("te", "start just shape");
//        for(int i=0; i<maxx; i++){
//            for(int j=0; j<maxy; j++){
//                filteredBitmaps.setPixel(i, j, Color.argb(1, white, white, white));
//
//                if(justBorder[i][j]){
//                    setThickPixel(filteredBitmaps, 0, i, j);
//                }
//            }
//        }

        Log.d("te", "highlight shape");
        for(List<Integer>shape:allBorder){
            int left = 9999;
            int upper = 9999;
            int right = -9999;
            int bottom = -9999;

            boolean axis = false;
            for(Integer pixel:shape){
                if(axis){
                    //now y
                    if(pixel < upper){
                        upper = pixel;
                    }
                    else if(pixel > bottom){
                        bottom = pixel;
                    }
                }
                else{
                    //now x
                    if(pixel < left){
                        left = pixel;
                    }
                    else if(pixel > right){
                        right = pixel;
                    }
                }

                axis = !axis;
            }

            Log.e("upper", Integer.toString(upper));
            Log.e("bottom", Integer.toString(bottom));
            Log.e("left", Integer.toString(left));
            Log.e("right", Integer.toString(right));

            Random rnd = new Random();
            int red = rnd.nextInt(256);
            int green = rnd.nextInt(256);
            int blue = rnd.nextInt(256);


            //draw boundary box
            for(int i=left; i<right; i++){
                drawThickPixel(filteredBitmaps, 3, i, upper, Color.argb(1, red, green, blue));
                drawThickPixel(filteredBitmaps, 3, i, bottom, Color.argb(1, red, green, blue));
//                filteredBitmaps.setPixel(i, upper, Color.argb(1, red, green, blue));
//                filteredBitmaps.setPixel(i, bottom, Color.argb(1, red, green, blue));
            }

            for(int j=upper; j<bottom; j++){
                drawThickPixel(filteredBitmaps, 3, left, j, Color.argb(1, red, green, blue));
                drawThickPixel(filteredBitmaps, 3, right, j, Color.argb(1, red, green, blue));
//                filteredBitmaps.setPixel(left, j, Color.argb(1, red, green, blue));
//                filteredBitmaps.setPixel(right, j, Color.argb(1, red, green, blue));
            }
        }

        

        runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  filtered.setImageBitmap(filteredBitmaps);
                  loading.setVisibility(View.INVISIBLE);
                  horizontal.setEnabled(true);
                  shapeCount.setText(Integer.toString(count));
              }
        });
    }
}
