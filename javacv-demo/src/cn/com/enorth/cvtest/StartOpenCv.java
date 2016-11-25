package cn.com.enorth.cvtest;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.util.concurrent.TimeUnit;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class StartOpenCv {
	
	public static final int WIDTH = 640;
	public static final int HEIGHT = 360;

	public static void main(String[] args) throws Throwable {
		CanvasFrame canvasFrameOut = new CanvasFrame("out");
		CanvasFrame canvasFrameIn = new CanvasFrame("in");
		
		IplImage srclogo = cvLoadImage("C:\\Users\\Administrator\\Desktop\\zawu\\新建文件夹/xiba.bmp");
		IplImage srclogoMask = cvLoadImage("C:\\Users\\Administrator\\Desktop\\zawu\\新建文件夹/xiba.bmp",0);
		
		int logoHeight = (int) (HEIGHT/8.0);
		int logoWidth = (int) (srclogo.width()*((logoHeight+0.0)/srclogo.height()));
		IplImage logo = IplImage.create(logoWidth, logoHeight, srclogo.depth(), srclogo.nChannels());
		cvResize(srclogo,logo);
		IplImage logoMask = IplImage.create(logoWidth, logoHeight, srclogoMask.depth(), srclogoMask.nChannels());
		cvResize(srclogoMask,logoMask);
		
		IplImage logoMaskInv = IplImage.create(logoWidth, logoHeight, logoMask.depth(), logoMask.nChannels());
		cvNot(logoMask,logoMaskInv);
		
		int logoLeft = (int) ((HEIGHT/15.0)*((WIDTH*1.0)/HEIGHT));
		int logoTop = (int) (HEIGHT/15.0);
		System.out.println(String.format("logo apply to %d,%d,%d,%d", logoLeft,logoTop,logoWidth,logoHeight));
		
		FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault("http://vod.cntv.lxdns.com/flash/mp4video54/TMS/2016/07/31/07719d830dca427f94d4f9fb40b96d7f_h264418000nero_aac32-1.mp4");
		grabber.start();
		
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("C:\\Users\\Administrator\\Desktop\\zawu\\20161122\\mp4\\11.mp4", WIDTH, HEIGHT, 1);
		recorder.setFormat("flv");
		recorder.setFrameRate(grabber.getFrameRate());
		recorder.setVideoQuality(1);
		recorder.setAudioQuality(1);
		recorder.start();
		
		OpenCVFrameConverter.ToIplImage imgCvt = new OpenCVFrameConverter.ToIplImage();
		OpenCVFrameConverter.ToMat matCvt = new OpenCVFrameConverter.ToMat();
		
		CvMemStorage storage = CvMemStorage.create();
		
		System.out.println("ready");
		
		long startTime = System.nanoTime();
		while (true) {
        	long nowTime = (System.nanoTime()-startTime)/1000;
        	long ptsTime = grabber.getTimestamp();
        	if(ptsTime>nowTime){
        		TimeUnit.MICROSECONDS.sleep(ptsTime-nowTime);
        	}
        	
        	Frame frame = grabber.grab();
        	if(frame==null){
        		System.out.println("end");
        		break;
        	}
        	if(frame.image!=null){
        		IplImage srcImage = imgCvt.convert(frame);
        		Mat srcMat = matCvt.convert(frame);
        		IplImage destImage = srcImage.clone();
        		Mat destMat = srcMat.clone();
        		if(canvasFrameIn.getCanvasSize().getWidth()==0||canvasFrameOut.getCanvasSize().getWidth()==0){
            		canvasFrameIn.setCanvasSize(srcImage.width(), srcImage.height());
            		canvasFrameOut.setCanvasSize(destImage.width(), destImage.height());
            	}
        		
        		srcMat.address();
        		cvSetImageROI(destImage,cvRect(logoLeft,logoTop,logoWidth,logoHeight));
        		cvCopy(logo, destImage,logoMask);
        		cvResetImageROI(destImage);
        		
        		Frame destFrame = imgCvt.convert(destImage);
        		//source preview
        		canvasFrameIn.showImage(frame);
        		//destination preview
        		canvasFrameOut.showImage(destFrame);
        		//output
        		recorder.record(destFrame);
        		
        	}else{
        		recorder.record(frame);
        	}
            
        }
        
        recorder.stop();
        System.out.println("recoder stop");
        grabber.stop();
        System.out.println("grabber stop");

        canvasFrameIn.dispose();
        canvasFrameOut.dispose();
	}

}
