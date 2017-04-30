package com.example.kickass.imagesearch;

/**
 * Created by kickass on 4/28/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;


public class TemplateMatch {

        public Mat run(Mat templ, Mat img, int match_method) {
            System.out.println("\nRunning Template Matching");


           /* Mat img = null;
            try {
                img = Utils.loadResource(context, R.drawable.input, Imgcodecs.CV_LOAD_IMAGE_COLOR);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Mat templ = Imgcodecs.imread(templateFile);
            Mat templ = null;
            try {
                templ = Utils.loadResource(context, R.drawable.template, Imgcodecs.CV_LOAD_IMAGE_COLOR);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            // / Create the result matrix
            int result_cols = img.cols() - templ.cols() + 1;
            int result_rows = img.rows() - templ.rows() + 1;
            System.out.println("\nResult cols"+result_cols+"result_rows"+result_rows);
            Mat result = new Mat(result_rows, result_cols, CvType.CV_8UC4);

            // / Do the Matching and Normalize
            Imgproc.matchTemplate(img, templ, result, match_method);
            Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

            // / Localizing the best match with minMaxLoc
            MinMaxLocResult mmr = Core.minMaxLoc(result);

            Point matchLoc;
            if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
                matchLoc = mmr.minLoc;
            } else {
                matchLoc = mmr.maxLoc;
            }

            // / Show me what you got
            Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
                    matchLoc.y + templ.rows()), new Scalar(0, 255, 0));

            //String output = "drawable://output.png";
            // Save the visualized detection.
            //System.out.println("Writing "+ output);
            //Imgcodecs.imwrite(output, img);

            return img;
        }

}
