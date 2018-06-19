package com.serio.core.utils.media;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serio.media.application.VideoProcessor;
import com.serio.media.exception.EncoderException;

/**
 * @author zl.shi
 */
public class VideoTest {
	protected static Logger logger = LoggerFactory.getLogger(VideoTest.class);

	
//	D:\software_package\ffmpeg-20171225-613f789-win64-static\bin\ffmpeg.exe -ss 00:00:03 -i C:\\Users\\zhengliang.shi\\Videos\\3zhzI640.mp4 -f mjpeg -r 1 -vframes 1 -s 640*360 -y C:\\Users\\zhengliang.shi\\Videos\\3zhzI640.jpg
//	@Test
	public void testCut() {
		
		File source = new File("C:/Users/zhengliang.shi/Videos/3zhzI640.mp4");
		File target = new File("C:/Users/zhengliang.shi/Videos/3zhzI640.jpg");
		try {
			VideoProcessor videoProcessor = new VideoProcessor();
			videoProcessor.videoCapture(source, target, 340f, 640, 360);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testTrasncoder() {
		
		File source = new File("C:/Users/zhengliang.shi/Videos/3zhzI640.mp4");
		File target = new File("C:/Users/zhengliang.shi/Videos/3zhzI640.3gp");
		try {
			VideoProcessor videoProcessor = new VideoProcessor();
			videoProcessor.transcode(source, target);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		
	}
	
}
