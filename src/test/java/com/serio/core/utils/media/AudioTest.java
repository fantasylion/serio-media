package com.serio.core.utils.media;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serio.media.application.AudioProcessor;
import com.serio.media.exception.EncoderException;

/**
 * @author zl.shi
 */
public class AudioTest {
	protected static Logger logger = LoggerFactory.getLogger(AudioTest.class);
	
	@Test
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
