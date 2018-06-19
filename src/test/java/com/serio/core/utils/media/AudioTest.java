package com.serio.core.utils.media;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serio.core.media.AudioProcessor;
import com.serio.core.media.EncoderException;

/**
 * @author zl.shi
 */
public class AudioTest {
	protected static Logger logger = LoggerFactory.getLogger(AudioTest.class);
	
	public void transcode() {
		File source = new File("C:\\Users\\zhengliang.shi\\Videos\\test.mp3");
		File target = new File("C:\\Users\\zhengliang.shi\\Videos\\test.wma");
		try {
			AudioProcessor videoProcessor = new AudioProcessor();
			videoProcessor.transcode(source, target);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
	}
	
	
}
