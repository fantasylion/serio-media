package com.serio.core.utils.media;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serio.core.media.EncoderException;
import com.serio.core.media.VideoProcessor;

/**
 * @author zl.shi
 */
public class VideoTest {
	protected static Logger logger = LoggerFactory.getLogger(VideoTest.class);

	
//	D:\software_package\ffmpeg-20171225-613f789-win64-static\bin\ffmpeg.exe -ss 00:00:03 -i C:\\Users\\zhengliang.shi\\Videos\\3zhzI640.mp4 -f mjpeg -r 1 -vframes 1 -s 640*360 -y C:\\Users\\zhengliang.shi\\Videos\\3zhzI640.jpg
//	@Test
	public void testCut() {
		
		File source = new File("C:\\Users\\zhengliang.shi\\Videos\\3zhzI640.mp4");
		File target = new File("C:\\Users\\zhengliang.shi\\Videos\\3zhzI64sssss17.jpg");
		try {
			VideoProcessor videoProcessor = new VideoProcessor();
			videoProcessor.videoCapture(source, target, 340f, 640, 360);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		
	}
	
//	@Test
	public void testTrasncoder() {
		
		File source = new File("C:\\Users\\zhengliang.shi\\Videos\\3zhzI640.mp4");
		File target = new File("C:\\Users\\zhengliang.shi\\Videos\\3zhzI64sssss17.3gp");
		try {
			VideoProcessor videoProcessor = new VideoProcessor();
			videoProcessor.transcode(source, target);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		
	}
	
	public void ebbinghausForgettingCurve() {
		
		int[] forgetTime = { 2, 4 , 7 ,15 };
		
		int listNum = 38;
		
		System.out.println("			新学		背诵");
		for ( int i = 0; i < listNum; i++ ) {
			System.out.print("第"+(i+1)+"天：	");
			System.out.print( "		list"+(i+1) );
			
			for ( int forget = 0; forget < forgetTime.length; forget++  ) {
				if ( (i+1) > forgetTime[forget] - 1 ) {
					System.out.print( "		*list"+((i+1) - (forgetTime[forget] - 1)) );
				}
			}
			
			System.out.println( "		*list"+(i+1) );
		}
		
		
	}
	
	
}
