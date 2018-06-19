package com.serio.core.media;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zl.shi
 */
public class AudioProcessor {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	String srcAudioFilePath = null;
	String dstAudioFilePath = null;
	
	/**
	 * The locator of the ffmpeg executable used by this encoder.
	 */
	private FFMPEGLocator locator;

	
	/**
	 * It builds an encoder using a {@link DefaultFFMPEGLocator} instance to
	 * locate the ffmpeg executable to use.
	 */
	public AudioProcessor(){
		this.locator = new DefaultFFMPEGLocator();
	}
	
	
	public AudioProcessor(String srcAudioFilePath, String dstAudioFilePath) {
		this.locator = new DefaultFFMPEGLocator();
		InitParameters(srcAudioFilePath, dstAudioFilePath);
	}

	
	public void InitParameters(String srcAudioFilePath, String dstAudioFilePath) {
		this.srcAudioFilePath = srcAudioFilePath;
		this.dstAudioFilePath = dstAudioFilePath; 		
	}


	/**
	 * 转码
	 * @return 转码结果，右起第一个四位表示标清转码结果（0000表示失败；0001表示成功）
	 */
	public int convert() {
		logger.info(srcAudioFilePath + "开始转码...");
		int result = 0x00;		
		result = process( srcAudioFilePath, dstAudioFilePath );
		return result;
	}
	
	/**
	 * @return 转码结果，右起第一个四位表示标清转码结果（0000表示失败；0001表示成功）
	 */
	public int process( String srcVideoFile, String dstAudioFilePath ) {
		int result = 0x00;	// 转码结果
		try {
			ConvertAuido( srcVideoFile, dstAudioFilePath );
			result = result | 0x01;	// 成功标志
		} catch (EncoderException e) {
			result = result & 0x10;	// 失败标志
			// 失败的话把文件移到失败文件目录
			File file = new File(dstAudioFilePath);
	    	if (file.exists())
	    		file.delete();
			e.printStackTrace();
		}
			
		return result;
	}

	
	/**
	 * Audio做转码
	 * @author zl.shi
	 * @param srcVideoFile
	 * @param dstAudioFilePath
	 * @throws EncoderException
	 */
	public void ConvertAuido(String srcVideoFile, String dstAudioFilePath) throws EncoderException {
		
		long startTime = System.currentTimeMillis();
		FFMPEGExecutor ffmpeg = locator.createExecutor();
		ffmpeg.addArgument(MediaConstant.PARAMETER_NAME_SOURCE);
		ffmpeg.addArgument(srcVideoFile);
		ffmpeg.addArgument(MediaConstant.PARAMETER_NAME_DESTINATION);
		ffmpeg.addArgument(dstAudioFilePath);
		
		logger.info(srcVideoFile + " -> " + dstAudioFilePath + " 转码成功,耗时 : (ms)" + (System.currentTimeMillis()-startTime));
		try {
			ffmpeg.execute();
		} catch (IOException e) {
			throw new EncoderException(e);
		}
	}

	/**
	 * 转码
	 * <p>
	 * eg:<br>
	 * 	videoSource:"C:\\Users\\serio\\Videos\\3zhzI640.mp4"
	 *  destSource:"C:\\Users\\serio\\Videos\\3zhzI640.3gp
	 * </p>
	 * @author zl.shi
	 * @param srcVideoPath
	 * @param bitRate
	 * @param size
	 * @param destVideoFile
	 * @throws IOException 
	 */
	public void transcode( File videoSource, File destSource ) throws IllegalArgumentException, InputFormatException, EncoderException {
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec(MediaConstant.PARAMETER_CODEC_COPY);
		
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setAudioAttributes(audio);
		
		Encoder encoder = new Encoder();
		encoder.encode( videoSource, destSource, attrs );
	}
	
}
